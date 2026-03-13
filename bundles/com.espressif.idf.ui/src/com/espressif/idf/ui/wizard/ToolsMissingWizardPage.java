/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ToolsMissingWizardPage extends WizardPage
{


	protected ToolsMissingWizardPage(String errorMsg)
	{
		super(Messages.ToolsMissingWizardPage_PagaName);
		setTitle(Messages.ToolsMissingWizardPage_Title);
		setErrorMessage(errorMsg);
	}

	@Override
	public void createControl(Composite parent)
	{
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));


		Label label = new Label(container, SWT.WRAP);
		label.setText(Messages.ToolsMissingWizardPage_MainText);

		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		label.setLayoutData(gd);

		setControl(container);

		setPageComplete(false);
	}
}
