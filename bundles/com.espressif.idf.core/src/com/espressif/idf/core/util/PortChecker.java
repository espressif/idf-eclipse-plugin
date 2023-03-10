/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.net.Socket;

import com.espressif.idf.core.logging.Logger;

/**
 * A helper class for checking the availability of ports for a socket connection.
 * 
 * @author Denys Almazov
 *
 */
public class PortChecker
{
	private PortChecker()
	{
	}

	public static boolean isPortAvailable(int port)
	{
		try (Socket ignored = new Socket("localhost", port)) //$NON-NLS-1$
		{
			return false;
		}
		catch (Exception e)
		{
			return true;
		}
	}

	/**
	 * checks the port for availability for the socket connection. If the port is not available, we get the next
	 * available port
	 */
	public static int getAvailablePort(int port)
	{
		while (!isPortAvailable(port))
		{
			Logger.log(String.format(Messages.PortChecker_PortNotAvailable, port, port + 1));
			port += 1;
		}
		Logger.log(String.format(Messages.PortChecker_PortIsAvailable, port));
		return port;
	}
}