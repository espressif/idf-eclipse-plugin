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
import org.eclipse.embedcdt.core.EclipseUtils;
import org.eclipse.embedcdt.core.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.launch.ITargetedLaunch;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.target.ILaunchTargetUIManager;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.build.IDFBuildConfiguration;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.DfuCommandsUtil;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.launch.serial.SerialFlashLaunchTargetProvider;
import com.espressif.idf.launch.serial.util.ESPFlashUtil;

/**
 * Flashing into esp32 board
 *
 */
public class SerialFlashLaunchConfigDelegate extends CoreBuildGenericLaunchConfigDelegate {
	private static final String SYSTEM_PATH_PYTHON = "${system_path:python}"; //$NON-NLS-1$
	private static final String OPENOCD_PREFIX = "com.espressif.idf.debug.gdbjtag.openocd"; //$NON-NLS-1$
	private static final String INSTALL_FOLDER = "install.folder"; //$NON-NLS-1$
	private static final String SERVER_EXECUTABLE = OPENOCD_PREFIX + ".openocd.gdbServerExecutable"; //$NON-NLS-1$
	private static final String DEFAULT_PATH = "${openocd_path}/"; //$NON-NLS-1$
	private static final String DEFAULT_EXECUTABLE = "bin/openocd"; //$NON-NLS-1$
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
		boolean isFlashOverJtag = configuration.getAttribute(IDFLaunchConstants.FLASH_OVER_JTAG, false);
		serialPort = ((SerialFlashLaunch) launch).getLaunchTarget()
				.getAttribute(SerialFlashLaunchTargetProvider.ATTR_SERIAL_PORT, ""); //$NON-NLS-1$
		if (DfuCommandsUtil.isDfu()) {
			if (checkIfPortIsEmpty(configuration)) {
				return;
			}
			DfuCommandsUtil.flashDfuBins(getProject(configuration), launch, monitor, serialPort);
			return;
		}
		if (isFlashOverJtag) {
			flashOverJtag(configuration, launch);
			return;
		}
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
		String espFlashCommand = ESPFlashUtil.getEspFlashCommand(serialPort);
		Logger.log(espFlashCommand);
		if (checkIfPortIsEmpty(configuration)) {
			return;
		}
		String arguments = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_TOOL_ARGUMENTS,
				espFlashCommand);
		arguments = arguments.replace(ESPFlashUtil.SERIAL_PORT, serialPort);
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
		Map<String, String> envMap = new IDFEnvironmentVariables().getSystemEnvMap();

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

	protected void flashOverJtag(ILaunchConfiguration configuration, ILaunch launch) throws CoreException {
		List<String> commands = new ArrayList<>();

		String openocdExe = configuration.getAttribute(SERVER_EXECUTABLE, DEFAULT_PATH + DEFAULT_EXECUTABLE);
		String tmp = EclipseUtils.getPreferenceValueForId(OPENOCD_PREFIX, INSTALL_FOLDER, "", //$NON-NLS-1$
				getProject(configuration));
		tmp = tmp.replace("bin", ""); //$NON-NLS-1$ //$NON-NLS-2$
		openocdExe = openocdExe.replace(DEFAULT_PATH, tmp);
		commands.add(openocdExe);

		String arguments = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_TOOL_ARGUMENTS, ""); //$NON-NLS-1$
		arguments = arguments.replace(DEFAULT_PATH, tmp).trim();
		commands.addAll(StringUtils.splitCommandLineOptions(arguments));

		String flashCommand = ESPFlashUtil.getEspJtagFlashCommand(configuration) + " exit"; //$NON-NLS-1$
		commands.add(flashCommand);

		try {
			Process p = Runtime.getRuntime().exec(commands.toArray(new String[0]));
			DebugPlugin.newProcess(launch, p, String.join(" ", commands)); //$NON-NLS-1$
		} catch (IOException e) {
			Logger.log(e);
		}
	}

	private boolean checkIfPortIsEmpty(ILaunchConfiguration configuration) {
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
			showMessage(configuration);
			return true;
		}
		return false;
	}

	private static void showMessage(ILaunchConfiguration configuration) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				boolean isYes = MessageDialog.openConfirm(Display.getDefault().getActiveShell(),
						com.espressif.idf.launch.serial.internal.Messages.SerialPortNotFoundTitle,
						com.espressif.idf.launch.serial.internal.Messages.SerialPortNotFoundMsg);
				if (isYes) {
					ILaunchTargetUIManager targetUIManager = Activator.getService(ILaunchTargetUIManager.class);
					ILaunchTargetManager launchTargetManager = Activator.getService(ILaunchTargetManager.class);
					targetUIManager.editLaunchTarget(launchTargetManager.getDefaultLaunchTarget(configuration));
				}
			}
		});
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

	public static String getSystemPythonPath() {
		return SYSTEM_PATH_PYTHON;
	}
}
