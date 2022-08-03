/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.wizard;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.tools.Messages;
import com.espressif.idf.ui.tools.wizard.pages.IToolsWizardPage;
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
	private Map<String, String> existingVarMap;
	private boolean exisitngInstallPreferencesStatus;

	public ToolsManagerWizard(Map<String, String> existingVarMap, boolean exisitngInstallPreferencesStatus)
	{
		super();
		setNeedsProgressMonitor(true);
		scopedPreferenceStore = InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID);
		scopedPreferenceStore.putBoolean(IToolsInstallationWizardConstants.INSTALL_TOOLS_FLAG, false);
		this.existingVarMap = existingVarMap;
		this.exisitngInstallPreferencesStatus = exisitngInstallPreferencesStatus;
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
	
	@Override
	public boolean performCancel() 
	{
		IToolsWizardPage toolsWizardPage = (IToolsWizardPage) parentWizardDialog.getCurrentPage();
		toolsWizardPage.cancel();
		restoreOldConfigsAndVars();
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
	
	private void restoreOldConfigsAndVars()
	{
		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
		
		for (Entry<String, String> varsEntry : existingVarMap.entrySet())
		{
			idfEnvironmentVariables.addEnvVariable(varsEntry.getKey(), varsEntry.getValue());
		}
		
		// we also need to remove any vars that were added and were not part of the original existing var map
		Set<String> existingVars = existingVarMap.keySet();
		Set<String> addedVars = idfEnvironmentVariables.getSystemEnvMap().keySet();
		for(String addedVar : addedVars)
		{
			if (!existingVars.contains(addedVar))
			{
				idfEnvironmentVariables.removeEnvVariable(addedVar);
			}
		}
		
		InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID).putBoolean(IToolsInstallationWizardConstants.INSTALL_TOOLS_FLAG, exisitngInstallPreferencesStatus);
	}
}
