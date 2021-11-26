package com.espressif.idf.terminal.connector.serial.launcher;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.core.LaunchConfiguration;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.SDKConfigJsonReader;

public class GDBStubDebuggerLauncher
{

	private static final String GDBSTUB_DEBUG_LAUNCH_CONFIG_FILE = "gdbstub_debug_launch.launch";
	private String messageReceived;
	private IProject project;
	private String port;
	private String elfFile;

	// {'event': 'gdb_stub', 'port': '\\\\.\\COM9', 'prog': '.\\build\\hello_world.elf'}

	public GDBStubDebuggerLauncher(String messageReceived, IProject project)
	{
		this.messageReceived = messageReceived;
		this.project = project;
	}

	public void launchDebugSession() throws Exception
	{
		parseMessageReceived();
		createXMLConfig();
		GDBLaunchConfig gdbLaunchConfig = new GDBLaunchConfig(project.getFile(GDBSTUB_DEBUG_LAUNCH_CONFIG_FILE));
		gdbLaunchConfig.launch("debug", new NullProgressMonitor()); //$NON-NLS-1$
	}

	private void parseMessageReceived() throws ParseException
	{
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(messageReceived);
		JSONObject jsonObject = (JSONObject) obj;
		port = jsonObject.get("port").toString(); //$NON-NLS-1$
		if (Platform.OS_WIN32.equals(Platform.getOS()))
		{
			port = port.replace("\\", "");
			port = port.replace(".", "");
		}

		elfFile = jsonObject.get("prog").toString(); //$NON-NLS-1$
	}

	private Element createElement(Document dom, Element root, String attribName, String key, String value)
	{
		Element element = dom.createElement(attribName);
		element.setAttribute("key", key); //$NON-NLS-1$
		element.setAttribute("value", value); //$NON-NLS-1$
		root.appendChild(element);

		return element;
	}

	private String getMonitorBaudRate()
	{
		return new SDKConfigJsonReader(project).getValue("ESPTOOLPY_MONITOR_BAUD"); //$NON-NLS-1$
	}

