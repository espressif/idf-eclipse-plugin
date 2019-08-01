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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.launch.CoreBuildGenericLaunchConfigDelegate;
import org.eclipse.cdt.debug.core.launch.NullProcess;
import org.eclipse.cdt.debug.internal.core.Messages;
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
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.launch.ITargetedLaunch;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.build.IDFBuildConfiguration;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.launch.serial.SerialFlashLaunchTargetProvider;

/**
 * Flashing into esp32 board
 *
 */
public class SerialFlashLaunchConfigDelegate extends CoreBuildGenericLaunchConfigDelegate {

	private static final String SYSTEM_PATH_PYTHON = "${system_path:python}"; //$NON-NLS-1$
	public static final String TYPE_ID = "com.espressif.idf.launch.serial.launchConfigurationType"; //$NON-NLS-1$

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

		String location = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_LOCATION,
				SYSTEM_PATH_PYTHON);
		if (location.isEmpty()) {
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
		String espFlashCommand = getEspFlashCommand(launch);
		Logger.log(espFlashCommand);

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

		String[] envp = DebugPlugin.getDefault().getLaunchManager().getEnvironment(configuration);
		IEnvironmentVariableManager buildEnvironmentManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IEnvironmentVariable[] variables = buildEnvironmentManager.getVariables((ICConfigurationDescription) null,
				true);
		if (variables != null) {
			String path = null;
			for (IEnvironmentVariable iEnvironmentVariable : variables) {
				if (iEnvironmentVariable.getName().equals("PATH")) { //$NON-NLS-1$
					path = iEnvironmentVariable.getValue();
					break;
				}
			}

			if (path != null) {
				if (envp != null) {
					boolean pathFound = false;
					for (int i = 0; i < envp.length; i++) {
						if (envp[i].startsWith("PATH")) { //Add to the existing PATH //$NON-NLS-1$
							envp[i] = envp[i].concat(":").concat(path); //$NON-NLS-1$
							pathFound = true;
							break;
						}
					}
					if (!pathFound) //add new PATH variable
					{
						String[] newEnvp = new String[envp.length + 1];
						System.arraycopy(envp, 0, newEnvp, 0, envp.length);

						newEnvp[envp.length + 1] = "PATH=" + path; //$NON-NLS-1$
						envp = newEnvp;
					}
				} else {
					envp = new String[] { "PATH=" + path }; //$NON-NLS-1$
				}
			}
		}

		Process p = DebugPlugin.exec(commands.toArray(new String[0]), workingDir, envp);
		DebugPlugin.newProcess(launch, p, String.join(" ", commands)); //$NON-NLS-1$
	}

	/**
	 * @param launch
	 * @return command to flash the application
	 */
	private String getEspFlashCommand(ILaunch launch) {

		List<String> commands = new ArrayList<>();
		commands.add(IDFUtil.getIDFPythonScriptFile().getAbsolutePath());
		commands.add("-p"); //$NON-NLS-1$

		String serialPort = ((SerialFlashLaunch) launch).getLaunchTarget()
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

			((IDFBuildConfiguration) buildConfig).setLaunchTarget(target);

		}

		// proceed with the build
		return superBuildForLaunch(configuration, mode, monitor);
	}

}
