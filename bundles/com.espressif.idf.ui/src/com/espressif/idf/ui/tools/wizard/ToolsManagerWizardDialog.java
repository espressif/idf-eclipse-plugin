/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.wizard;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.ui.tools.Messages;
import com.espressif.idf.ui.tools.wizard.pages.IToolsWizardPage;

/**
 * Tools manager wizard dialog class to extend some basic methods
 * 
 * @author Ali Azam Rana
 *
 */
public class ToolsManagerWizardDialog extends WizardDialog
{
	private Map<String, String> existingVarMap;
	
	public ToolsManagerWizardDialog(Shell parentShell, IWizard newWizard, Map<String, String> existingVarMap)
	{
		super(parentShell, newWizard);
		this.existingVarMap = existingVarMap;
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
		MessageBox messageBox = new MessageBox(getParentShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
		messageBox.setText(Messages.BtnCancel);
		messageBox.setMessage(Messages.CancelMsg);
		int resp = messageBox.open();
		if (resp == SWT.YES && getCurrentPage() instanceof IToolsWizardPage)
		{
			IToolsWizardPage toolsWizardPage = (IToolsWizardPage) getCurrentPage();
			toolsWizardPage.cancel();
			restoreOldVars();
			super.cancelPressed();
		}
	}
	
	private void restoreOldVars()
	{
		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
		for (Entry<String, String> varsEntry : existingVarMap.entrySet())
		{
			idfEnvironmentVariables.addEnvVariable(varsEntry.getKey(), varsEntry.getValue());
		}
	}
}
