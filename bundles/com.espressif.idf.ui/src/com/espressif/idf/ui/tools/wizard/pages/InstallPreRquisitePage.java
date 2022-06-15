/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.wizard.pages;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.ExecutableFinder;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.GitWinRegistryReader;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.PyWinRegistryReader;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.tools.Messages;
import com.espressif.idf.ui.tools.wizard.IToolsInstallationWizardConstants;

/**
 * Install initial tools page for git and python configs
 * 
 * @author Ali Azam Rana
 *
 */
public class InstallPreRquisitePage extends WizardPage
{
	private static final int GIT_TOOL = 0;
	private static final int PYTHON_TOOL = 1;
	private Text gitText;
	private Text pythonText;
	private Combo pythonVersionCombo;
	private Text logAreaText;
	private String pythonExecutablePath;
	private Map<String, String> pythonVersions;
	private String gitExecutablePath;
	private IDFEnvironmentVariables idfEnvironmentVariables;

	public InstallPreRquisitePage()
	{
		super(Messages.InstallPreRquisitePage);
		setTitle(Messages.InstallPreRquisitePage);
		setDescription(Messages.InstallToolsPreReqPageDescription);
		getPythonExecutablePath();
		idfEnvironmentVariables = new IDFEnvironmentVariables();
		loadGitExecutablePath();
	}

