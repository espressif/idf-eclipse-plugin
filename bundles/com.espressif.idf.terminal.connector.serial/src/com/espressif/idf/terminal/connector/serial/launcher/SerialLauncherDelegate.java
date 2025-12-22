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
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.terminal.connector.ISettingsStore;
import org.eclipse.terminal.connector.ITerminalConnector;
import org.eclipse.terminal.connector.InMemorySettingsStore;
import org.eclipse.terminal.connector.TerminalConnectorExtension;
import org.eclipse.terminal.view.core.ITerminalsConnectorConstants;
import org.eclipse.terminal.view.ui.launcher.AbstractLauncherDelegate;
import org.eclipse.terminal.view.ui.launcher.IConfigurationPanel;
import org.eclipse.terminal.view.ui.launcher.IConfigurationPanelContainer;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.terminal.connector.serial.connector.SerialSettings;
import com.espressif.idf.terminal.connector.serial.controls.SerialConfigPanel;

public class SerialLauncherDelegate extends AbstractLauncherDelegate
{

	@Override
	public boolean needsUserConfiguration()
	{
		return true;
	}

	@Override
	public IConfigurationPanel getPanel(IConfigurationPanelContainer container)
	{
		return new SerialConfigPanel(container);
	}

	@Override
	public ITerminalConnector createTerminalConnector(Map<String, Object> properties)
	{
		Assert.isNotNull(properties);

		// Check for the terminal connector id
		String connectorId = (String) properties.get(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID);
		if (connectorId == null)
		{
			connectorId = "com.espressif.idf.terminal.connector.serial.SerialConnector"; //$NON-NLS-1$
		}

		// Extract the properties
		SerialSettings settings = new SerialSettings();
		settings.setPortName((String) properties.get(SerialSettings.PORT_NAME_ATTR));
		settings.setFilterText((String) properties.get(SerialSettings.MONITOR_FILTER));
		settings.setProject((String) properties.get(SerialSettings.SELECTED_PROJECT_ATTR));
		Boolean encryptionOption = (Boolean) properties.get(SerialSettings.ENCRYPTION_ATTR);
		settings.setEncryptionOption(encryptionOption != null && encryptionOption);
		// Construct the terminal settings store
		ISettingsStore store = new InMemorySettingsStore();
		settings.save(store);

		// Construct the terminal connector instance
		ITerminalConnector connector = null;
		try
		{
			connector = TerminalConnectorExtension.makeTerminalConnector(connectorId);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		if (connector != null)
		{
			// Apply default settings
			connector.setDefaultSettings();
			// And load the real settings
			connector.load(store);
		}

		return connector;
	}

	@Override
	public CompletableFuture<?> execute(Map<String, Object> properties)
	{
		Assert.isNotNull(properties);

		// Set the terminal tab title
		String name = (String) properties.get(SerialSettings.PORT_NAME_ATTR);
		properties.put(ITerminalsConnectorConstants.PROP_TITLE, name);

		// Force a new terminal tab each time it is launched, if not set otherwise from outside
		// TODO need a command shell service routing to get this
		if (!properties.containsKey(ITerminalsConnectorConstants.PROP_FORCE_NEW))
		{
			properties.put(ITerminalsConnectorConstants.PROP_FORCE_NEW, Boolean.TRUE);
		}

		// Get the terminal service
		try
		{
			return getTerminalService().openConsole(properties);
		}
		catch (RuntimeException e)
		{
			return CompletableFuture.failedFuture(e);
		}
	}

}
