package com.espressif.idf.ui.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.embedcdt.ui.preferences.ScopedPreferenceStoreWithoutDefaults;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFCorePreferenceConstants;
import com.espressif.idf.core.logging.Logger;

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

	public EspresssifPreferencesPage()
	{
		super();
		setPreferenceStore(new ScopedPreferenceStoreWithoutDefaults(InstanceScope.INSTANCE, IDFCorePlugin.PLUGIN_ID));
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

		return mainComposite;
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
	}
}
