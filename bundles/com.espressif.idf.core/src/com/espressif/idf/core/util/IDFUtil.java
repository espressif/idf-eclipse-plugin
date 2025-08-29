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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.debug.core.launch.CoreBuildLaunchConfigDelegate;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.prefs.BackingStoreException;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFCorePreferenceConstants;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.LaunchBarTargetConstants;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.SystemExecutableFinder;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.toolchain.ESPToolChainManager;

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
	 * @return idf.py file path based on the IDF_PATH given in the argument
	 */
	public static File getIDFPythonScriptFile(String idf_path)
	{
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
	 * @return idf_tools.py file path based on the IDF_PATH given in the argument
	 */
	public static File getIDFToolsScriptFile(String idf_path)
	{
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
		IPath pythonPath = new SystemExecutableFinder().find(IDFConstants.PYTHON3_CMD); // look for python3
		if (pythonPath == null)
		{
			pythonPath = new SystemExecutableFinder().find(IDFConstants.PYTHON_CMD); // look for python
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

	public static String getToolchainExePathForActiveTarget()
	{
		ILaunchBarManager launchBarManager = IDFCorePlugin.getService(ILaunchBarManager.class);
		try
		{
			ILaunchTarget launchTarget = launchBarManager.getActiveLaunchTarget();
			if (launchTarget != null)
			{
				File file = new ESPToolChainManager()
						.findCompiler(launchTarget.getAttribute(LaunchBarTargetConstants.TARGET, StringUtil.EMPTY));
				if (file != null)
				{
					return file.getAbsolutePath();
				}

			}
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}

		return null;
	}

	public static String getXtensaToolchainExecutablePathByTarget(String projectEspTarget)
	{
		File file = new ESPToolChainManager().findDebugger(projectEspTarget);
		if (file != null)
		{
			return file.getAbsolutePath();
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
	 * Sets project build directory
	 * 
	 * @param project
	 * @param pathToBuildDir
	 * @throws CoreException
	 */
	public static void setBuildDir(IProject project, String pathToBuildDir) throws CoreException
	{
		project.setPersistentProperty(new QualifiedName(IDFCorePlugin.PLUGIN_ID, IDFConstants.BUILD_DIR_PROPERTY),
				pathToBuildDir);
	}

	/**
	 * Updates the build folder for the specified project in the given launch configuration.
	 * 
	 * This method retrieves the project associated with the given launch configuration, checks if a build folder path
	 * is specified in the configuration, and sets the build directory for the project. If no build folder path is
	 * specified, a default value is used. If the specified path is relative, it is converted to an absolute path based
	 * on the project's location.
	 * 
	 * @param configuration The launch configuration whose associated project’s build folder is to be updated. This
	 *                      parameter cannot be {@code null}.
	 * @throws CoreException If there is an issue with accessing the project or updating the build folder. This
	 *                       exception is logged, but not rethrown.
	 */
	public static void updateProjectBuildFolder(ILaunchConfigurationWorkingCopy configuration)
	{
		try
		{
			IProject project = CoreBuildLaunchConfigDelegate.getProject(configuration);
			if (project == null)
			{
				return;
			}
			String buildFolder = configuration.getAttribute(IDFLaunchConstants.BUILD_FOLDER_PATH,
					IDFUtil.getBuildDir(project));
			buildFolder = buildFolder.isBlank() ? IDFConstants.BUILD_FOLDER : buildFolder;

			IPath path = new Path(buildFolder);
			if (!path.isAbsolute())
			{
				IPath projectLocation = project.getLocation();
				path = projectLocation.append(path);
			}
			IDFUtil.setBuildDir(project, path.toOSString());
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
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

	public static IProject getProjectFromActiveLaunchConfig() throws CoreException
	{
		final ILaunchBarManager launchBarManager = IDFCorePlugin.getService(ILaunchBarManager.class);
		ILaunchConfiguration launchConfiguration = launchBarManager.getActiveLaunchConfiguration();
		IResource[] mappedResources = launchConfiguration.getMappedResources();
		if (mappedResources != null && mappedResources[0].getProject() != null)
		{
			return mappedResources[0].getProject();
		}

		return null;
	}

	public static String getGitExecutablePathFromSystem()
	{
		IPath gitPath = new SystemExecutableFinder().find("git"); //$NON-NLS-1$
		Logger.log("GIT path:" + gitPath); //$NON-NLS-1$
		if (gitPath != null)
		{
			return gitPath.toOSString();
		}

		if (Platform.OS_WIN32.equals(Platform.getOS()))
		{
			GitWinRegistryReader gitWinRegistryReader = new GitWinRegistryReader();
			String gitInstallPath = gitWinRegistryReader.getGitInstallPath();
			if (!StringUtil.isEmpty(gitInstallPath))
			{
				return gitInstallPath.concat(String.valueOf(Path.SEPARATOR)).concat("bin") //$NON-NLS-1$
						.concat(String.valueOf(Path.SEPARATOR)).concat("git.exe"); //$NON-NLS-1$
			}
		}
		else
		{
			// MAC & LINUX have whereis git to see where the command is located
			List<String> arguments = new ArrayList<String>();
			ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
			try
			{
				arguments.add("whereis"); //$NON-NLS-1$
				arguments.add("git"); //$NON-NLS-1$

				Map<String, String> environment = new HashMap<>(getSystemEnv());

				IStatus status = processRunner.runInBackground(arguments, org.eclipse.core.runtime.Path.ROOT,
						environment);
				if (status == null)
				{
					Logger.log(IDFCorePlugin.getPlugin(), IDFCorePlugin.errorStatus("Status can't be null", null)); //$NON-NLS-1$
					return StringUtil.EMPTY;
				}
				String gitLocation = status.getMessage().split(" ").length > 1 ? status.getMessage().split(" ")[1] : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				gitLocation = gitLocation.strip();
				return gitLocation;
			}
			catch (Exception e1)
			{
				Logger.log(e1);
			}
		}
		return StringUtil.EMPTY;
	}

	public static boolean isReparseTag(File file)
	{
		if (!Platform.getOS().equals(Platform.OS_WIN32))
			return false;
		int reparseTag = WinNativeFileTagOperations.getReparseTag(file.getAbsolutePath());
		if (reparseTag != -1)
		{
			return WinNativeFileTagOperations.isReparseTagMicrosoft(reparseTag);
		}
		return false;
	}

	public static String resolveEnvVariable(String path)
	{
		Pattern winEnvPattern = Pattern.compile("%(\\w+)%"); //$NON-NLS-1$
		Pattern unixEnvPattern = Pattern.compile("\\$(\\w+)"); //$NON-NLS-1$
		Matcher matcher;
		if (Platform.getOS().equals(Platform.OS_WIN32))
		{
			matcher = winEnvPattern.matcher(path);
		}
		else
		{
			matcher = unixEnvPattern.matcher(path);
		}

		StringBuffer resolvedPath = new StringBuffer();
		while (matcher.find())
		{
			String envVarName = matcher.group(1);
			String envVarValue = System.getenv(envVarName);

			if (envVarValue != null)
			{
				matcher.appendReplacement(resolvedPath, envVarValue.replace("\\", "\\\\")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else
			{
				// If the environment variable is not found, keep the original
				matcher.appendReplacement(resolvedPath, matcher.group(0));
			}
		}
		matcher.appendTail(resolvedPath);

		return resolvedPath.toString();

	}

	public static Map<String, String> getSystemEnv()
	{
		Map<String, String> env = new HashMap<>(System.getenv());

		String idfToolsPath = Platform.getPreferencesService().getString(IDFCorePlugin.PLUGIN_ID,
				IDFCorePreferenceConstants.IDF_TOOLS_PATH, IDFCorePreferenceConstants.IDF_TOOLS_PATH_DEFAULT, null);
		env.put(IDFCorePreferenceConstants.IDF_TOOLS_PATH, idfToolsPath);

		// Merge Homebrew bin paths into PATH
		String existingPath = env.getOrDefault("PATH", ""); //$NON-NLS-1$ //$NON-NLS-2$
		StringBuilder newPath = new StringBuilder();

		String[] brewPaths = { "/usr/local/bin", "/opt/homebrew/bin" }; //$NON-NLS-1$ //$NON-NLS-2$

		for (String brewPath : brewPaths)
		{
			if (Files.exists(Paths.get(brewPath)) && !existingPath.contains(brewPath))
			{
				newPath.append(brewPath).append(":"); //$NON-NLS-1$
			}
		}

		// Append the original PATH at the end
		newPath.append(existingPath);
		env.put("PATH", newPath.toString()); //$NON-NLS-1$

		return env;
	}

	public static String getIDFToolsPathFromPreferences()
	{
		String idfToolsPath = Platform.getPreferencesService().getString(IDFCorePlugin.PLUGIN_ID,
				IDFCorePreferenceConstants.IDF_TOOLS_PATH, IDFCorePreferenceConstants.IDF_TOOLS_PATH_DEFAULT, null);
		return idfToolsPath;
	}

	public static void closeWelcomePage(IWorkbenchWindow activeww)
	{
		Display.getDefault().syncExec(() -> {
			if (activeww != null)
			{
				IWorkbenchPage page = activeww.getActivePage();
				if (page != null)
				{
					ViewPart viewPart = (ViewPart) page.findView("org.eclipse.ui.internal.introview"); //$NON-NLS-1$
					if (viewPart != null)
					{
						page.hideView(viewPart);
					}

				}
			}
		});
	}

	/**
	 * Checks if esp_detect_config.py exists in the expected OpenOCD tools directory.
	 * 
	 * @return true if esp_detect_config.py exists, false otherwise.
	 */
	public static boolean espDetectConfigScriptExists()
	{
		String openocdBinDir = getOpenOCDLocation();
		if (StringUtil.isEmpty(openocdBinDir))
		{
			return false;
		}
		File binDir = new File(openocdBinDir);
		File openocdRoot = binDir.getParentFile();
		File toolsDir = Paths.get(openocdRoot.getPath(), "share", "openocd", "espressif", "tools").toFile(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		File scriptFile = new File(toolsDir, "esp_detect_config.py"); //$NON-NLS-1$
		return scriptFile.exists();
	}

	/**
	 * Runs the esp_detect_config.py script using the OPENOCD_SCRIPTS environment variable to locate the script and
	 * config files. Returns the JSON output as a string, or null on error.
	 */
	public static String runEspDetectConfigScript()
	{
		String openocdBinDir = getOpenOCDLocation();
		if (StringUtil.isEmpty(openocdBinDir))
		{
			Logger.log("OpenOCD location could not be determined."); //$NON-NLS-1$
			return null;
		}
		// Derive the scripts and tools directories from the bin directory
		File binDir = new File(openocdBinDir);
		File openocdRoot = binDir.getParentFile();
		File scriptsDir = Paths.get(openocdRoot.getPath(), "share", "openocd", "scripts").toFile(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		File toolsDir = Paths.get(openocdRoot.getPath(), "share", "openocd", "espressif", "tools").toFile(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		File configFile = new File(scriptsDir, "esp-config.json"); //$NON-NLS-1$
		if (!configFile.exists())
		{
			Logger.log("esp-config.json not found at expected location: " + configFile.getAbsolutePath()); //$NON-NLS-1$
			return null;
		}
		if (!espDetectConfigScriptExists())
		{
			Logger.log("esp_detect_config.py not found at expected location: " //$NON-NLS-1$
					+ new File(toolsDir, "esp_detect_config.py").getAbsolutePath()); //$NON-NLS-1$
			return null;
		}
		String scriptPath = new File(toolsDir, "esp_detect_config.py").getAbsolutePath(); //$NON-NLS-1$
		String configPath = configFile.getAbsolutePath();
		String openocdExecutable = Platform.getOS().equals(Platform.OS_WIN32) ? "openocd.exe" : "openocd"; //$NON-NLS-1$ //$NON-NLS-2$
		File openocdBin = new File(openocdBinDir, openocdExecutable);
		if (!openocdBin.exists())
		{
			Logger.log("OpenOCD binary not found at expected location."); //$NON-NLS-1$
			return null;
		}

		String idfPythonEnvPath = IDFUtil.getIDFPythonEnvPath();
		if (StringUtil.isEmpty(idfPythonEnvPath))
		{
			Logger.log("IDF_PYTHON_ENV_PATH could not be found."); //$NON-NLS-1$
			return null;
		}

		List<String> command = new ArrayList<>();
		command.add(idfPythonEnvPath);
		command.add(scriptPath);
		command.add("--esp-config"); //$NON-NLS-1$
		command.add(configPath);
		command.add("--oocd");//$NON-NLS-1$
		command.add(openocdBin.getAbsolutePath());

		Map<String, String> env = new IDFEnvironmentVariables().getSystemEnvMap();
		try
		{
			IStatus status = new ProcessBuilderFactory().runInBackground(command, null, env);
			if (status == null)
			{
				Logger.log("esp_detect_config.py did not return a result."); //$NON-NLS-1$
				return null;
			}
			return status.getMessage();
		}
		catch (Exception e)
		{
			Logger.log(e);
			return null;
		}
	}
}
