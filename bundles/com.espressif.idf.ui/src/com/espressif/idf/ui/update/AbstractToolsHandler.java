/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.ExecutableFinder;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.PyWinRegistryReader;
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
	protected String idfPath;
	protected String pythonExecutablenPath;
	protected String gitExecutablePath;
	private Map<String, String> pythonVersions;
	private String commandId;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		activateIDFConsoleView();
		
		String commmand_id = commandId;;
		if (event != null)
		{
			commmand_id = event.getCommand().getId();
		}
		
		Logger.log("Command id:" + commmand_id); //$NON-NLS-1$

		// Get IDF_PATH
		idfPath = IDFUtil.getIDFPath();
		Logger.log("IDF_PATH :" + idfPath); //$NON-NLS-1$

		// Look for git path
		IPath gitPath = ExecutableFinder.find("git", true); //$NON-NLS-1$
		Logger.log("GIT path:" + gitPath); //$NON-NLS-1$
		if (gitPath != null)
		{
			this.gitExecutablePath = gitPath.toOSString();
		}

		pythonExecutablenPath = getPythonExecutablePath();

		// Let user choose
		DirectorySelectionDialog dir = new DirectorySelectionDialog(Display.getDefault().getActiveShell(), commmand_id,
				pythonExecutablenPath, pythonVersions, idfPath, gitExecutablePath);
		if (dir.open() == Window.OK)
		{
			idfPath = dir.getIDFDirectory();
			gitExecutablePath = dir.getGitExecutable();
			pythonExecutablenPath = dir.getPythonExecutable();
		}
		else
		{
			return null; // dialog is cancelled
		}

		if (StringUtil.isEmpty(pythonExecutablenPath) || StringUtil.isEmpty(gitExecutablePath)
				|| StringUtil.isEmpty(idfPath))
		{
			console.print("One or more paths are empty! Make sure you provide IDF_PATH, git and python executables"); //$NON-NLS-1$
			return null;
		}

		// Add IDF_PATH to the eclipse CDT build environment variables
		IDFEnvironmentVariables idfEnvMgr = new IDFEnvironmentVariables();
		idfEnvMgr.addEnvVariable(IDFEnvironmentVariables.IDF_PATH, idfPath);

		execute();

		return null;
	}

	protected void activateIDFConsoleView()
	{
		// Create Tools console
		MessageConsole msgConsole = findConsole(Messages.IDFToolsHandler_ToolsManagerConsole);
		msgConsole.clearConsole();
		console = msgConsole.newMessageStream();
		msgConsole.activate();

		// Open console view so that users can see the output
		openConsoleView();
	}

	protected String getPythonExecutablePath()
	{
		// Get Python
		if (Platform.OS_WIN32.equals(Platform.getOS()))
		{
			PyWinRegistryReader pyWinRegistryReader = new PyWinRegistryReader();
			pythonVersions = pyWinRegistryReader.getPythonVersions();
			if (pythonVersions.isEmpty())
			{
				Logger.log("No Python installations found in the system."); //$NON-NLS-1$
			}
			if (pythonVersions.size() == 1)
			{
				Map.Entry<String, String> entry = pythonVersions.entrySet().iterator().next();
				pythonExecutablenPath = entry.getValue();
			}
		}
		else
		{
			pythonExecutablenPath = IDFUtil.getPythonExecutable();
		}
		return pythonExecutablenPath;
	}

	/**
	 * Execute specific action
	 */
	protected abstract void execute();

	protected void runCommand(List<String> arguments)
	{
		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();

		try
		{
			// insert python.sh/exe path and idf_tools.py
			arguments.add(0, pythonExecutablenPath);
			arguments.add(1, IDFUtil.getIDFToolsScriptFile().getAbsolutePath());

			console.println(Messages.AbstractToolsHandler_ExecutingMsg + " " + getCommandString(arguments));

			Map<String, String> environment = new HashMap<>(System.getenv());
			Logger.log(environment.toString());

			if (gitExecutablePath != null)
			{
				addGitToEnvironment(environment, gitExecutablePath);
			}
			IStatus status = processRunner.runInBackground(arguments, Path.ROOT, environment);

			console.println(status.getMessage());
			console.println();

		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);

		}
	}

	protected void addGitToEnvironment(Map<String, String> environment, String gitExecutablePath)
	{
		IPath gitPath = new Path(gitExecutablePath);
		if (gitPath.toFile().exists())
		{
			String gitDir = gitPath.removeLastSegments(1).toOSString();
			String path1 = environment.get("PATH"); //$NON-NLS-1$
			String path2 = environment.get("Path"); //$NON-NLS-1$
			if (!StringUtil.isEmpty(path1) && !path1.contains(gitDir)) // Git not found on the PATH environment
			{
				path1 = gitDir.concat(";").concat(path1);
				environment.put("PATH", path1); //$NON-NLS-1$
			}
			else if (!StringUtil.isEmpty(path2) && !path2.contains(gitDir)) // Git not found on the Path environment
			{
				path2 = gitDir.concat(";").concat(path2); //$NON-NLS-1$
				environment.put("Path", path2); //$NON-NLS-1$
			}
		}
	}

	protected String getCommandString(List<String> arguments)
	{
		StringBuilder builder = new StringBuilder();
		arguments.forEach(entry -> builder.append(entry + " ")); //$NON-NLS-1$

		return builder.toString().trim();
	}

	/**
	 * @return
	 */
	protected ILaunchManager getLaunchManager()
	{
		return DebugPlugin.getDefault().getLaunchManager();
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

	protected void openConsoleView()
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
	
	public void setCommandId(String commandId)
	{
		this.commandId = commandId;
	}
}
