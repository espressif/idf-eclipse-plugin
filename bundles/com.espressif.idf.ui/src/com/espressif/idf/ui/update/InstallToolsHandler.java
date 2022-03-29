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

import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
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

				handleToolsInstall();
				monitor.worked(1);

				monitor.setTaskName(Messages.InstallToolsHandler_InstallingPythonMsg);
				handleToolsInstallPython();
				monitor.worked(1);
				

				monitor.setTaskName("Installing websocket_client");
				handleWebSocketClientInstall();
				monitor.worked(1);

				monitor.setTaskName(Messages.InstallToolsHandler_ExportingPathsMsg);
				new ExportIDFTools().runToolsExport(pythonExecutablenPath, gitExecutablePath, console);
				monitor.worked(1);
				console.println(Messages.InstallToolsHandler_ConfiguredBuildEnvVarMsg);

				monitor.setTaskName(Messages.InstallToolsHandler_AutoConfigureToolchain);
				configureToolChain();
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
		installToolsJob.schedule();

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
								MessageBox messageBox = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
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
	
	protected void handleWebSocketClientInstall()
	{
		IPath pipPath = new org.eclipse.core.runtime.Path(pythonExecutablenPath); //$NON-NLS-1$
		pipPath = pipPath.removeLastSegments(1).append("pip3.exe"); //$NON-NLS-1$
		
		if (!pipPath.toFile().exists()) 
		{
			console.println("pip executable not found. Unable to run `pip install websocket-client`");
			return;
		}

		// pip install websocket-client
		List<String> arguments = new ArrayList<String>();
		arguments.add(pipPath.toOSString());
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
				Logger.log(IDFCorePlugin.getPlugin(), IDFCorePlugin.errorStatus("Unable to get the process status.", null)); //$NON-NLS-1$
				console.println("Unable to get the process status.");
				return;
			}

			console.println(status.getMessage());

		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);
			console.println(e1.getLocalizedMessage());

		}
		console.println();
	}

}
