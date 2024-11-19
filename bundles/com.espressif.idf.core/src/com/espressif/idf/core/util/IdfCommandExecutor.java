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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
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

	private IStatus runIdfReconfigureCommand(IProject project)
	{
		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		List<String> arguments = prepareArguments();
		Map<String, String> environment = new HashMap<>(new IDFEnvironmentVariables().getSystemEnvMap());

		try (MessageConsoleStream messageConsoleStream = console.newMessageStream())
		{
			return runProcess(arguments, environment, processRunner, project, messageConsoleStream);
		}
		catch (IOException e1)
		{
			Logger.log(e1);
			return IDFCorePlugin.errorStatus(e1.getMessage(), e1);
		}
	}

	private List<String> prepareArguments()
	{
		List<String> arguments = new ArrayList<>();
		arguments.add(IDFUtil.getIDFPythonEnvPath());
		arguments.add(IDFUtil.getIDFPythonScriptFile().getAbsolutePath());
		arguments.add("-DIDF_TARGET=" + target); //$NON-NLS-1$
		arguments.add("reconfigure"); //$NON-NLS-1$
		return arguments;
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
