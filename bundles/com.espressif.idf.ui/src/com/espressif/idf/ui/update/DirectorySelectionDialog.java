/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.update;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.UIPlugin;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class DirectorySelectionDialog extends TitleAreaDialog
{

	private Text text;
	private String idfDirPath;
	private String pythonExecutablePath;
	private Map<String, String> pythonVersions;
	private String gitPath;
	private Text gitLocationtext;
	private Text pythonLocationtext;
	private String commandId;
	private static final String pythonPathNodeKey = "PYTHON_EXECUTABLE"; //$NON-NLS-1$
	private static final String gitPathNodeKey = "GIT_EXECUTABLE"; //$NON-NLS-1$

	protected DirectorySelectionDialog(Shell parentShell, String commandId, String pythonExecutablePath,
			Map<String, String> pythonVersions, String idfPath, String gitExecutablePath)
	{
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.pythonExecutablePath = getPythonPreferenceOrDefault(pythonExecutablePath);
		this.pythonVersions = pythonVersions;
		this.idfDirPath = idfPath;
		this.gitPath = getGitPreferenceOrDefault(gitExecutablePath);
		this.commandId = commandId;
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		getShell().setText(Messages.DirectorySelectionDialog_InstallTools);
		setTitle(Messages.DirectorySelectionDialog_IDFToolsInstallationDialog);
		setTitleImage(UIPlugin.getImage("icons/espressif_logo.png")); //$NON-NLS-1$
		setMessage(Messages.DirectorySelectionDialog_ProvideIDFDirectory);

		Composite composite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 3;
		composite.setLayout(topLayout);
		composite.setLayoutData(gd);

		// IDF_PATH
		new Label(composite, SWT.NONE).setText(Messages.DirectorySelectionDialog_IDFDirLabel);

		text = new Text(composite, SWT.BORDER);
		GridData data = new GridData();
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		text.setLayoutData(data);
		text.setText(idfDirPath != null ? idfDirPath : StringUtil.EMPTY);
		text.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				validate();
			}
		});

		Button button = new Button(composite, SWT.PUSH);
		button.setText(Messages.DirectorySelectionDialog_Browse);
		button.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				DirectoryDialog dlg = new DirectoryDialog(Display.getDefault().getActiveShell());
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
		gitLocationtext.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				validate();
			}
		});

		Button gitBrowseBtn = new Button(composite, SWT.PUSH);
		gitBrowseBtn.setText(Messages.DirectorySelectionDialog_Browse);
		gitBrowseBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				FileDialog dlg = new FileDialog(Display.getDefault().getActiveShell());
				dlg.setText(Messages.DirectorySelectionDialog_GitExecutableLocation);

				String dir = dlg.open();
				if (dir != null)
				{
					gitLocationtext.setText(dir);
				}
			}
		});

		// Python version selection
		addPythonVersionSelectionControls(composite);

		Dialog.applyDialogFont(composite);
		return composite;
	}

	private void addPythonVersionSelectionControls(Composite composite)
	{
		// Python executable location
		new Label(composite, SWT.NONE).setText(Messages.DirectorySelectionDialog_PyExeLocation);

		pythonLocationtext = new Text(composite, SWT.BORDER);
		GridData data = new GridData();
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		pythonLocationtext.setLayoutData(data);
		
		pythonLocationtext.setText(pythonExecutablePath != null ? pythonExecutablePath : StringUtil.EMPTY);
		pythonLocationtext.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				validate();
			}
		});

		Button pyBrowseBtn = new Button(composite, SWT.PUSH);
		pyBrowseBtn.setText(Messages.DirectorySelectionDialog_Browse);
		pyBrowseBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				FileDialog dlg = new FileDialog(Display.getDefault().getActiveShell());
				dlg.setText(Messages.DirectorySelectionDialog_PyExecutableLocation);
				String pythonLocationPathString = dlg.open();
				if (pythonLocationPathString != null)
				{
					pythonLocationtext.setText(pythonLocationPathString);
				}
			}
		});
	}

	protected void validate()
	{
		idfDirPath = text.getText();
		pythonExecutablePath = pythonLocationtext.getText();

		gitPath = gitLocationtext.getText();
		
		boolean isValidPythonPath = validatePythonExecutable(pythonExecutablePath);
		
		if (!isValidPythonPath)
		{
			setErrorMessage(Messages.DirectorySelectionDialog_InvalidPythonPath);
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
		else if (StringUtil.isEmpty(pythonExecutablePath) || StringUtil.isEmpty(gitPath) || StringUtil.isEmpty(idfDirPath))
		{
			setErrorMessage(Messages.DirectorySelectionDialog_CantbeEmpty);
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
		else
		{
			setErrorMessage(null);
			getButton(IDialogConstants.OK_ID).setEnabled(true);
		}
	}

	private void saveExecutablePreferences()
	{
		Preferences scopedPreferenceStore = getPreferences();
		scopedPreferenceStore.put(pythonPathNodeKey, pythonExecutablePath);
		scopedPreferenceStore.put(gitPathNodeKey, gitPath);
		try
		{
			scopedPreferenceStore.flush();
		}
		catch (BackingStoreException e)
		{
			e.printStackTrace();
		}
	}

	private String getPythonPreferenceOrDefault(String pythonExecutablePath)
	{
		return getPreferences().get(pythonPathNodeKey, pythonExecutablePath);
	}

	private String getGitPreferenceOrDefault(String gitExecutablePath)
	{
		return getPreferences().get(gitPathNodeKey, gitExecutablePath);
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
		pythonExecutablePath = pythonLocationtext.getText();

		gitPath = gitLocationtext.getText();

		super.okPressed();
		saveExecutablePreferences();
	}

	@Override
	public void create()
	{
		super.create();
		if (commandId != null && commandId.equals("com.espressif.idf.ui.command.install")) //$NON-NLS-1$
		{
			getButton(IDialogConstants.OK_ID).setText(Messages.DirectorySelectionDialog_InstallTools);
			validate();
		}
		else
		{
			getButton(IDialogConstants.OK_ID).setText(Messages.DirectorySelectionDialog_CheckTools);
		}
	}

	private Preferences getPreferences()
	{
		return ConfigurationScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID).node("preference"); //$NON-NLS-1$
	}
	
	private boolean validatePythonExecutable(String path)
	{
		try
		{
			Process process = new ProcessBuilder(path, "--version").start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        String output = reader.readLine();
			int exitCode = process.waitFor();
			return exitCode == 0 && output.startsWith("Python");
		}
		catch (Exception e)
		{
			return false;
		}
	}
}
