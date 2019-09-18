/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.build;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.cmake.core.internal.Activator;
import org.eclipse.cdt.cmake.core.internal.CMakeBuildConfiguration;
import org.eclipse.cdt.cmake.core.internal.CMakeUtils;
import org.eclipse.cdt.cmake.core.internal.CompileCommand;
import org.eclipse.cdt.cmake.core.internal.Messages;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.utils.Platform;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.launchbar.core.target.ILaunchTarget;

import com.espressif.idf.core.IDFConstants;
import com.google.gson.Gson;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
@SuppressWarnings("restriction")
public class IDFBuildConfiguration extends CMakeBuildConfiguration
{

	private ILaunchTarget launchtarget;

	public IDFBuildConfiguration(IBuildConfiguration config, String name) throws CoreException
	{
		super(config, name);
	}

	public IDFBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain)
	{
		super(config, name, toolChain, null, "run"); //$NON-NLS-1$
	}

	public IDFBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain,
			ICMakeToolChainFile toolChainFile, String launchMode)
	{
		super(config, name, toolChain, toolChainFile, launchMode);

	}

	@Override
	public Path getBuildDirectory() throws CoreException
	{
		IProject project = getProject();
		String absolutePath = project.getLocation().toFile().getAbsolutePath();
		return Paths.get(absolutePath, IDFConstants.BUILD_FOLDER);

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

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException
	{
		IProject project = getProject();
		ICMakeToolChainFile toolChainFile = getToolChainFile();

		try
		{
			String generator = getProperty(CMAKE_GENERATOR);
			if (generator == null)
			{
				generator = "Ninja"; //$NON-NLS-1$
			}

			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream outStream = console.getOutputStream();

			Path buildDir = getBuildDirectory();
			if (!buildDir.toFile().exists())
			{
				buildDir.toFile().mkdir();
			}

			outStream.write(String.format(Messages.CMakeBuildConfiguration_BuildingIn, buildDir.toString()));

			// Make sure we have a toolchain file if cross
			if (toolChainFile == null && !isLocal())
			{
				ICMakeToolChainManager manager = Activator.getService(ICMakeToolChainManager.class);
				toolChainFile = manager.getToolChainFileFor(getToolChain());

				if (toolChainFile == null)
				{
					// error
					console.getErrorStream().write(Messages.CMakeBuildConfiguration_NoToolchainFile);
					return null;
				}
			}

			boolean runCMake;
			switch (generator)
			{
			case "Ninja": //$NON-NLS-1$
				runCMake = !Files.exists(buildDir.resolve("build.ninja")); //$NON-NLS-1$
				break;
			default:
				runCMake = !Files.exists(buildDir.resolve("CMakeFiles")); //$NON-NLS-1$
			}

			if (runCMake)
			{

				List<String> command = new ArrayList<>();

				command.add("cmake"); //$NON-NLS-1$
				command.add("-G"); //$NON-NLS-1$
				command.add(generator);

				if (toolChainFile != null)
				{
					command.add("-DCMAKE_TOOLCHAIN_FILE=" + toolChainFile.getPath().toString()); //$NON-NLS-1$
				}

				switch (getLaunchMode())
				{
				case "debug": //$NON-NLS-1$
					command.add("-DCMAKE_BUILD_TYPE=Debug"); //$NON-NLS-1$
					break;
				case "run": //$NON-NLS-1$
					command.add("-DCMAKE_BUILD_TYPE=Release"); //$NON-NLS-1$
					break;
				}
				command.add("-DCMAKE_EXPORT_COMPILE_COMMANDS=ON"); //$NON-NLS-1$

				if (launchtarget != null)
				{
					String idfTargetName = launchtarget.getAttribute("com.espressif.idf.launch.serial.core.idfTarget", //$NON-NLS-1$
							"");
					if (!idfTargetName.isEmpty())
					{
						command.add("-DIDF_TARGET=" + idfTargetName); //$NON-NLS-1$
					}
				}

				String userArgs = getProperty(CMAKE_ARGUMENTS);
				if (userArgs != null)
				{
					command.addAll(Arrays.asList(userArgs.trim().split("\\s+"))); //$NON-NLS-1$
				}

				command.add(new File(project.getLocationURI()).getAbsolutePath());

				outStream.write(String.join(" ", command) + '\n'); //$NON-NLS-1$

				org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(
						getBuildDirectory().toString());
				Process p = startBuildProcess(command, new IEnvironmentVariable[0], workingDir, console, monitor);
				if (p == null)
				{
					console.getErrorStream().write(String.format(Messages.CMakeBuildConfiguration_Failure, "")); //$NON-NLS-1$
					return null;
				}

				watchProcess(p, console);
			}

			try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
					getToolChain().getErrorParserIds()))
			{
				epm.setOutputStream(console.getOutputStream());

				List<String> command = new ArrayList<>();

				String envStr = getProperty(CMAKE_ENV);
				List<IEnvironmentVariable> envVars = new ArrayList<>();
				if (envStr != null)
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
				if (buildCommand == null)
				{
					command.add("cmake"); //$NON-NLS-1$
					command.add("--build"); //$NON-NLS-1$
					command.add("."); //$NON-NLS-1$
					if ("Ninja".equals(generator))
					{
						command.add("--"); //$NON-NLS-1$
						command.add("-v"); //$NON-NLS-1$
					}

				}
				else
				{
					command.addAll(Arrays.asList(buildCommand.split(" "))); //$NON-NLS-1$
				}

				outStream.write(String.join(" ", command) + '\n'); //$NON-NLS-1$

				org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(
						getBuildDirectory().toString());
				Process p = startBuildProcess(command, envVars.toArray(new IEnvironmentVariable[0]), workingDir,
						console, monitor);
				if (p == null)
				{
					console.getErrorStream().write(String.format(Messages.CMakeBuildConfiguration_Failure, "")); //$NON-NLS-1$
					return null;
				}

				watchProcess(p, new IConsoleParser[] { epm });

				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

				// Load compile_commands.json file
				processCompileCommandsFile(monitor);

				outStream.write(String.format(Messages.CMakeBuildConfiguration_BuildingComplete, epm.getErrorCount(),
						epm.getWarningCount(), buildDir.toString()));
			}

			return new IProject[] { project };
		}
		catch (IOException e)
		{
			throw new CoreException(Activator
					.errorStatus(String.format(Messages.CMakeBuildConfiguration_Building, project.getName()), e));
		}
	}

	private boolean isLocal() throws CoreException
	{
		IToolChain toolchain = getToolChain();
		return (Platform.getOS().equals(toolchain.getProperty(IToolChain.ATTR_OS))
				|| "linux-container".equals(toolchain.getProperty(IToolChain.ATTR_OS))) //$NON-NLS-1$
				&& (Platform.getOSArch().equals(toolchain.getProperty(IToolChain.ATTR_ARCH)));
	}

	private void processCompileCommandsFile(IProgressMonitor monitor) throws CoreException
	{
		IProject project = getProject();
		Path commandsFile = getBuildDirectory().resolve("compile_commands.json"); //$NON-NLS-1$
		if (Files.exists(commandsFile))
		{
			List<Job> jobsList = new ArrayList<>();
			monitor.setTaskName(Messages.CMakeBuildConfiguration_ProcCompJson);
			try (FileReader reader = new FileReader(commandsFile.toFile()))
			{
				Gson gson = new Gson();
				CompileCommand[] commands = gson.fromJson(reader, CompileCommand[].class);
				Map<String, CompileCommand> dedupedCmds = new HashMap<>();
				for (CompileCommand command : commands)
				{
					dedupedCmds.put(command.getFile(), command);
				}
				for (CompileCommand command : dedupedCmds.values())
				{
					processLine(command.getCommand(), jobsList);
				}
				for (Job j : jobsList)
				{
					try
					{
						j.join();
					}
					catch (InterruptedException e)
					{
						// ignore
					}
				}
				shutdown();
			}
			catch (IOException e)
			{
				throw new CoreException(Activator.errorStatus(
						String.format(Messages.CMakeBuildConfiguration_ProcCompCmds, project.getName()), e));
			}
		}
	}

	public void setLaunchTarget(ILaunchTarget target)
	{
		this.launchtarget = target;
	}
}
