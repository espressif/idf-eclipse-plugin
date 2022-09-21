/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.terminal.connector.serial.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.ui.IDFConsole;

/**
 * Core dump debugging configuration creator and launcher. We are using simply the postmortem c/c++ debug configuration
 * style to create a launch configuration xml file and then simply launch that through eclipse.
 *
 * @author Ali Azam Rana
 *
 */
public class CoreDumpPostmortemDebuggerLauncher implements ISerialWebSocketEventLauncher
{
	private static final String CORE_DUMP_POSTMORTEM_LAUNCH_CONFIG = "%s_core_dump_postmortem_debug.launch"; //$NON-NLS-1$
	private static final String GENERATED_CORE_ELF_NAME = "core.elf"; //$NON-NLS-1$
	private static final String GENERATED_CORE_DUMP_NAME = "core.dump"; //$NON-NLS-1$
	private static final String CORE_DUMP_FOLDER = "core_dump"; //$NON-NLS-1$
	private IProject project;
	private String messageReceived;
	private String elfFilePath;
	private String extractedFilePath;
	private IDFConsole idfConsole;

	public CoreDumpPostmortemDebuggerLauncher(String messageReceived, IProject project)
	{
		this.messageReceived = messageReceived;
		this.project = project;
		idfConsole = new IDFConsole();
	}

	@Override
	public IFile launchDebugSession() throws Exception
	{
		parseMessageReceived();
		parseExtractedFileFromPythonScript();
		createXMLConfig();
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		IFile launchFile = project.getFolder(IDFConstants.BUILD_FOLDER).getFolder(CORE_DUMP_FOLDER)
				.getFile(String.format(CORE_DUMP_POSTMORTEM_LAUNCH_CONFIG, project.getName()));
		return launchFile;
	}

	private void parseExtractedFileFromPythonScript() throws Exception
	{
		Logger.log("Converting coredump"); //$NON-NLS-1$
		String coreDumpDestination = getCoreDumpFileFromBuildDir(GENERATED_CORE_ELF_NAME);

		// espcoredump.py

		List<String> commands = new ArrayList<>();
		commands.add(IDFUtil.getIDFPythonEnvPath());
		commands.add(IDFUtil.getEspCoreDumpScriptFile().getAbsolutePath());
		commands.add("dbg_corefile"); //$NON-NLS-1$
		commands.add("-t"); //$NON-NLS-1$

		// Currently since we are only able to cater the UART based coredump via the
		// websocket event we are passing the encoding as b64 this will need to be
		// changed when the coredump events for the flash based core dump are also given
		commands.add("b64"); //$NON-NLS-1$

		commands.add("-c"); //$NON-NLS-1$
		commands.add(extractedFilePath);
		commands.add("-s"); //$NON-NLS-1$
		commands.add(coreDumpDestination);
		commands.add(elfFilePath);

		Logger.log(commands.toString());
		executeCommands(commands);

	}

	private void executeCommands(List<String> commands) throws Exception
	{
		Map<String, String> envMap = new IDFEnvironmentVariables().getEnvMap();
		Path pathToProject = new Path(project.getLocation().toString());
		runCommand(commands, pathToProject, envMap);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
	}

	private String runCommand(List<String> arguments, Path workDir, Map<String, String> env) throws Exception
	{
		ProcessBuilder processBuilder = new ProcessBuilder(arguments);
		processBuilder.directory(new File(workDir.toOSString()));
		processBuilder.environment().putAll(env);
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();

		Thread.sleep(100);
		StringBuilder sBuilder = new StringBuilder();
		if (process.isAlive() && process.getInputStream() != null)
		{
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = bufferedReader.readLine();
			while (line != null)
			{
				sBuilder.append(line);
				sBuilder.append(System.lineSeparator());
				if (line.contains("gdb") || line.contains("GDB")) //$NON-NLS-1$ //$NON-NLS-2$
				{
					if (process.isAlive())
						process.destroyForcibly(); // we are closing the gdb from the process here forcibly
													// since the eclipse will launch its own debug session
					line = null;
					continue;
				}
				line = bufferedReader.readLine();
			}

		}

		Logger.log(sBuilder.toString());
		return sBuilder.toString();
	}

