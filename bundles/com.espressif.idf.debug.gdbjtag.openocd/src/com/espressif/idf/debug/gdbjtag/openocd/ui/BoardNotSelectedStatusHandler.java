/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.ui.target.ILaunchTargetUIManager;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.debug.gdbjtag.openocd.Activator;

/**
 * Shows a clear, actionable dialog when a debug session is started without a board selected for the active launch
 * target. Confirming the dialog opens the launch target editor so the user can select a board.
 */
public class BoardNotSelectedStatusHandler implements IStatusHandler
{
	@Override
	public Object handleStatus(IStatus status, Object source) throws CoreException
	{
		Display.getDefault().asyncExec(() -> {
			boolean isYes = MessageDialog.openConfirm(Display.getDefault().getActiveShell(),
					Messages.BoardNotSelectedDialog_title, Messages.BoardNotSelectedDialog_message);
			if (isYes)
			{
				editActiveLaunchTarget();
			}
		});
		return null;
	}

	private void editActiveLaunchTarget()
	{
		try
		{
			ILaunchBarManager launchBarManager = Activator.getService(ILaunchBarManager.class);
			ILaunchTargetUIManager targetUIManager = Activator.getService(ILaunchTargetUIManager.class);
			if (launchBarManager == null || targetUIManager == null)
			{
				return;
			}
			ILaunchTarget target = launchBarManager.getActiveLaunchTarget();
			if (target != null)
			{
				targetUIManager.editLaunchTarget(target);
			}
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}
}
