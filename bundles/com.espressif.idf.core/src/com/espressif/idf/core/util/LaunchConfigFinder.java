/*******************************************************************************
 * Copyright 2024-2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.ILaunchDescriptor;

public class LaunchConfigFinder
{
	private ILaunchManager launchManager;

	public LaunchConfigFinder()
	{
		launchManager = DebugPlugin.getDefault().getLaunchManager();
	}

	public LaunchConfigFinder(ILaunchManager launchManager)
	{
		this.launchManager = launchManager;
	}

	public ILaunchConfiguration findAppropriateLaunchConfig(ILaunchDescriptor descriptor, String configIndentifier)
			throws CoreException
	{
		IProject project = descriptor.getAdapter(IProject.class);
		for (ILaunchConfiguration config : launchManager.getLaunchConfigurations())
		{
			launchManager.getLaunchConfigurations();
			IResource[] mappedResource = config.getMappedResources();
			if (mappedResource != null && mappedResource.length > 0 && mappedResource[0].getProject().equals(project)
					&& config.getType().getIdentifier().contentEquals(configIndentifier))
			{
				return config;
			}
		}
		return null;
	}

}