	private void parseMessageReceived() throws Exception
	{
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(messageReceived);
		JSONObject jsonObject = (JSONObject) obj;

		extractedFilePath = jsonObject.get("file").toString(); //$NON-NLS-1$
		File file = new File(extractedFilePath);
		if (!file.exists())
		{
			String errorMessage = "File not found: ".concat(extractedFilePath); //$NON-NLS-1$
			Logger.logError(errorMessage);
			idfConsole.getConsoleStream().print(errorMessage);
			throw new Exception(errorMessage);
		}
		IFolder buildRootFolder = project.getFolder(IDFConstants.BUILD_FOLDER);
		StringBuilder coreDumpDestination = new StringBuilder();
		coreDumpDestination.append(buildRootFolder.getLocation().toString());
		coreDumpDestination.append(IPath.SEPARATOR);
		coreDumpDestination.append(CORE_DUMP_FOLDER);

		file = new File(coreDumpDestination.toString());
		if (!file.exists())
		{
			Files.createDirectory(Paths.get(coreDumpDestination.toString()));
		}

		coreDumpDestination.append(IPath.SEPARATOR);
		coreDumpDestination.append(GENERATED_CORE_DUMP_NAME);

		java.nio.file.Path destinationPath = Paths.get(coreDumpDestination.toString());

		Files.copy(Paths.get(extractedFilePath), destinationPath, StandardCopyOption.REPLACE_EXISTING);

		extractedFilePath = destinationPath.toString();
		elfFilePath = jsonObject.get("prog").toString(); //$NON-NLS-1$
	}

