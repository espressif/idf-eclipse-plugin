/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.ui.tools.wizard.ToolsManagerWizard;
import com.espressif.idf.ui.tools.wizard.pages.ManageToolsInstallationWizardPage;

/**
 * Tools Installation Handler for the menu command
 * 
 * @author Ali Azam Rana
 *
 */
public class ManageToolsInstallationHandler extends AbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		ToolsManagerWizard toolsManagerWizard = new ToolsManagerWizard();
		ToolsManagerWizardDialog wizardDialog = new ToolsManagerWizardDialog(
				PlatformUI.getWorkbench().getDisplay().getActiveShell(), toolsManagerWizard);
		toolsManagerWizard.setParentWizardDialog(wizardDialog);
		wizardDialog.open();
		return null;
	}

	private class ToolsManagerWizardDialog extends WizardDialog
	{

		public ToolsManagerWizardDialog(Shell parentShell, IWizard newWizard)
		{
			super(parentShell, newWizard);
		}

		@Override
		public void showPage(IWizardPage page)
		{
			super.showPage(page);
			updateSize();
		}
		
		@Override
		protected void cancelPressed()
		{
			if (getCurrentPage() instanceof ManageToolsInstallationWizardPage)
			{
				// Cancel all the jobs here and notify user that the tools installation jobs were cancelled
			}
			super.cancelPressed();
		}
	}

}
