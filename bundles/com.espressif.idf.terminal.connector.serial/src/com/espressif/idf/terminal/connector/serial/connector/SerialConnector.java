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
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;

import com.espressif.idf.terminal.connector.serial.activator.Activator;

public class SerialConnector extends TerminalConnectorImpl {

	private SerialSettings settings = new SerialSettings();
	SerialPort serialPort;

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

		serialPort = new SerialPort(settings.getPortName());
		try {
			serialPort.setBaudRate(settings.getBaudRate());
			serialPort.setByteSize(settings.getByteSize());
			serialPort.setParity(settings.getParity());
			serialPort.setStopBits(settings.getStopBits());
			serialPort.open();
		} catch (IOException e) {
			Activator.log(e);
			control.setState(TerminalState.CLOSED);
			return;
		}

		openPorts.add(serialPort.getPortName());

		new Thread() {
			@Override
			public void run() {
				InputStream targetIn = serialPort.getInputStream();
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
		}.start();
		control.setState(TerminalState.CONNECTED);
	}

	@Override
	protected void doDisconnect() {
		if (serialPort != null && serialPort.isOpen()) {
			openPorts.remove(serialPort.getPortName());
			try {
				serialPort.close();
			} catch (IOException e) {
				Activator.log(e);
			}
		}
		serialPort = null;
	}

}
