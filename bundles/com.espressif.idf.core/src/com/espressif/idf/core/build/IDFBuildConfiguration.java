/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      QNX - Initial API and implementation
 *      kondal.kolipaka@espressif.com - ESP-IDF specific build configuration
 *******************************************************************************/
package com.espressif.idf.core.build;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.cdt.build.gcc.core.ClangToolChain;
import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.cmake.core.internal.CMakeUtils;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.model.BinaryRunner;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFCorePreferenceConstants;
import com.espressif.idf.core.internal.CMakeConsoleWrapper;
import com.espressif.idf.core.internal.CMakeErrorParser;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.DfuCommandsUtil;
import com.espressif.idf.core.util.HintsUtil;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.ParitionSizeHandler;
import com.espressif.idf.core.util.StringUtil;

@SuppressWarnings(value = { "restriction" })
public class IDFBuildConfiguration extends CBuildConfiguration
{

	private static final ActiveLaunchConfigurationProvider LAUNCH_CONFIG_PROVIDER = new ActiveLaunchConfigurationProvider();
	private static final String NINJA = "Ninja"; //$NON-NLS-1$
	protected static final String COMPILE_COMMANDS_JSON = "compile_commands.json"; //$NON-NLS-1$
	protected static final String COMPONENTS = "components"; //$NON-NLS-1$
	public static final String CMAKE_GENERATOR = "cmake.generator"; //$NON-NLS-1$
	public static final String CMAKE_ARGUMENTS = "cmake.arguments"; //$NON-NLS-1$
	public static final String CMAKE_ENV = "cmake.environment"; //$NON-NLS-1$
	public static final String BUILD_COMMAND = "cmake.command.build"; //$NON-NLS-1$
	public static final String CLEAN_COMMAND = "cmake.command.clean"; //$NON-NLS-1$
	private ILaunchTarget launchtarget;
	/**
	 * whether one of the CMakeLists.txt files in the project has been modified and saved by the user since the last
	 * build.<br>
	 * Cmake-generated build scripts re-run cmake if one of the CMakeLists.txt files was modified, but that output goes
	 * through ErrorParserManager and is impossible to parse because cmake outputs to both stderr and stdout and
	 * ErrorParserManager intermixes these streams making it impossible to parse for errors.<br>
	 * To work around that, we run cmake in advance with its dedicated working error parser.
	 */
	private boolean cmakeListsModified;
	private ICMakeToolChainFile toolChainFile;
	private String customBuildDir;
	private IProgressMonitor monitor;
	public boolean isProgressSet;
	private ILaunchConfiguration configuration;

	public IDFBuildConfiguration(IBuildConfiguration config, String name) throws CoreException
	{
		super(config, name);
		this.configuration = LAUNCH_CONFIG_PROVIDER.getActiveLaunchConfiguration();
	}

