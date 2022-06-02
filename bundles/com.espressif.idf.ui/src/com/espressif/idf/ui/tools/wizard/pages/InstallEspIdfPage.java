/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.wizard.pages;

import java.io.File;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.jface.wizard.IWizardPage;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.IDFVersion;
import com.espressif.idf.core.IDFVersionsReader;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.tools.GitDownloadAndCloneThread;
import com.espressif.idf.ui.tools.LogMessagesThread;
import com.espressif.idf.ui.tools.Messages;
import com.espressif.idf.ui.tools.wizard.ToolsManagerWizard;

/**
 * Install ESP-IDF Tools wizard page to install the esp idf tools
 * 
 * @author Ali Azam Rana
 *
 */
public class InstallEspIdfPage extends WizardPage
{
	private Text logAreaText;
	private Text txtIdfpath;
	private boolean enableNewSection;
	private boolean enableExistingSection;
	private Combo versionCombo;
	private Button btnBrowse;
	private Button btnDownload;
	private Button btnNew;
	private Button btnExisting;
	private IDFEnvironmentVariables idfEnvironmentVariables;
	private Map<String, IDFVersion> versionsMap;
	private Label lblDownloadDirectory;
	private Text txtDownloadDirectory;
	private Button btnBrowseDownloadDir;
	private Button btnCancel;
	private LogMessagesThread logMessagesThread;
	private Queue<String> logMessages;
	private GitDownloadAndCloneThread gitDownloadAndCloneThread;
	private Composite container;
	private boolean cloningOrDownloading;
	private ProgressBar progressBar;

	public InstallEspIdfPage()
	{
		super(Messages.InstallEspIdfPage);
		setDescription(Messages.InstallEspIdfPageDescription);
		setTitle(Messages.InstallEspIdfPage);
		idfEnvironmentVariables = new IDFEnvironmentVariables();
		versionsMap = new IDFVersionsReader().getVersionsMap();
		logMessages = new ConcurrentLinkedQueue<String>();
	}

