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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.espressif.idf.core.util.StringUtil;

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
	private String gitPath;
	private Text gitLocationtext;
	private Text pythonLocationtext;
	private String commandId;

	protected DirectorySelectionDialog(Shell parentShell, String commandId, String pythonExecutablePath,
			Map<String, String> pythonVersions, String idfPath, String gitExecutablePath)
	{
		super(parentShell);
		this.shell = parentShell;
		this.pythonExecutablePath = pythonExecutablePath;
		this.pythonVersions = pythonVersions;
		this.idfDirPath = idfPath;
		this.gitPath = gitExecutablePath;
		this.commandId = commandId;
	}
	

	@Override
	protected Control createDialogArea(Composite parent)
	{
		getShell().setText(Messages.DirectorySelectionDialog_InstallTools);

		Composite composite = (Composite) super.createDialogArea(parent);
		((GridLayout) composite.getLayout()).numColumns = 3;

		// IDF_PATH
		new Label(composite, SWT.NONE).setText(Messages.DirectorySelectionDialog_IDFDirLabel);

		text = new Text(composite, SWT.BORDER);
		GridData data = new GridData();
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		text.setLayoutData(data);
		text.setText(idfDirPath != null ? idfDirPath : StringUtil.EMPTY);

		Button button = new Button(composite, SWT.PUSH);
		button.setText(Messages.DirectorySelectionDialog_Browse);
		button.addSelectionListener(new SelectionAdapter()
		{
			@Override
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

		new Label(composite, SWT.NONE).setText(Messages.DirectorySelectionDialog_GitExeLocation);

		gitLocationtext = new Text(composite, SWT.BORDER);
		data = new GridData();
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		gitLocationtext.setLayoutData(data);
		gitLocationtext.setText(gitPath != null ? gitPath : StringUtil.EMPTY);

		Button gitBrowseBtn = new Button(composite, SWT.PUSH);
		gitBrowseBtn.setText(Messages.DirectorySelectionDialog_Browse);
		gitBrowseBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				FileDialog dlg = new FileDialog(shell);
				dlg.setText(Messages.DirectorySelectionDialog_GitExecutableLocation);

				String dir = dlg.open();
				if (dir != null)
				{
					gitLocationtext.setText(dir);
				}
			}
		});

		// Python version selection
		if (Platform.OS_WIN32.equals(Platform.getOS()) && pythonVersions != null && !pythonVersions.isEmpty())
		{
			new Label(composite, SWT.NONE).setText(Messages.DirectorySelectionDialog_ChoosePyVersion);

			pythonVersionCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
			GridData gridData = new GridData(SWT.NONE, SWT.NONE, true, false, 2, 1);
			gridData.widthHint = 250;
			pythonVersionCombo.setLayoutData(gridData);

			String[] versions = pythonVersions.keySet().toArray(new String[pythonVersions.size()]);
			pythonVersionCombo.setItems(versions);
			pythonVersionCombo.select(0); // select the first one

		}
		else
		{
			new Label(composite, SWT.NONE).setText(Messages.DirectorySelectionDialog_PyExeLocation);

			pythonLocationtext = new Text(composite, SWT.BORDER);
			data = new GridData();
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
			pythonLocationtext.setLayoutData(data);
			pythonLocationtext.setText(pythonExecutablePath != null ? pythonExecutablePath : StringUtil.EMPTY);

			Button pyBrowseBtn = new Button(composite, SWT.PUSH);
			pyBrowseBtn.setText(Messages.DirectorySelectionDialog_Browse);
			pyBrowseBtn.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent event)
				{
					FileDialog dlg = new FileDialog(shell);
					dlg.setText(Messages.DirectorySelectionDialog_PyExecutableLocation);

					String dir = dlg.open();
					if (dir != null)
					{
						pythonLocationtext.setText(dir);
					}
				}
			});
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

	public String getGitExecutable()
	{
		return gitPath;
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
		else
		{
			pythonExecutablePath = pythonLocationtext.getText();
		}
		gitPath = gitLocationtext.getText();
		super.okPressed();
	}
	
	@Override
	public void create()
	{
		super.create();
		if (commandId != null && commandId.equals("com.espressif.idf.ui.command.install")) //$NON-NLS-1$
		{
			getButton(IDialogConstants.OK_ID).setText(Messages.DirectorySelectionDialog_InstallTools);
		}
		else
		{
			getButton(IDialogConstants.OK_ID).setText("Check Tools");
		}
	}
	

}
