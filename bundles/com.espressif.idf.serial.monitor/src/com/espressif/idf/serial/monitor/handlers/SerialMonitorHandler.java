package com.espressif.idf.serial.monitor.handlers;

import java.io.File;
import java.io.IOException;

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
	private String numberOfCols;
	private String numberOfRows;
	
	public SerialMonitorHandler(IProject project, String serialPort, String filterOptions, String numberOfCols, String numberOfRows)
	{
		this.project = project;
		this.serialPort = serialPort;
		this.filterOptions = filterOptions;
		this.numberOfCols = numberOfCols;
		this.numberOfRows = numberOfRows;
	}

	public Process invokeIDFMonitor()
	{
		// python path
		String pythonPath = IDFUtil.getIDFPythonEnvPath();

		File idfMonitorScriptFile = IDFUtil.getIDFPythonScriptFile();

		IDFMonitor monitor = new IDFMonitor(project, serialPort, filterOptions, pythonPath,
				idfMonitorScriptFile.getAbsolutePath(), numberOfCols, numberOfRows);
		try
		{
			return monitor.start();
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
		return null;
	}

}
