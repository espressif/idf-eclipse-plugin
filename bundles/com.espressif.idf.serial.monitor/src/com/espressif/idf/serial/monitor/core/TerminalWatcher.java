package com.espressif.idf.serial.monitor.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;

import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.logging.Logger;

public class TerminalWatcher implements Runnable
{
	private InputStream outFromChannel;
	private MessageConsoleStream stream;

	public TerminalWatcher(InputStream outFromChannel, MessageConsoleStream stream)
	{
		this.outFromChannel = outFromChannel;
		this.stream = stream;
	}

	public void run()
	{
		InputStreamReader isr = new InputStreamReader(outFromChannel);
		try
		{
			char[] buff = new char[1024];
			int read;
			while ((read = isr.read(buff)) != -1)
			{
				String s = new String(buff, 0, read);
				stream.print(s);
			}
		}
		catch (InterruptedIOException e)
		{
			// ignore
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
	}

}
