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
 *     Espressif Systems - ESP-IDF Monitor Integration
 *******************************************************************************/
package com.espressif.idf.terminal.connector.serial.connector;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;

import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.terminal.connector.serial.activator.Activator;

public class SerialConnector extends TerminalConnectorImpl
{

	private SerialSettings settings = new SerialSettings();
	protected Process process;
	protected Thread thread;
	protected IProject project;
	protected String filterOptions;
	protected ITerminalControl control;
	private SerialPortHandler serialPort;

	private static Set<String> openPorts = new HashSet<>();

	public static boolean isOpen(String portName)
	{
		return openPorts.contains(portName);
	}

	@Override
	public OutputStream getTerminalToRemoteStream()
	{
		return process.getOutputStream();
	}

	public SerialSettings getSettings()
	{
		return settings;
	}

	@Override
	public String getSettingsSummary()
	{
		return settings.getSummary();
	}

	@Override
	public void load(ISettingsStore store)
	{
		settings.load(store);
	}

	@Override
	public void save(ISettingsStore store)
	{
		settings.save(store);
	}

	@Override
	public void connect(ITerminalControl control)
	{
		super.connect(control);
		this.control = control;

		// Get selected project - which is required for IDF Monitor
		project = settings.getProject();

		if (project == null)
		{
			String message = "project can't be null. Make sure you select a project before launch a serial monitor"; //$NON-NLS-1$
			Activator.log(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), message, null));
			return;
		}

		String portName = settings.getPortName();
		filterOptions = settings.getFilterText();
		filterOptions = StringUtil.isEmpty(filterOptions) ? StringUtil.EMPTY : filterOptions;

		serialPort = new SerialPortHandler(portName, this, project);
		serialPort.open();

		openPorts.add(serialPort.getPortName());

	}

	@Override
	protected void doDisconnect()
	{

		if (serialPort != null && serialPort.isOpen())
		{
			openPorts.remove(serialPort.getPortName());
			serialPort.close();
		}
	}

}
