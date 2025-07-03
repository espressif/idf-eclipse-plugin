/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.intro.IIntroManager;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.build.Messages;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.DownloadListener;
import com.espressif.idf.core.tools.EimConstants;
import com.espressif.idf.core.tools.EimLoader;
import com.espressif.idf.core.tools.ToolInitializer;
import com.espressif.idf.core.tools.vo.EimJson;
import com.espressif.idf.core.tools.watcher.EimJsonStateChecker;
import com.espressif.idf.core.tools.watcher.EimJsonWatchService;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.IDFConsole;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.handlers.EclipseHandler;
import com.espressif.idf.ui.tools.manager.ESPIDFManagerEditor;
import com.espressif.idf.ui.tools.manager.EimEditorInput;
import com.espressif.idf.ui.tools.watcher.EimJsonUiChangeHandler;

/**
 * Startup class to handle the tools
 * 
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class EspressifToolStartup implements IStartup
{
	private EimJsonUiChangeHandler eimJsonUiChangeHandler;
	private ToolInitializer toolInitializer;
	private Preferences preferences;
	private EimJson eimJson;
	private EimLoader eimLoader;
	private MessageConsoleStream standardConsoleStream;
	private MessageConsoleStream errorConsoleStream;
	private IDFEnvironmentVariables idfEnvironmentVariables;

	@Override
	public void earlyStartup()
	{
		preferences = org.eclipse.core.runtime.preferences.InstanceScope.INSTANCE
				.getNode(UIPlugin.PLUGIN_ID);
		toolInitializer = new ToolInitializer(preferences);
		standardConsoleStream = getConsoleStream(false);
		errorConsoleStream = getConsoleStream(true);
		idfEnvironmentVariables = new IDFEnvironmentVariables();
		eimLoader = new EimLoader(new StartupClassDownloadEimDownloadListener(), 
				standardConsoleStream, errorConsoleStream, Display.getDefault());
		EimJsonStateChecker stateChecker = new EimJsonStateChecker(preferences);
		eimJsonUiChangeHandler = new EimJsonUiChangeHandler(preferences);
		stateChecker.updateLastSeenTimestamp();
		EimJsonWatchService.getInstance().addEimJsonChangeListener(eimJsonUiChangeHandler);

		if (!toolInitializer.isEimInstalled() && !toolInitializer.isEimIdfJsonPresent())
		{
			Logger.log("EIM not installed");
			notifyMissingTools();
			return;
		}
		
		eimJson = toolInitializer.loadEimJson();

		if (toolInitializer.isOldEspIdfConfigPresent() && !toolInitializer.isOldConfigExported())
		{
			Logger.log("Old configuration found and not converted");
			boolean isEimInApplications = checkIfEimPathMacOsIsInApplications();
			if (!isEimInApplications)
			{
				promptUserToMoveEimToApplications();
			}
			
			EimJsonWatchService.withPausedListeners(()-> handleOldConfigExport());
		}
		else if (toolInitializer.isEimIdfJsonPresent() && !toolInitializer.isEspIdfSet())
		{
			promptUserToOpenToolManager(eimJson);
		}

		// Set EimPath on every startup to ensure proper path in configurations
		if (eimJson != null)
		{
			idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.EIM_PATH, eimJson.getEimPath());
		}
		else 
		{
			// Fail-safe call to ensure if the eim is in Applications or user.home it is setup in env vars
			toolInitializer.findAndSetEimPath();
		}

		if (stateChecker.wasModifiedSinceLastRun())
		{
			showEimJsonStateChangeNotification();
		}
	}

	private boolean checkIfEimPathMacOsIsInApplications()
	{
		if (!Platform.getOS().equals(Platform.OS_MACOSX))
			return true;
		
		String eimPath = idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.EIM_PATH); 
		if (!StringUtil.isEmpty(eimPath))
		{
			if (Files.exists(Paths.get(eimPath)))
			{
				boolean isInApplications = eimPath.startsWith("/Applications/") ||
                        eimPath.startsWith(System.getProperty("user.home") + "/Applications/");
				if (!isInApplications)
				{
					Logger.log("EIM_PATH not in applications: " + eimPath);
					return false;
				}
			}
		}
		
		return true;
	}

	private void handleOldConfigExport()
	{
		final int[] response = new int[] { -1 };
		Display display = Display.getDefault();
		display.syncExec(() -> {
			MessageDialog messageDialog = new MessageDialog(display.getActiveShell(),
					Messages.OldConfigFoundMsgBoxTitle, null, Messages.OldConfigFoundMsgBoxMsg, 0, 0,
					new String[] { Messages.ToolsInitializationDifferentPathMessageBoxOptionYes,
							Messages.ToolsInitializationDifferentPathMessageBoxOptionNo });
			response[0] = messageDialog.open();
		});

		if (response[0] == 0)
		{
			try
			{
				// if eim json is present it means that it contains the updated path and we use that else we fallback to finding eim in default paths
				Path eimPath;
				String eimPathEnvVar = idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.EIM_PATH);
				if (eimJson != null)
				{
					eimPath = Paths.get(eimJson.getEimPath());
				}
				else if (!StringUtil.isEmpty(eimPathEnvVar))
				{
					eimPath = Paths.get(eimPathEnvVar);
				}
				else 
				{
					eimPath = toolInitializer.getDefaultEimPath();
				}
				
				IStatus status = toolInitializer.exportOldConfig(eimPath);
				Logger.log("Tools Conversion Process Message: ");
				Logger.log(status.getMessage());
				if (status.getSeverity() != IStatus.ERROR)
				{
					preferences.putBoolean(EimConstants.OLD_CONFIG_EXPORTED_FLAG, true);
					displayInformationMessageBox(Messages.OldConfigExportCompleteSuccessMsgTitle,
							Messages.OldConfigExportCompleteSuccessMsg);
				}
				else
				{
					displayInformationMessageBox(Messages.OldConfigExportCompleteFailMsgTitle,
							Messages.OldConfigExportCompleteFailMsg);
				}
			}
			catch (IOException e)
			{
				Logger.log("Error exporting old configuration", e);
				displayInformationMessageBox(Messages.OldConfigExportCompleteFailMsgTitle,
						Messages.OldConfigExportCompleteFailMsg);
			}
		}
	}

	private void displayInformationMessageBox(String messageTitle, String message)
	{
		Display display = Display.getDefault();
		display.syncExec(() -> {
			MessageDialog.openInformation(display.getActiveShell(), messageTitle, message);
		});
	}

	private void showEimJsonStateChangeNotification()
	{
		int response = eimJsonUiChangeHandler.displayMessageToUser();
		eimJsonUiChangeHandler.handleUserResponse(response);
	}

	private void notifyMissingTools()
	{
		boolean [] userAgreed = new boolean[1];
		Display.getDefault().syncExec(() -> {
			userAgreed[0] = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
					Messages.ToolsInitializationEimMissingMsgBoxTitle,
					Messages.ToolsInitializationEimMissingMsgBoxMessage);
		});
		
		if (userAgreed[0])
		{
			// Download Launch EIM
			downloadAndLaunchEim();
		}
		else
		{
			Logger.log("User selected No to download EIM");
		}
	}

	private void downloadAndLaunchEim()
	{
		closeWelcomePage();
		Job downloadJob = new Job("Download and Launch EIM")
		{

			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				eimLoader.downloadEim(monitor);
				return Status.OK_STATUS;
			}
		};
		downloadJob.setUser(true);
		downloadJob.schedule();
	}

	private void closeWelcomePage()
	{
		Display.getDefault().asyncExec(() -> {
			IWorkbench workbench = PlatformUI.getWorkbench();
			if (workbench != null)
			{
				IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
				if (window != null)
				{
					IIntroManager introManager = workbench.getIntroManager();
					if (introManager.getIntro() != null)
					{
						introManager.closeIntro(introManager.getIntro());
					}
				}
			}
		});
	}

	private MessageConsoleStream getConsoleStream(boolean errorStream)
	{
		IDFConsole idfConsole = new IDFConsole();
		return idfConsole.getConsoleStream("EIM Launch Console", null, errorStream);
	}

	private void promptUserToOpenToolManager(EimJson eimJson)
	{
		Display.getDefault().syncExec(() -> {
			String testRunValue = System.getProperty("testRun");
			Logger.log("testRun: " + testRunValue);

			if (!StringUtil.isEmpty(testRunValue) && Boolean.parseBoolean(testRunValue))
			{
				openEspIdfManager(eimJson);
				return;
			}

			Shell shell = Display.getDefault().getActiveShell();
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
			messageBox.setText(Messages.NoActiveEspIdfInWorkspaceMsgTitle);
			messageBox.setMessage(Messages.NoActiveEspIdfInWorkspaceMsg);

			if (messageBox.open() == SWT.YES)
			{
				openEspIdfManager(eimJson);
			}
		});
	}
	
	private void promptUserToMoveEimToApplications()
	{
		Display.getDefault().asyncExec(() -> {
			MessageDialog.openInformation(
				    Display.getDefault().getActiveShell(),
				    Messages.EIMNotInApplicationsTitle, Messages.EIMNotInApplicationsMessage);
		});
	}

	private void openEspIdfManager(EimJson eimJson)
	{
		Display.getDefault().asyncExec(() -> {
			IWorkbenchWindow window = EclipseHandler.getActiveWorkbenchWindow();
			try
			{
				EimEditorInput input = new EimEditorInput(eimJson);
				input.setFirstStartup(true);
				IDE.openEditor(window.getActivePage(), input, ESPIDFManagerEditor.EDITOR_ID);
				IDFUtil.closeWelcomePage(window);
			}
			catch (PartInitException e)
			{
				Logger.log(e);
			}
		});
	}
	
	private class StartupClassDownloadEimDownloadListener implements DownloadListener
	{

		@Override
		public void onProgress(int percent)
		{
			Display.getDefault().asyncExec(() -> {
				try
				{
					int blocks = percent / 10;
					String bar = "[" + "#".repeat(blocks) + " ".repeat(10 - blocks) + "] " + percent + "%";
					standardConsoleStream.write("\r" + bar);
				}
				catch (IOException e)
				{
					Logger.log(e);
				}
			});
			
		}

		@Override
		public void onCompleted(String filePath)
		{
			Display.getDefault().syncExec(() -> {
				try
				{
					standardConsoleStream.write("\nEIM Downloaded to: " + filePath + "\nLaunching...\n");
				}
				catch (IOException e)
				{
					Logger.log(e);
				}
			});
			
			Process process = null;
			String appToLaunch = filePath;
			try
			{
				if (filePath.endsWith(".dmg"))
				{
					appToLaunch = eimLoader.installAndLaunchDmg(Paths.get(filePath));
				}
				
				idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.EIM_PATH, appToLaunch);
				process = eimLoader.launchEim(appToLaunch);
			}
			catch (IOException | InterruptedException e)
			{
				Logger.log(e);
			}
			
			eimLoader.waitForEimClosure(process, () -> {
				if (toolInitializer.isOldEspIdfConfigPresent() && !toolInitializer.isOldConfigExported())
				{
					Logger.log("Old configuration found and not converted");
					handleOldConfigExport();
				}
				eimJson = toolInitializer.loadEimJson();
				openEspIdfManager(eimJson);
			});
		}

		@Override
		public void onError(String message, Exception e)
		{
			Display.getDefault().asyncExec(() -> {
				try
				{
					errorConsoleStream.write("Download Failed: " + e.getMessage());
				}
				catch (IOException e1)
				{
					Logger.log(e1);
				}
			});
		}
	}
}
