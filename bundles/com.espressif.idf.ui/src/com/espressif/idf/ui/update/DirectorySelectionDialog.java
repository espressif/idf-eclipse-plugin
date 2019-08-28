/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.update;

import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.aptana.core.util.StringUtil;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class DirectorySelectionDialog extends Dialog
{

	private Shell shell;
	private Text text;
	private String idfDirPath;
	private String pythonExecutablePath;
	private Combo pythonVersionCombo;
	private Map<String, String> pythonVersions;

	protected DirectorySelectionDialog(Shell parentShell, Map<String, String> pythonVersions, String idfPath)
	{
		super(parentShell);
		this.shell = parentShell;
		this.pythonVersions = pythonVersions;
		this.idfDirPath = idfPath;
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite) super.createDialogArea(parent);
		((GridLayout) composite.getLayout()).numColumns = 6;

		// IDF_PATH
		new Label(composite, SWT.NONE).setText(Messages.DirectorySelectionDialog_IDFDirLabel);

		text = new Text(composite, SWT.BORDER);
		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		data.horizontalSpan = 4;
		text.setLayoutData(data);
		text.setText(idfDirPath != null ? idfDirPath : StringUtil.EMPTY);

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

		// Python version selection
		if (Platform.OS_WIN32.equals(Platform.getOS()))
		{
			new Label(composite, SWT.NONE).setText("Choose Python version:");

			pythonVersionCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
			pythonVersionCombo.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, 5, 1));

			GridData gridData = new GridData();
			gridData.widthHint = 250;
			pythonVersionCombo.setLayoutData(gridData);

			if (!pythonVersions.isEmpty())
			{
				String[] versions = pythonVersions.keySet().toArray(new String[pythonVersions.size()]);
				pythonVersionCombo.setItems(versions);
				pythonVersionCombo.select(0); // select the first one
			}

		}

		return composite;
	}

	public String getIDFDirectory()
	{
		return idfDirPath;
	}

	public String getPythonExecutable()
	{
		return pythonExecutablePath;
	}

	@Override
	protected void okPressed()
	{
		idfDirPath = text.getText();
		if (pythonVersionCombo != null)
		{
			String version = pythonVersionCombo.getText();
			pythonExecutablePath = pythonVersions.getOrDefault(version, null);
		}
		super.okPressed();
	}

}
