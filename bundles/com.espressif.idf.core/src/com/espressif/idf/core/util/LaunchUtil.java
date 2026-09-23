/*******************************************************************************
 * Copyright 2024-2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.ILaunchDescriptor;

import com.espressif.idf.core.build.IDFLaunchConstants;

public class LaunchUtil
{
	private final ILaunchManager launchManager;

	public LaunchUtil(ILaunchManager launchManager)
	{
		this.launchManager = launchManager;
	}

	public ILaunchConfiguration findAppropriateLaunchConfig(ILaunchDescriptor descriptor, String configIndentifier)
			throws CoreException
	{
		IProject project = descriptor.getAdapter(IProject.class);
		for (ILaunchConfiguration config : launchManager.getLaunchConfigurations())
		{
			IProject mappedProject = getMappedProject(config);
			if (mappedProject != null && mappedProject.equals(project)
					&& config.getType().getIdentifier().contentEquals(configIndentifier))
			{
				return config;
			}
		}
		return null;
	}

	/**
	 * Returns the project a launch configuration is mapped to, or <code>null</code> when it carries no mapped
	 * resource. Unlike CDT's {@code CoreBuildLaunchConfigDelegate#getProject}, this never fails on such a
	 * configuration.
	 */
	public static IProject getMappedProject(ILaunchConfiguration configuration) throws CoreException
	{
		if (configuration == null)
		{
			return null;
		}

		IResource[] mappedResources = configuration.getMappedResources();
		return mappedResources == null || mappedResources.length == 0 ? null : mappedResources[0].getProject();
	}

	/*
	 * In case when the active configuration is debugging, we are using bound launch configuration to build the project
	 */
	public ILaunchConfiguration getBoundConfiguration(ILaunchConfiguration configuration) throws CoreException
	{
		String bindedLaunchConfigName = configuration.getAttribute(IDFLaunchConstants.ATTR_LAUNCH_CONFIGURATION_NAME,
				StringUtil.EMPTY);
		ILaunchConfiguration[] launchConfigurations = launchManager.getLaunchConfigurations(
				launchManager.getLaunchConfigurationType(IDFLaunchConstants.RUN_LAUNCH_CONFIG_TYPE));
		for (ILaunchConfiguration launchConfiguration : launchConfigurations)
		{
			if (launchConfiguration.getName().contentEquals(bindedLaunchConfigName))
			{
				return launchConfiguration;
			}
		}

		IProject project = getMappedProject(configuration);
		if (project != null)
		{
			for (ILaunchConfiguration launchConfiguration : launchConfigurations)
			{
				if (project.equals(getMappedProject(launchConfiguration)))
				{
					return launchConfiguration;
				}
			}
		}

		return configuration;

	}

}
