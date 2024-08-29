/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFCorePreferenceConstants;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.ToolSetConfigurationManager;
import com.espressif.idf.core.tools.vo.IDFToolSet;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.IDFConsole;
import com.espressif.idf.ui.InputStreamConsoleThread;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.update.Messages;

/**
 * Parent class for all tools related activity
 * any common methods must be added here
 * The class was created to save 
 * code and split the operation of installation and activation in two
 * @author Ali Azam Rana
 *
 */
public abstract class ToolsJob extends Job
{

	protected IDFConsole idfConsole;
	protected MessageConsoleStream console;
	protected MessageConsoleStream errorConsoleStream;
	protected String pythonExecutablePath;
	protected String gitExecutablePath;
	protected IDFToolSet idfToolSet;
	protected ToolSetConfigurationManager toolSetConfigurationManager;
	protected String idfPath;
	
	public ToolsJob(String name, String pythonExecutablePath, String gitExecutablePath)
	{
		super(name);
		this.pythonExecutablePath = pythonExecutablePath;
		this.gitExecutablePath = gitExecutablePath;
		idfToolSet = new IDFToolSet();
		toolSetConfigurationManager = new ToolSetConfigurationManager();
		activateIDFConsoleView();
	}

	protected void processExportCmdOutput(final String exportCmdOp)
	{
		// process export command output
		final String[] exportEntries = exportCmdOp.split("\n"); //$NON-NLS-1$
		for (String entry : exportEntries)
		{
			entry = entry.replaceAll("\\r", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (entry.startsWith("Error"))
			{
				try
				{
					errorConsoleStream.write(entry);
				}
				catch (IOException e)
				{
					Logger.log(e);
				}
			}
			String[] keyValue = entry.split("="); //$NON-NLS-1$
			if (keyValue.length == 2) // 0 - key, 1 - value
			{
				final String msg = MessageFormat.format("Key: {0} Value: {1}", keyValue[0], keyValue[1]); //$NON-NLS-1$
				Logger.log(msg);
				String key = keyValue[0];
				String value = keyValue[1];
				if (idfToolSet.getEnvVars() == null)
				{
					idfToolSet.setEnvVars(new HashMap<String, String>());
				}

				idfToolSet.getEnvVars().put(key, value);
			}

		}

		idfToolSet.getEnvVars().put(IDFEnvironmentVariables.IDF_COMPONENT_MANAGER, "1"); //$NON-NLS-1$
		// IDF_MAINTAINER=1 to be able to build with the clang toolchain
		idfToolSet.getEnvVars().put(IDFEnvironmentVariables.IDF_MAINTAINER, "1"); //$NON-NLS-1$
		if (!StringUtil.isEmpty(idfPath))
		{
			idfToolSet.getEnvVars().put(IDFEnvironmentVariables.IDF_PATH, idfPath);
			idfToolSet.setIdfLocation(idfPath);	
		}
		
		IStatus status = getIdfVersionFromIdfPy();
		String cmdOutput = status.getMessage();
		Pattern pattern = Pattern.compile("v(\\d+\\.\\d+\\.\\d+)"); //$NON-NLS-1$
		Matcher matcher = pattern.matcher(cmdOutput.toLowerCase());
		if (matcher.find())
		{
			idfToolSet.setIdfVersion(matcher.group(1));
		}
		idfToolSet.getEnvVars().put(IDFEnvironmentVariables.ESP_IDF_VERSION, idfToolSet.getIdfVersion());
		
	}
	
	protected String replacePathVariable(String value)
	{
		// Get system PATH
		Map<String, String> systemEnv = new HashMap<>(IDFUtil.getSystemEnv());
		String pathEntry = systemEnv.get("PATH"); //$NON-NLS-1$
		if (pathEntry == null)
		{
			pathEntry = systemEnv.get("Path"); // for Windows //$NON-NLS-1$
			if (pathEntry == null) // no idea
			{
				Logger.log(new Exception("No PATH found in the system environment variables")); //$NON-NLS-1$
			}
		}

		if (!StringUtil.isEmpty(pathEntry))
		{
			value = value.replace("$PATH", pathEntry); // macOS //$NON-NLS-1$
			value = value.replace("%PATH%", pathEntry); // Windows //$NON-NLS-1$
		}
		return value;
	}
	
	protected IStatus loadTargetsAvailableFromIdfInCurrentToolSet()
	{
		List<String> arguments = new ArrayList<String>();
		arguments.add(IDFConstants.IDF_LIST_TARGETS_CMD);
		return runCommandIdfPyInIdfEnv(arguments, console);
	}

	protected List<String> extractTargets(String input)
	{
		List<String> targets = new ArrayList<String>();
		Pattern pattern = Pattern.compile("^esp32.*", Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(input);
		while (matcher.find())
		{
			targets.add(matcher.group());
		}
		return targets;
	}
	
	protected IStatus handleWebSocketClientInstall()
	{
		String websocketClient = "websocket-client"; //$NON-NLS-1$
		// pip install websocket-client
		List<String> arguments = new ArrayList<String>();
		final String pythonEnvPath = pythonVirtualExecutablePath(idfToolSet);
		if (pythonEnvPath == null || !new File(pythonEnvPath).exists())
		{
			console.println(String.format("%s executable not found. Unable to run `%s -m pip install websocket-client`", //$NON-NLS-1$
					IDFConstants.PYTHON_CMD, IDFConstants.PYTHON_CMD));
			return IDFCorePlugin.errorStatus(
					String.format("%s executable not found. Unable to run `%s -m pip install websocket-client`", //$NON-NLS-1$
							IDFConstants.PYTHON_CMD, IDFConstants.PYTHON_CMD),
					null);
		}
		arguments.add(pythonEnvPath);
		arguments.add("-m"); //$NON-NLS-1$
		arguments.add("pip"); //$NON-NLS-1$
		arguments.add("list"); //$NON-NLS-1$

		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();

		try
		{
			String cmdMsg = "Executing " + getCommandString(arguments); //$NON-NLS-1$
			if (console != null)
			{
				console.println(cmdMsg);
			}
			Logger.log(cmdMsg);

			Map<String, String> environment = new HashMap<>(IDFUtil.getSystemEnv());
			Logger.log(environment.toString());

			IStatus status = processRunner.runInBackground(arguments, org.eclipse.core.runtime.Path.ROOT, environment);
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(),
						IDFCorePlugin.errorStatus("Unable to get the process status.", null)); //$NON-NLS-1$
				if (errorConsoleStream != null)
				{
					errorConsoleStream.println("Unable to get the process status.");
				}
				return IDFCorePlugin.errorStatus("Unable to get the process status.", null); //$NON-NLS-1$
			}

			String cmdOutput = status.getMessage();
			if (cmdOutput.contains(websocketClient))
			{
				return IDFCorePlugin.okStatus("websocket-client already installed", null); //$NON-NLS-1$
			}

			// websocket client not installed so installing it now.
			arguments.remove(arguments.size() - 1);
			arguments.add("install"); //$NON-NLS-1$
			arguments.add(websocketClient);

			status = processRunner.runInBackground(arguments, org.eclipse.core.runtime.Path.ROOT, environment);
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(),
						IDFCorePlugin.errorStatus("Unable to get the process status.", null)); //$NON-NLS-1$
				if (errorConsoleStream != null)
				{
					errorConsoleStream.println("Unable to get the process status.");
				}
				return IDFCorePlugin.errorStatus("Unable to get the process status.", null); //$NON-NLS-1$
			}

