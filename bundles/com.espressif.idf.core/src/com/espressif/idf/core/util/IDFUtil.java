/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChain2;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import com.aptana.core.ShellExecutable;
import com.aptana.core.util.ExecutableUtil;
import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFEnvironmentVariables;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFUtil
{
	private static final String PYTHON = "python"; //$NON-NLS-1$

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
	 * @return idf_tools.py file path based on the IDF_PATH defined in the environment variables
	 */
	public static File getIDFToolsScriptFile()
	{
		String idf_path = getIDFPath();
		String idf_py_script = idf_path + IPath.SEPARATOR + IDFConstants.TOOLS_FOLDER + IPath.SEPARATOR
				+ IDFConstants.IDF_TOOLS_SCRIPT;
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

	public static IPath getPythonPath()
	{
		return Path.fromOSString(getPythonExecutable());
	}

	public static String getPythonExecutable()
	{

		IPath pythonPath = ExecutableUtil.find(PYTHON, true, null);
		if (pythonPath != null)
		{
			return pythonPath.toOSString();
		}

		return PYTHON;
	}
	
	/**
	 * Search for a command from the given path string
	 * 
	 * @param command to be searched
	 * @param pathStr PATH string 
	 * @return
	 */
	public static java.nio.file.Path findCommand(String command, String pathStr)
	{
		try
		{
			java.nio.file.Path cmdPath = Paths.get(command);
			if (cmdPath.isAbsolute())
			{
				return cmdPath;
			}

			String[] path = pathStr.split(File.pathSeparator);
			for (String dir : path)
			{
				java.nio.file.Path commandPath = Paths.get(dir, command);
				if (Files.exists(commandPath) && commandPath.toFile().isFile())
				{
					return commandPath;
				}
				else
				{
					if (Platform.getOS().equals(Platform.OS_WIN32)
							&& !(command.endsWith(".exe") || command.endsWith(".bat"))) //$NON-NLS-1$ //$NON-NLS-2$
					{
						commandPath = Paths.get(dir, command + ".exe"); //$NON-NLS-1$
						if (Files.exists(commandPath))
						{
							return commandPath;
						}
					}
				}
			}

		}
		catch (InvalidPathException e)
		{
			// ignore
		}
		return null;
	}
	
	/**
	 * Search for a command in the CDT build PATH environment variables
	 * 
	 * @param command name <i>ex: python</i>
	 * @return command complete path
	 */
	public static String findCommandFromBuildEnvPath(String command)
	{
		String pathStr = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.PATH);
		if (pathStr != null)
		{
			 java.nio.file.Path commandPath = findCommand(command, pathStr);
			 if (commandPath != null)
			 {
				 return commandPath.toFile().getAbsolutePath();
			 }
		}
		return null;
		
	}
}
