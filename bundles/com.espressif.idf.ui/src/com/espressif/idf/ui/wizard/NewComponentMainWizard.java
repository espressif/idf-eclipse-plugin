/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/


package com.espressif.idf.ui.wizard;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.handlers.NewComponentHandler;
import com.espressif.idf.ui.handlers.NewProjectHandlerUtil;

public class NewComponentMainWizard extends Wizard implements INewWizard
{

	public NewComponentMainWizard()
	{
		if (NewProjectHandlerUtil.installToolsCheck())
		{
			Display.getDefault().asyncExec(new Runnable()
			{
				@Override
				public void run()
				{
					NewComponentHandler handler = new NewComponentHandler();
					try
					{
						handler.execute(null);
					}
					catch (ExecutionException e)
					{
						Logger.log(e);
					}
				}
			});
		}
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection)
	{
	}

	@Override
	public boolean performFinish()
	{
		return true;
	}

}
