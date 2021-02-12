/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.help;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.ui.update.ListInstalledToolsHandler;
import com.espressif.idf.ui.update.Messages;


public class ProductInformationHandler extends ListInstalledToolsHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		super.execute(event);
		console.println(IDFEnvironmentVariables.IDF_PATH + ": "
				+ new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.IDF_PATH)); // $NON-NLS-1$
		console.println(IDFEnvironmentVariables.IDF_PYTHON_ENV_PATH + ": "
				+ new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.IDF_PYTHON_ENV_PATH)); // $NON-NLS-1$
		console.println(IDFEnvironmentVariables.PATH + ": "
				+ new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.PATH)); // $NON-NLS-1$
		console.println();
		console.println(Messages.OperatingSystemMsg + System.getProperty("os.name").toLowerCase()); //$NON-NLS-1$
		console.println(Messages.JavaRuntimeVersionMsg + System.getProperty("java.runtime.version")); //$NON-NLS-1$
		console.println(Messages.EclipseMsg + Platform.getBundle("org.eclipse.platform").getVersion().toString()); //$NON-NLS-1$
		console.println(Messages.EclipseCDTMsg + Platform.getBundle("org.eclipse.cdt").getVersion().toString()); //$NON-NLS-1$
		showEspIdfVersion();
		console.println(Messages.PythonIdfEnvMsg + getPythonExeVersion(IDFUtil.getIDFPythonEnvPath()));
		console.println(Messages.PythonPathMsg + getPythonExeVersion("python")); //$NON-NLS-1$

		return null;
	}

	private void showEspIdfVersion()
	{
		List<String> commands = new ArrayList<>();
		commands.add(IDFUtil.getIDFPythonEnvPath());
		commands.add(IDFUtil.getIDFPythonScriptFile().getAbsolutePath());
		commands.add("--version"); //$NON-NLS-1$
		Map<String, String> envMap = new IDFEnvironmentVariables().getEnvMap();
		console.println(runCommand(commands, envMap));
	}

	private String getPythonExeVersion(String pythonExePath)
	{
		List<String> commands = new ArrayList<>();
		commands.add(pythonExePath);
		commands.add("--version"); //$NON-NLS-1$
		return runCommand(commands, System.getenv());
	}

	private String runCommand(List<String> arguments, Map<String, String> env)
	{
		String exportCmdOp = ""; //$NON-NLS-1$
		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		try
		{
			IStatus status = processRunner.runInBackground(arguments, Path.ROOT, env);
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(), IDFCorePlugin.errorStatus("Status can't be null", null)); //$NON-NLS-1$

			}

			// process export command output
			exportCmdOp = status.getMessage();
			Logger.log(exportCmdOp);
		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);
		}
		return exportCmdOp;
	}

}
