/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.osgi.service.prefs.Preferences;

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
			notifyMissingTools();
			return;
		}

		EimJson eimJson = toolInitializer.loadEimJson();
		if (eimJson == null)
		{
			return;
		}

		if (!toolInitializer.isEspIdfSet())
		{
			promptUserToOpenToolManager(eimJson);
		}

		if (stateChecker.wasModifiedSinceLastRun())
		{
			showEimJsonStateChangeNotification();
		}
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
