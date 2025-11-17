/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.nvs.dialog;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.StringUtil;

/**
 * Manages loading and saving NvsEditorSettings to and from the Eclipse preference store for a specific project.
 */
public class NvsEditorPreferenceService
{
	private static final String PLUGIN_ID = "com.espressif.idf.core"; //$NON-NLS-1$
	private static final String PREF_PARTITION_SIZE = "nvsPartitionSize"; //$NON-NLS-1$
	private static final String PREF_ENCRYPT_ENABLED = "nvsEncryptEnabled"; //$NON-NLS-1$
	private static final String PREF_GENERATE_KEY_ENABLED = "nvsGenerateKeyEnabled"; //$NON-NLS-1$
	private static final String PREF_ENCRYPTION_KEY_PATH = "nvsEncryptionKeyPath"; //$NON-NLS-1$

	private IEclipsePreferences preferences;

	public NvsEditorPreferenceService(IProject project)
	{
		IScopeContext projectScope = new ProjectScope(project);
		this.preferences = projectScope.getNode(PLUGIN_ID);
	}

	/**
	 * Loads settings from the service and applies them to the UI.
	 */
	public NvsEditorSettings loadSettings()
	{
		// Read all values from the preference store first
		String partitionSize = preferences.get(PREF_PARTITION_SIZE, NvsEditorSettings.DEFAULT_PARTITION_SIZE);
		boolean encryptEnabled = preferences.getBoolean(PREF_ENCRYPT_ENABLED, false);
		boolean generateKeyEnabled = preferences.getBoolean(PREF_GENERATE_KEY_ENABLED, true);
		String encryptionKeyPath = preferences.get(PREF_ENCRYPTION_KEY_PATH, StringUtil.EMPTY);

		// Create the immutable record in one call
		return new NvsEditorSettings(partitionSize, encryptEnabled, generateKeyEnabled, encryptionKeyPath);
	}

	/**
	 * Saves the given settings to the project's preferences.
	 */
	public void saveSettings(NvsEditorSettings settings)
	{
		preferences.put(PREF_PARTITION_SIZE, settings.partitionSize());
		preferences.putBoolean(PREF_ENCRYPT_ENABLED, settings.encryptEnabled());
		preferences.putBoolean(PREF_GENERATE_KEY_ENABLED, settings.generateKeyEnabled());
		preferences.put(PREF_ENCRYPTION_KEY_PATH, settings.encryptionKeyPath());

		try
		{
			preferences.flush();
		}
		catch (BackingStoreException e)
		{
			Logger.log(e);
		}
	}
}