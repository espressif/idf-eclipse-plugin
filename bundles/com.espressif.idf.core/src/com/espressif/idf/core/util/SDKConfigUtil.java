/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import com.espressif.idf.core.IDFConstants;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class SDKConfigUtil
{

	/**
	 * @param buildDirectory
	 * @return
	 * @throws Exception
	 */
	public String getConfigMenuFilePath(IProject project) throws Exception
	{
		IFolder buildFolder = project.getFolder(IDFConstants.BUILD_FOLDER);
		if (!buildFolder.exists())
		{
			throw new Exception("Build directory not found");
		}
		return buildFolder.getLocation().toFile().getAbsolutePath() + IPath.SEPARATOR + IDFConstants.CONFIG_FOLDER
				+ IPath.SEPARATOR + IDFConstants.KCONFIG_MENUS_JSON;
	}

	/**
	 * @param buildDirectory
	 * @return
	 * @throws Exception
	 */
	public String getSDKConfigJsonFilePath(IProject project) throws Exception
	{
		IFolder buildFolder = project.getFolder(IDFConstants.BUILD_FOLDER);
		if (!buildFolder.exists())
		{
			throw new Exception("Build directory not found");
		}
		return buildFolder.getLocation().toFile().getAbsolutePath() + IPath.SEPARATOR + IDFConstants.CONFIG_FOLDER
				+ IPath.SEPARATOR + IDFConstants.SDKCONFIG_JSON_FILE_NAME;
	}
}
