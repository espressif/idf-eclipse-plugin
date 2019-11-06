/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core.server;

/**
 * 
 * JSON Configuration server command type - this will be helpful in taking action based on the command type when editor
 * received the response from the server
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public enum CommandType
{
	/**
	 * LOAD COMMAND - Discard the current changes and load the changes from the config server
	 */
	LOAD, 
	
	/**
	 * SAVE COMMAND - Save the current changes to the file system
	 */
	SAVE, 
	
	/**
	 * SET COMMAND - Save the changes to cache. It won't be saved to the file system until save command is invoked
	 */
	SET,
	
	/**
	 * To represent server connection is closed
	 */
	CONNECTION_CLOSED
}
