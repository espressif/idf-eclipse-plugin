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
package com.espressif.idf.launch.serial.ui.internal;

import java.nio.file.Path;

import org.eclipse.cdt.debug.core.launch.CoreBuildLaunchConfigDelegate;
import org.eclipse.cdt.launch.ui.corebuild.CoreBuildTab;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.launchbar.ui.internal.LaunchBarLaunchConfigDialog;

import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;

@SuppressWarnings("restriction")
public class SerialFlashLaunchConfigTabGroup extends AbstractLaunchConfigurationTabGroup
{

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode)
	{
		if (dialog instanceof LaunchBarLaunchConfigDialog)
		{
			setTabs(new ILaunchConfigurationTab[] { new CMakeMainTab2(), new EnvironmentTab(), new CommonTab() });
		}
		else
		{
			setTabs(new ILaunchConfigurationTab[] { new CoreBuildTab(), new CMakeMainTab2(), new EnvironmentTab(),
					new CommonTab() });
		}

	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration)
	{
		super.performApply(configuration);
		try
		{
			IProject project = CoreBuildLaunchConfigDelegate.getProject(configuration);
			if (project == null)
			{
				return;
			}
			String buildFolder = configuration.getAttribute(IDFLaunchConstants.BUILD_FOLDER_PATH,
					IDFUtil.getBuildDir(project));
			buildFolder = buildFolder.isBlank() ? IDFUtil.getBuildDir(project) : buildFolder;
			if (!Path.of(buildFolder).isAbsolute())
			{
				buildFolder = project.getLocation().append(buildFolder).toOSString();
			}
			IDFUtil.setBuildDir(project, buildFolder);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration)
	{
		try
		{
			IResource[] resources = configuration.getMappedResources();
			if (resources != null)
			{
				super.initializeFrom(configuration);
			}
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
	}

}
