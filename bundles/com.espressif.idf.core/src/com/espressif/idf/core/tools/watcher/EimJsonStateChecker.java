package com.espressif.idf.core.tools.watcher;

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.EimConstants;

/**
 * Checks if eim_idf.json was changed while Eclipse was not running. Stores and compares last seen timestamp to file
 * system's last modified.
 * 
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class EimJsonStateChecker
{
	private static final String LAST_MODIFIED_PREF_KEY = "lastEimJsonModified"; //$NON-NLS-1$

	private final Preferences preferences;

	public EimJsonStateChecker(Preferences preferences)
	{
		this.preferences = preferences;
	}

	public boolean wasModifiedSinceLastRun()
	{
		File jsonFile = new File(getEimJsonPath());
		if (!jsonFile.exists())
		{
			return false;
		}

		long lastModified = jsonFile.lastModified();
		long lastSeen = preferences.getLong(LAST_MODIFIED_PREF_KEY, 0L);

		if (lastSeen == 0L)
		{
			// First run ever, don't treat as changed
			Logger.log("eim_idf.json detected, but no last seen timestamp — assuming first run."); //$NON-NLS-1$
			return false;
		}

		return lastModified > lastSeen;
	}

	public void updateLastSeenTimestamp()
	{
		File jsonFile = new File(getEimJsonPath());
		if (jsonFile.exists())
		{
			preferences.putLong(LAST_MODIFIED_PREF_KEY, jsonFile.lastModified());
		}
	}

	private String getEimJsonPath()
	{
		return Platform.getOS().equals(Platform.OS_WIN32) ? EimConstants.EIM_WIN_PATH : EimConstants.EIM_POSIX_PATH;
	}
}
