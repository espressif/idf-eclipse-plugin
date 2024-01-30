/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.update;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;

/**
 * @author Kondal Kolipaka
 * 
 *         Run /tools/idf_tools.py export command
 *
 */
public class ExportIDFTools
{
	/**
	 * @param pythonExePath python executable full path
	 * @param gitExePath    git executable full path
	 * @param console       Console stream to write messages
	 * @param errorConsoleStream
	 */
	public IStatus runToolsExportAndProcessOutput(final String pythonExePath, final String gitExePath, final MessageConsoleStream console, MessageConsoleStream errorConsoleStream)
	{
		final List<String> arguments = getExportCommand(pythonExePath);

		final String cmd = Messages.AbstractToolsHandler_ExecutingMsg + " " + getCommandString(arguments); //$NON-NLS-1$
		log(cmd, console);

		final Map<String, String> environment = new HashMap<>(System.getenv());
		if (gitExePath != null)
		{
			addGitToEnvironment(environment, gitExePath);
		}
		final ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		try
		{
			final IStatus status = processRunner.runInBackground(arguments, Path.ROOT, environment);
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(), IDFCorePlugin.errorStatus("Status can't be null", null)); //$NON-NLS-1$
				return IDFCorePlugin.errorStatus("Status can't be null", null); //$NON-NLS-1$
			}
			
			if (status.getSeverity() == IStatus.ERROR)
			{
				log(status.getException() != null ? status.getException().getMessage() : status.getMessage(), errorConsoleStream);
				return status;				
			}

			// process export command output
			final String exportCmdOp = status.getMessage();
			log(exportCmdOp, console);
			processExportCmdOutput(exportCmdOp, gitExePath);
			
