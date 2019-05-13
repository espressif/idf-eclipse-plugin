/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core;

import java.io.File;
import java.net.URI;
import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.espressif.idf.core.IDFConstants;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class SDKConfigUtil
{

	/**
	 * Default build directory which used by CDT
	 * 
	 * @return
	 * @throws CoreException
	 */
	public File getBuildDirectory(IProject project) throws CoreException
	{
		assert (project == null || !project.exists()) : Messages.SDKConfigUtil_ProjectNull + project;

		IFile file = project.getFile(IDFConstants.BUILD_FOLDER);
		URI locationURI = file.getLocationURI();
		if (locationURI != null)
		{
			return new File(locationURI);
		}

		return null;
	}

	/**
	 * @param buildDirectory
	 * @return
	 * @throws Exception 
	 */
	public String getConfigMenuFilePath(IProject project) throws Exception
	{
		File buildDirectory = getBuildDirectory(project);
		if (buildDirectory == null || !buildDirectory.exists())
		{
			throw new Exception(MessageFormat.format(Messages.SDKConfigUtil_CouldNotFindBuildDir, buildDirectory));
		}
		return buildDirectory.getAbsolutePath() + IPath.SEPARATOR + IDFConstants.CONFIG_FOLDER + IPath.SEPARATOR
				+ IDFConstants.KCONFIG_MENUS_JSON;
	}

}
