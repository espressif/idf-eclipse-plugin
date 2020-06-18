/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.install;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFDownloadHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		IDFDownloadWizard wizard = new IDFDownloadWizard();
		wizard.setWindowTitle("ESP-IDF Configuration");

		WizardDialog wizDialog = new WizardDialog(window.getShell(), wizard);
		wizDialog.create();

		wizDialog.setTitle("ESP-IDF Configuration");
		wizDialog.setMessage("Configure and Install ESP-IDF");
		wizDialog.getShell().setSize(850, 500);
		wizDialog.open();

		return null;
	}
}
