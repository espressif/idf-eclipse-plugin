/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.update;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import com.aptana.core.ShellExecutable;
import com.aptana.core.util.ProcessRunner;
import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;

/**
 * IDF Tools command Manager
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFToolsHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		String commmand_id = event.getCommand().getId();
		Logger.log("Command id:" + commmand_id); //$NON-NLS-1$

		String idfPath = IDFUtil.getIDFPath();
		if (StringUtil.isEmpty(idfPath) || !new File(idfPath).exists())
		{
			idfPath = getIDFDirPath();

			// add the IDF_PATH to the eclipse environment variables?
			IDFEnvironmentVariables idfEnvMgr = new IDFEnvironmentVariables();
			idfEnvMgr.addEnvVariable(IDFEnvironmentVariables.IDF_PATH, idfPath);
		}

		if (commmand_id.equals("com.espressif.idf.ui.command.install")) //$NON-NLS-1$
		{
			handleToolsInstall();
		}
		else if (commmand_id.equals("com.espressif.idf.ui.command.list")) //$NON-NLS-1$
		{
			handleToolsList();
		}

		return null;
	}

	protected void runCommand(List<String> arguments)
	{
		ProcessRunner processRunner = new ProcessRunner();
		Process process;
		try
		{
			// insert idf_tools.py
			arguments.add(0, IDFUtil.getIDFToolsScriptFile().getAbsolutePath());

			process = processRunner.run(Path.ROOT, getEnvironment(Path.ROOT),
					arguments.toArray(new String[arguments.size()]));

			Launch fLaunch = new Launch(null, ILaunchManager.RUN_MODE, null);
			fLaunch.setAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP, Long.toString(System.currentTimeMillis()));
			getLaunchManager().addLaunch(fLaunch);

			DebugPlugin.newProcess(fLaunch, process, Messages.IDFToolsHandler_ToolsManagerConsole);

		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);

		}
	}

	protected void handleToolsInstall()
	{
		// idf_tools.py install all
		List<String> arguments = new ArrayList<String>();
		arguments.add(IDFConstants.TOOLS_INSTALL_CMD);
		arguments.add(IDFConstants.TOOLS_INSTALL_ALL_CMD);
		runCommand(arguments);

		// idf_tools.py install-python-env
		arguments = new ArrayList<String>();
		arguments.add(IDFConstants.TOOLS_INSTALL_PYTHON_CMD);
		runCommand(arguments);

		handleToolsExport();
	}

	protected void handleToolsExport()
	{
		List<String> arguments = new ArrayList<String>();
		arguments.add(IDFUtil.getIDFToolsScriptFile().getAbsolutePath());
		arguments.add(IDFConstants.TOOLS_EXPORT_CMD);
		arguments.add(IDFConstants.TOOLS_EXPORT_CMD_FORMAT_VAL);

		ProcessRunner processRunner = new ProcessRunner();
		IStatus status = null;
		try
		{
			status = processRunner.runInBackground(Path.ROOT, getEnvironment(Path.ROOT),
					arguments.toArray(new String[arguments.size()]));
		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);
		}

		Logger.log(IDFCorePlugin.getPlugin(), status);
		if (status != null && status.isOK())
		{
			String exportCmdOp = status.getMessage();

			processExportCmdOutput(exportCmdOp);
		}

	}

	protected void processExportCmdOutput(String exportCmdOp)
	{
		// process export command output
		String[] exportEntries = exportCmdOp.split("\n"); //$NON-NLS-1$
		for (String entry : exportEntries)
		{
			String[] keyValue = entry.split("="); //$NON-NLS-1$
			if (keyValue.length == 2 && keyValue[0].equals(IDFEnvironmentVariables.PATH)) // 0 - key, 1 - value
			{
				Logger.log("PATH: " + keyValue[1]); //$NON-NLS-1$

				IDFEnvironmentVariables idfEnvMgr = new IDFEnvironmentVariables();
				IEnvironmentVariable env = idfEnvMgr.getEnv(IDFEnvironmentVariables.PATH);

				// PATH not found in the environment variables
				if (env == null)
				{
					idfEnvMgr.addEnvVariable(IDFEnvironmentVariables.PATH, keyValue[1]);
					return;
				}

				// PATH is already defined in the environment variables - so let's identify and append the missing
				// paths

				// Process the old PATH
				String oldPath = env.getValue();
				String[] oldPathEntries = oldPath.split(File.pathSeparator);

				// Prepare a new set of entries
				Set<String> newPathSet = new HashSet<>(Arrays.asList(oldPathEntries));

				// Process a new PATH
				String[] newPathEntries = keyValue[1].split(File.pathSeparator);

				// Combine old and new path entries
				newPathSet.addAll(Arrays.asList(newPathEntries));

				// Prepare PATH string
				StringBuilder pathBuilder = new StringBuilder();
				for (String newEntry : newPathSet)
				{
					pathBuilder.append(newEntry);
					pathBuilder.append(File.pathSeparator);
				}

				// remove the last pathSeparator
				pathBuilder.deleteCharAt(pathBuilder.length() - 1);

				// Replace with a new PATH entry
				Logger.log(pathBuilder.toString());
				idfEnvMgr.addEnvVariable(IDFEnvironmentVariables.PATH, pathBuilder.toString());

				return;
			}
		}
	}

	protected void handleToolsList()
	{
		List<String> arguments = new ArrayList<String>();
		arguments.add(IDFConstants.TOOLS_LIST_CMD);

		runCommand(arguments);

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

}
