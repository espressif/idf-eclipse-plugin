/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.vo.EimJson;

/**
 * Initializer class to be used on startup of eclipse and also
 * to help with tools initialization
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class ToolInitializer
{
	private final Preferences preferences;
	private final EimIdfConfiguratinParser parser;

	public ToolInitializer(Preferences preferences)
	{
		this.preferences = preferences;
		this.parser = new EimIdfConfiguratinParser();
	}

	public boolean isEimInstalled()
	{
		return isEimIdfJsonPresent();
	}

	public EimJson loadEimJson()
	{
		try
		{
			return parser.getEimJson(true);
		}
		catch (IOException e)
		{
			Logger.log(e);
			return null;
		}
	}

	public boolean isOldEspIdfConfigPresent()
	{
		return getOldConfigFile().exists();
	}

	public void exportOldConfigIfNeeded(String exportPath) throws IOException
	{
		File oldConfig = getOldConfigFile();
		if (oldConfig.exists())
		{
			File destinationFile = new File(exportPath, oldConfig.getName());
			Files.copy(oldConfig.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			preferences.putBoolean(EimConstants.OLD_CONFIG_EXPORTED_FLAG, true);
		}
	}

	public boolean isOldConfigExported()
	{
		return preferences.getBoolean(EimConstants.OLD_CONFIG_EXPORTED_FLAG, false);
	}

	private File getOldConfigFile()
	{
		IPath path = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		return new File(path.toOSString(), EimConstants.TOOL_SET_CONFIG_LEGACY_CONFIG_FILE);
	}

	private boolean isEimIdfJsonPresent()
	{
		String path = Platform.getOS().equals(Platform.OS_WIN32) ? EimConstants.EIM_WIN_PATH
				: EimConstants.EIM_POSIX_PATH;
		return new File(path).exists();
	}

	public boolean isEspIdfSet()
	{
		return preferences.getBoolean(EimConstants.INSTALL_TOOLS_FLAG, false);
	}
}
