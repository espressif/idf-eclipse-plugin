/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.eim.terminal;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.terminal.connector.ISettingsStore;
import org.eclipse.terminal.connector.ITerminalConnector;
import org.eclipse.terminal.connector.InMemorySettingsStore;
import org.eclipse.terminal.connector.TerminalConnectorExtension;
import org.eclipse.terminal.connector.process.ProcessSettings;
import org.eclipse.terminal.view.core.ITerminalsConnectorConstants;
import org.eclipse.terminal.view.ui.launcher.AbstractConfigurationPanel;
import org.eclipse.terminal.view.ui.launcher.AbstractLauncherDelegate;
import org.eclipse.terminal.view.ui.launcher.IConfigurationPanel;
import org.eclipse.terminal.view.ui.launcher.IConfigurationPanelContainer;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.tools.Messages;

/**
 * Opens a local terminal tab running the EIM CLI wizard.
 *
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 */
public final class EimCliTerminalLauncherDelegate extends AbstractLauncherDelegate
{
	/** Must match {@code delegate} id in {@code plugin.xml}. */
	public static final String DELEGATE_ID = "com.espressif.idf.ui.launcher.eimCliTerminal"; //$NON-NLS-1$

	public static final String CONNECTOR_ID = "com.espressif.idf.ui.eimCliTerminalConnector"; //$NON-NLS-1$
	/** Map key for the absolute path to the EIM executable. */
	public static final String PROP_EIM_EXECUTABLE = "com.espressif.idf.ui.eimCli.executable"; //$NON-NLS-1$

	@Override
	public boolean needsUserConfiguration()
	{
		return false;
	}

	@Override
	public IConfigurationPanel getPanel(IConfigurationPanelContainer container)
	{
		return new EimCliTerminalEmptyConfigPanel(container);
	}

	/**
	 * Minimal configuration panel (not shown when the launcher is invoked programmatically).
	 */
	private static final class EimCliTerminalEmptyConfigPanel extends AbstractConfigurationPanel
	{
		EimCliTerminalEmptyConfigPanel(IConfigurationPanelContainer container)
		{
			super(container);
		}

		@Override
		public void setupPanel(Composite parent)
		{
			setControl(new Composite(parent, SWT.NONE));
		}
	}

	@Override
	public CompletableFuture<?> execute(Map<String, Object> properties)
	{
		if (!properties.containsKey(ITerminalsConnectorConstants.PROP_DELEGATE_ID))
		{
			properties.put(ITerminalsConnectorConstants.PROP_DELEGATE_ID, DELEGATE_ID);
		}
		if (!properties.containsKey(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID))
		{
			properties.put(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID, CONNECTOR_ID);
		}
		if (!properties.containsKey(ITerminalsConnectorConstants.PROP_TITLE))
		{
			properties.put(ITerminalsConnectorConstants.PROP_TITLE, Messages.EimCliTerminalWizardTitle);
		}
		if (!properties.containsKey(ITerminalsConnectorConstants.PROP_FORCE_NEW))
		{
			properties.put(ITerminalsConnectorConstants.PROP_FORCE_NEW, Boolean.TRUE);
		}
		return getTerminalService().openConsole(properties);
	}