			if (console != null)
			{
				console.println(status.getMessage());
			}

			return status;
		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);
			if (errorConsoleStream != null)
			{
				errorConsoleStream.println(e1.getLocalizedMessage());
			}
			return IDFCorePlugin.errorStatus(e1.getLocalizedMessage(), e1); // $NON-NLS-1$;
		}
	}
	
	protected String pythonVirtualExecutablePath(IDFToolSet idfToolSet)
	{
		String pythonVirtualPath = idfToolSet.getEnvVars().get(IDFEnvironmentVariables.IDF_PYTHON_ENV_PATH);
		StringBuilder pythonVirtualExePath = new StringBuilder();
		pythonVirtualExePath.append(pythonVirtualPath);
		pythonVirtualExePath.append("/"); //$NON-NLS-1$
		if (Platform.getOS().equals(Platform.OS_WIN32))
		{
			pythonVirtualExePath.append("Scripts"); //$NON-NLS-1$
			pythonVirtualExePath.append("/"); //$NON-NLS-1$
			pythonVirtualExePath.append("python.exe"); //$NON-NLS-1$
		}
		else
		{
			pythonVirtualExePath.append("bin"); //$NON-NLS-1$
			pythonVirtualExePath.append("/"); //$NON-NLS-1$
			pythonVirtualExePath.append("python"); //$NON-NLS-1$
		}
		
		
		return pythonVirtualExePath.toString();
	}


	private String getCommandString(List<String> arguments)
	{
		StringBuilder builder = new StringBuilder();
		arguments.forEach(entry -> builder.append(entry + " ")); //$NON-NLS-1$

		return builder.toString().trim();
	}

	protected void activateIDFConsoleView()
	{
		idfConsole = new IDFConsole();
		console = idfConsole.getConsoleStream(Messages.IDFToolsHandler_ToolsManagerConsole, null, false, true);
		errorConsoleStream = idfConsole.getConsoleStream(Messages.IDFToolsHandler_ToolsManagerConsole, null, true,
				true);
	}
	
	protected IStatus getIdfVersionFromIdfPy()
	{
		List<String> arguments = new ArrayList<String>();
		arguments.add("--version");
		return runCommandIdfPyInIdfEnv(arguments, console);
	}
	
	/**
	 * Append the git directory to the existing CDT build environment PATH variable
	 * 
	 * @param path              CDT build environment PATH
	 * @param gitExecutablePath
	 * @return PATH value with git appended
	 */
	protected String appendGitToPath(String path, String gitExecutablePath)
	{
		IPath gitPath = new Path(gitExecutablePath);
		if (!gitPath.toFile().exists())
		{
			Logger.log(NLS.bind("{0} doesn't exist", gitExecutablePath)); //$NON-NLS-1$
			return path;
		}

		String gitDir = gitPath.removeLastSegments(1).toOSString(); // ../bin/git
		if (!StringUtil.isEmpty(path) && !path.contains(gitDir)) // Git not found on the CDT build PATH environment
		{
			return path.concat(";").concat(gitDir); // append git path //$NON-NLS-1$
		}
		return path;
	}
	
	protected IStatus runCommandIdfPyInIdfEnv(List<String> arguments, MessageConsoleStream console)
	{
		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		StringBuilder output = new StringBuilder();
		int waitCount = 10;
		try
		{
			arguments.add(0, pythonVirtualExecutablePath(idfToolSet));
			arguments.add(1, IDFUtil.getIDFPythonScriptFile(idfToolSet.getIdfLocation()).getAbsolutePath());

			String cmdMsg = Messages.AbstractToolsHandler_ExecutingMsg + " " + getCommandString(arguments); //$NON-NLS-1$
			console.println(cmdMsg);
			Logger.log(cmdMsg);

			Map<String, String> environment = new HashMap<>(IDFUtil.getSystemEnv());
			Logger.log(environment.toString());
			environment.put("PYTHONUNBUFFERED", "1"); //$NON-NLS-1$ //$NON-NLS-2$
			loadIdfPathWithSystemPath(environment);
			if (gitExecutablePath != null)
			{
				addPathToEnvironmentPath(environment, gitExecutablePath);
			}
			Process process = processRunner.run(arguments, org.eclipse.core.runtime.Path.ROOT, environment);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null)
			{
				output.append(line).append(System.lineSeparator());
			}
			
			while (process.isAlive() && waitCount > 0)
			{
				try
				{
					Thread.sleep(300);					
				}
				catch (InterruptedException e)
				{
					Logger.log(e);
				}
				waitCount--;
			}
			
			if (waitCount == 0)
			{
				console.println("Process possibly stuck");
				Logger.log("Process possibly stuck");
				return Status.CANCEL_STATUS;
			}
			
			IStatus status = new Status(process.exitValue() == 0 ? IStatus.OK : IStatus.ERROR, UIPlugin.PLUGIN_ID,
					process.exitValue(), output.toString(), null);
			if (status.getSeverity() == IStatus.ERROR)
			{
				errorConsoleStream.print(
						status.getException() != null ? status.getException().getMessage() : status.getMessage());
			}
			console.println(status.getMessage());
			console.println();

			return status;
		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);
			return IDFCorePlugin.errorStatus(e1.getMessage(), e1);
		}
	}
	
	private void addPathToEnvironmentPath(Map<String, String> environment, String gitExecutablePath)
	{
		IPath gitPath = new org.eclipse.core.runtime.Path(gitExecutablePath);
		if (gitPath.toFile().exists())
		{
			String gitDir = gitPath.removeLastSegments(1).toOSString();
			String path1 = environment.get("PATH"); //$NON-NLS-1$
			String path2 = environment.get("Path"); //$NON-NLS-1$
			if (!StringUtil.isEmpty(path1) && !path1.contains(gitDir)) // Git not found on the PATH environment
			{
				path1 = gitDir.concat(";").concat(path1); //$NON-NLS-1$
				environment.put("PATH", path1); //$NON-NLS-1$
			}
			else if (!StringUtil.isEmpty(path2) && !path2.contains(gitDir)) // Git not found on the Path environment
			{
				path2 = gitDir.concat(";").concat(path2); //$NON-NLS-1$
				environment.put("Path", path2); //$NON-NLS-1$
			}
		}
	}
	
	private void loadIdfPathWithSystemPath(Map<String, String> systemEnv)
	{
		String idfExportPath = idfToolSet.getEnvVars().get(IDFEnvironmentVariables.PATH);
		String pathVar = "PATH"; // for Windows //$NON-NLS-1$
		String pathEntry = systemEnv.get(pathVar); // $NON-NLS-1$
		if (pathEntry == null)
		{
			pathVar = "Path"; //$NON-NLS-1$
			pathEntry = systemEnv.get(pathVar);
			if (pathEntry == null) // no idea
			{
				Logger.log(new Exception("No PATH found in the system environment variables")); //$NON-NLS-1$
			}
		}

		if (!StringUtil.isEmpty(pathEntry))
		{
			idfExportPath = idfExportPath.replace("$PATH", pathEntry); // macOS //$NON-NLS-1$
			idfExportPath = idfExportPath.replace("%PATH%", pathEntry); // Windows //$NON-NLS-1$
		}
		systemEnv.put(pathVar, idfExportPath);
		for (Entry<String, String> entry : idfToolSet.getEnvVars().entrySet())
		{
			if (entry.getKey().equals(IDFEnvironmentVariables.PATH))
				continue;

			systemEnv.put(entry.getKey(), entry.getValue());
		}
	}
	
	protected IStatus runCommand(List<String> arguments, MessageConsoleStream console)
	{
		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();

		try
		{
			// insert python.sh/exe path and idf_tools.py
			arguments.add(0, pythonExecutablePath);
			arguments.add(1, IDFUtil.getIDFToolsScriptFile(idfToolSet.getIdfLocation()).getAbsolutePath());

			String cmdMsg = Messages.AbstractToolsHandler_ExecutingMsg + " " + getCommandString(arguments); //$NON-NLS-1$
			console.println(cmdMsg);
			Logger.log(cmdMsg);

			Map<String, String> environment = new HashMap<>(IDFUtil.getSystemEnv());
			Logger.log(environment.toString());
			environment.put("PYTHONUNBUFFERED", "1"); //$NON-NLS-1$ //$NON-NLS-2$
			environment.put("IDF_GITHUB_ASSETS", //$NON-NLS-1$
					Platform.getPreferencesService().getString(IDFCorePlugin.PLUGIN_ID,
							IDFCorePreferenceConstants.IDF_GITHUB_ASSETS,
							IDFCorePreferenceConstants.IDF_GITHUB_ASSETS_DEFAULT, null));
			
			environment.put("PIP_EXTRA_INDEX_URL", //$NON-NLS-1$
					Platform.getPreferencesService().getString(IDFCorePlugin.PLUGIN_ID,
							IDFCorePreferenceConstants.PIP_EXTRA_INDEX_URL,
							IDFCorePreferenceConstants.PIP_EXTRA_INDEX_URL_DEFAULT, null));

			if (gitExecutablePath != null)
			{
				addPathToEnvironmentPath(environment, gitExecutablePath);
			}
			Process process = processRunner.run(arguments, org.eclipse.core.runtime.Path.ROOT, environment);
			IStatus status = processData(process);
			console.println();

			return IDFCorePlugin.okStatus(status.getMessage(), null);
		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);
			return IDFCorePlugin.errorStatus(e1.getMessage(), e1);
		}
	}

	private IStatus processData(Process process)
	{

		InputStream inputStream = process.getInputStream();
		InputStream errorStream = process.getErrorStream();

		InputStreamConsoleThread readerThread = null;
		InputStreamConsoleThread errorThread = null;
		try
		{

			readerThread = new InputStreamConsoleThread(inputStream, console);
			errorThread = new InputStreamConsoleThread(errorStream, console);

			readerThread.start();
			errorThread.start();

			// This will wait till the process is done.
			int exitValue = process.waitFor();

			readerThread.interrupt();
			errorThread.interrupt();
			readerThread.join();
			errorThread.join();

			if (exitValue == 0)
			{
				return Status.OK_STATUS;
			}

			return new Status(IStatus.ERROR, IDFCorePlugin.PLUGIN_ID, "Error"); //$NON-NLS-1$

		}
		catch (InterruptedException e)
		{
			try
			{
				if (readerThread != null)
				{
					readerThread.interrupt();
				}
				if (errorThread != null)
				{
					errorThread.interrupt();
				}
				if (readerThread != null)
				{
					readerThread.join();
				}
				if (errorThread != null)
				{
					errorThread.join();
				}
			}
			catch (InterruptedException e1)
			{
				// ignore
			}
			return new Status(IStatus.ERROR, IDFCorePlugin.PLUGIN_ID, e.getMessage(), e);
		}
	}

}
