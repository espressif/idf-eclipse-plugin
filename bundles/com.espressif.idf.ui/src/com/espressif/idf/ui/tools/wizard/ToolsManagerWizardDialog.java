/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.wizard;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.tools.Messages;
import com.espressif.idf.ui.tools.wizard.pages.IToolsWizardPage;
import com.espressif.idf.ui.tools.wizard.pages.ManageToolsInstallationWizardPage;

/**
 * Tools manager wizard dialog class to extend some basic methods
 * 
 * @author Ali Azam Rana
 *
 */
public class ToolsManagerWizardDialog extends WizardDialog
{
	private Map<String, String> existingVarMap;
	private Map<IWizardPage, Point> wizardPagePointMap;
	private boolean exisitngInstallPreferencesStatus;

	public ToolsManagerWizardDialog(Shell parentShell, IWizard newWizard, Map<String, String> existingVarMap, boolean exisitngInstallPreferencesStatus)
	{
		super(parentShell, newWizard);
		this.existingVarMap = existingVarMap;
		wizardPagePointMap = new HashMap<IWizardPage, Point>();
		this.exisitngInstallPreferencesStatus = exisitngInstallPreferencesStatus;
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
		IToolsWizardPage toolsWizardPage = (IToolsWizardPage) getCurrentPage();
		toolsWizardPage.cancel();
		restoreOldConfigsAndVars();
		super.cancelPressed();
	}

	/**
	 * The Back button has been pressed.
	 */
	@Override
	protected void backPressed()
	{
		IWizardPage currentPage = getCurrentPage();
		if (currentPage instanceof ManageToolsInstallationWizardPage)
		{
			((ManageToolsInstallationWizardPage) currentPage).restoreFinishButton();
		}
		super.backPressed();
		currentPage = getCurrentPage();
		getShell().setSize(wizardPagePointMap.get(currentPage));
		updateSize();
	}

	@Override
	public void updateButtons()
	{
		super.updateButtons();
		if (getCurrentPage() instanceof ManageToolsInstallationWizardPage)
		{
			((ManageToolsInstallationWizardPage) getCurrentPage()).setButtonsEnabled(true);
		}
	}

	@Override
	public void finishPressed()
	{
		super.finishPressed();
	}
	
	@Override
	protected void nextPressed() 
	{
		if (getCurrentPage() instanceof IToolsWizardPage)
		{
			wizardPagePointMap.put(getCurrentPage(), getShell().getSize());
		}
		super.nextPressed();
	}


	public Button getButton(int id)
	{
		return super.getButton(id);
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
