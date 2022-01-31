package com.espressif.idf.ui.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.espressif.idf.ui.UIPlugin;

import ilg.gnumcueclipse.core.preferences.ScopedPreferenceStoreWithoutDefaults;

public class EspresssifPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final String NUMBER_OF_LINES = "numberOfLines"; //$NON-NLS-1$
	private static final String NUMBER_OF_CHARS_IN_A_LINE = "numberOfCharsInALine"; //$NON-NLS-1$
	private static final String GDB_SERVER_LAUNCH_TIMEOUT = "fGdbServerLaunchTimeout"; //$NON-NLS-1$

	public EspresssifPreferencesPage() {
		super();
		setPreferenceStore(new ScopedPreferenceStoreWithoutDefaults(InstanceScope.INSTANCE, UIPlugin.PLUGIN_ID));
		setDescription(Messages.EspresssifPreferencesPage_IDFSpecificPrefs);
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors()
	{
		FieldEditor fGdbServerLaunchTimeout = new IntegerFieldEditor(GDB_SERVER_LAUNCH_TIMEOUT,
				Messages.GDBServerTimeoutPage_TimeoutField, getFieldEditorParent());
		getPreferenceStore().setDefault(GDB_SERVER_LAUNCH_TIMEOUT, 25);
		fGdbServerLaunchTimeout.setPreferenceStore(getPreferenceStore());
		fGdbServerLaunchTimeout.load();
		addField(fGdbServerLaunchTimeout);
		
		getPreferenceStore().setDefault(NUMBER_OF_CHARS_IN_A_LINE, 500);
		getPreferenceStore().setDefault(NUMBER_OF_LINES, 1000);
		
		FieldEditor numberOfCharsInALine = new IntegerFieldEditor(NUMBER_OF_CHARS_IN_A_LINE,
				Messages.SerialMonitorPage_Field_NumberOfCharsInLine, getFieldEditorParent());
		numberOfCharsInALine.setPreferenceStore(getPreferenceStore());
		numberOfCharsInALine.load();
		addField(numberOfCharsInALine);
		FieldEditor numberOfLines = new IntegerFieldEditor(NUMBER_OF_LINES,
				Messages.SerialMonitorPage_Field_NumberOfLines, getFieldEditorParent());
		addField(numberOfLines);
		
	}
}
