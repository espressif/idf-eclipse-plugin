/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFProjectNature implements IProjectNature
{

	private IProject project;
	public static final String ID = IDFCorePlugin.getId() + ".idfNature"; //$NON-NLS-1$

	@Override
	public IProject getProject()
	{
		return project;
	}

	@Override
	public void setProject(IProject project)
	{
		this.project = project;
	}

	@Override
	public void configure() throws CoreException
	{
	}

	@Override
	public void deconfigure() throws CoreException
	{
	}

	public static boolean hasNature(IProject project) throws CoreException
	{
		IProjectDescription projDesc = project.getDescription();
		for (String id : projDesc.getNatureIds())
		{
			if (id.equals(ID))
			{
				return true;
			}
		}
		return false;
	}

}
