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
import java.util.Map;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalConnectorExtension;
import org.eclipse.tm.terminal.connector.process.ProcessSettings;
import org.eclipse.tm.terminal.view.core.TerminalServiceFactory;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalService;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalServiceOutputStreamMonitorListener;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ILineSeparatorConstants;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanel;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanelContainer;
import org.eclipse.tm.terminal.view.ui.interfaces.IMementoHandler;
import org.eclipse.tm.terminal.view.ui.interfaces.IPreferenceKeys;
import org.eclipse.tm.terminal.view.ui.internal.SettingsStore;
import org.eclipse.tm.terminal.view.ui.launcher.AbstractLauncherDelegate;
import org.eclipse.ui.WorkbenchEncoding;
import org.osgi.framework.Bundle;

import com.espressif.idf.terminal.connector.activator.UIPlugin;
import com.espressif.idf.terminal.connector.controls.IDFConsoleWizardConfigurationPanel;

/**
 * Serial launcher delegate implementation.
 */
@SuppressWarnings("restriction")
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
	public void execute(Map<String, Object> properties, ITerminalService.Done done) {
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
			String initialCwd = org.eclipse.tm.terminal.view.ui.activator.UIPlugin.getScopedPreferences()
					.getString(IPreferenceKeys.PREF_LOCAL_TERMINAL_INITIAL_CWD);
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
		ITerminalService terminal = TerminalServiceFactory.getService();
		// If not available, we cannot fulfill this request
		if (terminal != null) {
			terminal.openConsole(properties, done);
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
			shell = "powershell.exe"; //$NON-NLS-1$
		}
		if (shell == null) {
			shell = org.eclipse.tm.terminal.view.ui.activator.UIPlugin.getScopedPreferences()
					.getString(IPreferenceKeys.PREF_LOCAL_TERMINAL_DEFAULT_SHELL_UNIX);
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

		// 1. Check for the terminal connector id
		String connectorId = (String) properties.get(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID);
		if (connectorId == null) {
			connectorId = ESP_IDF_CONSOLE_CONNECTOR_ID;
		}

		// 2. Extract the process image (shell)
		String image;
		if (!properties.containsKey(ITerminalsConnectorConstants.PROP_PROCESS_PATH)
				|| properties.get(ITerminalsConnectorConstants.PROP_PROCESS_PATH) == null) {
			File defaultShell = defaultShell();
			image = defaultShell.isAbsolute() ? defaultShell.getAbsolutePath() : defaultShell.getPath();
		} else {
			image = (String) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_PATH);
		}

		// 3. Determine PTY and Echo settings
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

		// 4. Determine Line Separator
		String lineSeparator = null;
		if (!properties.containsKey(ITerminalsConnectorConstants.PROP_LINE_SEPARATOR)
				|| !(properties.get(ITerminalsConnectorConstants.PROP_LINE_SEPARATOR) instanceof String)) {
			if (!isUsingPTY) {
				lineSeparator = Platform.OS_WIN32.equals(Platform.getOS()) ? ILineSeparatorConstants.LINE_SEPARATOR_CRLF
						: ILineSeparatorConstants.LINE_SEPARATOR_LF;
			}
		} else {
			lineSeparator = (String) properties.get(ITerminalsConnectorConstants.PROP_LINE_SEPARATOR);
		}

		// 5. Extract other properties
		Process process = (Process) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_OBJ);
		PTY pty = (PTY) properties.get(ITerminalsConnectorConstants.PROP_PTY_OBJ);
		ITerminalServiceOutputStreamMonitorListener[] stdoutListeners = (ITerminalServiceOutputStreamMonitorListener[]) properties
				.get(ITerminalsConnectorConstants.PROP_STDOUT_LISTENERS);
		ITerminalServiceOutputStreamMonitorListener[] stderrListeners = (ITerminalServiceOutputStreamMonitorListener[]) properties
				.get(ITerminalsConnectorConstants.PROP_STDERR_LISTENERS);
		String workingDir = (String) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR);

		Assert.isTrue(image != null || process != null);

		// 6. Construct and Save Settings
		ISettingsStore store = new SettingsStore();
		ProcessSettings processSettings = new ProcessSettings();
		processSettings.setImage(image);
		processSettings.setProcess(process);
		processSettings.setPTY(pty);
		processSettings.setLocalEcho(localEcho);
		processSettings.setLineSeparator(lineSeparator);
		processSettings.setStdOutListeners(stdoutListeners);
		processSettings.setStdErrListeners(stderrListeners);
		processSettings.setWorkingDir(workingDir);

		if (properties.containsKey(ITerminalsConnectorConstants.PROP_PROCESS_MERGE_ENVIRONMENT)) {
			Object value = properties.get(ITerminalsConnectorConstants.PROP_PROCESS_MERGE_ENVIRONMENT);
			processSettings
					.setMergeWithNativeEnvironment(value instanceof Boolean boolValue && boolValue.booleanValue());
		}
		processSettings.save(store);

		// 7. Create Connector
		ITerminalConnector connector = TerminalConnectorExtension.makeTerminalConnector(connectorId);
		if (connector != null) {
			connector.setDefaultSettings();
			connector.load(store);
		}

		return connector;
	}

}