	@Override
	public void createControl(Composite parent)
	{
		container = new Composite(parent, SWT.NONE);
		getControlsContainer().setLayout(new GridLayout(1, false));
		String idfPath = IDFUtil.getIDFPath();
		enableExistingSection = !StringUtil.isEmpty(idfPath);
		enableNewSection = StringUtil.isEmpty(idfPath);

		btnExisting = new Button(getControlsContainer(), SWT.RADIO);
		btnExisting.setText(Messages.InstallEspIdfPage_Existing);
		btnExisting.addSelectionListener(new RadioSectionButtonSelectionAdapter());
		btnExisting.setSelection(enableExistingSection);

		Composite compositeExisting = new Composite(getControlsContainer(), SWT.BORDER | SWT.EMBEDDED);
		compositeExisting.setLayout(new GridLayout(3, false));
		compositeExisting.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Label lblEspidfPath = new Label(compositeExisting, SWT.NONE);
		lblEspidfPath.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblEspidfPath.setText(Messages.InstallEspIdfPage_lblEspidfPath_text);

		txtIdfpath = new Text(compositeExisting, SWT.BORDER);
		txtIdfpath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtIdfpath.setText(enableExistingSection ? idfPath : StringUtil.EMPTY);
		txtIdfpath.setEnabled(enableExistingSection);
		txtIdfpath.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				setPageComplete(false);
			}
		});

		btnBrowse = new Button(compositeExisting, SWT.PUSH);
		btnBrowse.setText(Messages.BrowseButton);
		btnBrowse.setEnabled(enableExistingSection);
		btnBrowse.addSelectionListener(new BrowseButtonSelectionAdapter());

		btnNew = new Button(getControlsContainer(), SWT.RADIO);
		btnNew.setText(Messages.InstallEspIdfPage_btnNew_text);
		btnNew.addSelectionListener(new RadioSectionButtonSelectionAdapter());

		Composite compositeNew = new Composite(getControlsContainer(), SWT.BORDER | SWT.EMBEDDED);
		compositeNew.setLayout(new GridLayout(3, false));
		compositeNew.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		lblDownloadDirectory = new Label(compositeNew, SWT.NONE);
		lblDownloadDirectory.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDownloadDirectory.setText(Messages.InstallEspIdfPage_lblDownloadDirectory_text);

		txtDownloadDirectory = new Text(compositeNew, SWT.BORDER);
		txtDownloadDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtDownloadDirectory.setEnabled(enableNewSection);

		btnBrowseDownloadDir = new Button(compositeNew, SWT.PUSH);
		btnBrowseDownloadDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnBrowseDownloadDir.setText(Messages.BrowseButton);
		btnBrowseDownloadDir.addSelectionListener(new BrowseButtonSelectionAdapter());
		btnBrowseDownloadDir.setEnabled(enableNewSection);

		Label lblEspidfVersion = new Label(compositeNew, SWT.NONE);
		lblEspidfVersion.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblEspidfVersion.setText(Messages.InstallEspIdfPage_lblEspidfVersion_text);

		versionCombo = new Combo(compositeNew, SWT.READ_ONLY);
		versionCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		Set<String> keySet = versionsMap.keySet();
		versionCombo.setItems(keySet.toArray(new String[keySet.size()]));
		if (keySet.size() > 0)
		{
			versionCombo.select(0);
		}
		versionCombo.setEnabled(enableNewSection);

		btnDownload = new Button(compositeNew, SWT.NONE);
		btnDownload.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnDownload.setText(Messages.InstallEspIdfPage_btnDownload_text);
		btnDownload.setEnabled(enableNewSection);
		btnDownload.addSelectionListener(new DownloadButtonSelectionAdapter(this));

		Composite compositeLog = new Composite(getControlsContainer(), SWT.BORDER | SWT.EMBEDDED);
		compositeLog.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		compositeLog.setLayout(new GridLayout(1, false));

		Label label = new Label(compositeLog, SWT.NONE);
		label.setText(Messages.InstallPreRquisitePage_lblLog_text);
		btnCancel = new Button(compositeLog, SWT.PUSH);
		btnCancel.setText(Messages.BtnCancel);
		btnCancel.setVisible(false);
		btnCancel.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				btnCancel.setVisible(false);
				btnDownload.setEnabled(true);
				gitDownloadAndCloneThread.setCancelled(true);
				enableAllControls(enableNewSection, enableExistingSection, true);
			}
		});

		logAreaText = new Text(compositeLog,
				SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		GridData logGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		logGridData.heightHint = 200;
		logGridData.widthHint = 500;
		logAreaText.setLayoutData(logGridData);
		progressBar = new ProgressBar(compositeLog, SWT.HORIZONTAL);
		progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		logMessagesThread = new LogMessagesThread(logMessages, logAreaText, container.getDisplay());
		logMessagesThread.start();
		setControl(getControlsContainer());
	}

	@Override
	public IWizardPage getNextPage()
	{
		IWizardPage page = super.getNextPage();
		page.createControl(((ManageToolsInstallationWizardPage) page).getPageComposite());
		page.setWizard(getWizard());
		return page;
	}

	@Override
	public boolean canFlipToNextPage()
	{
		if (gitDownloadAndCloneThread != null && cloningOrDownloading)
		{
			return false;
		}

		if (btnExisting.getSelection() && !StringUtil.isEmpty(txtIdfpath.getText())
				&& new File(txtIdfpath.getText()).exists())
		{
			idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.IDF_PATH, txtIdfpath.getText());
			logMessages
					.add(MessageFormat.format(Messages.IDFDownloadWizard_UpdatingIDFPathMessage, txtIdfpath.getText()));
			return true;
		}
		else if (btnNew.getSelection())
		{
			return false;
		}

		return false;
	}

	private void loadDirectory(Text text)
	{
		DirectoryDialog directoryDialog = new DirectoryDialog(getShell());
		directoryDialog.setText(Messages.SelectDownloadDir);
		String dir = directoryDialog.open();
		if (!StringUtil.isEmpty(dir))
		{
			text.setText(dir);
		}
	}

	public void enableAllControls(boolean newSection, boolean existingSection, boolean revert)
	{
		if (revert)
		{
			btnExisting.setEnabled(true);
			btnNew.setEnabled(true);

			txtIdfpath.setEnabled(enableExistingSection);
			btnBrowse.setEnabled(enableExistingSection);

			versionCombo.setEnabled(enableNewSection);
			btnDownload.setEnabled(enableNewSection);
			btnBrowseDownloadDir.setEnabled(enableNewSection);
			txtDownloadDirectory.setEnabled(enableNewSection);
			container.layout(true);
			container.redraw();
			return;
		}

		btnExisting.setEnabled(existingSection);
		btnNew.setEnabled(newSection);

		txtIdfpath.setEnabled(existingSection);
		btnBrowse.setEnabled(existingSection);

		versionCombo.setEnabled(newSection);
		btnDownload.setEnabled(newSection);
		btnBrowseDownloadDir.setEnabled(newSection);
		txtDownloadDirectory.setEnabled(newSection);
		container.layout(true);
		container.redraw();
	}

	public void setCloningOrDownloading(boolean cloningOrDownloading)
	{
		this.cloningOrDownloading = cloningOrDownloading;
	}

	@Override
	protected boolean isCurrentPage()
	{
		if (getControlsContainer() != null)
		{
			getControlsContainer().layout(true);
		}

		return super.isCurrentPage();
	}

	public Composite getControlsContainer()
	{
		return container;
	}

	public Button getBtnCancel()
	{
		return btnCancel;
	}

	public Button getBtnExisting()
	{
		return btnExisting;
	}

	public Text getTxtIdfpath()
	{
		return txtIdfpath;
	}

	public Button getBtnNew()
	{
		return btnNew;
	}
	
	private class DownloadButtonSelectionAdapter extends SelectionAdapter
	{
		private InstallEspIdfPage installEspIdfPage;

		private DownloadButtonSelectionAdapter(InstallEspIdfPage installEspIdfPage)
		{
			this.installEspIdfPage = installEspIdfPage;
		}

		@Override
		public void widgetSelected(SelectionEvent selectionEvent)
		{
			((ToolsManagerWizard) getWizard()).getParentWizardDialog().updateSize();
			btnDownload.setEnabled(false);
			getBtnCancel().setVisible(true);
			String versionTxt = versionCombo.getText();
			IDFVersion version = versionsMap.get(versionTxt);
			String downloadLocation = txtDownloadDirectory.getText();
			new File(downloadLocation).mkdirs();
			String url = version.getUrl();
			GitDownloadAndCloneThread gitThread = new GitDownloadAndCloneThread(version, url, downloadLocation,
					logMessages, installEspIdfPage, progressBar);
			gitDownloadAndCloneThread = gitThread;
			gitThread.start();
			enableAllControls(false, false, false);
			setPageComplete(false);
		}
	}

	private class BrowseButtonSelectionAdapter extends SelectionAdapter
	{
		@Override
		public void widgetSelected(SelectionEvent selectionEvent)
		{
			if (getBtnExisting().getSelection())
			{
				loadDirectory(getTxtIdfpath());
				setPageComplete(false);
			}
			else if (getBtnNew().getSelection())
			{
				loadDirectory(txtDownloadDirectory);
				setPageComplete(false);
			}
		}
	}

	private class RadioSectionButtonSelectionAdapter extends SelectionAdapter
	{
		@Override
		public void widgetSelected(SelectionEvent e)
		{
			enableControls(getBtnNew().getSelection(), getBtnExisting().getSelection());
			setPageComplete(false);
		}

		private void enableControls(boolean newSection, boolean existingSection)
		{
			enableExistingSection = existingSection;
			enableNewSection = newSection;

			getTxtIdfpath().setEnabled(existingSection);
			btnBrowse.setEnabled(existingSection);

			versionCombo.setEnabled(newSection);
			btnDownload.setEnabled(newSection);
			btnBrowseDownloadDir.setEnabled(newSection);
			txtDownloadDirectory.setEnabled(newSection);
		}
	}
}
