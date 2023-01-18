package com.espressif.idf.serial.monitor.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.GenericJsonReader;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.SDKConfigJsonReader;
import com.espressif.idf.core.util.StringUtil;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFMonitor
{
	private String port;
	private String pythonBinPath;
	private IProject project;
	private String filterOptions;
	private int serverPort;

	public IDFMonitor(IProject project, String port, String filterOptions, String pythonBinPath, int serverPort)
	{
		this.project = project;
		this.port = port;
		this.pythonBinPath = pythonBinPath;
		this.filterOptions = filterOptions;
		this.serverPort = serverPort;
	}

	private List<String> commandArgsWithSocketServer()
	{
		List<String> args = new ArrayList<>();
		args.add(pythonBinPath);
		args.add(IDFUtil.getIDFMonitorPythonScriptFile().getAbsolutePath());
		if(!StringUtil.isEmpty(filterOptions))
		{
			args.add("--print_filter"); //$NON-NLS-1$
			args.add(filterOptions);
		}
		args.add("-p"); //$NON-NLS-1$
		args.add(port);
		args.add("-b"); //$NON-NLS-1$
		args.add(getMonitorBaudRate());
		args.add("--ws"); //$NON-NLS-1$
		args.add("ws://localhost:".concat(String.valueOf(serverPort))); //$NON-NLS-1$
		args.add(getElfFilePath(project).toString());
		return args;
	}

	private IPath getElfFilePath(IProject project)
	{
		try
		{
			String buildDir = IDFUtil.getBuildDir(project);
			GenericJsonReader jsonReader = new GenericJsonReader(
					buildDir + File.separator + IDFConstants.PROECT_DESCRIPTION_JSON);
			String value = jsonReader.getValue("app_elf"); //$NON-NLS-1$
			if (!StringUtil.isEmpty(value))
			{
				return new Path(buildDir).append(value);
			}
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return null;
	}

	private String getMonitorBaudRate()
	{
		return new SDKConfigJsonReader(project).getValue("ESPTOOLPY_MONITOR_BAUD"); //$NON-NLS-1$
	}

	public Process start() throws Exception
	{
		if(!dependenciesAreInstalled())
		{
			throw new Exception("Missing Dependencies"); //$NON-NLS-1$
		}
		List<String> arguments = commandArgsWithSocketServer();
		
		// command to execute
		Logger.log(arguments.toString());

		// CDT Build environment variables
		Map<String, String> idfEnvMap = new IDFEnvironmentVariables().getSystemEnvMap();

		// Disable buffering of output
		idfEnvMap.put("PYTHONUNBUFFERED", "1"); //$NON-NLS-1$ //$NON-NLS-2$

		// Update with the CDT build environment variables
		Map<String, String> environment = new HashMap<>(System.getenv());
		environment.putAll(idfEnvMap);

		Logger.log(environment.toString());

		// Update PATH and Path
		String idfPath = environment.get("PATH"); //$NON-NLS-1$
		String processPath = environment.get("Path"); //$NON-NLS-1$
		if (!StringUtil.isEmpty(idfPath) && !StringUtil.isEmpty(processPath)) // if both exist!
		{
			idfPath = idfPath.concat(";").concat(processPath); //$NON-NLS-1$
			environment.put("PATH", idfPath); //$NON-NLS-1$
			environment.remove("Path");//$NON-NLS-1$
		}

		// Add ptyprocess terminal argument
		environment.remove("TERM_PROGRAM"); // for OS X //$NON-NLS-1$
		environment.put("TERM", "vt102"); //$NON-NLS-1$ //$NON-NLS-2$

		Logger.log(environment.toString());

		// working dir
		IPath workingDir = project.getLocation();

		LocalTerminal localTerminal = new LocalTerminal(arguments, workingDir.toFile(), environment);
		return localTerminal.connect();
	}
	
	public boolean dependenciesAreInstalled()
	{
		List<String> arguments = new ArrayList<>();
		String websocketClient = "websocket-client"; //$NON-NLS-1$
		final String pythonEnvPath = IDFUtil.getIDFPythonEnvPath();
		arguments.add(pythonEnvPath);
		arguments.add("-m"); //$NON-NLS-1$
		arguments.add("pip"); //$NON-NLS-1$
		arguments.add("list"); //$NON-NLS-1$

		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		String cmdMsg = "Executing " + getCommandString(arguments); //$NON-NLS-1$
		Logger.log(cmdMsg);
		Map<String, String> environment = new HashMap<>(System.getenv());
		Logger.log(environment.toString());

		IStatus status;
		try
		{
			status = processRunner.runInBackground(arguments, org.eclipse.core.runtime.Path.ROOT, environment);
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(),
						IDFCorePlugin.errorStatus("Unable to get the process status.", null)); //$NON-NLS-1$
				return false;
			}

			String cmdOutput = status.getMessage();
			if (cmdOutput.contains(websocketClient))
			{
				return true;
			}

			arguments.remove(arguments.size() - 1);
			arguments.add("install"); //$NON-NLS-1$
			arguments.add(websocketClient);
			status = processRunner.runInBackground(arguments, org.eclipse.core.runtime.Path.ROOT, environment);
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(),
						IDFCorePlugin.errorStatus("Unable to get the process status.", null)); //$NON-NLS-1$
				return false;
			}

			Logger.log(status.getMessage());
			return true;
		}
		catch (IOException e)
		{
			Logger.log(e);
			return false;
		}
	}

	private String getCommandString(List<String> arguments)
	{
		StringBuilder builder = new StringBuilder();
		arguments.forEach(entry -> builder.append(entry + " ")); //$NON-NLS-1$

		return builder.toString().trim();
	}

}
