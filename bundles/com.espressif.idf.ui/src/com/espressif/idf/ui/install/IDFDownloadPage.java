/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.install;

import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.UIPlugin;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFDownloadPage extends WizardPage
{

	private Combo versionCombo;
	private Map<String, IDFVersion> versionsMap;
	private Text directoryTxt;
	private Button fileSystemBtn;
	private Text existingIdfDirTxt;
	private Button browseBtn;

	protected IDFDownloadPage(String pageName)
	{
		super(pageName);
		setImageDescriptor(UIPlugin.getImageDescriptor("icons/espressif_logo.png"));
	}

	@Override
	public void createControl(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NULL);

		initializeDialogUnits(parent);

		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createExistingComposite(composite);

		// esp-idf version selection group
		Group versionGrp = new Group(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		versionGrp.setLayout(layout);
		versionGrp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		versionGrp.setText("Download and Install ESP-IDF");
		versionGrp.setFont(parent.getFont());

		Label versionLbl = new Label(versionGrp, SWT.NONE);
		versionLbl.setText("Select ESP-IDF version:");

		versionCombo = new Combo(versionGrp, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gridData = new GridData(SWT.NONE, SWT.NONE, false, false, 2, 1);
		gridData.widthHint = 250;
		versionCombo.setLayoutData(gridData);

		versionsMap = new IDFVersionsReader().getVersionsMap();
		Set<String> keySet = versionsMap.keySet();
		versionCombo.setItems(keySet.toArray(new String[keySet.size()]));
		if (keySet.size() > 0)
		{
			versionCombo.select(0);
		}

		createDownloadComposite(versionGrp);

		Label noteLbl = new Label(composite, SWT.NONE);
		noteLbl.setText(
				"Note: The configured ESP-IDF directory will set as IDF_PATH in the CDT Build environment (Preferences > C/C++ > Build > Environment)");

		gridData = new GridData(SWT.LEFT, SWT.NONE, true, false, 1, 1);
		gridData.verticalIndent = 10;
		noteLbl.setLayoutData(gridData);

		setControl(composite);
		setPageComplete(false);
	}

	private void createExistingComposite(Composite parent)
	{
		// File system selection
		fileSystemBtn = new Button(parent, SWT.CHECK);
		fileSystemBtn.setText("Choose an existing ESP-IDF from file system");
		GridData gridData2 = new GridData(SWT.NONE, SWT.NONE, false, false, 1, 1);
		gridData2.verticalIndent = 10;
		fileSystemBtn.setLayoutData(gridData2);

		Group composite = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setFont(parent.getFont());

		Label label = new Label(composite, SWT.NONE);
		label.setText("ESP-IDF directory location:");

		existingIdfDirTxt = new Text(composite, SWT.BORDER);
		existingIdfDirTxt.setEnabled(false);
		GridData data = new GridData();
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		existingIdfDirTxt.setLayoutData(data);
		existingIdfDirTxt.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				validate();
			}
		});

		Button existingBrowseBtn = new Button(composite, SWT.PUSH);
		existingBrowseBtn.setText("Browse...");
		existingBrowseBtn.setEnabled(false);
		existingBrowseBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				DirectoryDialog dlg = new DirectoryDialog(getShell());
				dlg.setFilterPath(existingIdfDirTxt.getText());
				dlg.setText("ESP-IDF Directory:");
				dlg.setMessage("Select ESP-IDF Directory:");

				String dir = dlg.open();
				if (dir != null)
				{
					existingIdfDirTxt.setText(dir);
				}
			}
		});

		fileSystemBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (fileSystemBtn.getSelection())
				{
					existingIdfDirTxt.setEnabled(true);
					existingBrowseBtn.setEnabled(true);

					versionCombo.setEnabled(false);
					directoryTxt.setEnabled(false);
					browseBtn.setEnabled(false);

				}
				else
				{
					existingIdfDirTxt.setEnabled(false);
					existingBrowseBtn.setEnabled(false);

					versionCombo.setEnabled(true);
					directoryTxt.setEnabled(true);
					browseBtn.setEnabled(true);

				}
				validate();
			}
		});

	}

	private void createDownloadComposite(Composite composite)
	{
		Label descLbl = new Label(composite, SWT.NONE);
		descLbl.setText("Directory to install ESP-IDF:");

		directoryTxt = new Text(composite, SWT.BORDER);
		GridData data = new GridData();
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		directoryTxt.setLayoutData(data);
		directoryTxt.setFocus();
		directoryTxt.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				validate();
			}
		});

		browseBtn = new Button(composite, SWT.PUSH);
		browseBtn.setText("Browse...");
		browseBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				DirectoryDialog dlg = new DirectoryDialog(getShell());
				dlg.setFilterPath(directoryTxt.getText());
				dlg.setText("Choose Directory");
				dlg.setMessage("Choose Directory to download ESP-IDF");

				String dir = dlg.open();
				if (dir != null)
				{
					directoryTxt.setText(dir);
				}
			}
		});

	}

	protected void validate()
	{
		if (fileSystemBtn.getSelection())
		{
			// File system selection
			if (StringUtil.isEmpty(existingIdfDirTxt.getText()))
			{
				setPageComplete(false);
				return;
			}
		}
		else
		{
			if (StringUtil.isEmpty(directoryTxt.getText()))
			{
				setPageComplete(false);
				return;
			}
		}
		setPageComplete(true);
	}

	public IDFVersion Version()
	{
		String versionTxt = versionCombo.getText();
		IDFVersion version = versionsMap.get(versionTxt);
		return version;
	}

	public String getDestinationLocation()
	{
		return directoryTxt.getText().trim();
	}

	public String getExistingIDFLocation()
	{
		return existingIdfDirTxt.getText().trim();
	}

	public boolean isConfigureExistingEnabled()
	{
		return fileSystemBtn.getSelection();
	}

}
