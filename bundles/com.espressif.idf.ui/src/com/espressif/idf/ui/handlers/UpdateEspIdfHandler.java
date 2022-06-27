/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.ExecutableFinder;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.ui.IDFConsole;

public class UpdateEspIdfHandler extends AbstractHandler
{

	private MessageConsoleStream console;
	private String gitExecutablePath;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		console = new IDFConsole().getConsoleStream(Messages.UpdateEspIdfCommand_Title, null);
		IPath gitPath = ExecutableFinder.find("git", true); //$NON-NLS-1$
		if (gitPath != null)
		{
			this.gitExecutablePath = gitPath.toOSString();

		}
		Thread updateEspIdfMasterThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				ProcessBuilder[] builders = { new ProcessBuilder(getCheckoutMasterCommand()),
						new ProcessBuilder(getGitPullCommand()), new ProcessBuilder(getSubmoduleUpdateCommand()) };
				for (ProcessBuilder builder : builders)
				{
					builder.directory(new File(IDFUtil.getIDFPath()));
					builder.redirectErrorStream(true);
					Map<String, String> envMap = new IDFEnvironmentVariables().getEnvMap();
					builder.environment().putAll(envMap);
					startProcess(builder);
				}

			}

			private void startProcess(ProcessBuilder builder)
			{
				try
				{
					Process process = builder.start();
					InputStreamReader isr = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8);
					BufferedReader br = new BufferedReader(isr);
					br.lines().forEach(line -> console.println((line)));
				}
				catch (IOException e)
				{
					Logger.log(e);
				}
			}
		});
		updateEspIdfMasterThread.start();
		return null;
	}

	private List<String> getCheckoutMasterCommand()
	{
		List<String> command = new ArrayList<>();
		command.add(gitExecutablePath);
		command.add("checkout"); //$NON-NLS-1$
		command.add("-f"); //$NON-NLS-1$
		command.add("master"); //$NON-NLS-1$
		return command;
	}

	private List<String> getGitPullCommand()
	{
		List<String> command = new ArrayList<>();
		command.add(gitExecutablePath);
		command.add("pull"); //$NON-NLS-1$
		return command;
	}

	private List<String> getSubmoduleUpdateCommand()
	{
		List<String> command = new ArrayList<>();
		command.add(gitExecutablePath);
		command.add("submodule"); //$NON-NLS-1$
		command.add("update"); //$NON-NLS-1$
		command.add("--init"); //$NON-NLS-1$
		command.add("--recursive"); //$NON-NLS-1$
		return command;
	}
}
