/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.watcher;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.ide.IDE;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.EimIdfConfiguratinParser;
import com.espressif.idf.core.tools.SetupToolsInIde;
import com.espressif.idf.core.tools.vo.EimJson;
import com.espressif.idf.core.tools.watcher.EimJsonChangeListener;
import com.espressif.idf.core.tools.watcher.EimJsonStateChecker;
import com.espressif.idf.ui.EclipseUtil;
import com.espressif.idf.ui.GlobalModalLock;
import com.espressif.idf.ui.IDFConsole;
import com.espressif.idf.ui.handlers.EclipseHandler;
import com.espressif.idf.ui.tools.Messages;
import com.espressif.idf.ui.tools.SetupToolsJobListener;
import com.espressif.idf.ui.tools.manager.ESPIDFManagerEditor;
import com.espressif.idf.ui.tools.manager.EimEditorInput;
import com.espressif.idf.ui.tools.manager.pages.ESPIDFMainTablePage;

/**
 * eim_idf.json file ui change handler to notify user for changes.
 * 
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class EimJsonUiChangeHandler implements EimJsonChangeListener
{
	private Preferences preferences;
	private EimJson eimJson;

	public EimJsonUiChangeHandler(Preferences preferences)
	{
		this.preferences = preferences;
	}

	@Override
	public void onJsonFileChanged(Path file, boolean paused)
	{
		if (paused)
		{
			Logger.log("Listener is paused");
			return;
		}
		displayMessageToUser();
	}

	public void displayMessageToUser()
	{
		GlobalModalLock.showModal(() -> MessageDialog.openQuestion(EclipseUtil.getShell(),
				Messages.EimJsonChangedMsgTitle, Messages.EimJsonChangedMsgDetail), this::handleUserResponse);
	}

	public void handleUserResponse(Boolean response)
	{
		if (response)
		{
			try
			{
				loadEimJson();
				if (eimJson.getIdfInstalled().size() == 1)
				{
					// only one entry in eimJson so we can simply refresh the IDE environment with that.
					setupToolsInIde();
				}
				else
				{
					// multiple entries in json so launch manager for user to handle this
					Display.getDefault().asyncExec(() -> {
						try
						{
							launchEspIdfManager();
						}
						catch (PartInitException e)
						{
							Logger.log(e);
						}
						ESPIDFMainTablePage.getInstance(eimJson).refreshEditorUI();
					});
				}
			}
			catch (IOException e)
			{
				Logger.log(e);
			}
		}

		EimJsonStateChecker checker = new EimJsonStateChecker(preferences);
		checker.updateLastSeenTimestamp();
	}

	private void loadEimJson() throws IOException
	{
		EimIdfConfiguratinParser eimIdfConfiguratinParser = new EimIdfConfiguratinParser();
		eimJson = eimIdfConfiguratinParser.getEimJson(true);
	}

	private void setupToolsInIde()
	{
		SetupToolsInIde setupToolsInIde = new SetupToolsInIde(eimJson.getIdfInstalled().get(0), eimJson,
				getConsoleStream(true), getConsoleStream(false));
		SetupToolsJobListener toolsActivationJobListener = new SetupToolsJobListener(
				ESPIDFMainTablePage.getInstance(eimJson), setupToolsInIde);
		setupToolsInIde.addJobChangeListener(toolsActivationJobListener);
		setupToolsInIde.schedule();
	}

	private void launchEspIdfManager() throws PartInitException
	{
		Display.getDefault().asyncExec(() -> {
			IWorkbenchWindow activeww = EclipseHandler.getActiveWorkbenchWindow();
			if (activeww == null || activeww.getActivePage() == null)
			{
				Logger.log("Cannot open ESP-IDF Manager. No active workbench window or active page.");
				return;
			}

			try
			{
				IDE.openEditor(activeww.getActivePage(), new EimEditorInput(eimJson), ESPIDFManagerEditor.EDITOR_ID,
						true);
			}
			catch (PartInitException e)
			{
				Logger.log("Failed to open ESP-IDF Manager Editor.");
				Logger.log(e);
			}
		});

	}

	private MessageConsoleStream getConsoleStream(boolean errorStream)
	{
		IDFConsole idfConsole = new IDFConsole();
		return idfConsole.getConsoleStream(Messages.IDFToolsHandler_ToolsManagerConsole, null, errorStream, true);
	}
}
