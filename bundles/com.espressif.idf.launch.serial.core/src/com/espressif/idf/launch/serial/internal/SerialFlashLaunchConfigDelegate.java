/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial version
 *     Espressif Systems Ltd — Kondal Kolipaka <kondal.kolipaka@espressif.com>

 *******************************************************************************/
package com.espressif.idf.launch.serial.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.launch.CoreBuildGenericLaunchConfigDelegate;
import org.eclipse.cdt.debug.core.launch.NullProcess;
import org.eclipse.cdt.debug.internal.core.Messages;
import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.launch.ITargetedLaunch;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.target.ILaunchTargetUIManager;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.DfuCommandsUtil;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.RecheckConfigsHelper;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.launch.serial.SerialFlashLaunchTargetProvider;
import com.espressif.idf.launch.serial.util.ESPFlashUtil;

/**
 * Flashing into esp32 board
 *
 */
@SuppressWarnings("restriction")
public class SerialFlashLaunchConfigDelegate extends CoreBuildGenericLaunchConfigDelegate
{
	private static final String SYSTEM_PATH_PYTHON = "${system_path:python}"; //$NON-NLS-1$
	private String serialPort;

	@Override
	public ITargetedLaunch getLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target)
			throws CoreException
	{
		if (target == null)
		{
			ILaunchBarManager barManager = IDFCorePlugin.getService(ILaunchBarManager.class);
			target = barManager.getActiveLaunchTarget();
		}
		return new SerialFlashLaunch(configuration, mode, null, target);
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException
	{
		// Start the launch (pause the serial port)
		((SerialFlashLaunch) launch).start();

		serialPort = ((SerialFlashLaunch) launch).getLaunchTarget()
				.getAttribute(SerialFlashLaunchTargetProvider.ATTR_SERIAL_PORT, ""); //$NON-NLS-1$
		if (DfuCommandsUtil.isDfu())
		{
			DfuCommandsUtil.flashDfuBins(configuration, getProject(configuration), launch, monitor);
			return;
		}
		if (ESPFlashUtil.isJtag())
		{
			ESPFlashUtil.flashOverJtag(configuration, launch);
			return;
		}
		launchInternal(configuration, mode, launch, monitor);
	}

	protected void launchInternal(ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor monitor) throws CoreException
	{
		IStringVariableManager varManager = VariablesPlugin.getDefault().getStringVariableManager();

		String location = varManager.performStringSubstitution(IDFUtil.getIDFPythonEnvPath());
		if (StringUtil.isEmpty(location))
		{
			location = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_LOCATION, SYSTEM_PATH_PYTHON);
		}
		if (StringUtil.isEmpty(location))
		{
			launch.addProcess(new NullProcess(launch, Messages.CoreBuildGenericLaunchConfigDelegate_NoAction));
			return;
		}
		else
		{
			String substLocation = varManager.performStringSubstitution(location);
			if (substLocation.isEmpty())
			{
				throw new CoreException(new Status(IStatus.ERROR, CDebugCorePlugin.PLUGIN_ID,
						String.format(Messages.CoreBuildGenericLaunchConfigDelegate_SubstitutionFailed, location)));
			}
			location = substLocation;
		}

		if (!new File(location).canExecute())
		{
			throw new CoreException(new Status(IStatus.ERROR, CDebugCorePlugin.PLUGIN_ID,
					String.format(Messages.CoreBuildGenericLaunchConfigDelegate_CommandNotValid, location)));
		}

		List<String> commands = new ArrayList<>();
		commands.add(location);

		// build the flash command
		String espFlashCommand = ESPFlashUtil.getEspFlashCommand(serialPort);
		Logger.log(espFlashCommand);
		if (checkIfPortIsEmpty(configuration))
		{
			return;
		}
		String arguments = configuration.getAttribute(IDFLaunchConstants.ATTR_SERIAL_FLASH_ARGUMENTS, espFlashCommand);
		arguments = arguments.replace(ESPFlashUtil.SERIAL_PORT, serialPort);
		arguments = varManager.performStringSubstitution(arguments);
		if (!arguments.isEmpty())
		{
			commands.addAll(Arrays.asList(varManager.performStringSubstitution(arguments).split(" "))); //$NON-NLS-1$
		}

		String workingDirectory = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
				""); //$NON-NLS-1$ ˇ
		File workingDir;
		if (workingDirectory.isEmpty())
		{
			workingDir = new File(getProject(configuration).getLocationURI());
		}
		else
		{
			workingDir = new File(varManager.performStringSubstitution(workingDirectory));
			if (!workingDir.isDirectory())
			{
				throw new CoreException(new Status(IStatus.ERROR, CDebugCorePlugin.PLUGIN_ID,
						String.format(Messages.CoreBuildGenericLaunchConfigDelegate_WorkingDirNotExists, location)));
			}
		}

		// Reading CDT build environment variables
		Map<String, String> envMap = new IDFEnvironmentVariables().getSystemEnvMap();

		// Turn it into an envp format
		List<String> strings = new ArrayList<>(envMap.size());
		for (Entry<String, String> entry : envMap.entrySet())
		{
			StringBuilder buffer = new StringBuilder(entry.getKey());
			buffer.append('=').append(entry.getValue());
			strings.add(buffer.toString());
		}

		String[] envArray = strings.toArray(new String[strings.size()]);
		Process p = DebugPlugin.exec(commands.toArray(new String[0]), workingDir, envArray);
		DebugPlugin.newProcess(launch, p, String.join(" ", commands)); //$NON-NLS-1$
	}

	private boolean checkIfPortIsEmpty(ILaunchConfiguration configuration)
	{
		boolean isMatch = false;
		try
		{
			String[] ports = SerialPort.list();
			for (String port : ports)
			{
				if (port.equals(serialPort))
				{
					isMatch = true;
					break;
				}
			}
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
		if (!isMatch)
		{
			showMessage(configuration);
			return true;
		}
		return false;
	}

	private static void showMessage(ILaunchConfiguration configuration)
	{
		Display.getDefault().asyncExec(() -> {
			boolean isYes = MessageDialog.openConfirm(Display.getDefault().getActiveShell(),
					com.espressif.idf.launch.serial.internal.Messages.SerialPortNotFoundTitle,
					com.espressif.idf.launch.serial.internal.Messages.SerialPortNotFoundMsg);
			if (isYes)
			{
				ILaunchTargetUIManager targetUIManager = Activator.getService(ILaunchTargetUIManager.class);
				ILaunchTargetManager launchTargetManager = Activator.getService(ILaunchTargetManager.class);
				targetUIManager.editLaunchTarget(launchTargetManager.getDefaultLaunchTarget(configuration));
			}
		});
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException
	{

		IProject project = getProject(configuration);
		RecheckConfigsHelper.revalidateToolchain(project);
		return superBuildForLaunch(configuration, mode, monitor);
	}

	public static String getSystemPythonPath()
	{
		return SYSTEM_PATH_PYTHON;
	}
}
