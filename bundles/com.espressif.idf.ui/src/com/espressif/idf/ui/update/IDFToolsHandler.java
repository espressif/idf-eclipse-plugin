/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.update;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.jface.dialogs.InputDialog;
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

		String command_arg = event.getParameter("com.espressif.idf.ui.commandParameter.tools"); //$NON-NLS-1$
		Logger.log("Command param:" + command_arg); //$NON-NLS-1$

		// install idf tools
		String idfPath = IDFUtil.getIDFPath();
		if (StringUtil.isEmpty(idfPath) || !new File(idfPath).exists())
		{
			idfPath = getInputObject();

			// add the IDF_PATH to the eclipse environment variables?
			IDFEnvironmentVariables idfEnvMgr = new IDFEnvironmentVariables();
			idfEnvMgr.addEnvVariable(IDFEnvironmentVariables.IDF_PATH, idfPath);
		}

		File idfToolsScriptFile = IDFUtil.getIDFToolsScriptFile();
		List<String> arguments = new ArrayList<String>(
				Arrays.asList(idfToolsScriptFile.getAbsolutePath(), command_arg));

		if (command_arg.equals(IDFConstants.TOOLS_EXPORT_CMD))
		{
			arguments.add(IDFConstants.TOOLS_EXPORT_CMD_FORMAT_VAL);
		}

		ProcessRunner processRunner = new ProcessRunner();
		Process process;
		try
		{
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
		return null;
	}

	/**
	 * @param current
	 * @return
	 */
	protected String getInputObject()
	{
		final InputDialog dialog = new InputDialog(Display.getDefault().getActiveShell(),
				Messages.IDFToolsHandler_IDFPath, Messages.IDFToolsHandler_InstallationDirPath, "", null); // $NON-NLS-2$
		String param = null;
		final int dialogCode = dialog.open();
		if (dialogCode == 0)
		{
			param = dialog.getValue();
			if (param != null)
			{
				param = param.trim();
				if (param.length() == 0)
				{
					return null;
				}
			}
		}
		return param;
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
