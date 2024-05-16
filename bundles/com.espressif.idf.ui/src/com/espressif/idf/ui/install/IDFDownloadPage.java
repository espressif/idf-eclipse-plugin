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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.IDFVersion;
import com.espressif.idf.core.IDFVersionsReader;
import com.espressif.idf.core.SystemExecutableFinder;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.UIPlugin;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFDownloadPage extends WizardPage
{
	private static final int GIT_TOOL = 0;
	private static final int PYTHON_TOOL = 1;
	
	private Combo versionCombo;
	private Map<String, IDFVersion> versionsMap;
	private Text directoryTxt;
	private Button fileSystemBtn;
	private Text existingIdfDirTxt;
	private Button browseBtn;
	
	private Text gitText;
	private Text pythonText;
	
	private String pythonExecutablePath;
	private String gitExecutablePath;
	private SystemExecutableFinder systemExecutableFinder;

	protected IDFDownloadPage(String pageName)
	{
		super(pageName);
		setImageDescriptor(UIPlugin.getImageDescriptor(Messages.IDFDownloadPage_0));
		getPythonExecutablePath();
	}

	@Override
	public void createControl(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NULL);

		initializeDialogUnits(parent);

		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createExistingComposite(composite);
		
		createGitPythonComposite(composite);

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

	private void createGitPythonComposite(Composite parent)
	{
		Group gitPyGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		gitPyGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final int numColumns = 3;
		GridLayout gridLayout = new GridLayout(numColumns, false);
		gitPyGroup.setLayout(gridLayout);

		Label gitLabel = new Label(gitPyGroup, SWT.NONE);
		gitLabel.setText(Messages.GitLabel);

		gitText = new Text(gitPyGroup, SWT.SINGLE | SWT.BORDER);
		gitText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		gitExecutablePath = IDFUtil.getGitExecutablePathFromSystem();

		gitText.setText(gitExecutablePath);
		gitText.addModifyListener(new ModifyTextValidationListener(GIT_TOOL));

		Button gitBrowseButton = new Button(gitPyGroup, SWT.PUSH);
		gitBrowseButton.setText(Messages.BrowseButton);
		gitBrowseButton.addSelectionListener(new BrowseButtonSelectionAdapter(gitText, GIT_TOOL));

		if (StringUtil.isEmpty(pythonExecutablePath))
		{
			pythonExecutablePath = getPythonExecutablePath();
		}

		Label pythonLabel = new Label(gitPyGroup, SWT.NONE);
		pythonLabel.setText(Messages.PythonLabel);
		pythonText = new Text(gitPyGroup, SWT.SINGLE | SWT.BORDER);
		pythonText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		pythonText.setText(pythonExecutablePath);
		pythonText.addModifyListener(new ModifyTextValidationListener(PYTHON_TOOL));
		Button pythonBrowseButton = new Button(gitPyGroup, SWT.PUSH);
		pythonBrowseButton.setText(Messages.BrowseButton);
		pythonBrowseButton.addSelectionListener(new BrowseButtonSelectionAdapter(pythonText, PYTHON_TOOL));
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

	private String getPythonExecutablePath()
	{
		return IDFUtil.getPythonExecutable();
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

	private boolean validateGitAndPython()
	{
		if (systemExecutableFinder == null)
		{
			systemExecutableFinder = new SystemExecutableFinder();
		}
		
		if (StringUtil.isEmpty(pythonExecutablePath) || StringUtil.isEmpty(gitExecutablePath))
		{
			setErrorMessage("Python & Git are Required");
			return false;
		}

		
		IPath pathGit = systemExecutableFinder.find(gitExecutablePath);
		if (pathGit != null)
			gitExecutablePath = pathGit.toOSString();
		
		File file = new File(gitExecutablePath);
		if (!file.exists() && !IDFUtil.isReparseTag(file))
		{
			setErrorMessage("Git executable not found");
			return false;
		}

		IPath pythonPath = systemExecutableFinder.find(pythonExecutablePath);
		if (pythonPath != null)
		{
			pythonExecutablePath = pythonPath.toOSString();
		}
		file = new File(pythonExecutablePath);
		if (!file.exists() && !IDFUtil.isReparseTag(file))
		{
			setErrorMessage("Python executable not found");
			return false;
		}
		
		return true;
	}
	
	private void validate()
	{
		if (fileSystemBtn.getSelection())
		{
			String idfPath = existingIdfDirTxt.getText();
			if (StringUtil.isEmpty(idfPath))
			{
				setPageComplete(false);
				setErrorMessage("IDF Directory is Required");
				return;
			}
			
			if (!new File(idfPath).exists())
			{
				setErrorMessage(Messages.IDFDownloadPage_DirDoesnotExist+ idfPath);
				setPageComplete(false);
				return;
			}
			if (idfPath.contains(" ")) //$NON-NLS-1$
			{
				setErrorMessage(Messages.IDFDownloadPage_IDFBuildNotSupported);
				setPageComplete(false);
				showMessage();
				return;
			}
			
			String idfPyPath = idfPath + File.separator + "tools" + File.separator + "idf.py"; //$NON-NLS-1$ //$NON-NLS-2$
			if (!new File (idfPyPath).exists())
			{
				setErrorMessage(MessageFormat.format(Messages.IDFDownloadPage_CantfindIDFpy, idfPath));
				setPageComplete(false);
				return;
			}
			if (validateGitAndPython())
			{
				setPageComplete(true);
				setErrorMessage(null);
				setMessage(Messages.IDFDownloadPage_ClickOnFinish + idfPath);
			}
			else
			{
				setPageComplete(false);
			}
		}
		else
		{
			setMessage(StringUtil.EMPTY);
			setErrorMessage(null);
			boolean supportSpaces = false;
			if (versionCombo.getText().contentEquals("master")) //$NON-NLS-1$
			{
				supportSpaces = true;
			} 
			else 
			{
				Pattern p = Pattern.compile("([0-9][.][0-9])"); //$NON-NLS-1$
				Matcher m = p.matcher(versionCombo.getText());
				supportSpaces = m.find() && Double.parseDouble(m.group(0)) >= 5.0;
			}
			if (StringUtil.isEmpty(directoryTxt.getText()))
			{
				setPageComplete(false);
				return;
			}
			
			if (!supportSpaces && directoryTxt.getText().contains(" ")) //$NON-NLS-1$
			{
				setErrorMessage(Messages.IDFDownloadPage_IDFBuildNotSupported);
				setPageComplete(false);
				return;
			}

			if (validateGitAndPython())
			{
				setPageComplete(true);
				setErrorMessage(null);
				setMessage(Messages.IDFDownloadPage_ClickFinishToDownload);
			}
			else 
			{
				setPageComplete(false);
			}
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
	
	private void showMessage()
	{
		Display.getDefault().asyncExec(new Runnable()
		{

			@Override
			public void run() {
				boolean allowToCreateProjectWithSpaces = MessageDialog.openQuestion(Display.getDefault().getActiveShell(), Messages.IDFDownloadWizard_AllowSpacesTitle, 
						Messages.IDFDownloadWizard_AllowSpacesMsg);
				
				if (allowToCreateProjectWithSpaces) {
					setErrorMessage(null);
					setPageComplete(true);
				}
			}
		});
	}

	public String getPythonExePath()
	{
		return pythonExecutablePath;
	}

	public String getGitExecutablePath()
	{
		return gitExecutablePath;
	}

	private class BrowseButtonSelectionAdapter extends SelectionAdapter
	{
		private Text linkedText;
		private int dialog;
		private static final String GIT_FILE = "git"; //$NON-NLS-1$
		private static final String PYTHON_FILE = "python*"; //$NON-NLS-1$
		private static final String PYTHON_FILTERS = "Python Executables"; //$NON-NLS-1$
		private static final String WINDOWS_EXTENSION = ".exe"; //$NON-NLS-1$

		private BrowseButtonSelectionAdapter(Text text, int dialog)
		{
			this.linkedText = text;
			this.dialog = dialog;
		}

		@Override
		public void widgetSelected(SelectionEvent selectionEvent)
		{
			FileDialog dlg = null;
			if (dialog == GIT_TOOL)
			{
				dlg = gitDialog();
			}
			else
			{
				dlg = pythonDialog();
			}

			dlg.setText(Messages.FileSelectionDialogTitle);
			String dir = dlg.open();
			if (!StringUtil.isEmpty(dir))
			{
				linkedText.setText(dir);
			}
		}

		private FileDialog pythonDialog()
		{
			FileDialog dialog = new FileDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell());
			if (Platform.getOS().equals(Platform.OS_WIN32))
			{
				dialog.setFilterNames(new String[] { PYTHON_FILTERS });
				dialog.setFilterExtensions(new String[] { PYTHON_FILE.concat(WINDOWS_EXTENSION) });
			}
			else
			{
				dialog.setFilterNames(new String[] { PYTHON_FILTERS });
				dialog.setFilterExtensions(new String[] { PYTHON_FILE });
			}
			return dialog;
		}

		private FileDialog gitDialog()
		{
			FileDialog dialog = new FileDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell());
			if (Platform.getOS().equals(Platform.OS_WIN32))
			{
				dialog.setFilterNames(new String[] { GIT_FILE.concat(WINDOWS_EXTENSION) });
				dialog.setFilterExtensions(new String[] { GIT_FILE.concat(WINDOWS_EXTENSION) });
			}
			else
			{
				dialog.setFilterNames(new String[] { GIT_FILE });
				dialog.setFilterExtensions(new String[] { GIT_FILE });
			}

			return dialog;
		}
	}
	
	private class ModifyTextValidationListener implements ModifyListener
	{
		private int tool;

		private ModifyTextValidationListener(int tool)
		{
			this.tool = tool;
		}

		@Override
		public void modifyText(ModifyEvent e)
		{
			switch (tool)
			{
			case GIT_TOOL:
				gitExecutablePath = gitText.getText();
				break;
			case PYTHON_TOOL:
				pythonExecutablePath = pythonText.getText();
			default:
				break;
			}
			
			validate();
		}
	}
}
