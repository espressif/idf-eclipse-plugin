/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.update;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class DirectorySelectionDialog extends Dialog
{

	private Shell shell;
	private Text text;
	private String idfDirPath;

	protected DirectorySelectionDialog(Shell parentShell)
	{
		super(parentShell);
		this.shell = parentShell;
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite) super.createDialogArea(parent);
		((GridLayout) composite.getLayout()).numColumns = 6;

		new Label(composite, SWT.NONE).setText(Messages.DirectorySelectionDialog_IDFDirLabel);

		text = new Text(composite, SWT.BORDER);
		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		data.horizontalSpan = 4;
		text.setLayoutData(data);

		Button button = new Button(composite, SWT.PUSH);
		button.setText(Messages.DirectorySelectionDialog_Browse);
		button.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				DirectoryDialog dlg = new DirectoryDialog(shell);
				dlg.setFilterPath(text.getText());
				dlg.setText(Messages.DirectorySelectionDialog_IDFDirLabel);
				dlg.setMessage(Messages.DirectorySelectionDialog_SelectIDFDirMessage);

				String dir = dlg.open();
				if (dir != null)
				{
					text.setText(dir);
				}
			}
		});

		return composite;
	}

	public String getValue()
	{
		return idfDirPath;
	}

	@Override
	protected void okPressed()
	{
		idfDirPath = text.getText();
		super.okPressed();
	}

}
