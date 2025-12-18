/*******************************************************************************
 * Copyright (c) 2012, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * Dirk Fauth <dirk.fauth@googlemail.com> - Bug 460496
 * Kondal Kolipaka <kkolipaka@espressif.com> - ESP-IDF Console implementation
 *******************************************************************************/
package com.espressif.idf.terminal.connector.launcher;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.terminal.connector.ISettingsStore;
import org.eclipse.terminal.connector.ITerminalConnector;
import org.eclipse.terminal.connector.InMemorySettingsStore;
import org.eclipse.terminal.connector.TerminalConnectorExtension;
import org.eclipse.terminal.connector.process.ProcessSettings;
import org.eclipse.terminal.view.core.ILineSeparatorConstants;
import org.eclipse.terminal.view.core.ITerminalServiceOutputStreamMonitorListener;
import org.eclipse.terminal.view.core.ITerminalsConnectorConstants;
import org.eclipse.terminal.view.ui.IMementoHandler;
import org.eclipse.terminal.view.ui.IPreferenceKeys;
import org.eclipse.terminal.view.ui.launcher.AbstractLauncherDelegate;
import org.eclipse.terminal.view.ui.launcher.IConfigurationPanel;
import org.eclipse.terminal.view.ui.launcher.IConfigurationPanelContainer;
import org.eclipse.ui.WorkbenchEncoding;
import org.osgi.framework.Bundle;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.terminal.connector.activator.UIPlugin;
import com.espressif.idf.terminal.connector.controls.IDFConsoleWizardConfigurationPanel;

/**
 * Serial launcher delegate implementation.
 */
public class IDFConsoleLauncherDelegate extends AbstractLauncherDelegate {

	private static final String ESP_IDF_CONSOLE_CONNECTOR_ID = "com.espressif.idf.terminal.connector.espidfConnector"; //$NON-NLS-1$
	private final IMementoHandler mementoHandler = new IDFConsoleMementoHandler();

	@Override
	public boolean needsUserConfiguration() {
		return false;
	}

	@Override
	public IConfigurationPanel getPanel(IConfigurationPanelContainer container) {
		return new IDFConsoleWizardConfigurationPanel(container);
	}

