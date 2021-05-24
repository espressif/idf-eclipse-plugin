package com.espressif.idf.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.ui.wizard.NewComponentWizard;

public class NewComponentHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		if (!NewProjectHandlerUtil.installToolsCheck())
		{
			return null;
		}
		NewComponentWizard newComponentWizard = new NewComponentWizard();
		newComponentWizard.init(PlatformUI.getWorkbench(), null);

		WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), newComponentWizard);
		dialog.create();
		dialog.open();
		return null;
	}

}
