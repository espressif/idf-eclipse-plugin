/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.update;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.aptana.core.util.ProcessRunner;
import com.aptana.core.util.StringUtil;
import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.build.ESPToolChainManager;
import com.espressif.idf.core.build.ESPToolChainProvider;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;

/**
 * IDF Tools install command handler
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class InstallToolsHandler extends AbstractToolsHandler
{

	private IToolChainManager tcManager = CCorePlugin.getService(IToolChainManager.class);
	private ICMakeToolChainManager cmakeTcManager = CCorePlugin.getService(ICMakeToolChainManager.class);

	@Override
	protected void execute()
	{
		Job installToolsJob = new Job(Messages.InstallToolsHandler_InstallingToolsMsg)
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				monitor.beginTask(Messages.InstallToolsHandler_ItWilltakeTimeMsg, 5);
				monitor.worked(1);

				handleToolsInstall();
				monitor.worked(1);

				monitor.setTaskName(Messages.InstallToolsHandler_InstallingPythonMsg);
				handleToolsInstallPython();
				monitor.worked(1);

				monitor.setTaskName(Messages.InstallToolsHandler_ExportingPathsMsg);
				handleToolsExport();
				monitor.worked(1);

				monitor.setTaskName(Messages.InstallToolsHandler_AutoConfigureToolchain);
				configureToolChain();
				monitor.worked(1);

				return Status.OK_STATUS;
			}

		};
		installToolsJob.schedule();

	}

	/**
	 * Configure the toolchain and toolchain file in the preferences
	 */
	protected void configureToolChain()
	{
		ESPToolChainManager toolchainManager = new ESPToolChainManager();
		toolchainManager.initToolChain(tcManager, ESPToolChainProvider.ID);
		toolchainManager.initCMakeToolChain(tcManager, cmakeTcManager);
	}

	protected void handleToolsInstall()
	{
		// idf_tools.py install all
		List<String> arguments = new ArrayList<String>();
		arguments.add(IDFConstants.TOOLS_INSTALL_CMD);
		arguments.add(IDFConstants.TOOLS_INSTALL_ALL_CMD);

		console.println(Messages.InstallToolsHandler_InstallingToolsMsg);
		console.println(Messages.InstallToolsHandler_ItWilltakeTimeMsg);
		runCommand(arguments);

	}

	protected void handleToolsInstallPython()
	{
		List<String> arguments;
		// idf_tools.py install-python-env
		arguments = new ArrayList<String>();
		arguments.add(IDFConstants.TOOLS_INSTALL_PYTHON_CMD);
		runCommand(arguments);
	}

	protected void handleToolsExport()
	{
		List<String> arguments = new ArrayList<String>();
		arguments.add(pythonPath);
		arguments.add(IDFUtil.getIDFToolsScriptFile().getAbsolutePath());
		arguments.add(IDFConstants.TOOLS_EXPORT_CMD);
		arguments.add(IDFConstants.TOOLS_EXPORT_CMD_FORMAT_VAL);

		console.println(Messages.AbstractToolsHandler_ExecutingMsg + " " + getCommandString(arguments)); //$NON-NLS-1$

		ProcessRunner processRunner = new ProcessRunner();
		IStatus status = null;
		try
		{
			status = processRunner.runInBackground(Path.ROOT, getEnvironment(Path.ROOT),
					arguments.toArray(new String[arguments.size()]));
			console.println(status.getMessage());
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
			entry.replaceAll("\\r", ""); //$NON-NLS-1$
			String[] keyValue = entry.split("="); //$NON-NLS-1$
			if (keyValue.length == 2) // 0 - key, 1 - value
			{
				String msg = MessageFormat.format("Key: {0} Value: {1}", keyValue[0], keyValue[1]); //$NON-NLS-1$ //$NON-NLS-2$
				Logger.log(msg);

				IDFEnvironmentVariables idfEnvMgr = new IDFEnvironmentVariables();
				IEnvironmentVariable env = idfEnvMgr.getEnv(keyValue[0]);

				// Environment variable not found
				if (env == null)
				{
					idfEnvMgr.addEnvVariable(keyValue[0], keyValue[1]);
					
				}

				// Special processing in case of PATH
				if (env != null && keyValue[0].equals(IDFEnvironmentVariables.PATH))
				{
					// PATH is already defined in the environment variables - so let's identify and append the missing
					// paths

					// Process the old PATH
					String oldPath = env.getValue();
					String[] oldPathEntries = oldPath.split(File.pathSeparator);

					// Prepare a new set of entries
					Set<String> newPathSet = new LinkedHashSet<>(); //Order is important here, check IEP-60
					
					// Process a new PATH
					String[] newPathEntries = keyValue[1].split(File.pathSeparator);
					newPathSet.addAll(Arrays.asList(newPathEntries));
					
					//Add old entries
					newPathSet.addAll(Arrays.asList(oldPathEntries));

					// Prepare PATH string
					StringBuilder pathBuilder = new StringBuilder();
					for (String newEntry : newPathSet)
					{
						if (newEntry.equals("$PATH")) //$NON-NLS-1$
						{
							Map<String, String> systemEnv = new HashMap<>(System.getenv());
							newEntry = systemEnv.get("PATH"); //$NON-NLS-1$
							if (newEntry == null)
							{
								newEntry = systemEnv.get("Path"); // for Windows //$NON-NLS-1$
								if (newEntry == null)  // no idea
								{
									Logger.log(new Exception("No PATH found in the system environment variables")); //$NON-NLS-1$
								}
							}
						}
						if (!StringUtil.isEmpty(newEntry))
						{
							pathBuilder.append(newEntry);
							pathBuilder.append(File.pathSeparator);
						}
					}

					// remove the last pathSeparator
					pathBuilder.deleteCharAt(pathBuilder.length() - 1);

					// Replace with a new PATH entry
					Logger.log(pathBuilder.toString());
					idfEnvMgr.addEnvVariable(IDFEnvironmentVariables.PATH, pathBuilder.toString());
				}
			}

		}
	}

}