			return status;
		}
		catch (IOException e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);
			return IDFCorePlugin.errorStatus(e1.getMessage(), e1);
		}
	}
	
	public IStatus getToolsExportOutput(final String pythonExePath, final String gitExePath, final MessageConsoleStream console, MessageConsoleStream errorConsoleStream)
	{
		final List<String> arguments = getExportCommand(pythonExePath);

		final String cmd = Messages.AbstractToolsHandler_ExecutingMsg + " " + getCommandString(arguments); //$NON-NLS-1$
		log(cmd, console);

		final Map<String, String> environment = new HashMap<>(System.getenv());
		if (gitExePath != null)
		{
			addGitToEnvironment(environment, gitExePath);
		}
		final ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		try
		{
			final IStatus status = processRunner.runInBackground(arguments, Path.ROOT, environment);
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(), IDFCorePlugin.errorStatus("Status can't be null", null)); //$NON-NLS-1$
				return IDFCorePlugin.errorStatus("Status can't be null", null); //$NON-NLS-1$
			}
			
			if (status.getSeverity() == IStatus.ERROR)
			{
				log(status.getException() != null ? status.getException().getMessage() : status.getMessage(), errorConsoleStream);
				return status;				
			}
			
			return status;
		}
		catch (IOException e)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e);
			return IDFCorePlugin.errorStatus(e.getMessage(), e);
		}
	}
	
	public IStatus getToolsExportOutputFromGivenIdfPath(final String pythonExePath, final String gitExePath, final MessageConsoleStream console, MessageConsoleStream errorConsoleStream, final String idfPath)
	{
		final List<String> arguments = getExportCommandUsingGivenIdfPath(pythonExePath, idfPath);

		final String cmd = Messages.AbstractToolsHandler_ExecutingMsg + " " + getCommandString(arguments); //$NON-NLS-1$
		log(cmd, console);

		final Map<String, String> environment = new HashMap<>(System.getenv());
		if (gitExePath != null)
		{
			addGitToEnvironment(environment, gitExePath);
		}
		final ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		try
		{
			final IStatus status = processRunner.runInBackground(arguments, Path.ROOT, environment);
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(), IDFCorePlugin.errorStatus("Status can't be null", null)); //$NON-NLS-1$
				return IDFCorePlugin.errorStatus("Status can't be null", null); //$NON-NLS-1$
			}
			
			if (status.getSeverity() == IStatus.ERROR)
			{
				log(status.getException() != null ? status.getException().getMessage() : status.getMessage(), errorConsoleStream);
				return status;				
			}
			
			return status;
		}
		catch (IOException e)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e);
			return IDFCorePlugin.errorStatus(e.getMessage(), e);
		}
	}

	private List<String> getExportCommandUsingGivenIdfPath(String pythonExePath, String idfPath)
	{
		final List<String> arguments = new ArrayList<>();
		arguments.add(pythonExePath);
		arguments.add(IDFUtil.getIDFToolsScriptFile(idfPath).getAbsolutePath());
		arguments.add(IDFConstants.TOOLS_EXPORT_CMD);
		arguments.add(IDFConstants.TOOLS_EXPORT_CMD_FORMAT_VAL);
		return arguments;
	}

	private List<String> getExportCommand(String pythonExePath)
	{
		final List<String> arguments = new ArrayList<>();
		arguments.add(pythonExePath);
		arguments.add(IDFUtil.getIDFToolsScriptFile().getAbsolutePath());
		arguments.add(IDFConstants.TOOLS_EXPORT_CMD);
		arguments.add(IDFConstants.TOOLS_EXPORT_CMD_FORMAT_VAL);
		return arguments;
	}
	
	private void log(final String cmd, final MessageConsoleStream console)
	{
		Logger.log(cmd);
		if (console != null)
		{
			console.println(cmd);
		}
	}

	private String getCommandString(final List<String> arguments)
	{
		final StringBuilder builder = new StringBuilder();
		arguments.forEach(entry -> builder.append(entry + " ")); //$NON-NLS-1$

		return builder.toString().trim();
	}

	private void processExportCmdOutput(final String exportCmdOp, final String gitExecutablePath)
	{
		// process export command output
		final String[] exportEntries = exportCmdOp.split("\n"); //$NON-NLS-1$
		for (String entry : exportEntries)
		{
			entry = entry.replaceAll("\\r", ""); //$NON-NLS-1$ //$NON-NLS-2$
			String[] keyValue = entry.split("="); //$NON-NLS-1$
			if (keyValue.length == 2) // 0 - key, 1 - value
			{
				final String msg = MessageFormat.format("Key: {0} Value: {1}", keyValue[0], keyValue[1]); //$NON-NLS-1$
				Logger.log(msg);

				final IDFEnvironmentVariables idfEnvMgr = new IDFEnvironmentVariables();
				String key = keyValue[0];
				String value = keyValue[1];
				if (key.equals(IDFEnvironmentVariables.PATH))
				{
					value = replacePathVariable(value);
					value = appendGitToPath(value, gitExecutablePath);
				}

				// add new or replace old entries
				idfEnvMgr.addEnvVariable(key, value);
			}

		}
	}

	private String replacePathVariable(String value)
	{
		// Get system PATH
		Map<String, String> systemEnv = new HashMap<>(System.getenv());
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

	protected void addGitToEnvironment(Map<String, String> envMap, String executablePath)
	{
		IPath gitPath = new Path(executablePath);
		if (gitPath.toFile().exists())
		{
			String gitDir = gitPath.removeLastSegments(1).toOSString();
			String path1 = envMap.get("PATH"); //$NON-NLS-1$
			String path2 = envMap.get("Path"); //$NON-NLS-1$
			if (!StringUtil.isEmpty(path1) && !path1.contains(gitDir)) // Git not found on the PATH environment
			{
				path1 = gitDir.concat(";").concat(path1); //$NON-NLS-1$
				envMap.put("PATH", path1); //$NON-NLS-1$
			}
			else if (!StringUtil.isEmpty(path2) && !path2.contains(gitDir)) // Git not found on the Path environment
			{
				path2 = gitDir.concat(";").concat(path2); //$NON-NLS-1$
				envMap.put("Path", path2); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Append the git directory to the existing CDT build environment PATH variable
	 * 
	 * @param path              CDT build environment PATH
	 * @param gitExecutablePath
	 * @return PATH value with git appended
	 */
	private String appendGitToPath(String path, String gitExecutablePath)
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
}
