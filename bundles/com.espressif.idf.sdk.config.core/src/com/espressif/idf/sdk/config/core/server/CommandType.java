/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core.server;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 * 
 * JSON Configuration server command type - this will be helpful in taking action based on the command type when
 *         editor received the response from the server
 *
 */
public enum CommandType
{
	LOAD, 
	SAVE, 
	SET
}
