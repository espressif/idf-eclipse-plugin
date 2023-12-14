/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd.dsf.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;

import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IFlushableStreamMonitor;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.debug.gdbjtag.openocd.dsf.console.IdfProcessConsole;
import com.espressif.idf.debug.gdbjtag.openocd.dsf.console.IdfProcessConsoleFactory;
import com.espressif.idf.debug.gdbjtag.openocd.ui.Messages;

/**
 * This class listens to a specified stream monitor to get notified on output from the process connected to console. Its
 * separated so that we can apply filters and pass the text onto the respected stream. It is designed to handle both
 * error and standard stream. This class is built using {@link ProcessConsole} StreamListner. The original class is
 * private there.
 * 
 * @author Ali Azam Rana
 */
@SuppressWarnings("restriction")
public class StreamListener implements IStreamListener
{
	private static final String OPENOCD_FAQ_LINK = "https://github.com/espressif/openocd-esp32/wiki/Troubleshooting-FAQ";
	private IOConsoleOutputStream fConsoleErrorOutputStream;
	private IOConsoleOutputStream fConsoleOutputStream;

	private IStreamMonitor fErrorStreamMonitor;
	private IStreamMonitor fOutputStreamMonitor;

	private IdfProcessConsole idfProcessConsole;

	/** Flag to remember if stream was already closed. */
	private boolean fStreamClosed = false;

	public StreamListener(IProcess iProcess, IStreamMonitor errorStreamMonitor, IStreamMonitor outputStreamMonitor,
			Charset charset)
	{
		fErrorStreamMonitor = errorStreamMonitor;
		fOutputStreamMonitor = outputStreamMonitor;

		idfProcessConsole = IdfProcessConsoleFactory.showAndActivateConsole(charset);
		idfProcessConsole.clearConsole();
		fConsoleErrorOutputStream = idfProcessConsole.getErrorStream();
		fConsoleErrorOutputStream.setActivateOnWrite(true);
		fConsoleOutputStream = idfProcessConsole.getOutputStream();
		fConsoleOutputStream.setActivateOnWrite(true);

		flushAndDisableBuffer();
	}

	/**
	 * Process existing content in monitor and flush and disable buffering if it is a {@link IFlushableStreamMonitor}.
	 *
	 * @param monitor the monitor which might have buffered content
	 */
	private void flushAndDisableBuffer()
	{
		String contents;
		synchronized (fErrorStreamMonitor)
		{
			contents = fErrorStreamMonitor.getContents();
			if (fErrorStreamMonitor instanceof IFlushableStreamMonitor)
			{
				IFlushableStreamMonitor m = (IFlushableStreamMonitor) fErrorStreamMonitor;
				m.flushContents();
				m.setBuffered(false);
			}
			streamAppended(contents, fErrorStreamMonitor);
		}

		synchronized (fOutputStreamMonitor)
		{
			contents = fOutputStreamMonitor.getContents();
			if (fOutputStreamMonitor instanceof IFlushableStreamMonitor)
			{
				IFlushableStreamMonitor m = (IFlushableStreamMonitor) fOutputStreamMonitor;
				m.flushContents();
				m.setBuffered(false);
			}
			streamAppended(contents, fOutputStreamMonitor);
		}
	}

	@Override
	public void streamAppended(String text, IStreamMonitor monitor)
	{
		String line;
		try (BufferedReader bufferedReader = new BufferedReader(new StringReader(text)))
		{
			while ((line = bufferedReader.readLine()) != null)
			{
				if (line.startsWith("Error:") && fConsoleErrorOutputStream != null)
				{
					fConsoleErrorOutputStream.write((line + System.lineSeparator()).getBytes());
					fConsoleErrorOutputStream.flush();

					fConsoleOutputStream.write((Messages.OpenOCDConsole_ErrorGuideMessage + System.lineSeparator()
							+ OPENOCD_FAQ_LINK + System.lineSeparator()).getBytes());
					fConsoleOutputStream.flush();
				}
				else if (fConsoleOutputStream != null)
				{
					fConsoleOutputStream.write((line + System.lineSeparator()).getBytes());
					fConsoleOutputStream.flush();
				}
			}
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
	}

	public void closeStreams()
	{
		synchronized (fErrorStreamMonitor)
		{
			fErrorStreamMonitor.removeListener(this);
		}

		synchronized (fOutputStreamMonitor)
		{
			fOutputStreamMonitor.removeListener(this);
		}

		fStreamClosed = true;
	}

	public void dispose()
	{
		if (!fStreamClosed)
		{
			closeStreams();
		}
		fErrorStreamMonitor = null;
		fOutputStreamMonitor = null;
	}
}