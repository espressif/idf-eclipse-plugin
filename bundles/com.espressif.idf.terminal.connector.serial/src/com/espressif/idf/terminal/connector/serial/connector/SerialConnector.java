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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;

import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.serial.monitor.handlers.SerialMonitorHandler;
import com.espressif.idf.terminal.connector.serial.activator.Activator;
import com.espressif.idf.ui.EclipseUtil;

public class SerialConnector extends TerminalConnectorImpl {

	private SerialSettings settings = new SerialSettings();
	private Process process;
	private Thread thread;

	private static Set<String> openPorts = new HashSet<>();

	public static boolean isOpen(String portName) {
		return openPorts.contains(portName);
	}

	@Override
	public OutputStream getTerminalToRemoteStream() {
		return process.getOutputStream();
	}

	public SerialSettings getSettings() {
		return settings;
	}

	@Override
	public String getSettingsSummary() {
		return settings.getSummary();
	}

	@Override
	public void load(ISettingsStore store) {
		settings.load(store);
	}

	@Override
	public void save(ISettingsStore store) {
		settings.save(store);
	}

	@Override
	public void connect(ITerminalControl control) {
		super.connect(control);

		//Get selected project - which is required for IDF Monitor
		IProject project = EclipseUtil.getSelectedProjectInExplorer();
		if (project == null) {
			String message = "project can't be null. Make sure you select a project before launch a serial monitor"; //$NON-NLS-1$
			Activator.log(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), message, null));
			return;
		}

		//set state
		control.setState(TerminalState.CONNECTING);

		String portName = settings.getPortName();
		String filterOptions = settings.getFilterText();
		filterOptions = StringUtil.isEmpty(filterOptions) ? StringUtil.EMPTY : filterOptions;

		//Hook IDF Monitor with the CDT serial monitor
		SerialMonitorHandler serialMonitorHandler = new SerialMonitorHandler(project, portName, filterOptions);
		process = serialMonitorHandler.invokeIDFMonitor();

		thread = new Thread() {
			@Override
			public void run() {
				InputStream targetIn = process.getInputStream();
				byte[] buff = new byte[256];
				int n;
				try {
					while ((n = targetIn.read(buff, 0, buff.length)) >= 0) {
						if (n != 0) {
							control.getRemoteToTerminalOutputStream().write(buff, 0, n);
						}
					}
					disconnect();
				} catch (IOException e) {
					Activator.log(e);
				}
			}
		};

		thread.start();

		control.setState(TerminalState.CONNECTED);
	}

	@Override
	protected void doDisconnect() {

		//Disconnect ptyprocess and that will free the port
		if (process != null) {
			process.destroy();
		}
		if (thread != null) {
			thread.interrupt();
		}
	}

}
