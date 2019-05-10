/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core.server;


/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public interface IMessagesHandlerNotifier
{
	/**
	 * @param listener
	 */
	public void addListener(IMessageHandlerListener listener);

	/**
	 * @param message
	 * @param type
	 */
	public void notifyHandler(String message, CommandType type);

	/**
	 * @param listener
	 */
	public void removeListener(IMessageHandlerListener listener);
}