package com.espressif.idf.ui.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.PreferencePage;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFCorePreferenceConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.StringUtil;

public class EspresssifPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage
{

	public static final String NUMBER_OF_LINES = "numberOfLines"; //$NON-NLS-1$
	public static final String NUMBER_OF_CHARS_IN_A_LINE = "numberOfCharsInALine"; //$NON-NLS-1$
	public static final int DEFAULT_SERIAL_MONITOR_NUBMER_OF_LINES = 1000;
	public static final int DEFAULT_SERIAL_MONITOR_NUMBER_OF_CHARS_IN_LINE = 500;

	private static final String GDB_SERVER_LAUNCH_TIMEOUT = "fGdbServerLaunchTimeout"; //$NON-NLS-1$
	private Text numberOfCharsInLineText;
	private Text numberLineText;
	private Text gdbSettingsText;
	private Button ccacheBtn;
	private Button automateHintsBtn;
	private Button hideErrorsOnIdfComponentsBtn;

	private Combo gitAssetsCombo;
	private Combo pythonWheelCombo;
	private Text idfToolsPathText;

	public EspresssifPreferencesPage()
	{
		super();
		setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, IDFCorePlugin.PLUGIN_ID));
		setDescription(Messages.EspresssifPreferencesPage_IDFSpecificPrefs);
	}

	@Override
	public void init(IWorkbench workbench)
	{
		initializeDefaults();
	}

	@Override
	protected Control createContents(Composite parent)
	{
		Composite mainComposite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		mainComposite.setLayout(gridLayout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		mainComposite.setLayoutData(data);

		addccacheControl(mainComposite);

		addGdbSettings(mainComposite);

		addSerialSettings(mainComposite);

		addBuildSettings(mainComposite);

		addToolsInstallationSettings(mainComposite);

		return mainComposite;
	}

	private void addToolsInstallationSettings(Composite mainComposite)
	{
		Group toolsInstallationGroup = new Group(mainComposite, SWT.SHADOW_ETCHED_IN);
		toolsInstallationGroup.setText(Messages.EspressifPreferencesPage_ToolsInstallationGrpTxt);
		toolsInstallationGroup.setLayout(new GridLayout(3, false));

		Label githubAssetsLabel = new Label(toolsInstallationGroup, SWT.NONE);
		githubAssetsLabel.setText(Messages.EspressifPreferencesPage_ToolsInstallationGitAssetUrlLabel);
		gitAssetsCombo = new Combo(toolsInstallationGroup, SWT.DROP_DOWN | SWT.BORDER);
		gitAssetsCombo.setItems(IDFCorePreferenceConstants.IDF_GITHUB_ASSETS_DEFAULT_GLOBAL, IDFCorePreferenceConstants.IDF_GITHUB_ASSETS_DEFAULT_CHINA);
		gitAssetsCombo.select(0);
		
		Label pythonWheelsLabel = new Label(toolsInstallationGroup, SWT.NONE);
		pythonWheelsLabel.setText(Messages.EspressifPreferencesPage_ToolsInstallationPythonPyWheelUrlLabel);
		pythonWheelCombo = new Combo(toolsInstallationGroup, SWT.DROP_DOWN | SWT.BORDER);
	    pythonWheelCombo.setItems(IDFCorePreferenceConstants.PIP_EXTRA_INDEX_URL_DEFAULT_GLOBAL, IDFCorePreferenceConstants.PIP_EXTRA_INDEX_URL_DEFAULT_CHINA);
	    pythonWheelCombo.select(0);
	    
		GridData gitTextGridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gitTextGridData.widthHint = 200;
		GridData pythonTextGridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		pythonTextGridData.widthHint = 200;
		gitAssetsCombo.setLayoutData(gitTextGridData);
		pythonWheelCombo.setLayoutData(pythonTextGridData);

		Label idfToolsPathLabel = new Label(toolsInstallationGroup, SWT.NONE);
		idfToolsPathLabel.setText(Messages.EspressifPreferencesPage_EspIdfToolsInstallationDirectoryLabel);
		idfToolsPathText = new Text(toolsInstallationGroup, SWT.SINGLE | SWT.NONE);
		GridData idfToolsPathTextGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		idfToolsPathTextGridData.widthHint = 200;
		idfToolsPathText.setLayoutData(idfToolsPathTextGridData);
		 // Add browse button
	    Button browseButtonIdfToolsPath = new Button(toolsInstallationGroup, SWT.PUSH);
	    browseButtonIdfToolsPath.setText(Messages.EspressifPreferencesPage_DirectorySelectionIDFToolsPathBrowseButton);
	    browseButtonIdfToolsPath.addSelectionListener(new SelectionAdapter() {
	        @Override
	        public void widgetSelected(SelectionEvent e) {
	            DirectoryDialog dialog = new DirectoryDialog(mainComposite.getShell());
	            dialog.setText(Messages.EspressifPreferencesPage_DirectorySelectionIDFToolsPathTitle);
	            dialog.setMessage(Messages.EspressifPreferencesPage_DirectorySelectionIDFToolsPathMessage);
	            String dir = dialog.open();
	            if (dir != null) {
	                idfToolsPathText.setText(dir);
	            }
	        }
	    });
		
		String idfToolsPath = getPreferenceStore().getString(IDFCorePreferenceConstants.IDF_TOOLS_PATH);
		idfToolsPath = StringUtil.isEmpty(idfToolsPath)
				? getPreferenceStore().getDefaultString(IDFCorePreferenceConstants.IDF_TOOLS_PATH)
				: idfToolsPath;
		idfToolsPathText.setText(idfToolsPath);
		
		String gitUrl = getPreferenceStore().getString(IDFCorePreferenceConstants.IDF_GITHUB_ASSETS);
		String pyWheelUrl = getPreferenceStore().getString(IDFCorePreferenceConstants.PIP_EXTRA_INDEX_URL);
		gitUrl = StringUtil.isEmpty(gitUrl)
				? gitAssetsCombo.getItem(0)
				: gitUrl;
		pyWheelUrl = StringUtil.isEmpty(pyWheelUrl)
				? pythonWheelCombo.getItem(0)
				: pyWheelUrl;
		
		gitAssetsCombo.setText(gitUrl);
		pythonWheelCombo.setText(pyWheelUrl);
	}

	private void addBuildSettings(Composite mainComposite)
	{
		Group buildGroup = new Group(mainComposite, SWT.SHADOW_ETCHED_IN);
		buildGroup.setText(Messages.EspresssifPreferencesPage_BuildGroupTxt);
		buildGroup.setLayout(new GridLayout(1, false));

		automateHintsBtn = new Button(buildGroup, SWT.CHECK);
		automateHintsBtn.setText(Messages.EspresssifPreferencesPage_SearchHintsCheckBtn);
		automateHintsBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		automateHintsBtn.setToolTipText(Messages.EspresssifPreferencesPage_SearchHintsTooltip);
		automateHintsBtn
				.setSelection(getPreferenceStore().getBoolean(IDFCorePreferenceConstants.AUTOMATE_BUILD_HINTS_STATUS));

		hideErrorsOnIdfComponentsBtn = new Button(buildGroup, SWT.CHECK);
		hideErrorsOnIdfComponentsBtn.setText(Messages.EspresssifPreferencesPage_HideErrprOnIdfComponentsBtn);
		hideErrorsOnIdfComponentsBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		hideErrorsOnIdfComponentsBtn.setToolTipText(Messages.EspresssifPreferencesPage_HideErrprOnIdfComponentsToolTip);
		hideErrorsOnIdfComponentsBtn
				.setSelection(getPreferenceStore().getBoolean(IDFCorePreferenceConstants.HIDE_ERRORS_IDF_COMPONENTS));
	}

	private void addccacheControl(Composite mainComposite)
	{
		Composite ccacheComp = new Composite(mainComposite, SWT.SHADOW_ETCHED_IN);
		ccacheComp.setLayout(new GridLayout(1, false));
		ccacheComp.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		ccacheBtn = new Button(ccacheComp, SWT.CHECK);
		ccacheBtn.setText(Messages.EspresssifPreferencesPage_EnableCCache);
		ccacheBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		ccacheBtn.setToolTipText(Messages.EspresssifPreferencesPage_CCacheToolTip);
		ccacheBtn.setSelection(getPreferenceStore().getBoolean(IDFCorePreferenceConstants.CMAKE_CCACHE_STATUS));

	}

	private void addSerialSettings(Composite parent)
	{
		Group serialGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		serialGroup.setText(Messages.SerialMonitorPage_GroupHeading);
		serialGroup.setLayout(new GridLayout(2, false));

		Label numberOfCharsInLineLabel = new Label(serialGroup, SWT.None);
		numberOfCharsInLineLabel.setText(Messages.SerialMonitorPage_Field_NumberOfCharsInLine);
		numberOfCharsInLineText = new Text(serialGroup, SWT.SINGLE | SWT.BORDER);
		numberOfCharsInLineText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		numberOfCharsInLineText.setText(Integer.toString(getPreferenceStore().getInt(NUMBER_OF_CHARS_IN_A_LINE)));

		Label numberLineLabel = new Label(serialGroup, SWT.None);
		numberLineLabel.setText(Messages.SerialMonitorPage_Field_NumberOfLines);
		numberLineText = new Text(serialGroup, SWT.SINGLE | SWT.BORDER);
		numberLineText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		numberLineText.setText(Integer.toString(getPreferenceStore().getInt(NUMBER_OF_LINES)));
	}

	private void addGdbSettings(Composite parent)
	{
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;

		Composite gdbGroup = new Composite(parent, SWT.SHADOW_ETCHED_IN);
		gdbGroup.setLayout(new GridLayout(2, false));
		gdbGroup.setLayoutData(data);

		Label gdbSettingLabel = new Label(gdbGroup, SWT.None);
		gdbSettingLabel.setText(Messages.GDBServerTimeoutPage_TimeoutField);
		gdbSettingsText = new Text(gdbGroup, SWT.SINGLE | SWT.BORDER);
		gdbSettingsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		gdbSettingsText.setText(Integer.toString(getPreferenceStore().getInt(GDB_SERVER_LAUNCH_TIMEOUT)));
	}

	@Override
	public boolean performOk()
	{
		try
		{
			int gdbTimeout = Integer.parseInt(gdbSettingsText.getText());
			getPreferenceStore().setValue(GDB_SERVER_LAUNCH_TIMEOUT, gdbTimeout);

			int numberOfCharInLines = Integer.parseInt(numberOfCharsInLineText.getText());
			getPreferenceStore().setValue(NUMBER_OF_CHARS_IN_A_LINE, numberOfCharInLines);

			int numberOfLines = Integer.parseInt(numberLineText.getText());
			getPreferenceStore().setValue(NUMBER_OF_LINES, numberOfLines);

			getPreferenceStore().setValue(IDFCorePreferenceConstants.CMAKE_CCACHE_STATUS, ccacheBtn.getSelection());

			getPreferenceStore().setValue(IDFCorePreferenceConstants.AUTOMATE_BUILD_HINTS_STATUS,
					automateHintsBtn.getSelection());

			boolean prevMarkerValue = getPreferenceStore().getBoolean(IDFCorePreferenceConstants.HIDE_ERRORS_IDF_COMPONENTS);
			getPreferenceStore().setValue(IDFCorePreferenceConstants.HIDE_ERRORS_IDF_COMPONENTS,
					hideErrorsOnIdfComponentsBtn.getSelection());
			// need to initiate a cleanup for initial clean of markers after they are enabled
			if (!prevMarkerValue && hideErrorsOnIdfComponentsBtn.getSelection())
			{
				IDFCorePlugin.ERROR_MARKER_LISTENER.initialMarkerCleanup();
			}
			
			getPreferenceStore().setValue(IDFCorePreferenceConstants.IDF_GITHUB_ASSETS, gitAssetsCombo.getText());

			getPreferenceStore().setValue(IDFCorePreferenceConstants.PIP_EXTRA_INDEX_URL, pythonWheelCombo.getText());
			
			getPreferenceStore().setValue(IDFCorePreferenceConstants.IDF_TOOLS_PATH, idfToolsPathText.getText());
		}
		catch (Exception e)
		{
			Logger.log(e);
			return false;
		}
		return true;
	}

	@Override
	protected void performDefaults()
	{
		gdbSettingsText.setText(Integer.toString(getPreferenceStore().getDefaultInt(GDB_SERVER_LAUNCH_TIMEOUT)));
		numberLineText.setText(Integer.toString(getPreferenceStore().getDefaultInt(NUMBER_OF_LINES)));
		numberOfCharsInLineText
				.setText(Integer.toString(getPreferenceStore().getDefaultInt(NUMBER_OF_CHARS_IN_A_LINE)));
		ccacheBtn.setSelection(getPreferenceStore().getBoolean(IDFCorePreferenceConstants.CMAKE_CCACHE_STATUS));
		automateHintsBtn
				.setSelection(getPreferenceStore().getBoolean(IDFCorePreferenceConstants.AUTOMATE_BUILD_HINTS_STATUS));
		hideErrorsOnIdfComponentsBtn
				.setSelection(getPreferenceStore().getBoolean(IDFCorePreferenceConstants.HIDE_ERRORS_IDF_COMPONENTS));
		gitAssetsCombo.setText(gitAssetsCombo.getItem(0));
		pythonWheelCombo.setText(pythonWheelCombo.getItem(0));
		idfToolsPathText.setText(getPreferenceStore().getDefaultString(IDFCorePreferenceConstants.IDF_TOOLS_PATH));
	}

	private void initializeDefaults()
	{
		getPreferenceStore().setDefault(GDB_SERVER_LAUNCH_TIMEOUT, 25);
		getPreferenceStore().setDefault(NUMBER_OF_CHARS_IN_A_LINE, DEFAULT_SERIAL_MONITOR_NUMBER_OF_CHARS_IN_LINE);
		getPreferenceStore().setDefault(NUMBER_OF_LINES, DEFAULT_SERIAL_MONITOR_NUBMER_OF_LINES);
		getPreferenceStore().setDefault(IDFCorePreferenceConstants.CMAKE_CCACHE_STATUS,
				IDFCorePreferenceConstants.CMAKE_CCACHE_DEFAULT_STATUS);
		getPreferenceStore().setDefault(IDFCorePreferenceConstants.AUTOMATE_BUILD_HINTS_STATUS,
				IDFCorePreferenceConstants.AUTOMATE_BUILD_HINTS_DEFAULT_STATUS);
		getPreferenceStore().setDefault(IDFCorePreferenceConstants.HIDE_ERRORS_IDF_COMPONENTS,
				IDFCorePreferenceConstants.HIDE_ERRORS_IDF_COMPONENTS_DEFAULT_STATUS);
		
		getPreferenceStore().setDefault(IDFCorePreferenceConstants.IDF_GITHUB_ASSETS, IDFCorePreferenceConstants.IDF_GITHUB_ASSETS_DEFAULT_GLOBAL);
		getPreferenceStore().setDefault(IDFCorePreferenceConstants.PIP_EXTRA_INDEX_URL, IDFCorePreferenceConstants.PIP_EXTRA_INDEX_URL_DEFAULT_GLOBAL);
		getPreferenceStore().setDefault(IDFCorePreferenceConstants.IDF_TOOLS_PATH, IDFCorePreferenceConstants.IDF_TOOLS_PATH_DEFAULT);
	}
}
