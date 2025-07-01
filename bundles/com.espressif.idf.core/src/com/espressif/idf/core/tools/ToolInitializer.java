/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.vo.EimJson;
import com.espressif.idf.core.util.StringUtil;

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
	private IDFEnvironmentVariables idfEnvironmentVariables;

	public ToolInitializer(Preferences preferences)
	{
		this.preferences = preferences;
		this.parser = new EimIdfConfiguratinParser();
		idfEnvironmentVariables = new IDFEnvironmentVariables();
	}

	public boolean isEimInstalled()
	{
		String eimExePathEnv = idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.EIM_PATH);
		return !StringUtil.isEmpty(eimExePathEnv) && Files.exists(Paths.get(eimExePathEnv));
	}
	
	public boolean isEimIdfJsonPresent()
	{
		String path = Platform.getOS().equals(Platform.OS_WIN32) ? EimConstants.EIM_WIN_PATH
				: EimConstants.EIM_POSIX_PATH;
		return new File(path).exists();
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

	public IStatus exportOldConfig(String eimPath) throws IOException
	{
		File oldConfig = getOldConfigFile();
		if (oldConfig.exists())
		{
			// eim import pathToOldConfigJson
			List<String> commands = new ArrayList<>();
			commands.add(StringUtil.isEmpty(eimPath) ? 
					idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.EIM_PATH) : eimPath);
			commands.add("import"); //$NON-NLS-1$
			commands.add(oldConfig.getAbsolutePath());
			Logger.log("Running: " + commands.toString()); //$NON-NLS-1$
			ProcessBuilderFactory processBuilderFactory = new ProcessBuilderFactory();
			IStatus status = processBuilderFactory.runInBackground(commands, org.eclipse.core.runtime.Path.ROOT,
					System.getenv());
			
			Logger.log(status.getMessage());
			return status;			
		}
		
		return new Status(IStatus.ERROR, IDFCorePlugin.getId(), -1, "Error in conversion", null); //$NON-NLS-1$
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

	public boolean isEspIdfSet()
	{
		return preferences.getBoolean(EimConstants.INSTALL_TOOLS_FLAG, false);
	}
}