	private void createXMLConfig() throws Exception
	{
		StringBuilder commandBuilder = new StringBuilder();
		commandBuilder.append(IDFUtil.getXtensaToolchainExecutablePath(project));
		commandBuilder.append(" -ex ");
		commandBuilder.append("\"set serial baud ");
		commandBuilder.append(getMonitorBaudRate().concat("\""));
		commandBuilder.append(" -ex ");
		commandBuilder.append("\"target remote ");
		commandBuilder.append(port.concat("\""));
		commandBuilder.append(" -ex interrupt \"");
		commandBuilder.append(elfFile.concat("\""));

//		String command = "C:\\Users\\aliaz\\.espressif\\tools\\riscv32-esp-elf\\"
//				+ "esp-2021r2-8.4.0\\riscv32-esp-elf\\bin\\riscv32-esp-elf-gdb.exe "
//				+ "-ex \"set serial baud 115200\" -ex \"target remote COM9\" "
//				+ "-ex interrupt \"C:\\Users\\aliaz\\runtime-PluginDevLaunch"
//				+ "\\hello_world\\build\\hello_world.elf\"";
		final String stringAttribute = "stringAttribute"; //$NON-NLS-1$
		final String intAttribute = "intAttribute"; //$NON-NLS-1$
		final String booleanAttribute = "booleanAttribute"; //$NON-NLS-1$
		final String listAttribute = "listAttribute"; //$NON-NLS-1$
		final String listEntry = "listEntry"; //$NON-NLS-1$
		final String key = "key"; //$NON-NLS-1$
		final String value = "value"; //$NON-NLS-1$
		final String mapAttribute = "mapAttribute"; //$NON-NLS-1$
		final String booleanFalse = "false"; //$NON-NLS-1$
		final String booleanTrue = "true"; //$NON-NLS-1$

		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dom = builder.newDocument();

		Element root = dom.createElement("launchConfiguration"); //$NON-NLS-1$
		dom.appendChild(root);

		Attr attr = dom.createAttribute("type"); //$NON-NLS-1$
		attr.setValue("org.eclipse.cdt.debug.gdbjtag.launchConfigurationType"); //$NON-NLS-1$
		root.setAttributeNode(attr);

		createElement(dom, root, stringAttribute, "org.eclipse.cdt.debug.gdbjtag.core.connection", //$NON-NLS-1$
				"gdb:unspecified-ip-address:unspecified-port-number#"); //$NON-NLS-1$

		createElement(dom, root, intAttribute, "org.eclipse.cdt.debug.gdbjtag.core.delay", "0"); //$NON-NLS-1$ //$NON-NLS-2$

		createElement(dom, root, booleanAttribute, "org.eclipse.cdt.debug.gdbjtag.core.doHalt", booleanFalse); //$NON-NLS-1$

		createElement(dom, root, booleanAttribute, "org.eclipse.cdt.debug.gdbjtag.core.doReset", booleanFalse); //$NON-NLS-1$

		createElement(dom, root, stringAttribute, "org.eclipse.cdt.debug.gdbjtag.core.imageFileName", ""); //$NON-NLS-1$ //$NON-NLS-2$

		createElement(dom, root, stringAttribute, "org.eclipse.cdt.debug.gdbjtag.core.imageOffset", ""); //$NON-NLS-1$//$NON-NLS-2$

		createElement(dom, root, stringAttribute, "org.eclipse.cdt.debug.gdbjtag.core.initCommands", ""); //$NON-NLS-1$//$NON-NLS-2$

		createElement(dom, root, stringAttribute, "org.eclipse.cdt.debug.gdbjtag.core.jtagDeviceId", //$NON-NLS-1$
				"org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.genericDevice"); //$NON-NLS-1$

		createElement(dom, root, booleanAttribute, "org.eclipse.cdt.debug.gdbjtag.core.loadImage", booleanFalse); //$NON-NLS-1$

		createElement(dom, root, booleanAttribute, "org.eclipse.cdt.debug.gdbjtag.core.loadSymbols", booleanTrue); //$NON-NLS-1$

		createElement(dom, root, stringAttribute, "org.eclipse.cdt.debug.gdbjtag.core.pcRegister", ""); //$NON-NLS-1$ //$NON-NLS-2$

		createElement(dom, root, stringAttribute, "org.eclipse.cdt.debug.gdbjtag.core.runCommands", ""); //$NON-NLS-1$//$NON-NLS-2$

		createElement(dom, root, booleanAttribute, "org.eclipse.cdt.debug.gdbjtag.core.setPcRegister", booleanFalse); //$NON-NLS-1$

		createElement(dom, root, booleanAttribute, "org.eclipse.cdt.debug.gdbjtag.core.setResume", booleanFalse); //$NON-NLS-1$

		createElement(dom, root, booleanAttribute, "org.eclipse.cdt.debug.gdbjtag.core.setStopAt", booleanFalse); //$NON-NLS-1$

		createElement(dom, root, stringAttribute, "org.eclipse.cdt.debug.gdbjtag.core.symbolsFileName", elfFile); //$NON-NLS-1$

		createElement(dom, root, stringAttribute, "org.eclipse.cdt.debug.gdbjtag.core.stopAt", ""); //$NON-NLS-1$//$NON-NLS-2$

		createElement(dom, root, stringAttribute, "org.eclipse.cdt.debug.gdbjtag.core.symbolsFileName", ""); //$NON-NLS-1$//$NON-NLS-2$

		createElement(dom, root, stringAttribute, "org.eclipse.cdt.debug.gdbjtag.core.symbolsOffset", ""); //$NON-NLS-1$//$NON-NLS-2$

		createElement(dom, root, booleanAttribute, "org.eclipse.cdt.debug.gdbjtag.core.useFileForImage", booleanFalse); //$NON-NLS-1$

		createElement(dom, root, booleanAttribute, "org.eclipse.cdt.debug.gdbjtag.core.useFileForSymbols", //$NON-NLS-1$
				booleanTrue);

		createElement(dom, root, booleanAttribute, "org.eclipse.cdt.debug.gdbjtag.core.useProjBinaryForImage", //$NON-NLS-1$
				booleanFalse);

		createElement(dom, root, booleanAttribute, "org.eclipse.cdt.debug.gdbjtag.core.useProjBinaryForSymbols", //$NON-NLS-1$
				booleanTrue);

		createElement(dom, root, booleanAttribute, "org.eclipse.cdt.debug.gdbjtag.core.useRemoteTarget", booleanFalse); //$NON-NLS-1$

		createElement(dom, root, stringAttribute, "org.eclipse.cdt.dsf.gdb.DEBUG_NAME", commandBuilder.toString()); //$NON-NLS-1$

		createElement(dom, root, booleanAttribute, "org.eclipse.cdt.dsf.gdb.REMOTE_TIMEOUT_ENABLED", booleanFalse); //$NON-NLS-1$

		createElement(dom, root, stringAttribute, "org.eclipse.cdt.dsf.gdb.REMOTE_TIMEOUT_VALUE", ""); //$NON-NLS-1$//$NON-NLS-2$

		createElement(dom, root, booleanAttribute, "org.eclipse.cdt.dsf.gdb.UPDATE_THREADLIST_ON_SUSPEND", booleanTrue); //$NON-NLS-1$

		createElement(dom, root, intAttribute, "org.eclipse.cdt.launch.ATTR_BUILD_BEFORE_LAUNCH_ATTR", "0"); //$NON-NLS-1$//$NON-NLS-2$

		createElement(dom, root, stringAttribute, "org.eclipse.cdt.launch.COREFILE_PATH", ""); //$NON-NLS-1$ //$NON-NLS-2$

		createElement(dom, root, stringAttribute, "org.eclipse.cdt.launch.PROGRAM_NAME", elfFile.substring(2)); // "build/hello_world.elf" //$NON-NLS-1$

		createElement(dom, root, stringAttribute, "org.eclipse.cdt.launch.PROJECT_ATTR", project.getName()); //$NON-NLS-1$

		createElement(dom, root, booleanAttribute, "org.eclipse.cdt.launch.PROJECT_BUILD_CONFIG_AUTO_ATTR", //$NON-NLS-1$
				booleanTrue);

		createElement(dom, root, stringAttribute, "org.eclipse.cdt.launch.PROJECT_BUILD_CONFIG_ID_ATTR", //$NON-NLS-1$
				"org.eclipse.cdt.core.default.config.1039216864"); //$NON-NLS-1$

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

		Element mapElement = dom.createElement(mapAttribute);
		mapElement.setAttribute(key, "org.eclipse.debug.core.preferred_launchers"); //$NON-NLS-1$
		createElement(dom, mapElement, "mapEntry", "[debug]", "org.eclipse.cdt.debug.gdbjtag.core.dsfLaunchDelegate"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

		root.appendChild(mapElement);

		createElement(dom, root, booleanAttribute, "org.eclipse.lsp4e.debug.model.ATTR_CUSTOM_DEBUG_ADAPTER", //$NON-NLS-1$
				booleanFalse);

		createElement(dom, root, booleanAttribute, "org.eclipse.lsp4e.debug.model.ATTR_CUSTOM_LAUNCH_PARAMS", //$NON-NLS-1$
				booleanFalse);

		createElement(dom, root, stringAttribute, "org.eclipse.lsp4e.debug.model.ATTR_DSP_MODE", "launch server"); //$NON-NLS-1$ //$NON-NLS-2$

		createElement(dom, root, booleanAttribute, "org.eclipse.lsp4e.debug.model.ATTR_DSP_MONITOR_ADAPTER", //$NON-NLS-1$
				booleanFalse);

		createElement(dom, root, stringAttribute, "org.eclipse.lsp4e.debug.model.ATTR_DSP_SERVER_HOST", "127.0.0.1"); //$NON-NLS-1$ //$NON-NLS-2$

		createElement(dom, root, intAttribute, "org.eclipse.lsp4e.debug.model.ATTR_DSP_SERVER_PORT", "4711"); //$NON-NLS-1$//$NON-NLS-2$

		createElement(dom, root, stringAttribute, "process_factory_id", "org.eclipse.cdt.dsf.gdb.GdbProcessFactory"); //$NON-NLS-1$//$NON-NLS-2$

		Transformer tr = TransformerFactory.newInstance().newTransformer();
		tr.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		String launchFile = project.getLocation().makeAbsolute().toString().concat("/") // $NON-NLS-1$
				.concat(GDBSTUB_DEBUG_LAUNCH_CONFIG_FILE);
		tr.transform(new DOMSource(dom), new StreamResult(new File(launchFile)));
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
	}

	@SuppressWarnings("restriction")
	private class GDBLaunchConfig extends LaunchConfiguration
	{
		protected GDBLaunchConfig(IFile file)
		{
			super(file);
		}
	}
}
