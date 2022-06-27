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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.ExecutableFinder;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;

public class UpdateEspIdfHandler extends AbstractHandler
{

	private MessageConsoleStream console;
	private String gitExecutablePath;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		activateIDFConsoleView();
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
		command.add("checkout");
		command.add("-f");
		command.add("master");
		return command;
	}

	protected void activateIDFConsoleView()
	{
		// Create Tools console
		MessageConsole msgConsole = findConsole("Update ESP-IDF master console");
		msgConsole.clearConsole();
		console = msgConsole.newMessageStream();
		msgConsole.activate();

		// Open console view so that users can see the output
		openConsoleView();
	}

	private void openConsoleView()
	{
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(IConsoleConstants.ID_CONSOLE_VIEW);
		}
		catch (PartInitException e)
		{
			Logger.log(e);
		}

	}

	private MessageConsole findConsole(String name)
	{
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
		{
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		}
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	private List<String> getGitPullCommand()
	{
		List<String> command = new ArrayList<>();
		command.add(gitExecutablePath);
		command.add("pull");
		return command;
	}

	private List<String> getSubmoduleUpdateCommand()
	{
		List<String> command = new ArrayList<>();
		command.add(gitExecutablePath);
		command.add("submodule");
		command.add("update");
		command.add("--init");
		command.add("--recursive");
		return command;
	}

	protected String getCommandString(List<String> arguments)
	{
		StringBuilder builder = new StringBuilder();
		arguments.forEach(entry -> builder.append(entry + " ")); //$NON-NLS-1$

		return builder.toString().trim();
	}
}
