/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.io.File;
import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.core.runtime.IPath;

import com.aptana.core.ShellExecutable;
import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFEnvironmentVariables;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFUtil
{
	/**
	 * @return idf.py file path based on the IDF_PATH defined in the environment variables
	 */
	public static File getIDFPythonScriptFile()
	{
		String idf_path = getIDFPath();
		String idf_py_script = idf_path + IPath.SEPARATOR + IDFConstants.TOOLS_FOLDER + IPath.SEPARATOR
				+ IDFConstants.IDF_PYTHON_SCRIPT;
		return new File(idf_py_script);
	}

	/**
	 * @param projBuildDir project build directory which is used by CDT
	 * @return kconfig_menus.json file path from the project active build directory
	 * @throws Exception
	 */
	public static String getConfigMenuJsonFile(File projBuildDir) throws Exception
	{
		if (projBuildDir == null || !projBuildDir.exists())
		{
			throw new Exception(MessageFormat.format(Messages.IDFUtil_CouldNotFindDir, projBuildDir));
		}
		return projBuildDir.getAbsolutePath() + IPath.SEPARATOR + IDFConstants.CONFIG_FOLDER + IPath.SEPARATOR
				+ IDFConstants.KCONFIG_MENUS_JSON;
	}

	/**
	 * @return file path for IDF_PATH
	 */
	public static String getIDFPath()
	{
		String idfPath = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.IDF_PATH);
		if (StringUtil.isEmpty(idfPath))
		{

			// Try to get it from the system properties
			idfPath = System.getProperty(IDFEnvironmentVariables.IDF_PATH);
			if (StringUtil.isEmpty(idfPath))
			{
				Map<String, String> environment = ShellExecutable.getEnvironment();
				idfPath = environment.get(IDFEnvironmentVariables.IDF_PATH);
			}
		}

		return idfPath;
	}
}
