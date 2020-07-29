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
		wizard.setWindowTitle(Messages.IDFDownloadHandler_ESPIDFConfiguration);

		WizardDialog wizDialog = new WizardDialog(window.getShell(), wizard);
		wizDialog.create();

		wizDialog.setTitle(Messages.IDFDownloadHandler_DownloadPage_Title);
		wizDialog.setMessage(Messages.IDFDownloadHandler_DownloadPageMsg);
		wizDialog.getShell().setSize(Math.max(850,wizDialog.getShell().getSize().x), 500);
		
		wizDialog.open();

		return null;
	}
}
