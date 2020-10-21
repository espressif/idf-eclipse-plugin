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
package com.espressif.idf.terminal.connector.serial.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.util.SDKConfigJsonReader;
import com.espressif.idf.serial.monitor.handlers.SerialMonitorHandler;
import com.espressif.idf.terminal.connector.serial.activator.Activator;
import com.espressif.idf.ui.EclipseUtil;

public class SerialConnector extends TerminalConnectorImpl {

	private SerialSettings settings = new SerialSettings();
	SerialPort serialPort;
	private Process process;
	private Thread thread;

	private static Set<String> openPorts = new HashSet<>();

	public static boolean isOpen(String portName) {
		return openPorts.contains(portName);
	}

	@Override
	public OutputStream getTerminalToRemoteStream() {
		return serialPort.getOutputStream();
	}

	public SerialSettings getSettings() {
		return settings;
	}

	public SerialPort getSerialPort() {
		return serialPort;
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
		control.setState(TerminalState.CONNECTING);

		//Hook IDF Monitor with the Eclipse serial monitor
		IProject project = EclipseUtil.getSelectedProjectInExplorer();

		// Get serial port
		String serialPort = getLastUsedSerialPort();

		SerialMonitorHandler serialMonitorHandler = new SerialMonitorHandler(project, serialPort);
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

	protected String getsdkconfigBaudRate() {
		IResource resource = EclipseUtil.getSelectionResource();
		if (resource != null) {
			IProject project = resource.getProject();
			return new SDKConfigJsonReader(project).getValue("ESPTOOLPY_MONITOR_BAUD"); //$NON-NLS-1$
		}
		return null;
	}

	protected String getLastUsedSerialPort() {
		Preferences preferences = InstanceScope.INSTANCE.getNode("com.espressif.idf.launch.serial.ui"); //$NON-NLS-1$
		return preferences.get("com.espressif.idf.launch.serial.core.serialPort", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
