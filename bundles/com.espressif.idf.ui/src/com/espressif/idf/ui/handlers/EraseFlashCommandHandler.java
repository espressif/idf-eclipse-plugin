/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.ui.dialogs.EraseFlashDialog;

/**
 * Erase flash command handler
 * 
 * @author Ali Azam Rana
 *
 */
public class EraseFlashCommandHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		EraseFlashDialog eraseFlashDialog = new EraseFlashDialog(activeShell);
		eraseFlashDialog.create();
		eraseFlashDialog.open();

		return eraseFlashDialog;
	}

}
