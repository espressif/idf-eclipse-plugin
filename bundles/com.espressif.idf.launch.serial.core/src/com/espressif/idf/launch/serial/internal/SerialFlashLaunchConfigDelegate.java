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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager2;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.launch.CoreBuildGenericLaunchConfigDelegate;
import org.eclipse.cdt.debug.core.launch.NullProcess;
import org.eclipse.cdt.debug.internal.core.Messages;
import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
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
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.launch.ITargetedLaunch;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.build.IDFBuildConfiguration;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.launch.serial.SerialFlashLaunchTargetProvider;
import com.espressif.idf.launch.serial.util.EspFlashCommandGenerator;

/**
 * Flashing into esp32 board
 *
 */
public class SerialFlashLaunchConfigDelegate extends CoreBuildGenericLaunchConfigDelegate {

	private static final String SYSTEM_PATH_PYTHON = "${system_path:python}"; //$NON-NLS-1$
	private String serialPort;

	@Override
	public ITargetedLaunch getLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target)
			throws CoreException {
		return new SerialFlashLaunch(configuration, mode, null, target);
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		// Start the launch (pause the serial port)
		((SerialFlashLaunch) launch).start();

		launchInternal(configuration, mode, launch, monitor);
	}

	protected void launchInternal(ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor monitor) throws CoreException {
		IStringVariableManager varManager = VariablesPlugin.getDefault().getStringVariableManager();

		String location = IDFUtil.getIDFPythonEnvPath();
		if (StringUtil.isEmpty(location)) {
			location = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_LOCATION, SYSTEM_PATH_PYTHON);
		}
		if (StringUtil.isEmpty(location)) {
			launch.addProcess(new NullProcess(launch, Messages.CoreBuildGenericLaunchConfigDelegate_NoAction));
			return;
		} else {
			String substLocation = varManager.performStringSubstitution(location);
			if (substLocation.isEmpty()) {
				throw new CoreException(new Status(IStatus.ERROR, CDebugCorePlugin.PLUGIN_ID,
						String.format(Messages.CoreBuildGenericLaunchConfigDelegate_SubstitutionFailed, location)));
			}
			location = substLocation;
		}

		if (!new File(location).canExecute()) {
			throw new CoreException(new Status(IStatus.ERROR, CDebugCorePlugin.PLUGIN_ID,
					String.format(Messages.CoreBuildGenericLaunchConfigDelegate_CommandNotValid, location)));
		}

		List<String> commands = new ArrayList<>();
		commands.add(location);

		//build the flash command
		String espFlashCommand = EspFlashCommandGenerator.getEspFlashCommand(launch);
		Logger.log(espFlashCommand);
		if (checkIfPortIsEmpty()) {
			return;
		}
		String arguments = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_TOOL_ARGUMENTS,
				espFlashCommand);
		if (!arguments.isEmpty()) {
			commands.addAll(Arrays.asList(varManager.performStringSubstitution(arguments).split(" "))); //$NON-NLS-1$
		}

		String workingDirectory = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
				""); //$NON-NLS-1$ˇ
		File workingDir;
		if (workingDirectory.isEmpty()) {
			workingDir = new File(getProject(configuration).getLocationURI());
		} else {
			workingDir = new File(varManager.performStringSubstitution(workingDirectory));
			if (!workingDir.isDirectory()) {
				throw new CoreException(new Status(IStatus.ERROR, CDebugCorePlugin.PLUGIN_ID,
						String.format(Messages.CoreBuildGenericLaunchConfigDelegate_WorkingDirNotExists, location)));
			}
		}

		//Reading CDT build environment variables
		Map<String, String> envMap = new IDFEnvironmentVariables().getEnvMap();

		// Turn it into an envp format
		List<String> strings = new ArrayList<>(envMap.size());
		for (Entry<String, String> entry : envMap.entrySet()) {
			StringBuilder buffer = new StringBuilder(entry.getKey());
			buffer.append('=').append(entry.getValue());
			strings.add(buffer.toString());
		}

		String[] envArray = strings.toArray(new String[strings.size()]);
		Process p = DebugPlugin.exec(commands.toArray(new String[0]), workingDir, envArray);
		DebugPlugin.newProcess(launch, p, String.join(" ", commands)); //$NON-NLS-1$
	}

	private boolean checkIfPortIsEmpty() {
		boolean isMatch = false;
		try {
			String[] ports = SerialPort.list();
			for (String port : ports) {
				if (port.equals(serialPort)) {
					isMatch = true;
					break;
				}
			}
		} catch (IOException e) {
			Logger.log(e);
		}
		if (!isMatch) {
			showMessage();
			return true;
		}
		return false;
	}

	private static void showMessage() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openError(Display.getDefault().getActiveShell(),
						com.espressif.idf.launch.serial.internal.Messages.SerialPortNotFoundTitle,
						com.espressif.idf.launch.serial.internal.Messages.SerialPortNotFoundMsg);
			}
		});
	}

	/**
	 * @param launch
	 * @return command to flash the application
	 */
	private String getEspFlashCommand(ILaunch launch) {

		List<String> commands = new ArrayList<>();
		commands.add(IDFUtil.getIDFPythonScriptFile().getAbsolutePath());
		commands.add("-p"); //$NON-NLS-1$

		serialPort = ((SerialFlashLaunch) launch).getLaunchTarget()
				.getAttribute(SerialFlashLaunchTargetProvider.ATTR_SERIAL_PORT, ""); //$NON-NLS-1$

		commands.add(serialPort);

		commands.add(IDFConstants.FLASH_CMD);

		return String.join(" ", commands); //$NON-NLS-1$

	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException {
		ICBuildConfiguration buildConfig = getBuildConfiguration(configuration, mode, target, monitor);
		if (buildConfig != null && buildConfig instanceof IDFBuildConfiguration) {
			IProject project = getProject(configuration);
			IProjectDescription desc = project.getDescription();
			desc.setActiveBuildConfig(buildConfig.getBuildConfiguration().getName());
			project.setDescription(desc, monitor);
			ICBuildConfigurationManager mgr = CCorePlugin.getService(ICBuildConfigurationManager.class);
			ICBuildConfigurationManager2 manager = (ICBuildConfigurationManager2) mgr;
			manager.recheckConfigs();

			((IDFBuildConfiguration) buildConfig).setLaunchTarget(target);

		}

		// proceed with the build
		return superBuildForLaunch(configuration, mode, monitor);
	}

}
