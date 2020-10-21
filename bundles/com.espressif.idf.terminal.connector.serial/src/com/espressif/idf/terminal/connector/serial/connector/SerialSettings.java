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

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;

public class SerialSettings {

	public static final String PORT_NAME_ATTR = "cdtserial.portName"; //$NON-NLS-1$

	private String portName;

	/**
	 * Load information into the RemoteSettings object.
	 */
	public void load(ISettingsStore store) {
		portName = store.get(PORT_NAME_ATTR, ""); //$NON-NLS-1$

	}

	/**
	 * Extract information from the RemoteSettings object.
	 */
	public void save(ISettingsStore store) {
		store.put(PORT_NAME_ATTR, portName);
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public String getSummary() {
		return portName;
	}

}
