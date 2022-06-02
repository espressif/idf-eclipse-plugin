/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import java.util.Queue;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.espressif.idf.core.logging.Logger;

/**
 * Thread for logging messages in a queue to the SWT Text
 * 
 * @author Ali Azam Rana
 *
 */
public class LogMessagesThread extends Thread
{
	private final Queue<String> logMessages;
	private boolean stopLogging;
	private final Text logAreaText;
	private Display display;

	public LogMessagesThread(Queue<String> logMessages, Text logAreaText, Display display)
	{
		this.logMessages = logMessages;
		this.logAreaText = logAreaText;
		this.display = display;
	}

	@Override
	public void run()
	{
		while (!stopLogging)
		{
			try
			{
				Thread.sleep(50);
			}
			catch (InterruptedException e)
			{
				Logger.log(e);
			}
			if (logMessages.size() != 0)
			{
				String msg = logMessages.poll();
				showMessage(msg);
			}
		}
	}

	private void showMessage(final String message)
	{
		if (display == null && logAreaText != null)
		{
			display = logAreaText.getDisplay();
		}
		display.asyncExec(new Runnable()
		{
			public void run()
			{
				if (logAreaText.getText().length() != 0)
				{
					logAreaText.append(System.getProperty("line.separator")); //$NON-NLS-1$
				}
				logAreaText.append(message);
			}
		});
	}

	public void setStopLogging(boolean stopLogging)
	{
		this.stopLogging = stopLogging;
	}
}
