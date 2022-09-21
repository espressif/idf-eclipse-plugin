/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.terminal.connector.serial.server;

/**
 * Interface to store the possible terminal socket event name
 *
 * @author Ali Azam Rana
 *
 */
public interface ITerminalSocketEvents
{
	/**
	 * Event for Core Dump
	 */
	String CORE_DUMP = "coredump"; //$NON-NLS-1$

	/**
	 * Event for GDB Stub on Panic
	 */
	String GDB_STUB = "gdb_stub"; //$NON-NLS-1$

}
