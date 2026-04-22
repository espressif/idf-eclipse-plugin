/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFCorePreferenceConstants;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class EimIdfJsonPathResolver
{
	public Path resolveEimIdfJsonFile()
	{
		String custom = InstanceScope.INSTANCE.getNode(IDFCorePlugin.PLUGIN_ID)
				.get(IDFCorePreferenceConstants.EIM_IDF_JSON_PATH, ""); //$NON-NLS-1$
		return resolveEimIdfJsonFileFromPreferenceString(custom);
	}

	public Path resolveEimIdfJsonFileFromPreferenceString(String custom)
	{
		if (custom != null && !custom.isEmpty())
		{
			Path p = Paths.get(custom);
			if (Files.exists(p))
			{
				return p;
			}
		}
		return getDefaultEimIdfJsonFile();
	}

	public String getDefaultEimIdfJsonPathString()
	{
		return Platform.getOS().equals(Platform.OS_WIN32) ? EimConstants.EIM_WIN_PATH : EimConstants.EIM_POSIX_PATH;
	}

	public Path getDefaultEimIdfJsonFile()
	{
		return Paths.get(getDefaultEimIdfJsonPathString());
	}
}
