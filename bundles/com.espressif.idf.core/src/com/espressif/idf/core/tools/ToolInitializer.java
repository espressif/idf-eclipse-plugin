/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import com.espressif.idf.core.SystemExecutableFinder;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.exceptions.EimVersionMismatchException;
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
		if (!StringUtil.isEmpty(findEimOnSystemPath()))
		{
			return true;
		}

		String eimExePathEnv = idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.EIM_PATH);
		boolean exists = !StringUtil.isEmpty(eimExePathEnv) && Files.exists(Paths.get(eimExePathEnv));
		if (!exists)
		{
			// Fallback: well-known install locations (e.g. user home .espressif/eim_gui, /Applications on macOS)
			Path defaultEimPath = getDefaultEimPath();
			if (defaultEimPath != null)
			{
				exists = Files.exists(defaultEimPath);
			}
		}
		return exists;
	}

	/**
	 * Looks for an {@code eim} executable on the process {@code PATH}, using the same rules as other tools in this
	 * plugin ({@link SystemExecutableFinder}: PATHEXT on Windows, plain name on Linux/macOS).
	 *
	 * @return absolute path to the executable, or empty if not found
	 */
	private String findEimOnSystemPath()
	{
		IPath eimPath = new SystemExecutableFinder().find("eim"); //$NON-NLS-1$
		return eimPath != null ? eimPath.toOSString() : StringUtil.EMPTY;
	}

	/**
	 * Resolves the EIM executable path: <strong>system {@code PATH} first</strong>, then {@code eimPath} from
	 * {@code eim_idf.json} when the path exists on disk, then {@link #getDefaultEimPath()}.
	 *
	 * @param eimJson parsed JSON or {@code null}
	 * @return resolved absolute path string, or empty if nothing could be resolved
	 */
	public String resolveEimExecutablePath(EimJson eimJson)
	{
		String fromPath = findEimOnSystemPath();
		if (!StringUtil.isEmpty(fromPath))
		{
			return fromPath;
		}

		if (eimJson != null && !StringUtil.isEmpty(eimJson.getEimPath()))
		{
			String jsonPath = eimJson.getEimPath();
			if (Files.exists(Paths.get(jsonPath)))
			{
				return jsonPath;
			}
		}

		Path defaultEimPath = getDefaultEimPath();
		return defaultEimPath != null ? defaultEimPath.toString() : StringUtil.EMPTY;
	}
	
	public boolean isEimIdfJsonPresent()
	{
		String path = Platform.getOS().equals(Platform.OS_WIN32) ? EimConstants.EIM_WIN_PATH
				: EimConstants.EIM_POSIX_PATH;
		return new File(path).exists();
	}

	public EimJson loadEimJson() throws EimVersionMismatchException
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

	public IStatus exportOldConfig(Path eimPath) throws IOException
	{
		File oldConfig = getOldConfigFile();
		if (oldConfig.exists())
		{
			// eim import pathToOldConfigJson
			List<String> commands = new ArrayList<>();
			String eimPathStr = StringUtil.EMPTY;
			
			if (eimPath != null && Files.exists(eimPath))
			{
				eimPathStr = eimPath.toString();
			}
			else 
			{
				return new Status(IStatus.ERROR, IDFCorePlugin.getId(), -1, "Cannot Convert EIM is not installed", null); //$NON-NLS-1$
			}
			
			
			commands.add(eimPathStr);
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

	public Path getDefaultEimPath()
	{
		String userHome = System.getProperty("user.home"); //$NON-NLS-1$
        Path defaultEimPath;
        String os = Platform.getOS(); 
        if (os.equals(Platform.OS_WIN32))
        {
            defaultEimPath = Paths.get(userHome, ".espressif", "eim_gui", //$NON-NLS-1$//$NON-NLS-2$ 
            		"eim.exe"); //$NON-NLS-1$
        }
        else if (os.equals(Platform.OS_MACOSX))
        {
            defaultEimPath = Paths.get("/Applications", //$NON-NLS-1$
            		"eim.app", "Contents", //$NON-NLS-1$//$NON-NLS-2$
            		"MacOS", "eim"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else
        {
            defaultEimPath = Paths.get(userHome, ".espressif",  //$NON-NLS-1$
            		"eim_gui", "eim");  //$NON-NLS-1$//$NON-NLS-2$
        }
        
        return defaultEimPath;
	}
	
	public void findAndSetEimPath()
	{
		String resolved = resolveEimExecutablePath(null);
		if (!StringUtil.isEmpty(resolved))
		{
			setEimPathInEnvVar(resolved);
		}
	}
	
	private void setEimPathInEnvVar(String eimPath)
	{
		idfEnvironmentVariables.addEnvVariable(IDFEnvironmentVariables.EIM_PATH, eimPath);
	}
}
