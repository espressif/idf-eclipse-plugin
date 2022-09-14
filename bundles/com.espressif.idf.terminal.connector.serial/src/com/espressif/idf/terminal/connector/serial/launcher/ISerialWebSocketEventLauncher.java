/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.terminal.connector.serial.launcher;

import org.eclipse.core.resources.IFile;

/**
 * Interface to generalize and hide the implementation details for the socket server message handler to launch
 * configurations.
 *
 * @author Ali Azam Rana
 *
 */
public interface ISerialWebSocketEventLauncher
{
	public IFile launchDebugSession() throws Exception;
}
