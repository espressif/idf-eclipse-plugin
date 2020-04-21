package com.espressif.idf.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.ui.wizard.NewIDFProjectWizard;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class NewProjectHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		NewIDFProjectWizard newIDFProjectWizard = new NewIDFProjectWizard();
		newIDFProjectWizard.init(PlatformUI.getWorkbench(), null);
		
		WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), newIDFProjectWizard);
		dialog.create();
		dialog.open();
		
		return null;
	}

}
