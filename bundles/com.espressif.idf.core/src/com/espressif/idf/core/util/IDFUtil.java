/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.espressif.idf.core.ExecutableFinder;
import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;

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
	 * @return idf_monitor.py file path based on the configured IDF_PATH in the CDT build environment variables
	 */
	public static File getIDFMonitorScriptFile()
	{
		String idf_path = getIDFPath();
		String idf_py_monitor_script = idf_path + IPath.SEPARATOR + IDFConstants.TOOLS_FOLDER + IPath.SEPARATOR
				+ IDFConstants.IDF_MONITOR_SCRIPT;
		return new File(idf_py_monitor_script);
	}

	/**
	 * @return idf_size.py file path based on the IDF_PATH defined in the environment variables
	 */
	public static File getIDFSizeScriptFile()
	{
		String idf_path = getIDFPath();
		String idf_py_script = idf_path + IPath.SEPARATOR + IDFConstants.TOOLS_FOLDER + IPath.SEPARATOR
				+ IDFConstants.IDF_SIZE_SCRIPT;
		return new File(idf_py_script);
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
				idfPath = System.getenv(IDFEnvironmentVariables.IDF_PATH);
			}
		}

		return idfPath;
	}

	/**
	 * @return value for IDF_PYTHON_ENV_PATH environment variable. If IDF_PYTHON_ENV_PATH not found, will identify
	 *         python from the build environment PATH
	 */
	public static String getIDFPythonEnvPath()
	{
		String idfPyEnvPath = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.IDF_PYTHON_ENV_PATH);
		if (!StringUtil.isEmpty(idfPyEnvPath))
		{

			if (Platform.getOS().equals(Platform.OS_WIN32))
			{
				idfPyEnvPath = idfPyEnvPath + "/" + "Scripts"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			else
			{
				idfPyEnvPath = idfPyEnvPath + "/" + "bin"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			java.nio.file.Path commandPath = findCommand(IDFConstants.PYTHON_CMD, idfPyEnvPath);
			if (commandPath != null)
			{
				return commandPath.toFile().getAbsolutePath();
			}
		}
		return findCommandFromBuildEnvPath(IDFConstants.PYTHON_CMD);

	}

	public static String getPythonExecutable()
	{
		IPath pythonPath = ExecutableFinder.find(IDFConstants.PYTHON_CMD, true);
		if (pythonPath != null)
		{
			return pythonPath.toOSString();
		}

		return IDFConstants.PYTHON_CMD;
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
			Logger.log(e);
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

	public static String getLineSeparatorValue()
	{
		IScopeContext scope = InstanceScope.INSTANCE;

		IScopeContext[] scopeContext = new IScopeContext[] { scope };
		IEclipsePreferences node = scopeContext[0].getNode(Platform.PI_RUNTIME);
		return node.get(Platform.PREF_LINE_SEPARATOR, System.getProperty("line.separator")); //$NON-NLS-1$
	}

	public static String getIDFExtraPaths()
	{
		String IDF_PATH = getIDFPath();
		if (!StringUtil.isEmpty(IDF_PATH))
		{
			IPath IDF_ADD_PATHS_EXTRAS = new Path(StringUtil.EMPTY);
			IDF_ADD_PATHS_EXTRAS = IDF_ADD_PATHS_EXTRAS.append(IDF_PATH).append("components/esptool_py/esptool"); //$NON-NLS-1$
			IDF_ADD_PATHS_EXTRAS = IDF_ADD_PATHS_EXTRAS.append(":"); //$NON-NLS-1$
			IDF_ADD_PATHS_EXTRAS = IDF_ADD_PATHS_EXTRAS.append(IDF_PATH).append("components/espcoredump"); //$NON-NLS-1$
			IDF_ADD_PATHS_EXTRAS = IDF_ADD_PATHS_EXTRAS.append(":"); //$NON-NLS-1$
			IDF_ADD_PATHS_EXTRAS = IDF_ADD_PATHS_EXTRAS.append(IDF_PATH).append("components/partition_table"); //$NON-NLS-1$
			IDF_ADD_PATHS_EXTRAS = IDF_ADD_PATHS_EXTRAS.append(":"); //$NON-NLS-1$
			IDF_ADD_PATHS_EXTRAS = IDF_ADD_PATHS_EXTRAS.append(IDF_PATH).append("components/app_update"); //$NON-NLS-1$

			return IDF_ADD_PATHS_EXTRAS.toString();
		}

		return StringUtil.EMPTY;
	}
	
	/**
	 * OpenOCD Installation folder
	 * 
	 * @return
	 */
	public static String getOpenOCDLocation()
	{
		String openOCDScriptPath = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.OPENOCD_SCRIPTS);
		if (!StringUtil.isEmpty(openOCDScriptPath))
		{
			return openOCDScriptPath.replace(File.separator + "share" + File.separator + "openocd" + File.separator + "scripts", "") + File.separator + "bin"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		return StringUtil.EMPTY;
	}
	
	/**
	 * Get Xtensa toolchain path based on the target configured for the project
	 * @return
	 */
	public static String getXtensaToolchainExecutablePath(IProject project)
	{
		Pattern GDB_PATTERN = Pattern.compile("xtensa-esp(.*)-elf-gdb(\\.exe)?"); //$NON-NLS-1$
		String projectEspTarget = null;
		if (project != null)
		{
			projectEspTarget = new SDKConfigJsonReader(project).getValue("IDF_TARGET"); //$NON-NLS-1$
		}

		// Process PATH to find the toolchain path
		IEnvironmentVariable cdtPath = new IDFEnvironmentVariables().getEnv("PATH"); //$NON-NLS-1$
		if (cdtPath != null)
		{
			for (String dirStr : cdtPath.getValue().split(File.pathSeparator))
			{
				File dir = new File(dirStr);
				if (dir.isDirectory())
				{
					for (File file : dir.listFiles())
					{
						if (file.isDirectory())
						{
							continue;
						}
						Matcher matcher = GDB_PATTERN.matcher(file.getName());
						if (matcher.matches())
						{
							String path = file.getAbsolutePath();
							Logger.log("GDB executable:"+ path);
							String[] tuples = file.getName().split("-");
							if (projectEspTarget == null) //If no IDF_TARGET
							{
								return path;
							}
							else if (tuples[1].equals(projectEspTarget))
							{
								return path;
							}

						}
					}
				}
			}
		}
		return null;
	}
}
