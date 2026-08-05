/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.wizard;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.tools.ManageEspIdfVersionsHandler;

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

		Link link = new Link(container, SWT.WRAP);
		link.setText(Messages.ToolsMissingWizardPage_MainText);
		link.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				openEspIdfManagerAndCloseWizard();
			}
		});

		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		link.setLayoutData(gd);

		setControl(container);

		setPageComplete(false);
	}

	private void openEspIdfManagerAndCloseWizard()
	{
		try
		{
			new ManageEspIdfVersionsHandler().execute(null);
		}
		catch (ExecutionException ex)
		{
			Logger.log(ex);
		}
		getShell().close();
	}
}
