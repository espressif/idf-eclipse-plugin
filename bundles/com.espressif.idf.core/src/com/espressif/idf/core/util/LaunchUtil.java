/*******************************************************************************
 * Copyright 2024-2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
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
			IResource[] mappedResource = config.getMappedResources();
			if (mappedResource != null && mappedResource.length > 0 && mappedResource[0].getProject().equals(project)
					&& config.getType().getIdentifier().contentEquals(configIndentifier))
			{
				return config;
			}
		}
		return null;
	}

	/*
	 * In case when the active configuration is debugging, we are using bound launch configuration to build the project
	 */
	public ILaunchConfiguration getBoundConfiguration(ILaunchConfiguration configuration) throws CoreException
	{
		String bindedLaunchConfigName = configuration.getAttribute(IDFLaunchConstants.ATTR_LAUNCH_CONFIGURATION_NAME,
				StringUtil.EMPTY);
		ILaunchConfiguration[] launchConfigurations = launchManager.getLaunchConfigurations(DebugPlugin.getDefault()
				.getLaunchManager().getLaunchConfigurationType(IDFLaunchConstants.RUN_LAUNCH_CONFIG_TYPE));
		ILaunchConfiguration defaultConfiguration = launchConfigurations[0];
		return Stream.of(launchConfigurations).filter(config -> config.getName().contentEquals(bindedLaunchConfigName))
				.findFirst().orElse(defaultConfiguration);

	}

}
