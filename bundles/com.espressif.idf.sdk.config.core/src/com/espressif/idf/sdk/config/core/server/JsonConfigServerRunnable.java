/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.sdk.config.core.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import com.aptana.core.util.ProcessRunnable;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.sdk.config.core.SDKConfigCorePlugin;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class JsonConfigServerRunnable extends ProcessRunnable
{

	private JsonConfigServer configServer;
	private OutputStream in;
	private InputStream out;
	private CommandType type;

	public JsonConfigServerRunnable(Process process, JsonConfigServer configServer)
	{
		super(process, null, true);
		this.configServer = configServer;

	}

	public void destory()
	{
		if (p != null)
		{
			p.destroy();
		}
	}

	public void executeCommand(String command, CommandType type)
	{
		this.type = type;

		String msg = MessageFormat.format(Messages.JsonConfigServerRunnable_CmdToBeExecuted, command);
		Logger.log(SDKConfigCorePlugin.getPlugin(), msg);

		PrintWriter pwdWriter = new PrintWriter(in);
		pwdWriter.println(command);
		pwdWriter.flush();
	}

	public boolean isAlive(Process p)
	{
		try
		{
			p.exitValue();
			return false;
		}
		catch (IllegalThreadStateException e)
		{
			return true;
		}
	}

	public void run()
	{
		StringBuilder builder = new StringBuilder();

		try
		{
			out = p.getInputStream();
			in = p.getOutputStream();

			byte[] buffer = new byte[4000];

			// sleep to make process.getErrorStream()/getInputStream() to return an available stream.
			p.waitFor(3000, TimeUnit.MILLISECONDS);
			boolean isAlive = true;
			while (isAlive)
			{
				int no = out.available();
				if (no == 0 && !builder.toString().isEmpty())
				{
					// notify and reset
					configServer.notifyHandler(builder.toString(), type);
					builder = new StringBuilder();
				}
				else if (no > 0)
				{
					int n = out.read(buffer, 0, Math.min(no, buffer.length));
					String string = new String(buffer, 0, n);
					configServer.console.print(string);
					configServer.console.flush();
					builder.append(string);
				}

				p.waitFor(100, TimeUnit.MILLISECONDS);
				isAlive = p.isAlive();

			}
		}

		catch (IOException e)
		{
			Logger.log(e);
		}
		catch (InterruptedException e1)
		{
		}

	}

}