	@Override
	public ITerminalConnector createTerminalConnector(Map<String, Object> properties)
	{
		String eimPath = (String) properties.get(PROP_EIM_EXECUTABLE);
		if (eimPath == null || eimPath.isBlank())
		{
			Logger.log("EIM CLI terminal: missing executable path property"); //$NON-NLS-1$
			return null;
		}
		Path exe = Path.of(eimPath);
		if (!Files.exists(exe) || Files.isDirectory(exe))
		{
			Logger.log("EIM CLI terminal: executable not found: " + eimPath); //$NON-NLS-1$
			return null;
		}

		ProcessSettings processSettings = new ProcessSettings();
		processSettings.setLocalEcho(false);

		String userHome = System.getProperty("user.home"); //$NON-NLS-1$
		processSettings.setWorkingDir(userHome != null && !userHome.isBlank() ? userHome : "."); //$NON-NLS-1$

		// Build a merged environment: System.getenv() + CDT build env + package-manager PATH entries
		Map<String, String> envMap = new IDFEnvironmentVariables().getSystemEnvMap();
		enrichPathWithPackageManagers(envMap);
		String[] envArray = envMap.entrySet().stream()
				.map(e -> e.getKey() + "=" + e.getValue()) //$NON-NLS-1$
				.toArray(String[]::new);
		processSettings.setEnvironment(envArray);
		processSettings.setMergeWithNativeEnvironment(false);

		// Launch a login shell so user profiles (.zprofile, .bash_profile) are sourced
		String os = Platform.getOS();
		if (Platform.OS_WIN32.equals(os))
		{
			processSettings.setImage("powershell.exe"); //$NON-NLS-1$
		}
		else
		{
			processSettings.setImage(resolvePosixShell());
			processSettings.setArguments("-l"); //$NON-NLS-1$
		}

		try
		{
			ITerminalConnector connector = TerminalConnectorExtension.makeTerminalConnector(CONNECTOR_ID);
			if (connector != null)
			{
				ISettingsStore store = new InMemorySettingsStore();
				processSettings.save(store);
				// Round-trip the EIM path so EimCliTerminalConnector.load() can read it
				store.put(EimCliTerminalConnector.SETTINGS_KEY_EIM_PATH, eimPath);
				connector.setDefaultSettings();
				connector.load(store);
				return connector;
			}
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return null;
	}

	/**
	 * Prepends well-known package-manager bin directories to PATH so tools like git, python, cmake are visible to EIM.
	 * Directories are only added if they exist on disk and are not already present in PATH.
	 */
	private static void enrichPathWithPackageManagers(Map<String, String> envMap)
	{
		String os = Platform.getOS();
		String home = System.getProperty("user.home"); //$NON-NLS-1$
		List<String> extraDirs = new ArrayList<>();

		if (Platform.OS_MACOSX.equals(os))
		{
			// Homebrew Apple Silicon
			extraDirs.add("/opt/homebrew/bin"); //$NON-NLS-1$
			extraDirs.add("/opt/homebrew/sbin"); //$NON-NLS-1$
			// Homebrew Intel
			extraDirs.add("/usr/local/bin"); //$NON-NLS-1$
			extraDirs.add("/usr/local/sbin"); //$NON-NLS-1$
			// MacPorts
			extraDirs.add("/opt/local/bin"); //$NON-NLS-1$
			extraDirs.add("/opt/local/sbin"); //$NON-NLS-1$
		}
		else if (Platform.OS_LINUX.equals(os))
		{
			// apt / pacman / dnf — install to standard system paths; ensure they are present
			extraDirs.add("/usr/local/bin"); //$NON-NLS-1$
			extraDirs.add("/usr/bin"); //$NON-NLS-1$
			// pip / pipx user-local installs
			if (home != null)
			{
				extraDirs.add(home + "/.local/bin"); //$NON-NLS-1$
			}
			// Snap packages
			extraDirs.add("/snap/bin"); //$NON-NLS-1$
			// Flatpak exports
			extraDirs.add("/var/lib/flatpak/exports/bin"); //$NON-NLS-1$
			if (home != null)
			{
				extraDirs.add(home + "/.local/share/flatpak/exports/bin"); //$NON-NLS-1$
			}
			// Nix package manager
			if (home != null)
			{
				extraDirs.add(home + "/.nix-profile/bin"); //$NON-NLS-1$
			}
			extraDirs.add("/nix/var/nix/profiles/default/bin"); //$NON-NLS-1$
		}
		else if (Platform.OS_WIN32.equals(os))
		{
			String localAppData = System.getenv("LOCALAPPDATA"); //$NON-NLS-1$
			if (localAppData != null)
			{
				// WinGet shim links
				extraDirs.add(localAppData + "\\Microsoft\\WinGet\\Links"); //$NON-NLS-1$
			}
			// Chocolatey
			String chocoInstall = System.getenv("ChocolateyInstall"); //$NON-NLS-1$
			if (chocoInstall != null && !chocoInstall.isBlank())
			{
				extraDirs.add(chocoInstall + "\\bin"); //$NON-NLS-1$
			}
			else
			{
				extraDirs.add("C:\\ProgramData\\chocolatey\\bin"); //$NON-NLS-1$
			}
			// Scoop
			if (home != null)
			{
				extraDirs.add(home + "\\scoop\\shims"); //$NON-NLS-1$
			}
		}

		if (extraDirs.isEmpty())
		{
			return;
		}

		// Find the PATH key (case-insensitive for Windows where it can be "Path" or "PATH")
		String pathKey = "PATH"; //$NON-NLS-1$
		for (String key : envMap.keySet())
		{
			if (key.equalsIgnoreCase(pathKey))
			{
				pathKey = key;
				break;
			}
		}

		String existing = envMap.getOrDefault(pathKey, ""); //$NON-NLS-1$
		String sep = File.pathSeparator;

		Set<String> currentEntries = new LinkedHashSet<>(List.of(existing.split(sep)));
		StringBuilder sb = new StringBuilder();
		for (String dir : extraDirs)
		{
			if (!currentEntries.contains(dir) && Files.isDirectory(Path.of(dir)))
			{
				sb.append(dir).append(sep);
				currentEntries.add(dir);
			}
		}
		sb.append(existing);
		envMap.put(pathKey, sb.toString());
	}

	private static String resolvePosixShell()
	{
		Path bash = Path.of("/bin/bash"); //$NON-NLS-1$
		if (Files.isExecutable(bash))
		{
			return bash.toString();
		}
		String envShell = System.getenv("SHELL"); //$NON-NLS-1$
		if (envShell != null && !envShell.isBlank() && Files.isExecutable(Path.of(envShell)))
		{
			return envShell;
		}
		return "/bin/sh"; //$NON-NLS-1$
	}
}
