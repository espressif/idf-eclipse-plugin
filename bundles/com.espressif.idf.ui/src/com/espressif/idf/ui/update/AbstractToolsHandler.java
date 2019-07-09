/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.update;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.aptana.core.ShellExecutable;
import com.aptana.core.util.ProcessRunner;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public abstract class AbstractToolsHandler extends AbstractHandler
{
	/**
	 * Tools console
	 */
	protected MessageConsoleStream console;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		String commmand_id = event.getCommand().getId();
		Logger.log("Command id:" + commmand_id); //$NON-NLS-1$

		String idfPath = IDFUtil.getIDFPath();
		if (StringUtil.isEmpty(idfPath) || !new File(idfPath).exists())
		{
			idfPath = getIDFDirPath();
			if (idfPath == null) //IDF directory selection dialog would have been cancelled
			{
				return null;
			}

			// add the IDF_PATH to the eclipse environment variables?
			IDFEnvironmentVariables idfEnvMgr = new IDFEnvironmentVariables();
			idfEnvMgr.addEnvVariable(IDFEnvironmentVariables.IDF_PATH, idfPath);
		}

		// Create Tools console
		MessageConsole msgConsole = findConsole(Messages.IDFToolsHandler_ToolsManagerConsole);
		msgConsole.clearConsole();
		console = msgConsole.newMessageStream();

		execute();

		return null;
	}

	/**
	 * Execute specific action
	 */
	protected abstract void execute();

	protected void runCommand(List<String> arguments)
	{
		ProcessRunner processRunner = new ProcessRunner();

		try
		{
			// insert idf_tools.py
			arguments.add(0, IDFUtil.getIDFToolsScriptFile().getAbsolutePath());

			console.println(Messages.AbstractToolsHandler_ExecutingMsg + getCommandString(arguments));

			IStatus status = processRunner.runInBackground(Path.ROOT, getEnvironment(Path.ROOT),
					arguments.toArray(new String[arguments.size()]));

			console.println(status.getMessage());
			console.println();

		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);

		}
	}

	protected String getCommandString(List<String> arguments)
	{
		StringBuilder builder = new StringBuilder();
		arguments.forEach(entry -> builder.append(entry + " ")); //$NON-NLS-1$

		return builder.toString().trim();
	}

	/**
	 * @param location
	 * @return
	 */
	protected Map<String, String> getEnvironment(IPath location)
	{
		return ShellExecutable.getEnvironment(location);
	}

	/**
	 * @return
	 */
	protected ILaunchManager getLaunchManager()
	{
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * @param current
	 * @return
	 */
	protected String getIDFDirPath()
	{
		DirectorySelectionDialog dir = new DirectorySelectionDialog(Display.getDefault().getActiveShell());
		if (dir.open() == Window.OK)
		{
			return dir.getValue();
		}

		return null;
	}

	/**
	 * Find a console for a given name. If not found, it will create a new one and return
	 * 
	 * @param name
	 * @return
	 */
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

}
