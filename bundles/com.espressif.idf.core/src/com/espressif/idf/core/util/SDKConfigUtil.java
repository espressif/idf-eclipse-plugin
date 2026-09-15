/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.io.File;

import org.eclipse.core.resources.IProject;

import com.espressif.idf.core.IDFConstants;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class SDKConfigUtil
{

	/**
	 * @param project project whose active build directory should be used
	 * @return path to kconfig_menus.json
	 * @throws Exception if the build directory does not exist
	 * @deprecated Pass the already resolved build directory to keep multi-config operations scoped.
	 */
	@Deprecated(forRemoval = true)
	public String getConfigMenuFilePath(IProject project) throws Exception
	{
		return getConfigMenuFilePath(IDFUtil.getBuildDir(project));
	}

	/**
	 * @param buildDirectory
	 * @return
	 * @throws Exception
	 */
	public String getConfigMenuFilePath(String buildDirectory) throws Exception
	{
		if (!new File(buildDirectory).exists())
		{
			throw new Exception("Build directory is not found: " + buildDirectory); //$NON-NLS-1$
		}
		return new File(new File(buildDirectory, IDFConstants.CONFIG_FOLDER), IDFConstants.KCONFIG_MENUS_JSON)
				.getAbsolutePath();
	}

	/**
	 * @param buildDirectory
	 * @return
	 * @throws Exception
	 */
	public String getSDKConfigJsonFilePath(IProject project) throws Exception
	{
		return getSDKConfigJsonFilePath(IDFUtil.getBuildDir(project));
	}

	/**
	 * @param buildDirectory
	 * @return
	 * @throws Exception
	 */
	public String getSDKConfigJsonFilePath(String buildDirectory) throws Exception
	{
		if (!new File(buildDirectory).exists())
		{
			throw new Exception("Build directory is not found: " + buildDirectory); //$NON-NLS-1$
		}
		return new File(new File(buildDirectory, IDFConstants.CONFIG_FOLDER), IDFConstants.SDKCONFIG_JSON_FILE_NAME)
				.getAbsolutePath();
	}
}
