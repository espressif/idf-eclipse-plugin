/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.io.File;

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
		String buildDir = IDFUtil.getBuildDir(project);
		if (!new File(buildDir).exists())
		{
			throw new Exception("Build directory is not found: "+ buildDir); //$NON-NLS-1$
		}
		return new File(buildDir).getAbsolutePath() + IPath.SEPARATOR + IDFConstants.CONFIG_FOLDER
				+ IPath.SEPARATOR + IDFConstants.KCONFIG_MENUS_JSON;
	}

	/**
	 * @param buildDirectory
	 * @return
	 * @throws Exception
	 */
	public String getSDKConfigJsonFilePath(IProject project) throws Exception
	{
		String buildDir = IDFUtil.getBuildDir(project);
		if (!new File(buildDir).exists())
		{
			throw new Exception("Build directory is not found: "+ buildDir); //$NON-NLS-1$
		}
		return new File(buildDir).getAbsolutePath() + IPath.SEPARATOR + IDFConstants.CONFIG_FOLDER
				+ IPath.SEPARATOR + IDFConstants.SDKCONFIG_JSON_FILE_NAME;
	}
}
