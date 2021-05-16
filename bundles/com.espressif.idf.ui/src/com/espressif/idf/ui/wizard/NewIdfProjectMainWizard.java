/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.ui.handlers.NewProjectHandlerUtil;

public class NewIdfProjectMainWizard extends Wizard implements INewWizard
{

	public NewIdfProjectMainWizard()
	{
		if (NewProjectHandlerUtil.installToolsCheck())
		{
			NewIDFProjectWizard newIDFProjectWizard = new NewIDFProjectWizard();
			newIDFProjectWizard.init(PlatformUI.getWorkbench(), null);

			WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), newIDFProjectWizard);

			dialog.create();
			dialog.open();
		}
	}

	@Override
	public boolean performFinish()
	{
		return false;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection)
	{
	}

}
