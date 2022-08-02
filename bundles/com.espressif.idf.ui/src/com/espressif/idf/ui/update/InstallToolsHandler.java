/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.update;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.build.ESPToolChainManager;
import com.espressif.idf.core.build.ESPToolChainProvider;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.UIPlugin;

/**
 * IDF Tools install command handler
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class InstallToolsHandler extends AbstractToolsHandler
{

	public static final String INSTALL_TOOLS_FLAG = "INSTALL_TOOLS_FLAG"; //$NON-NLS-1$
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

				IStatus status = handleToolsInstall();
				if (status.getSeverity() == IStatus.ERROR)
				{
					return status;
				}

				monitor.worked(1);
				monitor.setTaskName(Messages.InstallToolsHandler_InstallingPythonMsg);
				status = handleToolsInstallPython();
				if (status.getSeverity() == IStatus.ERROR)
				{
					return status;
				}

				monitor.worked(1);

				monitor.setTaskName(Messages.InstallToolsHandler_ExportingPathsMsg);
				status = new ExportIDFTools().runToolsExport(pythonExecutablenPath, gitExecutablePath, console);
				if (status.getSeverity() == IStatus.ERROR)
				{
					return status;
				}

				monitor.worked(1);
				console.println(Messages.InstallToolsHandler_ConfiguredBuildEnvVarMsg);

				monitor.setTaskName(Messages.InstallToolsHandler_AutoConfigureToolchain);
				configureToolChain();
				monitor.worked(1);

				configEnv();
				
				monitor.setTaskName(Messages.InstallToolsHandler_InstallingWebscoketMsg);
				handleWebSocketClientInstall();
				monitor.worked(1);
				
				copyOpenOcdRules();
				console.println(Messages.InstallToolsHandler_ConfiguredCMakeMsg);

				console.println(Messages.InstallToolsHandler_ToolsCompleted);

				return Status.OK_STATUS;
			}
		};
		Preferences scopedPreferenceStore = InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID);
		scopedPreferenceStore.putBoolean(INSTALL_TOOLS_FLAG, true);
		try
		{
			scopedPreferenceStore.flush();
		}
		catch (BackingStoreException e)
		{
			Logger.log(e);
		}
		installToolsJob.addJobChangeListener(new ToolInstallListener());
		installToolsJob.schedule();
	}

	/**
	 * Configure all the required environment variables here
	 */
	protected void configEnv()
	{
		IDFEnvironmentVariables idfEnvMgr = new IDFEnvironmentVariables();

		// Enable IDF_COMPONENT_MANAGER by default
		idfEnvMgr.addEnvVariable(IDFEnvironmentVariables.IDF_COMPONENT_MANAGER, "1");

	}

	private void copyOpenOcdRules()
	{
		if (Platform.getOS().equals(Platform.OS_LINUX)
				&& !IDFUtil.getOpenOCDLocation().equalsIgnoreCase(StringUtil.EMPTY))
		{
			console.println(Messages.InstallToolsHandler_CopyingOpenOCDRules);
			// Copy the rules to the idf
			StringBuilder pathToRules = new StringBuilder();
			pathToRules.append(IDFUtil.getOpenOCDLocation());
			pathToRules.append("/../share/openocd/contrib/60-openocd.rules"); //$NON-NLS-1$
			File rulesFile = new File(pathToRules.toString());
			if (rulesFile.exists())
			{
				Path source = Paths.get(pathToRules.toString());
				Path target = Paths.get("/etc/udev/rules.d/60-openocd.rules"); //$NON-NLS-1$
				console.println(String.format(Messages.InstallToolsHandler_OpenOCDRulesCopyPaths, source.toString(),
						target.toString()));

				Display.getDefault().syncExec(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							if (target.toFile().exists())
							{
								MessageBox messageBox = new MessageBox(Display.getDefault().getActiveShell(),
										SWT.ICON_WARNING | SWT.YES | SWT.NO);
								messageBox.setText(Messages.InstallToolsHandler_OpenOCDRulesCopyWarning);
								messageBox.setMessage(Messages.InstallToolsHandler_OpenOCDRulesCopyWarningMessage);
								int response = messageBox.open();
								if (response == SWT.YES)
								{
									Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
								}
								else
								{
									console.println(Messages.InstallToolsHandler_OpenOCDRulesNotCopied);
									return;
								}
							}
							else
							{
								Files.copy(source, target);
							}

							console.println(Messages.InstallToolsHandler_OpenOCDRulesCopied);
						}
						catch (IOException e)
						{
							Logger.log(e);
							console.println(Messages.InstallToolsHandler_OpenOCDRulesCopyError);
						}
					}
				});
			}
		}
	}

	/**
	 * Configure the toolchain and toolchain file in the preferences
	 */
	protected void configureToolChain()
	{
		ESPToolChainManager toolchainManager = new ESPToolChainManager();
		toolchainManager.removePrevInstalledToolchains(tcManager);
		toolchainManager.initToolChain(tcManager, ESPToolChainProvider.ID);
		toolchainManager.initCMakeToolChain(tcManager, cmakeTcManager);
	}

	protected IStatus handleToolsInstall()
	{
		// idf_tools.py install all
		List<String> arguments = new ArrayList<String>();
		arguments.add(IDFConstants.TOOLS_INSTALL_CMD);
		arguments.add(IDFConstants.TOOLS_INSTALL_ALL_CMD);

		console.println(Messages.InstallToolsHandler_InstallingToolsMsg);
		console.println(Messages.InstallToolsHandler_ItWilltakeTimeMsg);
		return runCommand(arguments);
	}

	protected IStatus handleToolsInstallPython()
	{
		List<String> arguments;
		// idf_tools.py install-python-env
		arguments = new ArrayList<String>();
		arguments.add(IDFConstants.TOOLS_INSTALL_PYTHON_CMD);
		return runCommand(arguments);
	}

	protected IStatus handleWebSocketClientInstall()
	{
		 
		// pip install websocket-client
		List<String> arguments = new ArrayList<String>();
		final String pythonEnvPath = IDFUtil.getIDFPythonEnvPath();
		if (pythonEnvPath == null || !new File(pythonEnvPath).exists())
		{
			console.println(String.format("%s executable not found. Unable to run `%s -m pip install websocket-client`", //$NON-NLS-1$
					IDFConstants.PYTHON_CMD, IDFConstants.PYTHON_CMD));
			return IDFCorePlugin.errorStatus(String.format("%s executable not found. Unable to run `%s -m pip install websocket-client`", //$NON-NLS-1$
					IDFConstants.PYTHON_CMD, IDFConstants.PYTHON_CMD), null);
		}
		arguments.add(pythonEnvPath);
		arguments.add("-m"); //$NON-NLS-1$
		arguments.add("pip"); //$NON-NLS-1$
		arguments.add("install"); //$NON-NLS-1$
		arguments.add("websocket-client"); //$NON-NLS-1$

		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();

		try
		{
			String cmdMsg = "Executing " + getCommandString(arguments); //$NON-NLS-1$
			console.println(cmdMsg);
			Logger.log(cmdMsg);

			Map<String, String> environment = new HashMap<>(System.getenv());
			Logger.log(environment.toString());

			IStatus status = processRunner.runInBackground(arguments, org.eclipse.core.runtime.Path.ROOT, environment);
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(),
						IDFCorePlugin.errorStatus("Unable to get the process status.", null)); //$NON-NLS-1$
				console.println("Unable to get the process status.");
				return IDFCorePlugin.errorStatus("Unable to get the process status.", null); //$NON-NLS-1$ ;
			}

			console.println(status.getMessage());
			return status;
		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);
			console.println(e1.getLocalizedMessage());
			return IDFCorePlugin.errorStatus(e1.getLocalizedMessage(), e1); // $NON-NLS-1$;
		}
	}

	private class ToolInstallListener implements IJobChangeListener
	{
		Map<String, String> existingVarMap;

		@Override
		public void aboutToRun(IJobChangeEvent event)
		{
			this.existingVarMap = loadExistingVars();
		}

		private Map<String, String> loadExistingVars()
		{
			IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
			Map<String, String> existingVarMap = new HashMap<>();

			existingVarMap.put(IDFEnvironmentVariables.IDF_COMPONENT_MANAGER,
					idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.IDF_COMPONENT_MANAGER));
			existingVarMap.put(IDFEnvironmentVariables.IDF_PATH,
					idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.IDF_PATH));
			existingVarMap.put(IDFEnvironmentVariables.IDF_PYTHON_ENV_PATH,
					idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.IDF_PYTHON_ENV_PATH));
			existingVarMap.put(IDFEnvironmentVariables.OPENOCD_SCRIPTS,
					idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.OPENOCD_SCRIPTS));
			existingVarMap.put(IDFEnvironmentVariables.PATH,
					idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.PATH));

			return existingVarMap;
		}

		@Override
		public void awake(IJobChangeEvent event)
		{

		}

		@Override
		public void done(IJobChangeEvent event)
		{
			if (event.getResult().getSeverity() == IStatus.ERROR)
			{
				restoreOldVars();
			}
		}

		private void restoreOldVars()
		{
			IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
			for (Entry<String, String> varsEntry : existingVarMap.entrySet())
			{
				idfEnvironmentVariables.addEnvVariable(varsEntry.getKey(), varsEntry.getValue());
			}
		}
		
		@Override
		public void running(IJobChangeEvent event)
		{

		}

		@Override
		public void scheduled(IJobChangeEvent event)
		{

		}

		@Override
		public void sleeping(IJobChangeEvent event)
		{

		}
	}
}
