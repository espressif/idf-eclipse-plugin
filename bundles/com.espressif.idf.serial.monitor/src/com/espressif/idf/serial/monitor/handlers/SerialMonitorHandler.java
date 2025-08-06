package com.espressif.idf.serial.monitor.handlers;

import org.eclipse.core.resources.IProject;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.serial.monitor.core.IDFMonitor;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class SerialMonitorHandler
{

	private IProject project;
	private String serialPort;
	private String filterOptions;
	private int serverPort;
	private boolean encryptionOption;

	public SerialMonitorHandler(IProject project, String serialPort, String filterOptions, boolean encryptionOption,
			int serverPort)
	{
		this.project = project;
		this.serialPort = serialPort;
		this.filterOptions = filterOptions;
		this.serverPort = serverPort;
		this.encryptionOption = encryptionOption;
	}

	public Process invokeIDFMonitor()
	{
		// python path
		String pythonPath = IDFUtil.getIDFPythonEnvPath();

		IDFMonitor monitor = new IDFMonitor(project, serialPort, filterOptions, encryptionOption, pythonPath,
				serverPort);
		try
		{
			return monitor.start();
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		return null;
	}

}
