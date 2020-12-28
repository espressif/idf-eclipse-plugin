package com.espressif.idf.serial.monitor.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.StringUtil;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFMonitor
{
	private String port;
	private String pythonBinPath;
	private String idfMonitorToolPath;
	private IProject project;
	private String filterOptions;

	public IDFMonitor(IProject project, String port, String filterOptions, String pythonBinPath, String idfMonitorToolPath)
	{
		this.project = project;
		this.port = port;
		this.pythonBinPath = pythonBinPath;
		this.idfMonitorToolPath = idfMonitorToolPath;
		this.filterOptions = filterOptions;
	}

	public List<String> commandArgs()
	{
		List<String> args = new ArrayList<>();
		args.add(pythonBinPath);
		args.add(idfMonitorToolPath);
		args.add("monitor"); //$NON-NLS-1$
		args.add("-p"); //$NON-NLS-1$
		args.add(port);
		args.add("--print-filter=" + filterOptions); //$NON-NLS-1$

		return args;
	}

	public Process start() throws IOException
	{
		// command to execute
		List<String> arguments = commandArgs();
		Logger.log(arguments.toString());

		// CDT Build environment variables
		Map<String, String> idfEnvMap = new IDFEnvironmentVariables().getEnvMap();

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

}
