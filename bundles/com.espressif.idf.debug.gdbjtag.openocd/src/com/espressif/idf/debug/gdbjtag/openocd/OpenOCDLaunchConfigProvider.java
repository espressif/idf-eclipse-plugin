/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.launchbar.core.AbstractLaunchConfigProvider;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;

import com.espressif.idf.core.build.IDFLaunchConstants;

public class OpenOCDLaunchConfigProvider extends AbstractLaunchConfigProvider
{
	private Map<IProject, ILaunchConfiguration> configs = new HashMap<>();

	@Override
	public boolean supports(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException
	{
		return target != null && target.getTypeId().equals(IDFLaunchConstants.LAUNCH_TARGET_TYPE_ID);
	}

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException
	{
		return DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfigurationType("com.espressif.idf.debug.gdbjtag.openocd.launchConfigurationType"); //$NON-NLS-1$
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException
	{
		ILaunchConfiguration config = null;
		IProject project = descriptor.getAdapter(IProject.class);
		if (project != null)
		{
			config = configs.get(project);
			if (config == null)
			{
				config = createLaunchConfiguration(descriptor, target);
				// launch config added will get called below to add it to the
				// configs map
			}
		}
		return config;
	}

	@Override
	protected void populateLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target,
			ILaunchConfigurationWorkingCopy workingCopy) throws CoreException
	{
		super.populateLaunchConfiguration(descriptor, target, workingCopy);

		// Set the project and the connection
		IProject project = descriptor.getAdapter(IProject.class);
		workingCopy.setMappedResources(new IResource[] { project });
	}

	@Override
	public boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException
	{
		if (ownsLaunchConfiguration(configuration))
		{
			IProject project = configuration.getMappedResources()[0].getProject();
			configs.put(project, configuration);
			return true;
		}
		return false;
	}

	@Override
	public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException
	{
		for (Entry<IProject, ILaunchConfiguration> entry : configs.entrySet())
		{
			if (configuration.equals(entry.getValue()))
			{
				configs.remove(entry.getKey());
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean launchConfigurationChanged(ILaunchConfiguration configuration) throws CoreException
	{
		// TODO not sure I care
		return false;
	}

	@Override
	public void launchDescriptorRemoved(ILaunchDescriptor descriptor) throws CoreException
	{
		IProject project = descriptor.getAdapter(IProject.class);
		if (project != null)
		{
			configs.remove(project);
		}
	}

	@Override
	public void launchTargetRemoved(ILaunchTarget target) throws CoreException
	{
		// TODO:
	}

}
