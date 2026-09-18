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
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;

public class IdfCommandExecutor
{

	private final String target;
	private final MessageConsole console;


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

	public IStatus executeSetTarget(IProject project)
	{
		console.activate();
		return runIdfSetTargetCommand(project);
	}

	private IStatus runIdfReconfigureCommand(IProject project)
	{
		List<String> arguments = prepareCmakeArguments(project);
		return runCommand(project, arguments);
	}

	private IStatus runIdfSetTargetCommand(IProject project)
	{
		try (MessageConsoleStream messageConsoleStream = console.newMessageStream())
		{
			List<String> arguments = prepareSetTargetArguments(project);
			return runCommand(project, arguments, messageConsoleStream);
		}
		catch (CoreException | IOException e)
		{
			Logger.log(e);
			return IDFCorePlugin.errorStatus(e.getMessage(), e);
		}
	}

	private IStatus runCommand(IProject project, List<String> arguments)
	{
		try (MessageConsoleStream messageConsoleStream = console.newMessageStream())
		{
			return runCommand(project, arguments, messageConsoleStream);
		}
		catch (IOException e)
		{
			Logger.log(e);
			return IDFCorePlugin.errorStatus(e.getMessage(), e);
		}
	}

	private IStatus runCommand(IProject project, List<String> arguments, MessageConsoleStream messageConsoleStream)
	{
		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		Map<String, String> environment = new HashMap<>(new IDFEnvironmentVariables().getSystemEnvMap());
		messageConsoleStream.println(String.join(" ", arguments)); //$NON-NLS-1$
		return runProcess(arguments, environment, processRunner, project, messageConsoleStream);
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

	private List<String> prepareSetTargetArguments(IProject project) throws CoreException
	{
		List<String> arguments = new ArrayList<>();
		arguments.add(IDFUtil.getIDFPythonEnvPath());
		arguments.add(IDFUtil.getIDFPythonScriptFile().getAbsolutePath());
		arguments.add("-B"); //$NON-NLS-1$
		arguments.add(IDFUtil.getBuildDir(project));
		arguments.add("set-target"); //$NON-NLS-1$
		arguments.add(target);
		return arguments;
	}

	public String getProperty(String name)
	{
		try
		{
			ILaunchBarManager launchBarManager = IDFCorePlugin.getService(ILaunchBarManager.class);
			ILaunchConfiguration configuration = null;

			if (launchBarManager != null)
			{
				configuration = launchBarManager.getActiveLaunchConfiguration();
			}

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
		try
		{
			Process process = processRunner.run(arguments, project.getLocation(), environment);
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
			{
				String line;
				while ((line = reader.readLine()) != null)
				{
					output.append(line).append(System.lineSeparator());
					messageConsoleStream.println(line);
				}
			}
			int exitCode = process.waitFor();
			return new Status(exitCode == 0 ? IStatus.OK : IStatus.ERROR, IDFCorePlugin.PLUGIN_ID, exitCode,
					output.toString(), null);
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			Logger.log(e);
			return IDFCorePlugin.errorStatus(e.getMessage(), e);
		}
		catch (Exception e)
		{
			Logger.log(e);
			return IDFCorePlugin.errorStatus(e.getMessage(), e);
		}
	}
}
