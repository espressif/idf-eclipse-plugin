/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core.server;


/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public interface IMessageHandlerListener
{
	public void notifyRequestServed(String message, CommandType type);
}