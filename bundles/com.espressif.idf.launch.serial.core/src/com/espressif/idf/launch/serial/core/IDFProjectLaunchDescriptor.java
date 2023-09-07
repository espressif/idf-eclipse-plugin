/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.launch.serial.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFProjectLaunchDescriptor extends PlatformObject implements ILaunchDescriptor
{

	private final ILaunchDescriptorType type;
	private final IProject project;
	private ILaunchConfiguration configuration;

	public IDFProjectLaunchDescriptor(ILaunchDescriptorType type, IProject project, ILaunchConfiguration configuration)
	{
		this.type = type;
		this.project = project;
		this.configuration = configuration;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter)
	{
		if (ILaunchConfiguration.class.equals(adapter))
		{
			return adapter.cast(configuration);
		}
		else if (IProject.class.equals(adapter))
		{
			return adapter.cast(project);
		}
		return super.getAdapter(adapter);
	}

	@Override
	public String getName()
	{
		if (configuration != null)
		{
			return configuration.getName();
		}
		return project.getName();
	}

	@Override
	public ILaunchDescriptorType getType()
	{
		return type;
	}

	public ILaunchConfiguration getConfiguration()
	{
		return configuration;
	}

	public IProject getProject()
	{
		return project;
	}

	@Override
	public String toString()
	{
		return getName(); // for debugging purposes
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IDFProjectLaunchDescriptor other = (IDFProjectLaunchDescriptor) obj;
		if (configuration == null)
		{
			if (other.configuration != null)
				return false;
		}
		else if (!configuration.equals(other.configuration))
			return false;

		if (project == null)
		{
			if (other.project != null)
				return false;
		}
		else if (!project.equals(other.project))
			return false;

		if (type == null)
		{
			if (other.type != null)
				return false;
		}
		else if (!type.equals(other.type))
			return false;
		return true;
	}

}
