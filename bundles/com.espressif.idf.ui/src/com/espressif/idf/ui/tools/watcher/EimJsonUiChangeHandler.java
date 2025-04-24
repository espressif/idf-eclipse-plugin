/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.watcher;

import java.nio.file.Path;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.tools.watcher.EimJsonChangeListener;
import com.espressif.idf.core.tools.watcher.EimJsonStateChecker;
import com.espressif.idf.ui.tools.Messages;

/**
 * eim_idf.json file ui change handler to notify user for changes.
 * 
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class EimJsonUiChangeHandler implements EimJsonChangeListener
{
	private Preferences preferences;

	public EimJsonUiChangeHandler(Preferences preferences)
	{
		this.preferences = preferences;
	}

	@Override
	public void onJsonFileChanged(Path file)
	{
		Display.getDefault().asyncExec(() -> {
			MessageDialog.openWarning(Display.getDefault().getActiveShell(), Messages.EimJsonChangedMsgTitle,
					Messages.EimJsonChangedMsgDetail);
		});

		EimJsonStateChecker checker = new EimJsonStateChecker(preferences);
		checker.updateLastSeenTimestamp();
	}

}
