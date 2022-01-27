/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.espressif.idf.debug.gdbjtag.openocd.Activator;

import ilg.gnumcueclipse.core.preferences.ScopedPreferenceStoreWithoutDefaults;

/**
 * Preferences page for setting the launch timeout
 * @author Ali Azam Rana
 *
 */
public class GDBServerLaunchTimeoutPreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
	public static final String ID = "com.espressif.idf.serial.monitor.monitorPreferencesPage"; //$NON-NLS-1$

	public GDBServerLaunchTimeoutPreferences()
	{
		super(GRID);
		setPreferenceStore(new ScopedPreferenceStoreWithoutDefaults(InstanceScope.INSTANCE, Activator.PLUGIN_ID));
		setDescription(Messages.GDBServerTimeoutPage_Description);
	}

	@Override
	protected void createFieldEditors()
	{
		FieldEditor fGdbServerLaunchTimeout = new IntegerFieldEditor(Activator.GDB_SERVER_LAUNCH_TIMEOUT,
				Messages.GDBServerTimeoutPage_TimeoutField, getFieldEditorParent());
		getPreferenceStore().setDefault(Activator.GDB_SERVER_LAUNCH_TIMEOUT, 25);
		fGdbServerLaunchTimeout.setPreferenceStore(getPreferenceStore());
		fGdbServerLaunchTimeout.load();
		addField(fGdbServerLaunchTimeout);
	}

	@Override
	public void init(IWorkbench workbench)
	{
	}
}
