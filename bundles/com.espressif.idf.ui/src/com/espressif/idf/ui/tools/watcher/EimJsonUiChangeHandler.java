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
import org.eclipse.ui.ide.IDE;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.EimIdfConfiguratinParser;
import com.espressif.idf.core.tools.exceptions.EimVersionMismatchException;
import com.espressif.idf.core.tools.watcher.EimJsonChangeListener;
import com.espressif.idf.core.tools.watcher.EimJsonStateChecker;
import com.espressif.idf.ui.EclipseUtil;
import com.espressif.idf.ui.GlobalModalLock;
import com.espressif.idf.ui.handlers.EclipseHandler;
import com.espressif.idf.ui.tools.Messages;
import com.espressif.idf.ui.tools.manager.ESPIDFManagerEditor;
import com.espressif.idf.ui.tools.manager.EimEditorInput;
import com.espressif.idf.ui.tools.manager.pages.ESPIDFMainTablePage;

/**
 * Handles {@code eim_idf.json} changes (offline on startup or live via the watcher).
 * When the user accepts the prompt, opens ESP-IDF Manager so they can review installs
 * before activating or refreshing the IDE environment.
 *
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 */
public class EimJsonUiChangeHandler implements EimJsonChangeListener
{
	private Preferences preferences;

	public EimJsonUiChangeHandler(Preferences preferences)
	{
		this.preferences = preferences;
	}

	@Override
	public void onJsonFileChanged(Path file, boolean paused)
	{
		EimJsonStateChecker checker = new EimJsonStateChecker(preferences);
		checker.updateLastSeenState();
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
				Messages.EimJsonChangedMsgTitle, Messages.EimJsonChangedMsgDetail), t -> {
					try
					{
						handleUserResponse(t);
					}
					catch (EimVersionMismatchException e)
					{
						MessageDialog.openError(EclipseUtil.getShell(), e.msgTitle(), e.getMessage());
						Logger.log(e);
					}
				});
	}

	public void handleUserResponse(Boolean response) throws EimVersionMismatchException
	{
		if (response)
		{
			try
			{
				validateConfig();
				Display.getDefault().asyncExec(this::launchEspIdfManager);
			}
			catch (IOException e)
			{
				Logger.log(e);
			}
		}

		EimJsonStateChecker checker = new EimJsonStateChecker(preferences);
		checker.updateLastSeenState();
	}

	private void validateConfig() throws IOException, EimVersionMismatchException
	{
		new EimIdfConfiguratinParser().getConfigModel(true);
	}

	private void launchEspIdfManager()
	{
		IWorkbenchWindow activeww = EclipseHandler.getActiveWorkbenchWindow();
		if (activeww == null || activeww.getActivePage() == null)
		{
			Logger.log("Cannot open ESP-IDF Manager. No active workbench window or active page.");
			return;
		}

		try
		{
			IDE.openEditor(activeww.getActivePage(), new EimEditorInput(), ESPIDFManagerEditor.EDITOR_ID, true);
		}
		catch (PartInitException e)
		{
			Logger.log("Failed to open ESP-IDF Manager Editor.");
			Logger.log(e);
			return;
		}

		ESPIDFMainTablePage page = ESPIDFManagerEditor.findOpenTablePage(activeww.getActivePage());
		if (page != null)
		{
			page.refreshEditorUI();
		}
	}
}
