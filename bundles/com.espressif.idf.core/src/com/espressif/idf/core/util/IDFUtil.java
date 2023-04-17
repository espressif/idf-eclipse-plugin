/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.osgi.service.prefs.BackingStoreException;

import com.espressif.idf.core.ExecutableFinder;
import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.build.ESP32C2ToolChain;
import com.espressif.idf.core.build.ESP32C3ToolChain;
import com.espressif.idf.core.build.ESP32H2ToolChain;
import com.espressif.idf.core.build.ESPToolChainProvider;
import com.espressif.idf.core.logging.Logger;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFUtil
{

	private IDFUtil()
	{
	}

	private static Boolean idfSupportsSpaces;

	/**
	 * @return sysviewtrace_proc.py file path based on the IDF_PATH defined in the environment variables
	 */
	public static File getIDFSysviewTraceScriptFile()
	{
		String idf_path = getIDFPath();
		String idf_sysview_trace_script = idf_path + IPath.SEPARATOR + IDFConstants.TOOLS_FOLDER + IPath.SEPARATOR
				+ IDFConstants.IDF_APP_TRACE_FOLDER + IPath.SEPARATOR + IDFConstants.IDF_SYSVIEW_TRACE_SCRIPT;
		return new File(idf_sysview_trace_script);
	}

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
	 * @return idf_monitor.py file path based on the IDF_PATH defined in the environment variables
	 */
	public static File getIDFMonitorPythonScriptFile()
	{
		String idf_path = getIDFPath();
		String idf_py_script = idf_path + IPath.SEPARATOR + IDFConstants.TOOLS_FOLDER + IPath.SEPARATOR
				+ IDFConstants.IDF_MONITOR_PYTHON_SCRIPT;
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
	 * @return tools.json file for tools to install
	 */
	public static File getIDFToolsJsonFileForInstallation()
	{
		String idf_path = getIDFPath();
		String idf_tools_json_file = idf_path + IPath.SEPARATOR + IDFConstants.TOOLS_FOLDER + IPath.SEPARATOR
				+ IDFConstants.IDF_TOOLS_JSON;
		return new File(idf_tools_json_file);
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
		idfPyEnvPath = idfPyEnvPath.strip();
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

	public static boolean checkIfIdfSupportsSpaces()
	{

		if (idfSupportsSpaces != null)
		{
			return idfSupportsSpaces;
		}
		String version = getEspIdfVersion();
		Pattern p = Pattern.compile("([0-9][.][0-9])"); //$NON-NLS-1$
		Matcher m = p.matcher(version);
		idfSupportsSpaces = m.find() && Double.parseDouble(m.group(0)) >= 5.0;
		return idfSupportsSpaces;
	}

	public static String getPythonExecutable()
	{
		IPath pythonPath = ExecutableFinder.find(IDFConstants.PYTHON3_CMD, true); // look for python3
		if (pythonPath == null)
		{
			pythonPath = ExecutableFinder.find(IDFConstants.PYTHON_CMD, true); // look for python
		}
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
			IPath IDF_ADD_PATHS_EXTRAS = new Path(IDF_PATH).append("components/esptool_py/esptool"); //$NON-NLS-1$
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
			return openOCDScriptPath
					.replace(File.separator + "share" + File.separator + "openocd" + File.separator + "scripts", "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					+ File.separator + "bin"; //$NON-NLS-1$
		}

		return StringUtil.EMPTY;
	}

	/**
	 * Get Xtensa toolchain path based on the target configured for the project
	 * 
	 * @return
	 */
	public static String getXtensaToolchainExecutablePath(IProject project)
	{
		String projectEspTarget = null;
		if (project != null)
		{
			projectEspTarget = new SDKConfigJsonReader(project).getValue("IDF_TARGET"); //$NON-NLS-1$
		}
		return getXtensaToolchainExecutablePathByTarget(projectEspTarget);
	}

	public static String getXtensaToolchainExecutablePathByTarget(String projectEspTarget)
	{
		Pattern gdb_pattern = ESPToolChainProvider.GDB_PATTERN; // default
		if (!StringUtil.isEmpty(projectEspTarget) && (projectEspTarget.equals(ESP32C3ToolChain.OS)
				|| projectEspTarget.equals(ESP32C2ToolChain.OS) || projectEspTarget.equals(ESP32H2ToolChain.OS)))
		{
			gdb_pattern = ESPToolChainProvider.GDB_PATTERN_ESP32C3;
			projectEspTarget = ESP32C3ToolChain.ARCH;
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

						Matcher matcher = gdb_pattern.matcher(file.getName());
						if (matcher.matches())
						{
							String path = file.getAbsolutePath();
							Logger.log("GDB executable:" + path); //$NON-NLS-1$
							String[] tuples = file.getName().split("-"); //$NON-NLS-1$
							if (projectEspTarget == null) // If no IDF_TARGET
							{
								return null;
							}
							else if (tuples[1].equals(projectEspTarget) || tuples[0].equals(projectEspTarget))
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

	/**
	 * Get Addr2Line path based on the target configured for the project with toolchain
	 * 
	 * @return
	 */
	public static String getXtensaToolchainExecutableAddr2LinePath(IProject project)
	{
		Pattern GDB_PATTERN = Pattern.compile("xtensa-esp(.*)-elf-addr2line(\\.exe)?"); //$NON-NLS-1$
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
							Logger.log("addr2line executable:" + path); //$NON-NLS-1$
							String[] tuples = file.getName().split("-"); //$NON-NLS-1$
							if (projectEspTarget == null) // If no IDF_TARGET
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

	/**
	 * @return esptool.py file path based on configured IDF_PATH in the CDT build environment variables
	 */
	public static File getEspToolScriptFile()
	{
		String idf_path = getIDFPath();
		String esp_tool_script = idf_path + IPath.SEPARATOR + IDFConstants.COMPONENTS_FOLDER + IPath.SEPARATOR
				+ IDFConstants.ESP_TOOL_FOLDER_PY + IPath.SEPARATOR + IDFConstants.ESP_TOOL_FOLDER + IPath.SEPARATOR
				+ IDFConstants.ESP_TOOL_SCRIPT;
		return new File(esp_tool_script);
	}

	public static File getEspCoreDumpScriptFile()
	{
		String idf_path = getIDFPath();
		String esp_tool_script = idf_path + IPath.SEPARATOR + IDFConstants.COMPONENTS_FOLDER + IPath.SEPARATOR
				+ IDFConstants.ESP_CORE_DUMP_FOLDER + IPath.SEPARATOR + IDFConstants.ESP_CORE_DUMP_SCRIPT;
		return new File(esp_tool_script);
	}

	public static String getEspIdfVersion()
	{
		if (IDFUtil.getIDFPath() != null && IDFUtil.getIDFPythonEnvPath() != null)
		{
			List<String> commands = new ArrayList<>();
			commands.add(IDFUtil.getIDFPythonEnvPath());
			commands.add(IDFUtil.getIDFPythonScriptFile().getAbsolutePath());
			commands.add("--version"); //$NON-NLS-1$
			Map<String, String> envMap = new IDFEnvironmentVariables().getSystemEnvMap();
			return runCommand(commands, envMap);
		}

		return ""; //$NON-NLS-1$
	}

	public static String getOpenocdVersion()
	{
		String openocdLocation = IDFUtil.getOpenOCDLocation();
		String openocdExecutable = Platform.getOS().equals(Platform.OS_WIN32) ? "openocd.exe" : "openocd"; //$NON-NLS-1$ //$NON-NLS-2$
		if (openocdLocation != null && !openocdLocation.isBlank())
		{
			List<String> commands = new ArrayList<>();
			commands.add(IDFUtil.getOpenOCDLocation() + File.separator + openocdExecutable);
			commands.add("--version"); //$NON-NLS-1$
			Map<String, String> envMap = new IDFEnvironmentVariables().getSystemEnvMap();
			return runCommand(commands, envMap);
		}
		return ""; //$NON-NLS-1$
	}

	private static String runCommand(List<String> arguments, Map<String, String> env)
	{
		String exportCmdOp = ""; //$NON-NLS-1$
		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		try
		{
			IStatus status = processRunner.runInBackground(arguments, Path.ROOT, env);
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(), IDFCorePlugin.errorStatus("Status can't be null", null)); //$NON-NLS-1$
				return exportCmdOp;
			}

			// process export command output
			exportCmdOp = status.getMessage();
			Logger.log(exportCmdOp);
		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);
		}
		return exportCmdOp;
	}

	/**
	 * Project build directory
	 * 
	 * @param project
	 * @return
	 * @throws CoreException
	 */
	public static String getBuildDir(IProject project) throws CoreException
	{
		String buildDirectory = project
				.getPersistentProperty(new QualifiedName(IDFCorePlugin.PLUGIN_ID, IDFConstants.BUILD_DIR_PROPERTY));
		if (StringUtil.isEmpty(buildDirectory))
		{
			buildDirectory = project.getFolder(IDFConstants.BUILD_FOLDER).getLocation().toOSString();
		}

		return buildDirectory;
	}

	/**
	 * Project .map file path
	 * 
	 * @param project
	 * @return
	 */
	public static IPath getMapFilePath(IProject project)
	{
		try
		{
			String buildDir = IDFUtil.getBuildDir(project);
			String filePath = buildDir + File.separator + IDFConstants.PROECT_DESCRIPTION_JSON;
			GenericJsonReader jsonReader = new GenericJsonReader(filePath);
			String value = jsonReader.getValue("app_elf"); //$NON-NLS-1$
			if (!StringUtil.isEmpty(value))
			{
				value = value.replace(".elf", ".map"); // Assuming .elf and .map files have //$NON-NLS-1$ //$NON-NLS-2$
														// the
														// same file name

				return new Path(buildDir).append(value);
			}
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return null;
	}

	/**
	 * Project .bin file path
	 * 
	 * @param project
	 * @return
	 */
	public static IPath getBinFilePath(IProject project)
	{
		try
		{
			String buildDir = IDFUtil.getBuildDir(project);
			String filePath = buildDir + File.separator + IDFConstants.PROECT_DESCRIPTION_JSON;
			GenericJsonReader jsonReader = new GenericJsonReader(filePath);
			String value = jsonReader.getValue("app_bin"); //$NON-NLS-1$
			if (!StringUtil.isEmpty(value))
			{
				return new Path(buildDir).append(value);

			}
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return null;
	}

	/**
	 * Project .elf file path
	 * 
	 * @param project
	 * @return
	 */
	public static IPath getELFFilePath(IProject project)
	{
		try
		{
			String buildDir = IDFUtil.getBuildDir(project);
			String filePath = buildDir + File.separator + IDFConstants.PROECT_DESCRIPTION_JSON;
			GenericJsonReader jsonReader = new GenericJsonReader(filePath);
			String value = jsonReader.getValue("app_elf"); //$NON-NLS-1$
			if (!StringUtil.isEmpty(value))
			{
				return new Path(buildDir).append(value);

			}
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return null;
	}

	@SuppressWarnings("nls")
	public static String getNvsGeneratorScriptPath()
	{
		return Stream.of(getIDFPath(), "components", "nvs_flash", "nvs_partition_generator", "nvs_partition_gen.py")
				.collect(Collectors.joining(String.valueOf(IPath.SEPARATOR)));

	}

	/**
	 * Update the openocd path in configurations
	 */
	public static void updateEspressifPrefPageOpenocdPath()
	{
		IEclipsePreferences newNode = DefaultScope.INSTANCE.getNode("com.espressif.idf.debug.gdbjtag.openocd"); //$NON-NLS-1$
		newNode.put("install.folder", getOpenOCDLocation()); //$NON-NLS-1$
		try
		{
			newNode.flush();
		}
		catch (BackingStoreException e)
		{
			Logger.log(e);
		}
	}

	public static String getCurrentTarget()
	{
		IProject project = null;
		try
		{
			ILaunchBarManager launchBarManager = IDFCorePlugin.getService(ILaunchBarManager.class);
			ILaunchConfiguration activeConfig = launchBarManager.getActiveLaunchConfiguration();
			if (activeConfig == null || activeConfig.getMappedResources() == null)
			{
				Logger.log(Messages.IDFUtil_CantFindProjectMsg);
				return StringUtil.EMPTY;
			}
			project = activeConfig.getMappedResources()[0].getProject();
			Logger.log("Project:: " + project); //$NON-NLS-1$
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return new SDKConfigJsonReader(project).getValue("IDF_TARGET"); //$NON-NLS-1$
	}
}
