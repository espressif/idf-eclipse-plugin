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
package com.espressif.idf.terminal.connector.serial.nls;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings management.
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "com.espressif.idf.terminal.connector.serial.nls.Messages"; //$NON-NLS-1$

	/**
	 * Static constructor.
	 */
	static {
		// Load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	// **** Declare externalized string id's down here *****

	public static String SerialTerminalSettingsPage_BaudRate;
	public static String SerialTerminalSettingsPage_DataSize;
	public static String SerialTerminalSettingsPage_Parity;
	public static String SerialTerminalSettingsPage_SerialPort;
	public static String SerialTerminalSettingsPage_StopBits;

}
