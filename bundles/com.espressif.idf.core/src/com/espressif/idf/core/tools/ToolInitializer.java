/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
		return !StringUtil.isEmpty(resolveEimExecutablePath(null));
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
	 * Probes well-known package manager bin directories for the {@code eim} executable. GUI-launched Eclipse processes
	 * on macOS/Linux often have a minimal PATH that excludes directories like {@code /opt/homebrew/bin}, so this method
	 * checks those locations directly regardless of the JVM's process PATH.
	 *
	 * @return absolute path to the executable, or empty if not found in any known location
	 */
	private String findEimInPackageManagerPaths()
	{
		String os = Platform.getOS();
		String home = System.getProperty("user.home"); //$NON-NLS-1$
		boolean isWindows = Platform.OS_WIN32.equals(os);
		String execName = isWindows ? "eim.exe" : "eim"; //$NON-NLS-1$ //$NON-NLS-2$

		List<String> candidateDirs = new ArrayList<>();

		if (Platform.OS_MACOSX.equals(os))
		{
			candidateDirs.add("/opt/homebrew/bin"); //$NON-NLS-1$
			candidateDirs.add("/usr/local/bin"); //$NON-NLS-1$
			candidateDirs.add("/opt/local/bin"); //$NON-NLS-1$
		}
		else if (Platform.OS_LINUX.equals(os))
		{
			candidateDirs.add("/usr/local/bin"); //$NON-NLS-1$
			candidateDirs.add("/usr/bin"); //$NON-NLS-1$
			if (home != null)
			{
				candidateDirs.add(home + "/.local/bin"); //$NON-NLS-1$
			}
			candidateDirs.add("/snap/bin"); //$NON-NLS-1$
			candidateDirs.add("/var/lib/flatpak/exports/bin"); //$NON-NLS-1$
			if (home != null)
			{
				candidateDirs.add(home + "/.local/share/flatpak/exports/bin"); //$NON-NLS-1$
				candidateDirs.add(home + "/.nix-profile/bin"); //$NON-NLS-1$
			}
			candidateDirs.add("/nix/var/nix/profiles/default/bin"); //$NON-NLS-1$
		}
		else if (isWindows)
		{
			String localAppData = System.getenv("LOCALAPPDATA"); //$NON-NLS-1$
			if (localAppData != null)
			{
				candidateDirs.add(localAppData + "\\Microsoft\\WinGet\\Links"); //$NON-NLS-1$
			}
			String chocoInstall = System.getenv("ChocolateyInstall"); //$NON-NLS-1$
			if (chocoInstall != null && !chocoInstall.isBlank())
			{
				candidateDirs.add(chocoInstall + "\\bin"); //$NON-NLS-1$
			}
			else
			{
				candidateDirs.add("C:\\ProgramData\\chocolatey\\bin"); //$NON-NLS-1$
			}
			if (home != null)
			{
				candidateDirs.add(home + "\\scoop\\shims"); //$NON-NLS-1$
			}
		}

		for (String dir : candidateDirs)
		{
			Path candidate = Paths.get(dir, execName);
			if (Files.isRegularFile(candidate) && Files.isExecutable(candidate))
			{
				return candidate.toString();
			}
		}

		return StringUtil.EMPTY;
	}

	/**
	 * Resolves the EIM executable path using priority-based resolution:
	 * <ol>
	 * <li>System {@code PATH}</li>
	 * <li>Well-known package manager directories (Homebrew, MacPorts, WinGet, Chocolatey, Scoop, etc.)</li>
	 * <li>{@code eimPath} from {@code eim_idf.json} (when the path exists on disk)</li>
	 * <li>{@code EIM_PATH} env variable (existence-checked)</li>
	 * <li>Default GUI install location (existence-checked)</li>
	 * <li>Default CLI install location (existence-checked)</li>
	 * </ol>
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

		String fromPkgMgr = findEimInPackageManagerPaths();
		if (!StringUtil.isEmpty(fromPkgMgr))
		{
			return fromPkgMgr;
		}

		if (eimJson != null && !StringUtil.isEmpty(eimJson.getEimPath()))
		{
			String jsonPath = eimJson.getEimPath();
			if (Files.exists(Paths.get(jsonPath)))
			{
				return jsonPath;
			}
		}

		String eimExePathEnv = idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.EIM_PATH);
		if (!StringUtil.isEmpty(eimExePathEnv) && Files.exists(Paths.get(eimExePathEnv)))
		{
			return eimExePathEnv;
		}

		Path defaultEimPath = getDefaultEimPath();
		if (defaultEimPath != null && Files.exists(defaultEimPath))
		{
			return defaultEimPath.toString();
		}

		Path cliEimPath = getDefaultCliEimPath();
		if (cliEimPath != null && Files.exists(cliEimPath))
		{
			return cliEimPath.toString();
		}

		return StringUtil.EMPTY;
	}
	
	public boolean isEimIdfJsonPresent()
	{
		Path path = new EimIdfJsonPathResolver().resolveEimIdfJsonFile();
		return Files.isRegularFile(path) && Files.isReadable(path);
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
			if (!Files.exists(defaultEimPath))
			{
				Path eimGuiDir = Paths.get(userHome, ".espressif", "eim_gui"); //$NON-NLS-1$ //$NON-NLS-2$
				if (Files.isDirectory(eimGuiDir))
				{
					try (var entries = Files.list(eimGuiDir))
					{
						Path found = entries
								.filter(Files::isRegularFile)
								.filter(p -> p.getFileName().toString().toLowerCase().startsWith("eim") //$NON-NLS-1$
										&& p.getFileName().toString().toLowerCase().endsWith(".exe")) //$NON-NLS-1$
								.findFirst()
								.orElse(null);
						if (found != null)
						{
							return found;
						}
					}
					catch (IOException e)
					{
						Logger.log(e);
					}
				}
			}
		}
		else if (os.equals(Platform.OS_MACOSX))
		{
			defaultEimPath = Paths.get("/Applications", //$NON-NLS-1$
					"eim.app", "Contents", //$NON-NLS-1$//$NON-NLS-2$
					"MacOS", "eim"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
		{
			defaultEimPath = Paths.get(userHome, ".espressif", //$NON-NLS-1$
					"eim_gui", "eim"); //$NON-NLS-1$//$NON-NLS-2$
		}

		return defaultEimPath;
	}

	/**
	 * Returns the default CLI EIM binary path per platform. Unlike the GUI path, this points to the CLI-only install
	 * directory ({@code ~/.espressif/eim/}).
	 */
	public Path getDefaultCliEimPath()
	{
		String userHome = System.getProperty("user.home"); //$NON-NLS-1$
		String os = Platform.getOS();
		if (os.equals(Platform.OS_WIN32))
		{
			return Paths.get(userHome, ".espressif", "eim", "eim.exe"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return Paths.get(userHome, ".espressif", "eim", "eim"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Checks whether the EIM binary at the given path supports GUI mode by running {@code eim gui --help} and checking
	 * for a successful exit code. This is used to determine whether to launch EIM as a GUI application or in CLI/wizard
	 * mode.
	 *
	 * @param eimPath absolute path to the EIM executable
	 * @return {@code true} if the binary supports the {@code gui} subcommand, {@code false} otherwise
	 */
	public boolean isEimGuiCapable(String eimPath)
	{
		if (StringUtil.isEmpty(eimPath))
		{
			return false;
		}

		try
		{
			ProcessBuilder pb = new ProcessBuilder(eimPath, "gui", "--help"); //$NON-NLS-1$ //$NON-NLS-2$
			Logger.log("Checking if EIM supports GUI mode with command: " + String.join(" ", pb.command())); //$NON-NLS-1$ //$NON-NLS-2$
			pb.redirectErrorStream(true);
			Process process = pb.start();
			// Drain stdout so the process doesn't block
			process.getInputStream().transferTo(OutputStream.nullOutputStream());
			boolean finished = process.waitFor(5, TimeUnit.SECONDS);
			if (!finished)
			{
				process.destroyForcibly();
				return false;
			}
			return process.exitValue() == 0;
		}
		catch (IOException | InterruptedException e)
		{
			Logger.log("EIM does not support the gui subcommand, falling back to CLI mode."); //$NON-NLS-1$
			return false;
		}
	}

}
