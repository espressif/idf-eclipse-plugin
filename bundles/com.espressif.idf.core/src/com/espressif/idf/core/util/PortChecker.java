/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.io.IOException;
import java.net.ServerSocket;
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
	private static final int MAX_ATTEMPS = 20;

	private PortChecker()
	{
	}

	public static boolean isPortAvailable(int port)
	{
		try (Socket socket = new Socket("127.0.0.1", port)) { //$NON-NLS-1$
	        // If connection is successful, port is in use
	        Logger.log("Port: " + port + " is not available (in use)");  //$NON-NLS-1$//$NON-NLS-2$
	        return false;
	    } catch (IOException e) {
	        // If connection fails, port is likely available
	        Logger.log("Port: " + port + " is available"); //$NON-NLS-1$ //$NON-NLS-2$
	        return true;
	    }
	}

	/**
	 * checks the port for availability for the socket connection. If the port is not available, we get the next
	 * available port
	 */
	public static int getAvailablePort(int port)
	{
		int attemptsCount = 0;
		while (!isPortAvailable(port) && attemptsCount < MAX_ATTEMPS)
		{
			Logger.log(String.format(Messages.PortChecker_PortNotAvailable, port, port + 1));
			port += 1;
			attemptsCount += 1;
		}

		if (attemptsCount >= MAX_ATTEMPS)
		{
			Logger.log(Messages.PortChecker_AttemptLimitExceededMsg);
			return port;
		}

		Logger.log(String.format(Messages.PortChecker_PortIsAvailable, port));
		return port;
	}
}