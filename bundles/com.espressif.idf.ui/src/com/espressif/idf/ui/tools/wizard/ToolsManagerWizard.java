/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.wizard;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.osgi.service.prefs.Preferences;

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
	private ToolsManagerWizardDialog parentWizardDialog;
	private Preferences scopedPreferenceStore;

	public ToolsManagerWizard()
	{
		super();
		setDefaultPageImageDescriptor(UIPlugin.getImageDescriptor(IToolsInstallationWizardConstants.ESPRESSIF_LOGO));
		setNeedsProgressMonitor(true);
		scopedPreferenceStore = InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID);
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
				parentWizardDialog.getButton(IDialogConstants.FINISH_ID);
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
	public boolean canFinish()
	{
		return super.canFinish()
				&& scopedPreferenceStore.getBoolean(IToolsInstallationWizardConstants.INSTALL_TOOLS_FLAG, false);
	}

	@Override
	public boolean performFinish()
	{
		return true;
	}
	
	public void open()
	{
		parentWizardDialog.open();
	}

	public ToolsManagerWizardDialog getParentWizardDialog()
	{
		return parentWizardDialog;
	}

	public void setParentWizardDialog(ToolsManagerWizardDialog parentWizardDialog)
	{
		this.parentWizardDialog = parentWizardDialog;
	}
}
