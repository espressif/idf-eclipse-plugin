/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core;

import java.text.MessageFormat;

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
			throw new Exception(MessageFormat.format(Messages.SDKConfigUtil_CouldNotFindBuildDir,
					buildFolder.getFullPath().toOSString()));
		}
		return buildFolder.getLocation().toFile().getAbsolutePath() + IPath.SEPARATOR + IDFConstants.CONFIG_FOLDER
				+ IPath.SEPARATOR + IDFConstants.KCONFIG_MENUS_JSON;
	}

}
