/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.templates;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.wizard.WizardSelectionPage;
import org.eclipse.pde.internal.ui.parts.FormBrowser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Creates a base wizard for creating a templates for new project creation
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 */
@SuppressWarnings("restriction")
public abstract class BaseWizardSelectionPage extends WizardSelectionPage implements ISelectionChangedListener
{
	private String label;
	private FormBrowser descriptionBrowser;

	public BaseWizardSelectionPage(String name, String label)
	{
		super(name);
		this.label = label;
		descriptionBrowser = new FormBrowser(SWT.BORDER | SWT.V_SCROLL);
		descriptionBrowser.setText(""); //$NON-NLS-1$
	}

	public void createDescriptionIn(Composite composite)
	{
		descriptionBrowser.createControl(composite);
		Control c = descriptionBrowser.getControl();
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 200;
		c.setLayoutData(gd);
	}

	public String getLabel()
	{
		return label;
	}

	public void setDescriptionText(String text)
	{
		if (text == null)
			text = Messages.BaseWizardSelectionPage_No_Desc;
		descriptionBrowser.setText(text);
	}

	public void setDescriptionEnabled(boolean enabled)
	{
		Control dcontrol = descriptionBrowser.getControl();
		if (dcontrol != null)
			dcontrol.setEnabled(enabled);
	}
}
