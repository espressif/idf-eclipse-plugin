/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.build;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.internal.CMakeBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.espressif.idf.core.IDFConstants;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
@SuppressWarnings("restriction")
public class IDFBuildConfiguration extends CMakeBuildConfiguration
{

	public IDFBuildConfiguration(IBuildConfiguration config, String name) throws CoreException
	{
		super(config, name);
	}

	public IDFBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain)
	{
		super(config, name, toolChain, null, "run"); //$NON-NLS-1$
	}

	public IDFBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain,
			ICMakeToolChainFile toolChainFile, String launchMode)
	{
		super(config, name, toolChain, toolChainFile, launchMode);

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter)
	{
		if (adapter.equals(IDFBuildConfiguration.class))
		{
			return (T) this;
		}
		return super.getAdapter(adapter);
	}
	
	@Override
	public Path getBuildDirectory() throws CoreException
	{
		IProject project = getProject();
		String absolutePath = project.getLocation().toFile().getAbsolutePath();
		return Paths.get(absolutePath, IDFConstants.BUILD_FOLDER);
		 
	}
	
	@Override
	public IContainer getBuildContainer() throws CoreException
	{
		IProject project = getProject();
		IFolder buildRootFolder = project.getFolder("build"); //$NON-NLS-1$

		IProgressMonitor monitor = new NullProgressMonitor();
		if (!buildRootFolder.exists())
		{
			buildRootFolder.create(IResource.FORCE | IResource.DERIVED, true, monitor);
		}

		return buildRootFolder;
	}
}
