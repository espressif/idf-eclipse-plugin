/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
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
import com.espressif.idf.core.tools.EimConstants;
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
import com.espressif.idf.ui.tools.manager.pages.ESPIDFMainTablePage;
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

	@Override
	public void earlyStartup()
	{
		Preferences preferences = org.eclipse.core.runtime.preferences.InstanceScope.INSTANCE
				.getNode(UIPlugin.PLUGIN_ID);
		ToolInitializer toolInitializer = new ToolInitializer(preferences);
		EimJsonStateChecker stateChecker = new EimJsonStateChecker(preferences);
		eimJsonUiChangeHandler = new EimJsonUiChangeHandler(preferences);
		stateChecker.updateLastSeenTimestamp();
		EimJsonWatchService.getInstance().addEimJsonChangeListener(eimJsonUiChangeHandler);

		if (!toolInitializer.isEimInstalled())
		{
			Logger.log("EIM not installed");
			notifyMissingTools();
			return;
		}

		if (toolInitializer.isOldEspIdfConfigPresent() && !toolInitializer.isOldConfigExported())
		{
			Logger.log("Old configuration found and not converted");
			handleOldConfigExport();
		}

		EimJson eimJson = toolInitializer.loadEimJson();
		if (eimJson == null)
		{
			return;
		}

		// Set EimPath on every startup to ensure proper path in configurations
		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
		idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.EIM_PATH, eimJson.getEimPath());

		if (!toolInitializer.isEspIdfSet())
		{
			promptUserToOpenToolManager(eimJson);
		}

		if (stateChecker.wasModifiedSinceLastRun())
		{
			showEimJsonStateChangeNotification();
		}
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
				IStatus status = toolInitializer.exportOldConfig();
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
		Display.getDefault().asyncExec(() -> {
			boolean userAgreed = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
					Messages.ToolsInitializationEimMissingMsgBoxTitle,
					Messages.ToolsInitializationEimMissingMsgBoxMessage);
			if (userAgreed)
			{
				// Download Launch EIM
				downloadAndLaunchEim();
			}
			else
			{
				Logger.log("User selected No to download EIM");
			}
		});
	}

	private void downloadAndLaunchEim()
	{
		closeWelcomePage();
		Event event = new Event();
		event.widget = new Label(Display.getDefault().getActiveShell(), 0);
		SelectionEvent simulatedEvent = new SelectionEvent(event);
		EimButtonLaunchListener eimButtonLaunchListener = new EimButtonLaunchListener(
				ESPIDFMainTablePage.getInstance(null), Display.getDefault(), getConsoleStream(false),
				getConsoleStream(true));
		eimButtonLaunchListener.widgetSelected(simulatedEvent);
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

	private void openEspIdfManager(EimJson eimJson)
	{
		IWorkbenchWindow window = EclipseHandler.getActiveWorkbenchWindow();
		IDFUtil.closeWelcomePage(window);
		try
		{
			EimEditorInput input = new EimEditorInput(eimJson);
			input.setFirstStartup(true);
			IDE.openEditor(window.getActivePage(), input, ESPIDFManagerEditor.EDITOR_ID);
		}
		catch (PartInitException e)
		{
			Logger.log(e);
		}
	}
}