	private void createXMLConfig() throws Exception
	{
		final String stringAttribute = "stringAttribute"; //$NON-NLS-1$
		final String intAttribute = "intAttribute"; //$NON-NLS-1$
		final String booleanAttribute = "booleanAttribute"; //$NON-NLS-1$
		final String listAttribute = "listAttribute"; //$NON-NLS-1$
		final String listEntry = "listEntry"; //$NON-NLS-1$
		final String key = "key"; //$NON-NLS-1$
		final String value = "value"; //$NON-NLS-1$
		final String booleanFalse = "false"; //$NON-NLS-1$
		final String booleanTrue = "true"; //$NON-NLS-1$

		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dom = builder.newDocument();

		Element root = dom.createElement("launchConfiguration"); //$NON-NLS-1$
		dom.appendChild(root);
		Attr attr = dom.createAttribute("type"); //$NON-NLS-1$
		attr.setValue("org.eclipse.cdt.launch.postmortemLaunchType"); //$NON-NLS-1$
		root.setAttributeNode(attr);

		createElement(dom, root, booleanAttribute, "org.eclipse.cdt.dsf.gdb.AUTO_SOLIB", booleanTrue); //$NON-NLS-1$
		createElement(dom, root, listAttribute, "org.eclipse.cdt.dsf.gdb.AUTO_SOLIB_LIST", null); //$NON-NLS-1$
		createElement(dom, root, stringAttribute, "org.eclipse.cdt.dsf.gdb.DEBUG_NAME", //$NON-NLS-1$
				IDFUtil.getXtensaToolchainExecutablePath(project));
		createElement(dom, root, stringAttribute, "org.eclipse.cdt.dsf.gdb.GDB_INIT", ""); //$NON-NLS-1$ //$NON-NLS-2$
		createElement(dom, root, stringAttribute, "org.eclipse.cdt.dsf.gdb.POST_MORTEM_TYPE", "CORE_FILE"); //$NON-NLS-1$//$NON-NLS-2$
		createElement(dom, root, listAttribute, "org.eclipse.cdt.dsf.gdb.SOLIB_PATH", null); //$NON-NLS-1$
		createElement(dom, root, intAttribute, "org.eclipse.cdt.launch.ATTR_BUILD_BEFORE_LAUNCH_ATTR", //$NON-NLS-1$
				String.valueOf(100));

		createElement(dom, root, stringAttribute, "org.eclipse.cdt.launch.COREFILE_PATH", //$NON-NLS-1$
				getCoreDumpFileFromBuildDir(GENERATED_CORE_ELF_NAME));

		createElement(dom, root, stringAttribute, "org.eclipse.cdt.launch.DEBUGGER_ID", "gdb"); //$NON-NLS-1$ //$NON-NLS-2$
		createElement(dom, root, stringAttribute, "org.eclipse.cdt.launch.DEBUGGER_START_MODE", "core"); //$NON-NLS-1$ //$NON-NLS-2$
		createElement(dom, root, stringAttribute, "org.eclipse.cdt.launch.PROGRAM_NAME", elfFilePath); //$NON-NLS-1$
		createElement(dom, root, stringAttribute, "org.eclipse.cdt.launch.PROJECT_ATTR", project.getName()); //$NON-NLS-1$
		createElement(dom, root, booleanAttribute, "org.eclipse.cdt.launch.PROJECT_BUILD_CONFIG_AUTO_ATTR", //$NON-NLS-1$
				booleanFalse);
		createElement(dom, root, stringAttribute, "org.eclipse.cdt.launch.PROJECT_BUILD_CONFIG_ID_ATTR", //$NON-NLS-1$
				"org.eclipse.cdt.core.default.config.2083943554"); //$NON-NLS-1$

		Element listMappResPath = dom.createElement(listAttribute);
		listMappResPath.setAttribute(key, "org.eclipse.debug.core.MAPPED_RESOURCE_PATHS"); //$NON-NLS-1$
		Element subListEntryResPath = dom.createElement(listEntry);
		subListEntryResPath.setAttribute(value, "/".concat(project.getName())); //$NON-NLS-1$
		listMappResPath.appendChild(subListEntryResPath);
		root.appendChild(listMappResPath);

		Element listMappResType = dom.createElement(listAttribute);
		listMappResType.setAttribute(key, "org.eclipse.debug.core.MAPPED_RESOURCE_TYPES"); //$NON-NLS-1$
		Element subListEntryResType = dom.createElement(listEntry);
		subListEntryResType.setAttribute(value, "4"); //$NON-NLS-1$
		listMappResType.appendChild(subListEntryResType);
		root.appendChild(listMappResType);

		Transformer tr = TransformerFactory.newInstance().newTransformer();
		tr.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

		String launchFile = getCoreDumpFileFromBuildDir(
				String.format(CORE_DUMP_POSTMORTEM_LAUNCH_CONFIG, project.getName()));
		tr.transform(new DOMSource(dom), new StreamResult(new File(launchFile)));
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

	}

	private String getCoreDumpFileFromBuildDir(String fileName)
	{
		IFolder buildRootFolder = project.getFolder(IDFConstants.BUILD_FOLDER);
		StringBuilder coreDumpDestination = new StringBuilder();
		coreDumpDestination.append(buildRootFolder.getLocation().toString());
		coreDumpDestination.append(IPath.SEPARATOR);
		coreDumpDestination.append(CORE_DUMP_FOLDER);
		coreDumpDestination.append(IPath.SEPARATOR);
		coreDumpDestination.append(fileName);
		return coreDumpDestination.toString();
	}

	private Element createElement(Document dom, Element root, String attribName, String key, String value)
	{
		Element element = dom.createElement(attribName);
		element.setAttribute("key", key); //$NON-NLS-1$
		if (value != null)
		{
			element.setAttribute("value", value); //$NON-NLS-1$
		}
		root.appendChild(element);

		return element;
	}
}
