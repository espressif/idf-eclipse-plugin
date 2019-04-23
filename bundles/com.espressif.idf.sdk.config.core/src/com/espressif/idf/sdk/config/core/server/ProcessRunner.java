/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ProcessRunner
{

	public Process run(IPath workingDirectory, Map<String, String> environment, String... arguments)
			throws IOException, CoreException
	{
		List<String> commands = new ArrayList<String>(Arrays.asList(arguments));
		return doRun(commands, workingDirectory, environment);
	}

	private Process doRun(List<String> command, IPath workingDirectory, Map<String, String> environment)
			throws IOException, CoreException
	{
		ProcessBuilder processBuilder = createProcessBuilder(command);
		if (workingDirectory != null)
		{
			processBuilder.directory(workingDirectory.toFile());
		}

		if (environment != null && !environment.isEmpty())
		{
			processBuilder.environment().putAll(environment);
		}
		return startProcess(processBuilder);
	}

	protected ProcessBuilder createProcessBuilder(List<String> command)
	{
		return new ProcessBuilder(command);
	}

	protected Process startProcess(ProcessBuilder processBuilder) throws IOException
	{
		return processBuilder.start();
	}
}