	public IDFBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain)
	{
		this(config, name, toolChain, null, "run"); //$NON-NLS-1$
	}

	public IDFBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain,
			ICMakeToolChainFile toolChainFile, String launchMode)
	{
		super(config, name, toolChain, launchMode);
		this.toolChainFile = toolChainFile;
		try
		{
			this.configuration = LAUNCH_CONFIG_PROVIDER.getActiveLaunchConfiguration();
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
	}

	@Override
	public Path getBuildDirectory() throws CoreException
	{
		return Paths.get(getBuildDirectoryURI());

	}

	@Override
	public IContainer getBuildContainer() throws CoreException
	{
		IProject project = getProject();
		IFolder buildRootFolder = project.getFolder(IDFConstants.BUILD_FOLDER);

		IProgressMonitor monitor = new NullProgressMonitor();
		if (!buildRootFolder.exists())
		{
			buildRootFolder.create(IResource.FORCE | IResource.DERIVED, true, monitor);
		}

		return buildRootFolder;
	}

	public IPath getBuildContainerPath() throws CoreException
	{
		if (hasCustomBuild())
		{
			org.eclipse.core.runtime.Path path = new org.eclipse.core.runtime.Path(customBuildDir);
			if (!path.toFile().exists())
			{
				path.toFile().mkdirs();
			}
			return path;
		}

		return getBuildContainer().getLocation();
	}

	private boolean hasCustomBuild()
	{
		String userArgs = getProperty(CMAKE_ARGUMENTS);
		// Custom build directory
		int buildDirIndex = userArgs.indexOf("-B"); //$NON-NLS-1$
		customBuildDir = buildDirIndex == -1 ? StringUtil.EMPTY
				: userArgs.replaceFirst("-B", StringUtil.EMPTY).stripLeading(); //$NON-NLS-1$
		try
		{
			getProject().setPersistentProperty(
					new QualifiedName(IDFCorePlugin.PLUGIN_ID, IDFConstants.BUILD_DIR_PROPERTY), customBuildDir);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}

		return !customBuildDir.isBlank();
	}

	@Override
	public URI getBuildDirectoryURI() throws CoreException
	{
		IPath buildContainerPath = getBuildContainerPath();
		return buildContainerPath.toFile().toURI();
	}

	@Override
	public IBinary[] getBuildOutput() throws CoreException
	{
		ICProject cproject = CoreModel.getDefault().create(getProject());
		IBinaryContainer binaries = cproject.getBinaryContainer();
		IPath outputPath = getBuildContainerPath();
		final IBinary[] outputs = getBuildOutput(binaries, outputPath);
		if (outputs.length > 0)
		{
			return outputs;
		}

		// Give the binary runner a kick and try again.
		BinaryRunner runner = CModelManager.getDefault().getBinaryRunner(cproject);
		runner.start();
		runner.waitIfRunning();
		return getBuildOutput(binaries, outputPath);
	}

	@Override
	public String getProperty(String name)
	{
		try
		{
			if (configuration != null
					&& configuration.getType().getIdentifier().equals(IDFLaunchConstants.DEBUG_LAUNCH_CONFIG_TYPE))
			{
				configuration = getBoundConfiguration(configuration);
			}
			String property = configuration == null ? StringUtil.EMPTY
					: configuration.getAttribute(name, StringUtil.EMPTY);
			property = property.isBlank() ? getSettings().get(name, StringUtil.EMPTY) : property;
			return property;
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}

		return super.getProperty(name);
	}

	/*
	 * In case when the active configuration is debugging, we are using bound launch configuration to build the project
	 */
	private ILaunchConfiguration getBoundConfiguration(ILaunchConfiguration configuration) throws CoreException
	{
		String bindedLaunchConfigName = configuration.getAttribute(IDFLaunchConstants.ATTR_LAUNCH_CONFIGURATION_NAME,
				StringUtil.EMPTY);
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfiguration[] launchConfigurations = launchManager.getLaunchConfigurations(DebugPlugin.getDefault()
				.getLaunchManager().getLaunchConfigurationType(IDFLaunchConstants.RUN_LAUNCH_CONFIG_TYPE));
		ILaunchConfiguration defaultConfiguration = launchConfigurations[0];
		return Stream.of(launchConfigurations).filter(config -> config.getName().contentEquals(bindedLaunchConfigName))
				.findFirst().orElse(defaultConfiguration);

	}

	private IBinary[] getBuildOutput(final IBinaryContainer binaries, final IPath outputPath) throws CoreException
	{
		return Arrays.stream(binaries.getBinaries()).filter(b -> b.isExecutable() && outputPath.isPrefixOf(b.getPath()))
				.toArray(IBinary[]::new);
	}

	public ICMakeToolChainFile getToolChainFile() throws CoreException
	{
		ICMakeToolChainManager manager = IDFCorePlugin.getService(ICMakeToolChainManager.class);
		this.toolChainFile = manager.getToolChainFileFor(getToolChain());
		return toolChainFile;
	}

	private boolean isLocal() throws CoreException
	{
		IToolChain toolchain = getToolChain();
		return (Platform.getOS().equals(toolchain.getProperty(IToolChain.ATTR_OS))
				|| "linux-container".equals(toolchain.getProperty(IToolChain.ATTR_OS))) //$NON-NLS-1$
				&& (Platform.getOSArch().equals(toolchain.getProperty(IToolChain.ATTR_ARCH)));
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException
	{
		this.monitor = monitor;
		isProgressSet = false;

		IProject project = getProject();
		toolChainFile = getToolChainFile();

		Instant start = Instant.now();
		if (!checkLaunchTarget(console) || !checkSpacesSupport(project, console) || !checkToolChainFile(console))
		{
			return new IProject[0];
		}

		String generator = getProperty(CMAKE_GENERATOR);
		generator = generator.isBlank() ? NINJA : generator;
		project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

		ConsoleOutputStream infoStream = console.getInfoStream();
		// create build directory
		Path buildDir = getBuildDirectory();
		if (!buildDir.toFile().exists())
		{
			buildDir.toFile().mkdir();
		}

		try
		{
			infoStream.write(String.format(Messages.CMakeBuildConfiguration_BuildingIn, buildDir.toString()));
			boolean runCMake = cmakeListsModified;
			if (!runCMake)
			{
				runCMake = generator.contentEquals(NINJA) ? !Files.exists(buildDir.resolve("build.ninja")) //$NON-NLS-1$
						: !Files.exists(buildDir.resolve("CMakeFiles")); //$NON-NLS-1$
			}

			if (runCMake)
			{
				runCmakeCommand(console, monitor, project, generator, infoStream, buildDir);
			}
			runCmakeBuildCommand(console, monitor, project, start, generator, infoStream, buildDir);

			return new IProject[] { project };
		}
		catch (Exception e)
		{
			throw new CoreException(IDFCorePlugin
					.errorStatus(String.format(Messages.CMakeBuildConfiguration_Building, project.getName()), e));
		}
	}

	private void runCmakeBuildCommand(IConsole console, IProgressMonitor monitor, IProject project, Instant start,
			String generator, ConsoleOutputStream infoStream, Path buildDir)
			throws CoreException, IOException, CmakeBuildException
	{
		try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
				getToolChain().getErrorParserIds()))
		{
			epm.setOutputStream(console.getOutputStream());

			List<String> command = new ArrayList<>();

			String envStr = getProperty(CMAKE_ENV);
			List<IEnvironmentVariable> envVars = new ArrayList<>();
			if (envStr != null && !envStr.isBlank())
			{
				List<String> envList = CMakeUtils.stripEnvVars(envStr);
				for (String s : envList)
				{
					int index = s.indexOf("="); //$NON-NLS-1$
					if (index == -1)
					{
						envVars.add(new EnvironmentVariable(s));
					}
					else
					{
						envVars.add(new EnvironmentVariable(s.substring(0, index), s.substring(index + 1)));
					}
				}
			}

			String buildCommand = getProperty(BUILD_COMMAND);
			if (buildCommand.isBlank())
			{
				command.add("cmake"); //$NON-NLS-1$
				command.add("--build"); //$NON-NLS-1$
				command.add("."); //$NON-NLS-1$
				if (NINJA.equals(generator)) // $NON-NLS-1$
				{
					command.add("--"); //$NON-NLS-1$
					command.add("-v"); //$NON-NLS-1$
				}
			}
			else
			{
				command.addAll(Arrays.asList(buildCommand.split(" "))); //$NON-NLS-1$
			}

			IPath workingDir = new org.eclipse.core.runtime.Path(getBuildDirectory().toString());
			Process p = null;
			if (DfuCommandsUtil.isDfu() && DfuCommandsUtil.isDfuSupported(launchtarget))
			{
				command = DfuCommandsUtil.getDfuBuildCommand();
				workingDir = project.getLocation();
			}

			infoStream.write(String.join(" ", command) + '\n'); //$NON-NLS-1$
			p = startBuildProcess(command, envVars.toArray(new IEnvironmentVariable[0]), workingDir, console, monitor);

			if (p == null)
			{
				console.getErrorStream().write(String.format(Messages.CMakeBuildConfiguration_Failure, "")); //$NON-NLS-1$
				throw new CmakeBuildException();
			}
			boolean buildHintsStatus = Platform.getPreferencesService().getBoolean(IDFCorePlugin.PLUGIN_ID,
					IDFCorePreferenceConstants.AUTOMATE_BUILD_HINTS_STATUS,
					IDFCorePreferenceConstants.AUTOMATE_BUILD_HINTS_DEFAULT_STATUS, null);
			IConsoleParser[] consoleParsers = buildHintsStatus
					? new IConsoleParser[] { epm, new StatusParser(),
							new EspIdfErrorParser(HintsUtil.getReHintsList(new File(HintsUtil.getHintsYmlPath()))) }
					: new IConsoleParser[] { epm, new StatusParser() };
			watchProcess(consoleParsers, monitor);

			infoStream.write(String.format(Messages.CMakeBuildConfiguration_BuildingComplete, epm.getErrorCount(),
					epm.getWarningCount(), buildDir.toString()));

			Instant finish = Instant.now();
			long timeElapsed = Duration.between(start, finish).toMillis();
			if (!monitor.isCanceled() && epm.getErrorCount() == 0)
			{
				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				ParitionSizeHandler paritionSizeHandler = new ParitionSizeHandler(project, infoStream, console);
				paritionSizeHandler.startCheckingSize();
			}

			infoStream.write(MessageFormat.format("Total time taken to build the project: {0} ms", timeElapsed)); //$NON-NLS-1$
		}
	}

	private void runCmakeCommand(IConsole console, IProgressMonitor monitor, IProject project, String generator,
			ConsoleOutputStream infoStream, Path buildDir) throws CoreException, IOException, CmakeBuildException
	{
		deleteCMakeErrorMarkers(project);

		infoStream.write(String.format(Messages.CMakeBuildConfiguration_Configuring, buildDir));
		// clean output to make sure there is no content
		// incompatible with current settings (cmake config would fail)
		cleanBuildDirectory(buildDir);

		List<String> command = new ArrayList<>();

		command.add("cmake"); //$NON-NLS-1$
		command.add("-G"); //$NON-NLS-1$
		command.add(generator);

		if (toolChainFile != null)
		{
			command.add("-DCMAKE_TOOLCHAIN_FILE=" + toolChainFile.getPath().toString()); //$NON-NLS-1$
		}

		command.add("-DCMAKE_EXPORT_COMPILE_COMMANDS=ON"); //$NON-NLS-1$
		if (isCCacheEnabled())
		{
			command.add("-DCCACHE_ENABLE=1"); //$NON-NLS-1$
		}

		if (launchtarget != null)
		{
			String idfTargetName = launchtarget.getAttribute(IDFLaunchConstants.ATTR_IDF_TARGET, StringUtil.EMPTY);
			if (!idfTargetName.isEmpty())
			{
				command.add("-DIDF_TARGET=" + idfTargetName); //$NON-NLS-1$
			}
		}

		if (Objects.equals(getToolChain().getTypeId(), ClangToolChain.TYPE_ID))
		{
			command.add("-DIDF_TOOLCHAIN=clang"); //$NON-NLS-1$
		}

		String userArgs = getProperty(CMAKE_ARGUMENTS);
		if (userArgs != null && !userArgs.isBlank())
		{
			command.addAll(Arrays.asList(userArgs.trim().split("\\s+"))); //$NON-NLS-1$
		}
		IContainer srcFolder = project;
		command.add(new File(srcFolder.getLocationURI()).getAbsolutePath());

		infoStream.write(String.join(" ", command) + '\n'); //$NON-NLS-1$

		org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(getBuildDirectory().toString());
		// hook in cmake error parsing
		IConsole errConsole = new CMakeConsoleWrapper(srcFolder, console);

		// Set PYTHONUNBUFFERED to 1/TRUE to dump the messages back immediately without
		// buffering
		IEnvironmentVariable bufferEnvVar = new EnvironmentVariable("PYTHONUNBUFFERED", "1"); //$NON-NLS-1$ //$NON-NLS-2$

		Process p = startBuildProcess(command, new IEnvironmentVariable[] { bufferEnvVar }, workingDir, errConsole,
				monitor);
		if (p == null)
		{
			console.getErrorStream().write(String.format(Messages.CMakeBuildConfiguration_Failure, "")); //$NON-NLS-1$
			throw new CmakeBuildException();
		}

		try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
				getToolChain().getErrorParserIds()))
		{
			epm.setOutputStream(console.getOutputStream());
			watchProcess(new IConsoleParser[] { epm }, monitor);
		}

		cmakeListsModified = false;
	}

	private boolean checkToolChainFile(IConsole console)
	{
		try
		{
			if (toolChainFile == null && !isLocal())
			{
				ICMakeToolChainManager manager = IDFCorePlugin.getService(ICMakeToolChainManager.class);
				toolChainFile = manager.getToolChainFileFor(getToolChain());
				if (toolChainFile == null)
				{
					console.getErrorStream()
							.write(Messages.IDFBuildConfiguration_CMakeBuildConfiguration_NoToolchainFile);
					return false;
				}
			}
		}
		catch (
				IOException
				| CoreException e)
		{
			Logger.log(e);
		}
		return true;
	}

	private boolean checkSpacesSupport(IProject project, IConsole console)
	{
		if (!IDFUtil.checkIfIdfSupportsSpaces() && project.getLocation().toOSString().contains(" ")) //$NON-NLS-1$
		{
			try
			{
				console.getErrorStream()
						.write("Project path can’t include space " + project.getLocation().toOSString()); //$NON-NLS-1$
			}
			catch (
					IOException
					| CoreException e)
			{
				Logger.log(e);
			}
			return false;
		}
		return true;
	}

	private boolean checkLaunchTarget(IConsole console)
	{
		ILaunchBarManager launchBarManager = IDFCorePlugin.getService(ILaunchBarManager.class);
		try
		{
			launchtarget = launchBarManager.getActiveLaunchTarget();
			ILaunchMode activeLaunchMode = launchBarManager.getActiveLaunchMode();
			// Allow build only through esp launch target in run mode
			if (launchtarget != null && !launchtarget.getTypeId().equals(IDFLaunchConstants.ESP_LAUNCH_TARGET_TYPE)
					&& activeLaunchMode != null && activeLaunchMode.getIdentifier().equals("run")) //$NON-NLS-1$
			{
				console.getErrorStream()
						.write("No esp launch target found. Please create/select the correct 'Launch Target'"); //$NON-NLS-1$
				return false;
			}
		}
		catch (
				CoreException
				| IOException e)
		{
			Logger.log(e);
		}

		return true;
	}

	private boolean isCCacheEnabled()
	{
		IEclipsePreferences node = IDFCorePreferenceConstants
				.getPreferenceNode(IDFCorePreferenceConstants.CMAKE_CCACHE_STATUS, null);
		return node.getBoolean(IDFCorePreferenceConstants.CMAKE_CCACHE_STATUS, true);
	}

	@Override
	public IToolChain getToolChain() throws CoreException
	{
		String typeId = getProperty(TOOLCHAIN_TYPE);
		String id = getProperty(TOOLCHAIN_ID);
		IToolChainManager toolChainManager = CCorePlugin.<IToolChainManager>getService(IToolChainManager.class);
		ILaunchBarManager launchBarManager = CCorePlugin.getService(ILaunchBarManager.class);
		Collection<IToolChain> matchedToolChains = toolChainManager
				.getToolChainsMatching(Map.of(IToolChain.ATTR_OS, launchBarManager.getActiveLaunchTarget()
						.getAttribute(IDFLaunchConstants.ATTR_IDF_TARGET, StringUtil.EMPTY), TOOLCHAIN_TYPE, typeId));
		return matchedToolChains.stream().findAny().orElse(toolChainManager.getToolChain(typeId, id));
	}


	@Override
	public void clean(IConsole console, IProgressMonitor monitor) throws CoreException
	{
		IProject project = getProject();
		try
		{
			String generator = getProperty(CMAKE_GENERATOR);

			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream outStream = console.getOutputStream();

			Path buildDir = getBuildDirectory();

			if (!Files.exists(buildDir.resolve("CMakeFiles"))) //$NON-NLS-1$
			{
				outStream.write(Messages.CMakeBuildConfiguration_NotFound);
				return;
			}

			List<String> command = new ArrayList<>();
			String cleanCommand = getProperty(CLEAN_COMMAND);
			if (cleanCommand == null || cleanCommand.isBlank())
			{
				if (generator == null || generator.isBlank() || generator.equals(NINJA)) // $NON-NLS-1$
				{
					command.add("ninja"); //$NON-NLS-1$
					command.add("clean"); //$NON-NLS-1$
				}
				else
				{
					command.add("make"); //$NON-NLS-1$
					command.add("clean"); //$NON-NLS-1$
				}
			}
			else
			{
				command.addAll(Arrays.asList(cleanCommand.split(" "))); //$NON-NLS-1$
			}

			IEnvironmentVariable[] env = new IEnvironmentVariable[0];

			outStream.write(String.join(" ", command) + '\n'); //$NON-NLS-1$

			org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(
					getBuildDirectory().toString());
			Process p = startBuildProcess(command, env, workingDir, console, monitor);
			if (p == null)
			{
				console.getErrorStream().write(String.format(Messages.CMakeBuildConfiguration_Failure, "")); //$NON-NLS-1$
				return;
			}

			watchProcess(console, monitor);

			outStream.write(Messages.CMakeBuildConfiguration_BuildComplete);

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		}
		catch (IOException e)
		{
			throw new CoreException(IDFCorePlugin
					.errorStatus(String.format(Messages.CMakeBuildConfiguration_Cleaning, project.getName()), e));
		}
	}


	/**
	 * Recursively removes any files and directories found below the specified Path.
	 */
	private static void cleanDirectory(Path dir) throws IOException
	{
		SimpleFileVisitor<Path> deltor = new SimpleFileVisitor<>()
		{
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
			{
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
			{
				super.postVisitDirectory(dir, exc);
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		};
		Path[] files = Files.list(dir).toArray(Path[]::new);
		for (Path file : files)
		{
			Files.walkFileTree(file, deltor);
		}
	}

	private void cleanBuildDirectory(Path buildDir) throws IOException
	{
		if (!Files.exists(buildDir))
			return;
		if (Files.isDirectory(buildDir))
			cleanDirectory(buildDir);
		// TODO: not a directory should we do something?
	}

	/**
	 * Overwritten to detect whether one of the CMakeLists.txt files in the project was modified since the last build.
	 */
	@Override
	public void elementChanged(ElementChangedEvent event)
	{
		super.elementChanged(event);
		// Only respond to post change events
		if (event.getType() != ElementChangedEvent.POST_CHANGE)
			return;
		if (!cmakeListsModified)
		{
			processElementDelta(event.getDelta());
		}
	}

	/**
	 * Processes the delta in order to detect whether one of the CMakeLists.txt files in the project has been modified
	 * and saved by the user since the last build.
	 *
	 * @return <code>true</code> to continue with delta processing, otherwise <code>false</code>
	 */
	private boolean processElementDelta(ICElementDelta delta)
	{
		if (delta == null)
		{
			return true;
		}

		if (delta.getKind() == ICElementDelta.CHANGED && ((delta.getFlags() & ICElementDelta.F_CONTENT) != 0))
		{
			// check for modified CMakeLists.txt file
			IResourceDelta[] resourceDeltas = delta.getResourceDeltas();
			if (resourceDeltas != null)
			{
				cmakeListsModified = Stream.of(resourceDeltas).map(t -> t.getResource())
						.filter(t -> t.getType() == IResource.FILE
								&& !t.getFullPath().toOSString().contains(IDFConstants.BUILD_FOLDER))
						.map(t -> t.getName()).anyMatch(t -> t.equals("CMakeLists.txt") || t.endsWith(".cmake")); //$NON-NLS-1$ //$NON-NLS-2$

			}
			if (cmakeListsModified)
			{
				return false;
			}
		}

		// recurse...
		for (ICElementDelta child : delta.getAffectedChildren())
		{
			if (!processElementDelta(child))
			{
				return false; // stop processing
			}
		}
		return true;
	}

	/**
	 * Overwritten since we do not parse console output to get scanner information.
	 */
	// interface IConsoleParser2
	@Override
	public boolean processLine(String line)
	{
		return true;
	}

	/**
	 * Overwritten since we do not parse console output to get scanner information.
	 */
	// interface IConsoleParser2
	@Override
	public boolean processLine(String line, List<Job> jobsArray)
	{
		return true;
	}

	/**
	 * Overwritten since we do not parse console output to get scanner information.
	 */
	// interface IConsoleParser2
	@Override
	public void shutdown()
	{
	}

	/**
	 * Deletes all CMake error markers on the specified project.
	 *
	 * @param project the project where to remove the error markers.
	 * @throws CoreException
	 */
	private static void deleteCMakeErrorMarkers(IProject project) throws CoreException
	{
		project.deleteMarkers(CMakeErrorParser.CMAKE_PROBLEM_MARKER_ID, false, IResource.DEPTH_INFINITE);
	}

	public void setLaunchTarget(ILaunchTarget target)
	{
		this.launchtarget = target;
	}

	/**
	 * Process the CMake build output and figure out the percentage of work done.
	 */
	class StatusParser implements IConsoleParser
	{
		@Override
		public boolean processLine(String line)
		{
			try
			{
				if (line.indexOf("[") != -1 && line.indexOf("]") != -1) //$NON-NLS-1$ //$NON-NLS-2$
				{
					String str = line.substring(line.indexOf("[") + 1, line.indexOf("]")); //$NON-NLS-1$ //$NON-NLS-2$
					if (str.length() > 0)
					{
						String[] split = str.split("/"); //$NON-NLS-1$
						if (!isProgressSet && monitor != null && split.length > 1)
						{
							isProgressSet = true;
							int y = Integer.parseInt(split[1].strip());
							monitor.beginTask("Building", y); //$NON-NLS-1$
						}

						monitor.worked(1);
					}
				}
			}
			catch (NumberFormatException e)
			{
				Logger.log(e, true); // Silently report
			}
			return true;
		}

		@Override
		public void shutdown()
		{
			// nothing
		}

	}

}
