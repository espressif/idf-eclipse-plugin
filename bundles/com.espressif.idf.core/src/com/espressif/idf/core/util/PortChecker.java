/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.io.IOException;
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
		int attempts = 0;
		int retryCount = 3;
		long retryDelayMillis = 200;

		while (attempts <= retryCount)
		{
			try (Socket ignored = new Socket("localhost", port)) //$NON-NLS-1$
			{
				// If the socket opens, the port is in use
				return false;
			}
			catch (IOException e)
			{
				// Port is unavailable, retrying if there are attempts left
				if (attempts == retryCount)
				{
					// After exhausting all retries, return false
					Logger.log("Port " + port + " is not available after " + retryCount + " retries."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					return false; // Failure, port is still not available
				}

				attempts++;

				// Log retry attempt
				Logger.log("Attempt " + attempts + " failed, retrying in " + retryDelayMillis + " ms..."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

				try
				{
					Thread.sleep(retryDelayMillis);
				}
				catch (InterruptedException interruptedException)
				{
					Thread.currentThread().interrupt(); // Restore interrupt status
					Logger.log("Port availability check interrupted."); //$NON-NLS-1$
					return false; // If interrupted, assume port unavailable and stop
				}
			}
		}
		return true; //Fallback not reachable
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