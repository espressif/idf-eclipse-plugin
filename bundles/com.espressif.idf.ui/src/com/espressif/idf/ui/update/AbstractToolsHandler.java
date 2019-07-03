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
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

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

}
