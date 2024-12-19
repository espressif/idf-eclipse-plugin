/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.toolchain.ESPToolChainManager;
import com.espressif.idf.core.tools.vo.IDFToolSet;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.update.ExportIDFTools;
import com.espressif.idf.ui.update.Messages;

/**
 * Job to activate the provide {@link IDFToolSet} in the given ide environment
 * 
 * @author Ali Azam Rana
 *
 */
public class ToolsActivationJob extends ToolsJob
{
	public static final String INSTALL_TOOLS_FLAG = "INSTALL_TOOLS_FLAG"; //$NON-NLS-1$

	public ToolsActivationJob(IDFToolSet idfToolSet, String pythonExecutablePath, String gitExecutablePath)
	{
		super("Tools Activation Job", null, null);
		this.idfToolSet = idfToolSet;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor)
	{
		if (idfToolSet == null)
		{
			return Status.error("IDF Tool Set Cannot be null");
		}

		// verify with export script to test if everything is okay with installation

		monitor.beginTask("Verifying if there are any changes to the installed tools", 5);
		monitor.worked(1);

		ExportIDFTools exportIDFTools = new ExportIDFTools();
		IStatus status = exportIDFTools.getToolsExportOutputFromGivenIdfPath(idfToolSet.getSystemPythonExecutablePath(),
				idfToolSet.getSystemGitExecutablePath(), console, errorConsoleStream, idfToolSet.getIdfLocation());
		if (status.getSeverity() == IStatus.ERROR)
		{
			return Status.error("INSTALL_AGAIN");
		}
		monitor.worked(1);

		monitor.setTaskName("Exporting variables to eclipse");
		processExportCmdOutput(status.getMessage());
		setEnvVarsInEclipse();
		monitor.worked(1);

		monitor.setTaskName("Setting up toolchains and targets");
		setUpToolChainsAndTargets();
		monitor.worked(1);

		// post export operations like copying openocd rules may also need to setup with the python dependencies here as
		// well like websocket-client
		monitor.setTaskName(Messages.InstallToolsHandler_InstallingWebscoketMsg);
		handleWebSocketClientInstall();
		monitor.worked(1);

		monitor.setTaskName("Setting OpenOCD rules");
		copyOpenOcdRules();
		monitor.worked(1);

		idfToolSet.setActive(true);

		toolSetConfigurationManager.export(idfToolSet);
		console.println("Tools Activated");

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

		return Status.OK_STATUS;
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
							errorConsoleStream.println(Messages.InstallToolsHandler_OpenOCDRulesCopyError);
						}
					}
				});
			}
		}
	}

	private void setUpToolChainsAndTargets()
	{
		IStatus status = loadTargetsAvailableFromIdfInCurrentToolSet();
		if (status.getSeverity() == IStatus.ERROR)
		{
			Logger.log("Unable to get IDF targets from current toolset");
			return;
		}

		List<String> targets = extractTargets(status.getMessage());
		ESPToolChainManager espToolChainManager = new ESPToolChainManager();
		espToolChainManager.removeLaunchTargetsNotPresent(targets);
		espToolChainManager.removeCmakeToolChains();
		espToolChainManager.removeStdToolChains();
		espToolChainManager.configureToolChain(targets);
	}

	private void setEnvVarsInEclipse()
	{
		if (idfToolSet == null)
			throw new RuntimeException("Tools Cannot be null");

		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
		idfEnvironmentVariables.removeAllEnvVariables();
		idfToolSet.getEnvVars().forEach((key, value) -> {
			if (value != null)
				idfEnvironmentVariables.addEnvVariable(key, value);
		});
		String path = replacePathVariable(idfToolSet.getEnvVars().get(IDFEnvironmentVariables.PATH));

		idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.IDF_PATH, idfToolSet.getIdfLocation());
		idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.PATH, path);
		idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.PYTHON_EXE_PATH,
				idfToolSet.getSystemPythonExecutablePath());

		idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.IDF_COMPONENT_MANAGER, "1");
		// IDF_MAINTAINER=1 to be able to build with the clang toolchain
		idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.IDF_MAINTAINER, "1");
		IDFUtil.updateEspressifPrefPageOpenocdPath();
	}
}
