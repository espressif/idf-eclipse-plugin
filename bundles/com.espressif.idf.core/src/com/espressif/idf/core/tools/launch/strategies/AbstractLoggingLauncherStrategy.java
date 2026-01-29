/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.launch.strategies;

import java.io.IOException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.logging.Logger;

/**
 * Abstract class for logging used by strategies launching EIM
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public abstract class AbstractLoggingLauncherStrategy implements EimLauncherStrategy
{
	protected final Display display;
	protected final MessageConsoleStream standardConsoleStream;
	protected final MessageConsoleStream errorConsoleStream;

	protected AbstractLoggingLauncherStrategy(Display display, MessageConsoleStream standardConsoleStream,
			MessageConsoleStream errorConsoleStream)
	{
		this.display = display;
		this.standardConsoleStream = standardConsoleStream;
		this.errorConsoleStream = errorConsoleStream;
	}

	protected void logMessage(String message)
	{
		display.asyncExec(() -> {
			try
			{
				standardConsoleStream.write(message);
			}
			catch (IOException e)
			{
				Logger.log(e);
				logError(e.getMessage());
			}
		});

		Logger.log(message);
	}

	protected void logError(String message)
	{
		display.asyncExec(() -> {
			try
			{
				errorConsoleStream.write(message);
			}
			catch (IOException e)
			{
				Logger.log(e);
			}
		});

		Logger.log(message);
	}
}