	@Override
	public CompletableFuture<?> execute(Map<String, Object> properties) {

		Assert.isNotNull(properties);

		// Set the terminal tab title
		setTerminalTitle(properties);

		// If not configured, set the default encodings for the local terminal
		if (!properties.containsKey(ITerminalsConnectorConstants.PROP_ENCODING)) {
			String encoding = null;
			// Set the default encoding:
			// Default UTF-8 on Mac or Windows for Local, Preferences:Platform encoding
			// otherwise
			if (Platform.OS_MACOSX.equals(Platform.getOS()) || Platform.OS_WIN32.equals(Platform.getOS())) {
				encoding = "UTF-8"; //$NON-NLS-1$
			} else {
				encoding = WorkbenchEncoding.getWorkbenchDefaultEncoding();
			}
			if (encoding != null && !"".equals(encoding)) //$NON-NLS-1$
				properties.put(ITerminalsConnectorConstants.PROP_ENCODING, encoding);
		}

		// For local terminals, force a new terminal tab each time it is launched,
		// if not set otherwise from outside
		if (!properties.containsKey(ITerminalsConnectorConstants.PROP_FORCE_NEW)) {
			properties.put(ITerminalsConnectorConstants.PROP_FORCE_NEW, Boolean.TRUE);
		}

		// Initialize the local terminal working directory if not already set by the panel.
		// By default, start the local terminal in the users home directory
		if (!properties.containsKey(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR)) {
			String initialCwd = Platform.getPreferencesService().getString("org.eclipse.terminal.view.ui", //$NON-NLS-1$
					IPreferenceKeys.PREF_LOCAL_TERMINAL_INITIAL_CWD, null, null);
			String cwd = null;
			if (initialCwd == null || IPreferenceKeys.PREF_INITIAL_CWD_USER_HOME.equals(initialCwd)
					|| "".equals(initialCwd.trim())) { //$NON-NLS-1$
				cwd = System.getProperty("user.home"); //$NON-NLS-1$
			} else if (IPreferenceKeys.PREF_INITIAL_CWD_ECLIPSE_HOME.equals(initialCwd)) {
				String eclipseHomeLocation = System.getProperty("eclipse.home.location"); //$NON-NLS-1$
				if (eclipseHomeLocation != null) {
					try {
						URI uri = URIUtil.fromString(eclipseHomeLocation);
						File f = URIUtil.toFile(uri);
						cwd = f.getAbsolutePath();
					} catch (URISyntaxException ex) {
						/* ignored on purpose */ }
				}
			} else if (IPreferenceKeys.PREF_INITIAL_CWD_ECLIPSE_WS.equals(initialCwd)) {
				Bundle bundle = Platform.getBundle("org.eclipse.core.resources"); //$NON-NLS-1$
				if (bundle != null && bundle.getState() != Bundle.UNINSTALLED && bundle.getState() != Bundle.STOPPING) {
					if (org.eclipse.core.resources.ResourcesPlugin.getWorkspace() != null
							&& org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot() != null
							&& org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot()
									.getLocation() != null) {
						cwd = org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getLocation()
								.toOSString();
					}
				}
			} else {
				try {
					// Resolve possible dynamic variables
					IStringVariableManager vm = VariablesPlugin.getDefault().getStringVariableManager();
					String resolved = vm.performStringSubstitution(initialCwd);

					IPath p = new Path(resolved);
					if (p.toFile().canRead() && p.toFile().isDirectory()) {
						cwd = p.toOSString();
					}
				} catch (CoreException ex) {
					if (Platform.inDebugMode()) {
						UIPlugin.getDefault().getLog().log(ex.getStatus());
					}
				}
			}

			if (cwd != null && !"".equals(cwd)) { //$NON-NLS-1$
				properties.put(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR, cwd);
			}
		}

		// Get the terminal service
		try {
			return getTerminalService().openConsole(properties);
		} catch (RuntimeException e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	/**
	 * Setting the terminal title.
	 * <p>
	 * @param properties 
	 *
	 * @return void
	 */
	private void setTerminalTitle(Map<String, Object> properties) {
		if (properties.containsKey(ITerminalsConnectorConstants.PROP_TITLE)) {
			var projectName = properties.get(ITerminalsConnectorConstants.PROP_TITLE);
			properties.put(ITerminalsConnectorConstants.PROP_TITLE,
					String.format("%s (%s)", projectName, Messages.IDFConsoleLauncherDelegate_ESPIDFTerminal)); //$NON-NLS-1$
		} else {
			properties.put(ITerminalsConnectorConstants.PROP_TITLE, Messages.IDFConsoleLauncherDelegate_ESPIDFTerminal);
		}
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (IMementoHandler.class.equals(adapter)) {
			return adapter.cast(mementoHandler);
		}
		return super.getAdapter(adapter);
	}

	/**
	 * Returns the default shell to launch. Looks at the environment
	 * variable "SHELL" first before assuming some default default values.
	 *
	 * @return The default shell to launch.
	 */
	private final File defaultShell() {
		String shell = null;
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			if (System.getenv("ComSpec") != null && !"".equals(System.getenv("ComSpec").trim())) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				shell = System.getenv("ComSpec").trim(); //$NON-NLS-1$
			} else {
				shell = "cmd.exe"; //$NON-NLS-1$
			}
		}
		if (shell == null) {
			shell = Platform.getPreferencesService().getString("org.eclipse.terminal.view.ui", //$NON-NLS-1$
					IPreferenceKeys.PREF_LOCAL_TERMINAL_DEFAULT_SHELL_UNIX, null, null);
			if (shell == null || "".equals(shell)) { //$NON-NLS-1$
				if (System.getenv("SHELL") != null && !"".equals(System.getenv("SHELL").trim())) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					shell = System.getenv("SHELL").trim(); //$NON-NLS-1$
				} else {
					shell = "/bin/sh"; //$NON-NLS-1$
				}
			}
		}

