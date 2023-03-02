/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.wokwi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.SDKConfigJsonReader;
import com.espressif.idf.core.util.StringUtil;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class WokwiSimulatorHandler
{

	public void execute(ILaunchConfiguration configuration, ILaunch launch, IProject project)
			throws ExecutionException, CoreException
	{

		SDKConfigJsonReader sdkconfig = new SDKConfigJsonReader(project);
		String target = sdkconfig.getValue("IDF_TARGET"); //$NON-NLS-1$

		String elfFilePath = IDFUtil.getELFFilePath(project).toOSString();

		String wokwiExecutablePath = getWokwiServerPath();
		if (StringUtil.isEmpty(wokwiExecutablePath))
		{
			String msg = "WOKWI_SERVER_PATH is not found. Please make sure WOKWI_SERVER_PATH is configured in the Preferences > C/C+ > Build > Environment";
			Display.getDefault().asyncExec(new Runnable()
			{

				@Override
				public void run()
				{
					MessageDialog.openError(Display.getDefault().getActiveShell(), "wokwi-server not found", msg);
				}
			});

			return;
		}

		String diagram_id = configuration.getAttribute(IWokwiLaunchConstants.ATTR_WOKWI_PROJECT_ID, StringUtil.EMPTY);

		List<String> arguments = new ArrayList<>();
		arguments.add(wokwiExecutablePath);
		arguments.add("--chip"); //$NON-NLS-1$
		arguments.add(target);

		if (!StringUtil.isEmpty(diagram_id))
		{
			arguments.add("--id"); //$NON-NLS-1$
			arguments.add(diagram_id);
		}
		arguments.add(elfFilePath);

		try
		{
			startWokwiServer(arguments, project, launch);
		}
		catch (
				IOException
				| CoreException e)
		{
			Logger.log(e);
		}

	}

	private void startWokwiServer(List<String> arguments, IProject project, ILaunch launch)
			throws IOException, CoreException
	{
		ProcessBuilder processBuilder = new ProcessBuilder(arguments);
		Process process = processBuilder.start();
		DebugPlugin.newProcess(launch, process, String.join(" ", arguments)); //$NON-NLS-1$
	}

	private String getWokwiServerPath()
	{
		IEnvironmentVariable env = new IDFEnvironmentVariables().getEnv(IWokwiLaunchConstants.WOKWI_SERVER_PATH);
		if (env != null)
		{
			return env.getValue();
		}
		return null;
	}

}
