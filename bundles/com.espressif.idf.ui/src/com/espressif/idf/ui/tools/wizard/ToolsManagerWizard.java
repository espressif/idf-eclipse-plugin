/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.wizard;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;

import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.tools.Messages;
import com.espressif.idf.ui.tools.wizard.pages.InstallEspIdfPage;
import com.espressif.idf.ui.tools.wizard.pages.InstallPreRquisitePage;
import com.espressif.idf.ui.tools.wizard.pages.ManageToolsInstallationWizardPage;

/**
 * Tools Manager wizard
 * 
 * @author Ali Azam Rana
 *
 */
public class ToolsManagerWizard extends Wizard
{
	private InstallPreRquisitePage installPreRquisitePage;
	private InstallEspIdfPage installEspIdfPage;
	private ManageToolsInstallationWizardPage manageToolsInstallationPage;
	private WizardDialog parentWizardDialog;

	public ToolsManagerWizard()
	{
		super();
		setDefaultPageImageDescriptor(UIPlugin.getImageDescriptor(IToolsInstallationWizardConstants.ESPRESSIF_LOGO));
		setNeedsProgressMonitor(true);

	}

	@Override
	public String getWindowTitle()
	{
		return Messages.ToolsManagerWizard;
	}

	@Override
	public void addPages()
	{
		installPreRquisitePage = new InstallPreRquisitePage();
		installEspIdfPage = new InstallEspIdfPage();
		manageToolsInstallationPage = new ManageToolsInstallationWizardPage(parentWizardDialog);
		addPage(installPreRquisitePage);
		addPage(installEspIdfPage);
		addPage(manageToolsInstallationPage);
	}

	@Override
	public void createPageControls(Composite pageContainer)
	{
		// the default behavior is to create all the pages controls
		for (IWizardPage page : getPages())
		{
			if (page instanceof ManageToolsInstallationWizardPage)
			{
				((ManageToolsInstallationWizardPage) page).setPageComposite(pageContainer);
				continue;
			}
			page.createControl(pageContainer);
			// page is responsible for ensuring the created control is
			// accessible
			// via getControl.
			Assert.isNotNull(page.getControl(),
					"getControl() of wizard page returns null. Did you call setControl() in your wizard page?"); //$NON-NLS-1$
		}
	}

	@Override
	public boolean performFinish()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public WizardDialog getParentWizardDialog()
	{
		return parentWizardDialog;
	}

	public void setParentWizardDialog(WizardDialog parentWizardDialog)
	{
		this.parentWizardDialog = parentWizardDialog;
	}
}
