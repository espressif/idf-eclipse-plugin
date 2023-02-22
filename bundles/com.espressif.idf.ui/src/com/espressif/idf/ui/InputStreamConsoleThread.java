/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.logging.Logger;

/**
 * @author Kondal Kolipaka
 *
 */
public class InputStreamConsoleThread extends Thread
{
	private InputStream input;
	private MessageConsoleStream console;

	public InputStreamConsoleThread(InputStream inputStream, MessageConsoleStream console)
	{
		if (inputStream == null)
		{
			throw new IllegalArgumentException("The InputStream cannot be null!"); //$NON-NLS-1$
		}
		this.input = inputStream;
		this.console = console;
	}

	@Override
	public void run()
	{
		InputStreamReader streamReader = null;
		try
		{
			streamReader = new InputStreamReader(input, "UTF-8"); //$NON-NLS-1$
			BufferedReader br = new BufferedReader(streamReader);
			String line = null;
			while ((line = br.readLine()) != null)
			{
				console.println(line);
			}
		}
		catch (IOException e)
		{
			Logger.log(e);
		} finally
		{
			if (streamReader != null)
			{
				try
				{
					streamReader.close();
				}
				catch (Exception e)
				{
				}
			}
		}
	}
}