	@Override
	public void createControl(Composite parent)
	{
		Composite container = new Composite(parent, SWT.NONE);

		final int numColumns = 3;
		GridLayout gridLayout = new GridLayout(numColumns, false);
		container.setLayout(gridLayout);

		Label gitLabel = new Label(container, SWT.NONE);
		gitLabel.setText(Messages.GitLabel);

		gitText = new Text(container, SWT.SINGLE | SWT.BORDER);
		gitText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		gitExecutablePath = getPreferences().get(IToolsInstallationWizardConstants.GIT_PATH_NODE_KEY, StringUtil.EMPTY);
		if (StringUtil.isEmpty(gitExecutablePath))
		{
			gitExecutablePath = getGitExecutablePath();
		}

		gitText.setText(gitExecutablePath);
		gitText.addModifyListener(new ModifyTextValidationListener(GIT_TOOL));

		Button gitBrowseButton = new Button(container, SWT.PUSH);
		gitBrowseButton.setText(Messages.BrowseButton);
		gitBrowseButton.addSelectionListener(new BrowseButtonSelectionAdapter(gitText, GIT_TOOL));

		pythonExecutablePath = getPreferences().get(IToolsInstallationWizardConstants.PYTHON_PATH_NODE_KEY,
				StringUtil.EMPTY);
		if (StringUtil.isEmpty(pythonExecutablePath))
		{
			pythonExecutablePath = getPythonExecutablePath();
		}

		if (Platform.OS_WIN32.equals(Platform.getOS()) && pythonVersions != null && !pythonVersions.isEmpty())
		{
			new Label(container, SWT.NONE).setText(Messages.SelectPythonVersion);
			pythonVersionCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			GridData gridData = new GridData(SWT.NONE, SWT.NONE, true, false, 2, 1);
			gridData.widthHint = 250;
			pythonVersionCombo.setLayoutData(gridData);

			String[] versions = pythonVersions.keySet().toArray(new String[pythonVersions.size()]);
			pythonVersionCombo.setItems(versions);
			pythonVersionCombo.select(0); // select the first one
			pythonVersionCombo.addModifyListener(new ModifyListener()
			{
				@Override
				public void modifyText(ModifyEvent e)
				{
					String version = pythonVersionCombo.getText();
					pythonExecutablePath = pythonVersions.getOrDefault(version, null);
				}
			});
		}
		else
		{
			Label pythonLabel = new Label(container, SWT.NONE);
			pythonLabel.setText(Messages.PythonLabel);
			pythonText = new Text(container, SWT.SINGLE | SWT.BORDER);
			pythonText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			pythonText.setText(pythonExecutablePath);
			pythonText.addModifyListener(new ModifyTextValidationListener(PYTHON_TOOL));
			Button pythonBrowseButton = new Button(container, SWT.PUSH);
			pythonBrowseButton.setText(Messages.BrowseButton);
			pythonBrowseButton.addSelectionListener(new BrowseButtonSelectionAdapter(pythonText, PYTHON_TOOL));
		}

		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, numColumns, 2));

		Label lblLog = new Label(composite, SWT.NONE);
		lblLog.setText(Messages.InstallPreRquisitePage_lblLog_text);

		logAreaText = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		logAreaText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		setControl(container);
	}

	private void loadGitExecutablePath()
	{
		IPath gitPath = ExecutableFinder.find("git", true); //$NON-NLS-1$
		Logger.log("GIT path:" + gitPath); //$NON-NLS-1$
		if (gitPath != null)
		{
			this.gitExecutablePath = gitPath.toOSString();
		}
		else
		{
			if (Platform.OS_WIN32.equals(Platform.getOS()))
			{
				GitWinRegistryReader gitWinRegistryReader = new GitWinRegistryReader();
				String gitInstallPath = gitWinRegistryReader.getGitInstallPath();
				if (!StringUtil.isEmpty(gitInstallPath))
				{
					this.gitExecutablePath = gitInstallPath.concat(String.valueOf(Path.SEPARATOR)).concat("bin").concat(String.valueOf(Path.SEPARATOR)).concat("git.exe"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			else 
			{
				// MAC & LINUX have whereis git to see where the command is located
				List<String> arguments = new ArrayList<String>();
				ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
				try
				{
					arguments.add("whereis"); //$NON-NLS-1$
					arguments.add("git"); //$NON-NLS-1$

					Map<String, String> environment = new HashMap<>(System.getenv());

					IStatus status = processRunner.runInBackground(arguments, org.eclipse.core.runtime.Path.ROOT, environment);
					if (status == null)
					{
						Logger.log(IDFCorePlugin.getPlugin(), IDFCorePlugin.errorStatus("Status can't be null", null)); //$NON-NLS-1$
						return;
					}
					String gitLocation = status.getMessage().split(" ").length > 1 ? status.getMessage().split(" ")[1] : ""; //$NON-NLS-1$ //$NON-NLS-2$
					gitLocation = gitLocation.strip();
					this.gitExecutablePath = gitLocation;
				}
				catch (Exception e1)
				{
					Logger.log(e1);
				}
			}
		}
	}

	private String getGitExecutablePath()
	{
		String gitPath = idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.GIT_PATH);
		return StringUtil.isEmpty(gitPath) ? StringUtil.EMPTY : gitPath;
	}

	private String getPythonExecutablePath()
	{
		if (Platform.OS_WIN32.equals(Platform.getOS()))
		{
			PyWinRegistryReader pyWinRegistryReader = new PyWinRegistryReader();
			pythonVersions = pyWinRegistryReader.getPythonVersions();
			if (pythonVersions.isEmpty())
			{
				Logger.log("No Python installations found in the system."); //$NON-NLS-1$
				logAreaText.append(System.getProperty("line.separator")); //$NON-NLS-1$
				logAreaText.append("No Python installations found in the system."); //$NON-NLS-1$
			}
			if (pythonVersions.size() == 1)
			{
				Map.Entry<String, String> entry = pythonVersions.entrySet().iterator().next();
				pythonExecutablePath = entry.getValue();
			}
		}
		else
		{
			pythonExecutablePath = IDFUtil.getPythonExecutable();
		}
		return pythonExecutablePath;
	}

	@Override
	public boolean isPageComplete()
	{
		if (StringUtil.isEmpty(pythonExecutablePath) || StringUtil.isEmpty(gitExecutablePath))
			return false;

		File file = new File(gitExecutablePath);
		if (!file.exists())
			return false;

		file = new File(pythonExecutablePath);
		if (!file.exists())
			return false;

		return true;
	}

	@Override
	public IWizardPage getNextPage()
	{
		Preferences scopedPreferenceStore = getPreferences();
		scopedPreferenceStore.put(IToolsInstallationWizardConstants.PYTHON_PATH_NODE_KEY, pythonExecutablePath);
		scopedPreferenceStore.put(IToolsInstallationWizardConstants.GIT_PATH_NODE_KEY, gitExecutablePath);
		idfEnvironmentVariables.prependEnvVariableValue(IDFEnvironmentVariables.PYTHON_EXE_PATH, pythonExecutablePath);
		addGitToEnvironment(gitExecutablePath);

		try
		{
			scopedPreferenceStore.flush();
		}
		catch (Exception e)
		{
		}
		return super.getNextPage();
	}

	private Preferences getPreferences()
	{
		return ConfigurationScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID).node("preference"); //$NON-NLS-1$
	}

	private void addGitToEnvironment(String executablePath)
	{
		String pathEnv = idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.PATH);
		String gitDir = new Path(executablePath).removeLastSegments(1).toOSString();
		if (!pathEnv.contains(gitDir))
		{
			idfEnvironmentVariables.prependEnvVariableValue(IDFEnvironmentVariables.PATH, gitDir);
		}

		idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.GIT_PATH, executablePath);
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

			setPageComplete(isPageComplete());
		}
	}

	private class BrowseButtonSelectionAdapter extends SelectionAdapter
	{
		private Text linkedText;
		private int dialog;
		private static final String GIT_FILE = "git"; //$NON-NLS-1$
		private static final String PYTHON_FILE = "python"; //$NON-NLS-1$
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
				dialog.setFilterNames(new String[] { PYTHON_FILE.concat(WINDOWS_EXTENSION) });
				dialog.setFilterExtensions(new String[] { PYTHON_FILE.concat(WINDOWS_EXTENSION) });
			}
			else
			{
				dialog.setFilterNames(new String[] { PYTHON_FILE });
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
}
