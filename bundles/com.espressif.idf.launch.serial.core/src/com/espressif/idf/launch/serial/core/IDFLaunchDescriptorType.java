/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.launch.serial.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;

import com.espressif.idf.core.IDFProjectNature;
import com.espressif.idf.core.build.IDFLaunchConstants;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFLaunchDescriptorType implements ILaunchDescriptorType
{
	private final Map<ILaunchConfiguration, ILaunchDescriptor> descriptors = new HashMap<>();

	@Override
	public ILaunchDescriptor getDescriptor(Object launchObject) throws CoreException
	{
		if (launchObject instanceof IProject)
		{
			IProject project = (IProject) launchObject;
			if (IDFProjectNature.hasNature(project))
			{
				return new IDFProjectLaunchDescriptor(this, project, null);
			}
		}
		else if (launchObject instanceof ILaunchConfiguration)
		{
			ILaunchConfiguration configuration = (ILaunchConfiguration) launchObject;
			if (IDFLaunchConstants.RUN_LAUNCH_CONFIG_TYPE.equals(configuration.getType().getIdentifier()))
			{
				IProject project = getProject(configuration);
				if (project != null && project.isAccessible() && IDFProjectNature.hasNature(project))
				{
					return descriptors.computeIfAbsent(configuration,
							config -> new IDFProjectLaunchDescriptor(this, project, config));
				}
			}
		}
		return null;
	}

	private IProject getProject(ILaunchConfiguration configuration) throws CoreException
	{
		IResource[] mappedResources = configuration.getMappedResources();
		if (mappedResources != null)
		{
			for (IResource resource : mappedResources)
			{
				if (resource != null)
				{
					return resource.getProject();
				}
			}
		}

		String projectName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
		return projectName.isEmpty() ? null : ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	}

}
