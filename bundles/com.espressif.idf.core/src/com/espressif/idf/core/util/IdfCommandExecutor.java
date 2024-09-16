/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.build.ActiveLaunchConfigurationProvider;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;

public class IdfCommandExecutor
{

	private final String target;
	private final MessageConsole console;
	private static final String CMAKE_ARGUMENTS = "cmake.arguments"; //$NON-NLS-1$
	private static final ActiveLaunchConfigurationProvider LAUNCH_CONFIG_PROVIDER = new ActiveLaunchConfigurationProvider();

	public IdfCommandExecutor(String target, MessageConsole console)
	{
		this.target = target;
		this.console = console;
	}

	public IStatus executeReconfigure(IProject project)
	{
		console.activate();
		return runIdfReconfigureCommand(project);
	}

	private IStatus runIdfReconfigureCommand(IProject project)
	{
		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		setBuildFolder(project);
		List<String> arguments = prepareCmakeArguments(project);
		Map<String, String> environment = new HashMap<>(new IDFEnvironmentVariables().getSystemEnvMap());

		try (MessageConsoleStream messageConsoleStream = console.newMessageStream())
		{
			messageConsoleStream.println(String.join(" ", arguments)); //$NON-NLS-1$
			return runProcess(arguments, environment, processRunner, project, messageConsoleStream);
		}
		catch (IOException e1)
		{
			Logger.log(e1);
			return IDFCorePlugin.errorStatus(e1.getMessage(), e1);
		}
	}

	private List<String> prepareCmakeArguments(IProject project)
	{
		List<String> arguments = new ArrayList<>();
		arguments.add(IDFUtil.findCommandFromBuildEnvPath("cmake")); //$NON-NLS-1$
		arguments.add("-G"); //$NON-NLS-1$
		arguments.add("Ninja"); //$NON-NLS-1$
		arguments.add("-DPYTHON_DEPS_CHECKED=1"); //$NON-NLS-1$
		arguments.add("-DPYTHON=" + IDFUtil.getIDFPythonEnvPath()); //$NON-NLS-1$
		arguments.add("-DESP_PLATFORM=1"); //$NON-NLS-1$
		arguments.add("-DIDF_TARGET=" + target); //$NON-NLS-1$
		String ccache = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.IDF_CCACHE_ENABLE);
		ccache = ccache.isBlank() ? "0" : ccache; //$NON-NLS-1$

		arguments.add("-DCCACHE_ENABLE=" + ccache); //$NON-NLS-1$
		arguments.add(project.getLocation().toOSString());
		arguments.add("-B"); //$NON-NLS-1$
		try
		{
			arguments.add(IDFUtil.getBuildDir(project));
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return arguments;
	}

	private boolean setBuildFolder(IProject project)
	{
		String userArgs = getProperty(CMAKE_ARGUMENTS);
		// Custom build directory
		String[] cmakeArgumentsArr = userArgs.split(" "); //$NON-NLS-1$
		String customBuildDir = StringUtil.EMPTY;
		for (int i = 0; i < cmakeArgumentsArr.length; i++)
		{
			if (cmakeArgumentsArr[i].equals("-B")) //$NON-NLS-1$
			{
				customBuildDir = cmakeArgumentsArr[i + 1];
				break;
			}
		}
		try
		{
			IDFUtil.setBuildDir(project, customBuildDir);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}

		return !customBuildDir.isBlank();
	}

	public String getProperty(String name)
	{
		try
		{
			ILaunchConfiguration configuration = LAUNCH_CONFIG_PROVIDER.getActiveLaunchConfiguration();
			if (configuration != null
					&& configuration.getType().getIdentifier().equals(IDFLaunchConstants.DEBUG_LAUNCH_CONFIG_TYPE))
			{
				configuration = getBoundConfiguration(configuration);
			}
			return configuration == null ? StringUtil.EMPTY : configuration.getAttribute(name, StringUtil.EMPTY);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return StringUtil.EMPTY;
	}

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

	private IStatus runProcess(List<String> arguments, Map<String, String> environment,
			ProcessBuilderFactory processRunner, IProject project, MessageConsoleStream messageConsoleStream)
	{
		StringBuilder output = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				processRunner.run(arguments, project.getLocation(), environment).getInputStream())))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				output.append(line).append(System.lineSeparator());
				messageConsoleStream.println(line);
			}
			return new Status(IStatus.OK, IDFCorePlugin.PLUGIN_ID, output.toString());
		}
		catch (Exception e)
		{
			Logger.log(e);
			return IDFCorePlugin.errorStatus(e.getMessage(), e);
		}
	}
}
