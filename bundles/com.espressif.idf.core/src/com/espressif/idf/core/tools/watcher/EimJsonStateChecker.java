/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.watcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;

import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.EimIdfJsonPathResolver;

/**
 * Checks if eim_idf.json was changed while Eclipse was not running. Stores and compares last seen size and hash to
 * determine if actual content has changed. * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class EimJsonStateChecker
{
	private static final String PREF_LAST_SEEN_SIZE = "lastEimJsonSize"; //$NON-NLS-1$
	private static final String PREF_LAST_SEEN_HASH = "lastEimJsonHash"; //$NON-NLS-1$

	private final Preferences preferences;

	public EimJsonStateChecker(Preferences preferences)
	{
		this.preferences = preferences;
	}

	public boolean wasModifiedSinceLastRun()
	{
		Path jsonPath = new EimIdfJsonPathResolver().resolveEimIdfJsonFile();
		if (!Files.exists(jsonPath))
		{
			return false;
		}

		try
		{
			long lastSeenSize = preferences.getLong(PREF_LAST_SEEN_SIZE, -1L);
			String lastSeenHash = preferences.get(PREF_LAST_SEEN_HASH, ""); //$NON-NLS-1$

			if (lastSeenSize == -1L || lastSeenHash.isEmpty())
			{
				// First run ever, don't treat as changed
				Logger.log("eim_idf.json detected, but no last seen state — assuming first run."); //$NON-NLS-1$
				return false;
			}

			// 1. Fast-fail check: If size is different, it was definitely modified
			long currentSize = Files.size(jsonPath);
			if (currentSize != lastSeenSize)
			{
				return true;
			}

			// 2. Deep check: If size is the same, verify the hash
			String currentHash = computeHashBase64(jsonPath);
			return !currentHash.equals(lastSeenHash);
		}
		catch (Exception e)
		{
			Logger.log("Failed to check if eim_idf.json was modified since last run"); //$NON-NLS-1$
			Logger.log(e);
			// Default to false on error to prevent unwanted popups/refreshes
			return false;
		}
	}

	public void updateLastSeenState()
	{
		Path jsonPath = new EimIdfJsonPathResolver().resolveEimIdfJsonFile();
		if (Files.exists(jsonPath))
		{
			try
			{
				long size = Files.size(jsonPath);
				String hash = computeHashBase64(jsonPath);

				preferences.putLong(PREF_LAST_SEEN_SIZE, size);
				preferences.put(PREF_LAST_SEEN_HASH, hash);
			}
			catch (Exception e)
			{
				Logger.log("Failed to update last seen state for eim_idf.json"); //$NON-NLS-1$
				Logger.log(e);
			}
		}
	}

	private String computeHashBase64(Path filePath) throws Exception
	{
		MessageDigest digest = MessageDigest.getInstance("SHA-256"); //$NON-NLS-1$
		byte[] fileBytes = Files.readAllBytes(filePath);
		byte[] hash = digest.digest(fileBytes);
		return Base64.getEncoder().encodeToString(hash);
	}
}
