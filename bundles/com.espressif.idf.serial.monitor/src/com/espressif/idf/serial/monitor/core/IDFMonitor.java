package com.espressif.idf.serial.monitor.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.IDFConsole;

public class IDFMonitor
{
	private String port;
	private String baudRate;
	private String pythonBinPath;
	private String idfMonitorToolPath;
	private IProject project;

	public IDFMonitor(IProject project, String port, String baudRate, String pythonBinPath, String idfMonitorToolPath)
	{
		this.project = project;
		this.port = port;
		this.baudRate = baudRate;
		this.pythonBinPath = pythonBinPath;
		this.idfMonitorToolPath = idfMonitorToolPath;
	}

	public List<String> commandArgs()
	{
		List<String> args = new ArrayList<>();
		args.add(pythonBinPath);
		args.add(idfMonitorToolPath);
		args.add("monitor");
		args.add("-p");
		args.add(port);

		return args;
	}

	public void start() throws IOException
	{
		// Create console
		IDFConsole console = new IDFConsole();
		MessageConsoleStream stream = console.getConsoleStream("ESP-IDF Monitor"); //$NON-NLS-1$

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
		environment.remove("TERM_PROGRAM"); // for OS X
		environment.put("TERM", "vt102");

		Logger.log(environment.toString());

		// working dir
		IPath workingDir = project.getLocation();

		LocalTerminal localTerminal = new LocalTerminal(arguments, workingDir.toFile(), environment, stream);
		localTerminal.connect();
	}

}
