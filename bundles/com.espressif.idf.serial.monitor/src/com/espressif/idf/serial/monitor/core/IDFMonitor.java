package com.espressif.idf.serial.monitor.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.embedcdt.debug.gdbjtag.core.DebugUtils;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFEnvironmentVariables;
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
		if (!StringUtil.isEmpty(filterOptions))
		{
			args.add("--print_filter"); //$NON-NLS-1$
			args.add(filterOptions);
		}
		if (isDebuggerRunning(project) && isNoResetFlagSupported())
		{
			args.add("--no-reset"); //$NON-NLS-1$
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

	private boolean isDebuggerRunning(IProject project)
	{
		ILaunchManager launchManager = getLaunchManager();
		ILaunchConfiguration[] configs;
		boolean isDebugLaunchRunning = false;
		try
		{
			configs = launchManager.getLaunchConfigurations();
			for (ILaunchConfiguration config : configs)
			{
				IResource[] resources = config.getMappedResources();
				if (resources != null && resources[0].getProject().equals(project))
				{
					isDebugLaunchRunning = DebugUtils.isLaunchConfigurationStarted(config);
					if (isDebugLaunchRunning) // any debug launchconfig related to the project
					{
						return true;
					}
				}
			}

		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return false;
	}

	private boolean isNoResetFlagSupported()
	{
		String version = IDFUtil.getEspIdfVersion();
		Pattern p = Pattern.compile("([0-9][.][0-9])"); //$NON-NLS-1$
		Matcher m = p.matcher(version);
		return m.find() && Double.parseDouble(m.group(0)) >= 5.0;
	}

	protected ILaunchManager getLaunchManager()
	{
		return DebugPlugin.getDefault().getLaunchManager();
	}

	private String getMonitorBaudRate()
	{
		return new SDKConfigJsonReader(project).getValue("ESPTOOLPY_MONITOR_BAUD"); //$NON-NLS-1$
	}

	public Process start() throws Exception
	{
		List<String> arguments = commandArgsWithSocketServer();

		// command to execute
		Logger.log(arguments.toString());

		// CDT Build environment variables
		Map<String, String> idfEnvMap = new IDFEnvironmentVariables().getSystemEnvMap();

		// Disable buffering of output
		idfEnvMap.put("PYTHONUNBUFFERED", "1"); //$NON-NLS-1$ //$NON-NLS-2$

		// Update with the CDT build environment variables
		Map<String, String> environment = new HashMap<>(IDFUtil.getSystemEnv());
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
}