		return new File(shell);
	}

	@Override
	public ITerminalConnector createTerminalConnector(Map<String, Object> properties) {
		Assert.isNotNull(properties);

		// Check for the terminal connector id
		String connectorId = (String) properties.get(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID);
		if (connectorId == null)
			connectorId = ESP_IDF_CONSOLE_CONNECTOR_ID;

		// Extract the process properties using defaults
		String image;
		if (!properties.containsKey(ITerminalsConnectorConstants.PROP_PROCESS_PATH)
				|| properties.get(ITerminalsConnectorConstants.PROP_PROCESS_PATH) == null) {
			File defaultShell = defaultShell();
			image = defaultShell.isAbsolute() ? defaultShell.getAbsolutePath() : defaultShell.getPath();
		} else {
			image = (String) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_PATH);
		}

		String arguments = (String) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_ARGS);
		if (arguments == null && !Platform.OS_WIN32.equals(Platform.getOS())) {
			arguments = Platform.getPreferencesService().getString("org.eclipse.terminal.view.ui", //$NON-NLS-1$
					IPreferenceKeys.PREF_LOCAL_TERMINAL_DEFAULT_SHELL_UNIX_ARGS, null, null);
		}

		// Avoding profiles to isolate PATH enviroment
		arguments = ""; //$NON-NLS-1$
		if (image.contains("bash")) { //$NON-NLS-1$
			arguments = "--noprofile --norc"; //$NON-NLS-1$
		} else if (image.contains("zsh")) { //$NON-NLS-1$
			arguments = "--no-rcs --no-globalrcs"; //$NON-NLS-1$
		} else if (image.contains("powershell")) { //$NON-NLS-1$
			arguments = "-NoProfile"; //$NON-NLS-1$
		} else if (Platform.OS_WIN32.equals(Platform.getOS()) && image.contains("cmd.exe")) { //$NON-NLS-1$
			// This is the new part that rewrites the arguments for cmd.exe
			String title = (String) properties.get(ITerminalsConnectorConstants.PROP_TITLE);
			if (title != null && !title.isEmpty()) {
				String safeTitle = title.replaceAll("[\\r\\n\"&|<>^]", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$
				arguments = "/c \"title " + safeTitle + " && cmd.exe\""; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		// Determine if a PTY will be used
		boolean isUsingPTY = (properties.get(ITerminalsConnectorConstants.PROP_PROCESS_OBJ) == null
				&& PTY.isSupported(PTY.Mode.TERMINAL))
				|| properties.get(ITerminalsConnectorConstants.PROP_PTY_OBJ) instanceof PTY;

		boolean localEcho = false;
		if (!properties.containsKey(ITerminalsConnectorConstants.PROP_LOCAL_ECHO)
				|| !(properties.get(ITerminalsConnectorConstants.PROP_LOCAL_ECHO) instanceof Boolean)) {
			// On Windows, turn on local echo by default if no PTY is used (bug 433645)
			if (Platform.OS_WIN32.equals(Platform.getOS())) {
				localEcho = !isUsingPTY;
			}
		} else {
			localEcho = ((Boolean) properties.get(ITerminalsConnectorConstants.PROP_LOCAL_ECHO)).booleanValue();
		}

		String lineSeparator = null;
		if (!properties.containsKey(ITerminalsConnectorConstants.PROP_LINE_SEPARATOR)
				|| !(properties.get(ITerminalsConnectorConstants.PROP_LINE_SEPARATOR) instanceof String)) {
			// No line separator will be set if a PTY is used
			if (!isUsingPTY) {
				lineSeparator = Platform.OS_WIN32.equals(Platform.getOS()) ? ILineSeparatorConstants.LINE_SEPARATOR_CRLF
						: ILineSeparatorConstants.LINE_SEPARATOR_LF;
			}
		} else {
			lineSeparator = (String) properties.get(ITerminalsConnectorConstants.PROP_LINE_SEPARATOR);
		}

		Process process = (Process) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_OBJ);
		PTY pty = (PTY) properties.get(ITerminalsConnectorConstants.PROP_PTY_OBJ);
		ITerminalServiceOutputStreamMonitorListener[] stdoutListeners = (ITerminalServiceOutputStreamMonitorListener[]) properties
				.get(ITerminalsConnectorConstants.PROP_STDOUT_LISTENERS);
		ITerminalServiceOutputStreamMonitorListener[] stderrListeners = (ITerminalServiceOutputStreamMonitorListener[]) properties
				.get(ITerminalsConnectorConstants.PROP_STDERR_LISTENERS);
		String workingDir = (String) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR);

		String[] envp = null;
		if (properties.containsKey(ITerminalsConnectorConstants.PROP_PROCESS_ENVIRONMENT)
				&& properties.get(ITerminalsConnectorConstants.PROP_PROCESS_ENVIRONMENT) != null
				&& properties.get(ITerminalsConnectorConstants.PROP_PROCESS_ENVIRONMENT) instanceof String[]) {
			envp = (String[]) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_ENVIRONMENT);
		}

		// Set the ECLIPSE_HOME and ECLIPSE_WORKSPACE environment variables
		List<String> envpList = new ArrayList<>();
		if (envp != null)
			envpList.addAll(Arrays.asList(envp));

		// ECLIPSE_HOME
		String eclipseHomeLocation = System.getProperty("eclipse.home.location"); //$NON-NLS-1$
		if (eclipseHomeLocation != null) {
			try {
				URI uri = URIUtil.fromString(eclipseHomeLocation);
				File f = URIUtil.toFile(uri);
				envpList.add("ECLIPSE_HOME=" + f.getAbsolutePath()); //$NON-NLS-1$
			} catch (URISyntaxException e) {
				/* ignored on purpose */ }
		}

		// ECLIPSE_WORKSPACE
		Bundle bundle = Platform.getBundle("org.eclipse.core.resources"); //$NON-NLS-1$
		if (bundle != null && bundle.getState() != Bundle.UNINSTALLED && bundle.getState() != Bundle.STOPPING) {
			if (org.eclipse.core.resources.ResourcesPlugin.getWorkspace() != null
					&& org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot() != null
					&& org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getLocation() != null) {
				envpList.add("ECLIPSE_WORKSPACE=" + org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot() //$NON-NLS-1$
						.getLocation().toOSString());
			}
		}

		//Set CDT build environment variables
		Map<String, String> envMap = new IDFEnvironmentVariables().getSystemEnvMap();
		Set<String> keySet = envMap.keySet();

		//Removing path, since we are using PATH
		if (envMap.containsKey("PATH") && envMap.containsKey("Path")) { //$NON-NLS-1$ //$NON-NLS-2$
			envMap.remove("Path"); //$NON-NLS-1$
		}
		// Adding esp-idf/tools in the path if it's missing (IEP-1668)
		String idfToolsPath = Paths.get(IDFUtil.getIDFPath(), "tools").toAbsolutePath().toString(); //$NON-NLS-1$
		String currentPath = envMap.get("PATH"); //$NON-NLS-1$

		if (currentPath != null && !currentPath.contains(idfToolsPath)) {
			envMap.put("PATH", currentPath + File.pathSeparator + idfToolsPath); //$NON-NLS-1$
		}

		for (String envKey : keySet) {
			String envValue = envMap.get(envKey);
			if (envKey.equals("PATH")) //$NON-NLS-1$
			{
				String idfExtraPaths = IDFUtil.getIDFExtraPaths();
				if (!StringUtil.isEmpty(idfExtraPaths)) {
					envValue = idfExtraPaths + ":" + envValue; //$NON-NLS-1$
				}
			}
			envpList.add(envKey + "=" + envValue); //$NON-NLS-1$
		}

		// Convert back into a string array
		envp = envpList.toArray(new String[envpList.size()]);

		Assert.isTrue(image != null || process != null);

		// Construct the terminal settings store
		ISettingsStore store = new InMemorySettingsStore();

		// Construct the process settings
		ProcessSettings processSettings = new ProcessSettings();
		processSettings.setImage(image);
		processSettings.setArguments(arguments);
		processSettings.setProcess(process);
		processSettings.setPTY(pty);
		processSettings.setLocalEcho(localEcho);
		processSettings.setLineSeparator(lineSeparator);
		processSettings.setStdOutListeners(stdoutListeners);
		processSettings.setStdErrListeners(stderrListeners);
		processSettings.setWorkingDir(workingDir);
		processSettings.setEnvironment(envp);

		if (properties.containsKey(ITerminalsConnectorConstants.PROP_PROCESS_MERGE_ENVIRONMENT)) {
			Object value = properties.get(ITerminalsConnectorConstants.PROP_PROCESS_MERGE_ENVIRONMENT);
			processSettings
					.setMergeWithNativeEnvironment(value instanceof Boolean ? ((Boolean) value).booleanValue() : false);
		}
		// And save the settings to the store
		processSettings.save(store);

		// Construct the terminal connector instance
		ITerminalConnector connector = null;
		try {
			connector = TerminalConnectorExtension.makeTerminalConnector(connectorId);
		} catch (CoreException e) {
			Logger.log(e);
		}
		if (connector != null) {
			// Apply default settings
			connector.setDefaultSettings();
			// And load the real settings
			connector.load(store);
		}

		return connector;
	}

}
