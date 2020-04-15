/*******************************************************************************
 * Copyright (c) 2017, 2018 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package com.espressif.idf.terminal.connector.serial.launcher;

import java.util.Map;

import org.eclipse.cdt.serial.BaudRate;
import org.eclipse.cdt.serial.ByteSize;
import org.eclipse.cdt.serial.Parity;
import org.eclipse.cdt.serial.StopBits;
import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalConnectorExtension;
import org.eclipse.tm.terminal.view.core.TerminalServiceFactory;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalService;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalService.Done;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanel;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanelContainer;
import org.eclipse.tm.terminal.view.ui.internal.SettingsStore;
import org.eclipse.tm.terminal.view.ui.launcher.AbstractLauncherDelegate;

import com.espressif.idf.terminal.connector.serial.connector.SerialSettings;
import com.espressif.idf.terminal.connector.serial.controls.SerialConfigPanel;

public class SerialLauncherDelegate extends AbstractLauncherDelegate {

	@Override
	public boolean needsUserConfiguration() {
		return true;
	}

	@Override
	public IConfigurationPanel getPanel(IConfigurationPanelContainer container) {
		return new SerialConfigPanel(container);
	}

	@Override
	public ITerminalConnector createTerminalConnector(Map<String, Object> properties) {
		Assert.isNotNull(properties);

		// Check for the terminal connector id
		String connectorId = (String) properties.get(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID);
		if (connectorId == null) {
			connectorId = "com.espressif.idf.terminal.connector.serial.SerialConnector"; //$NON-NLS-1$
		}

		// Extract the properties
		SerialSettings settings = new SerialSettings();
		settings.setPortName((String) properties.get(SerialSettings.PORT_NAME_ATTR));
		settings.setBaudRate((BaudRate) properties.get(SerialSettings.BAUD_RATE_ATTR));
		settings.setByteSize((ByteSize) properties.get(SerialSettings.BYTE_SIZE_ATTR));
		settings.setParity((Parity) properties.get(SerialSettings.PARITY_ATTR));
		settings.setStopBits((StopBits) properties.get(SerialSettings.STOP_BITS_ATTR));

		// Construct the terminal settings store
		ISettingsStore store = new SettingsStore();
		settings.save(store);

		// Construct the terminal connector instance
		ITerminalConnector connector = TerminalConnectorExtension.makeTerminalConnector(connectorId);
		if (connector != null) {
			// Apply default settings
			connector.setDefaultSettings();
			// And load the real settings
			connector.load(store);
		}

		return connector;
	}

	@Override
	public void execute(Map<String, Object> properties, Done done) {
		Assert.isNotNull(properties);

		// Set the terminal tab title
		String name = (String) properties.get(SerialSettings.PORT_NAME_ATTR);
		properties.put(ITerminalsConnectorConstants.PROP_TITLE, name);

		// Force a new terminal tab each time it is launched, if not set otherwise from outside
		// TODO need a command shell service routing to get this
		if (!properties.containsKey(ITerminalsConnectorConstants.PROP_FORCE_NEW)) {
			properties.put(ITerminalsConnectorConstants.PROP_FORCE_NEW, Boolean.TRUE);
		}

		// Get the terminal service
		ITerminalService terminal = TerminalServiceFactory.getService();
		// If not available, we cannot fulfill this request
		if (terminal != null) {
			terminal.openConsole(properties, done);
		}
	}

}
