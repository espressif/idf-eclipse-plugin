/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.serial.monitor.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.espressif.idf.serial.monitor.SerialMonitorBundle;
import com.espressif.idf.serial.monitor.ui.Messages;

import ilg.gnumcueclipse.core.preferences.ScopedPreferenceStoreWithoutDefaults;

/**
 * Serial Monitor Configuration page class to handle changes to serial monitor console
 * 
 * @author Ali Azam Rana
 *
 */
public class SerialMonitorPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
	public static final String ID = "com.espressif.idf.serial.monitor.monitorPreferencesPage";

	public SerialMonitorPage()
	{
		super(GRID);
		setPreferenceStore(
				new ScopedPreferenceStoreWithoutDefaults(InstanceScope.INSTANCE, SerialMonitorBundle.PLUGIN_ID));
		setDescription(Messages.SerialMonitorPagePropertyPage_description);
	}

	@Override
	protected void createFieldEditors()
	{
		FieldEditor numberOfCharsInALine = new IntegerFieldEditor("numberOfCharsInALine",
				Messages.SerialMonitorPage_Field_NumberOfCharsInLine, getFieldEditorParent());
		numberOfCharsInALine.setPreferenceStore(getPreferenceStore());
		numberOfCharsInALine.load();
		addField(numberOfCharsInALine);
		FieldEditor numberOfLines = new IntegerFieldEditor("numberOfLines",
				Messages.SerialMonitorPage_Field_NumberOfLines, getFieldEditorParent());
		addField(numberOfLines);
	}

	@Override
	public void init(IWorkbench workbench)
	{
	}
}
