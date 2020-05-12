package com.espressif.idf.ui.handlers;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
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
		// IDF_PATH
		String idfPath = IDFUtil.getIDFPath();

		// PATH
		IEnvironmentVariable pathEnv = new IDFEnvironmentVariables().getEnv(IDFEnvironmentVariables.PATH);
		String path = pathEnv.getValue();

		if (StringUtil.isEmpty(idfPath) || StringUtil.isEmpty(path))
		{

			MessageDialog.openError(Display.getDefault().getActiveShell(), Messages.NewProjectHandler_PathErrorTitle,
					Messages.NewProjectHandler_CouldntFindPaths + Messages.NewProjectHandler_NavigateToHelpMenu
							+ Messages.NewProjectHandler_MandatoryMsg);
			return null;
		}

		NewIDFProjectWizard newIDFProjectWizard = new NewIDFProjectWizard();
		newIDFProjectWizard.init(PlatformUI.getWorkbench(), null);

		WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), newIDFProjectWizard);
		dialog.create();
		dialog.open();

		return null;
	}

}
