/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.install;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
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
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.IDFVersion;
import com.espressif.idf.core.IDFVersionsReader;
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
		setImageDescriptor(UIPlugin.getImageDescriptor(Messages.IDFDownloadPage_0));
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
		versionGrp.setText(Messages.IDFDownloadPage_DownloadIDF);
		versionGrp.setFont(parent.getFont());

		Label versionLbl = new Label(versionGrp, SWT.NONE);
		versionLbl.setText(Messages.IDFDownloadPage_ChooseIDFVersion);

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
		createLinkArea(versionGrp);

		Label noteLbl = new Label(composite, SWT.NONE);
		noteLbl.setText(Messages.IDFDownloadPage_Note);

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
		fileSystemBtn.setText(Messages.IDFDownloadPage_ChooseAnExistingIDF);
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
		label.setText(Messages.IDFDownloadPage_ChooseDirIDF);

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
		existingBrowseBtn.setText(Messages.IDFDownloadPage_BrowseBtn);
		existingBrowseBtn.setEnabled(false);
		existingBrowseBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				DirectoryDialog dlg = new DirectoryDialog(getShell());
				dlg.setFilterPath(existingIdfDirTxt.getText());
				dlg.setText(Messages.IDFDownloadPage_DirectoryDialogTxt);
				dlg.setMessage(Messages.IDFDownloadPage_DirectoryDialogMsg);

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
		descLbl.setText(Messages.IDFDownloadPage_ChooseIDFDir);

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
		browseBtn.setText(Messages.IDFDownloadPage_BrowseBtnTxt);
		browseBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				DirectoryDialog dlg = new DirectoryDialog(getShell());
				dlg.setFilterPath(directoryTxt.getText());
				dlg.setText(Messages.IDFDownloadPage_DirectoryDialogText);
				dlg.setMessage(Messages.IDFDownloadPage_DirectoryDialogMessage);

				String dir = dlg.open();
				if (dir != null)
				{
					directoryTxt.setText(dir);
				}
			}
		});

	}

	private void createLinkArea(Composite parent)
	{
		Link link = new Link(parent, SWT.NONE);
		String message = Messages.IDFDownloadPage_VersionLinkMsg;
		link.setText(message);
		link.setSize(400, 100);

		GridData gridData = new GridData(SWT.NONE, SWT.NONE, false, false, 3, 1);
		gridData.verticalIndent = 10;
		link.setLayoutData(gridData);

		link.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				try
				{
					// Open default external browser
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(e.text));
				}
				catch (PartInitException ex)
				{
					ex.printStackTrace();
				}
				catch (MalformedURLException ex)
				{
					ex.printStackTrace();
				}
			}
		});
	}

	private void validate()
	{
		if (fileSystemBtn.getSelection())
		{
			String idfPath = existingIdfDirTxt.getText();
			if (StringUtil.isEmpty(idfPath))
			{
				setPageComplete(false);
				return;
			}
			
			if (!new File(idfPath).exists())
			{
				setErrorMessage("Directory doesn''t exist: "+ idfPath);
				setPageComplete(false);
				return;
			}
			
			if (idfPath.contains(" "))
			{
				setErrorMessage("ESP-IDF build system does not support spaces in paths. Please choose a different directory.");
				setPageComplete(false);
				return;
			}
			
			String idfPyPath = idfPath + File.separator + "tools" + File.separator + "idf.py";
			if (!new File (idfPyPath).exists())
			{
				setErrorMessage(MessageFormat.format("Can not find idf.py in {0} tools", idfPath));
				setPageComplete(false);
				return;
			}
			
			String requirementsPath = idfPath + File.separator + "requirements.txt";
			if (!new File (requirementsPath).exists())
			{
				setErrorMessage(MessageFormat.format("Can not find requirements.txt in {0}", idfPath));
				setPageComplete(false);
				return;
			}
			
			setPageComplete(true);
			setErrorMessage(null);
			setMessage("Click on `Finish` to configure IDF_PATH with "+ idfPath);
		}
		else
		{
			if (StringUtil.isEmpty(directoryTxt.getText()))
			{
				setPageComplete(false);
				return;
			}
			
			setPageComplete(true);
			setErrorMessage(null);
			setMessage("Click on `Finish` to download");
		}
		
	}

	protected IDFVersion Version()
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
