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

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.terminal.connector.ISettingsStore;
import org.eclipse.terminal.connector.ITerminalConnector;
import org.eclipse.terminal.connector.InMemorySettingsStore;
import org.eclipse.terminal.connector.TerminalConnectorExtension;
import org.eclipse.terminal.connector.process.ProcessSettings;
import org.eclipse.terminal.view.core.ITerminalsConnectorConstants;
import org.eclipse.terminal.view.ui.IMementoHandler;
import org.eclipse.terminal.view.ui.launcher.AbstractLauncherDelegate;
import org.eclipse.terminal.view.ui.launcher.IConfigurationPanel;
import org.eclipse.terminal.view.ui.launcher.IConfigurationPanelContainer;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.terminal.connector.controls.IDFConsoleWizardConfigurationPanel;

public class IDFConsoleLauncherDelegate extends AbstractLauncherDelegate {

	private static final String CONNECTOR_ID = "com.espressif.idf.terminal.connector.espidfConnector"; //$NON-NLS-1$
	private static final String TERMINAL_TITLE_LABEL = Messages.IDFConsoleLauncherDelegate_TerminalTitle;
	private IMementoHandler mementoHandler = new IDFConsoleMementoHandler();

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
		properties.put(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID, CONNECTOR_ID);

		String projectName = (String) properties.get(ITerminalsConnectorConstants.PROP_TITLE);
		String title = (projectName != null) ? String.format("%s (%s)", projectName, TERMINAL_TITLE_LABEL) //$NON-NLS-1$
				: TERMINAL_TITLE_LABEL;
		properties.put(ITerminalsConnectorConstants.PROP_TITLE, title);

		return getTerminalService().openConsole(properties);
	}

	@Override
	public ITerminalConnector createTerminalConnector(Map<String, Object> properties) {
		String image = getShellExecutable();

		ProcessSettings processSettings = new ProcessSettings();
		processSettings.setImage(image);
		processSettings.setLocalEcho(false);

		String workingDir = (String) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR);
		if (workingDir != null) {
			processSettings.setWorkingDir(workingDir);
		}

		try {
			ITerminalConnector connector = TerminalConnectorExtension.makeTerminalConnector(CONNECTOR_ID);
			if (connector != null) {
				ISettingsStore store = new InMemorySettingsStore();
				processSettings.save(store);
				connector.setDefaultSettings();
				connector.load(store);
				return connector;
			}
		} catch (CoreException e) {
			Logger.log(e);
		}
		return null;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (IMementoHandler.class.equals(adapter)) {
			return adapter.cast(mementoHandler);
		}
		return super.getAdapter(adapter);
	}

	private String getShellExecutable() {
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			return "powershell.exe"; //$NON-NLS-1$
		}
		String shell = System.getenv("SHELL"); //$NON-NLS-1$
		return (shell != null && !shell.isBlank()) ? shell : "/bin/sh"; //$NON-NLS-1$
	}
}