package com.espressif.idf.ui.wizard;

import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

import com.espressif.idf.core.logging.Logger;

@SuppressWarnings("restriction")
public class NewComponentWizard extends Wizard implements INewWizard
{

	private IStructuredSelection selection;
	private NewComponentWizardPage mainPage;
	@SuppressWarnings("unused")
	private IWorkbench workbench;

	public NewComponentWizard()
	{

		setWindowTitle(Messages.NewIdfComponentWizard_Component_Title);

	}

	@Override
	public void addPages()
	{
		mainPage = new NewComponentWizardPage(selection);
		addPage(mainPage);
	}

	@Override
	public boolean performFinish()
	{
		String message = mainPage.createIdfComponent();
		try
		{
			IDEWorkbenchPlugin.getPluginWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
			MessageDialog.openInformation(null, Messages.NewIdfComponentWizard_Component_Title, message);
			triggerResourceChanges();
			IDEWorkbenchPlugin.getPluginWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return true;
	}

	private void triggerResourceChanges() 
	{
		try
		{
			FileWriter fr = new FileWriter(mainPage.getCreatedComponentPath() + "/CMakeLists.txt", true); //$NON-NLS-1$
			fr.write("\n"); //$NON-NLS-1$
			fr.flush();
			fr.close();
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection)
	{
		this.workbench = workbench;
		this.selection = selection;
	}
}
