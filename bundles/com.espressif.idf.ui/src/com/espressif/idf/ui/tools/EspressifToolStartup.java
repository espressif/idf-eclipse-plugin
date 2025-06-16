/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
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
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.dialogs.MessageLinkDialog;
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
		
		if (toolInitializer.isOldEspIdfConfigPresent()
				&& !toolInitializer.isOldConfigExported())
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
			MessageLinkDialog.openWarning(Display.getDefault().getActiveShell(),
					Messages.ToolsInitializationEimMissingMsgBoxTitle,
					MessageFormat.format(Messages.ToolsInitializationEimMissingMsgBoxMessage, EimConstants.EIM_URL));
		});
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
