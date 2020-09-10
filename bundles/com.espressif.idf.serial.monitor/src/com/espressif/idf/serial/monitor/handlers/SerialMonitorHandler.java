package com.espressif.idf.serial.monitor.handlers;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.SDKConfigJsonReader;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.serial.monitor.core.IDFMonitor;
import com.espressif.idf.ui.EclipseUtil;

public class SerialMonitorHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{

		// Get project
		IProject project = EclipseUtil.getSelectedProjectInExplorer();

		// Get baud rate
		String baudRate = getsdkconfigBaudRate();
		if (StringUtil.isEmpty(baudRate))
		{
			baudRate = "115200"; // default baud rate
		}

		// Get serial port
		String serialPort = getLastUsedSerialPort();

		// python path
		String pythonPath = IDFUtil.getIDFPythonEnvPath();

		File idfMonitorScriptFile = IDFUtil.getIDFPythonScriptFile();

		IDFMonitor monitor = new IDFMonitor(project, serialPort, baudRate, pythonPath,
				idfMonitorScriptFile.getAbsolutePath());
		try
		{
			monitor.start();
		}
		catch (IOException e)
		{
			Logger.log(e);
		}

		return null;
	}

	protected String getsdkconfigBaudRate()
	{
		IResource resource = EclipseUtil.getSelectionResource();
		if (resource != null)
		{
			IProject project = resource.getProject();
			return new SDKConfigJsonReader(project).getValue("ESPTOOLPY_MONITOR_BAUD"); //$NON-NLS-1$
		}
		return null;
	}

	protected String getLastUsedSerialPort()
	{
		Preferences preferences = InstanceScope.INSTANCE.getNode("com.espressif.idf.launch.serial.ui"); //$NON-NLS-1$
		return preferences.get("com.espressif.idf.launch.serial.core.serialPort", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
