/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.sdk.config.core.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.aptana.core.util.ProcessRunnable;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.sdk.config.core.SDKConfigCorePlugin;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class JsonConfigServerRunnable extends ProcessRunnable
{

	private StringBuilder builder;
	private OutputStream in;
	private InputStream out;
	private JsonConfigServer configServer;

	public JsonConfigServerRunnable(Process process, JsonConfigServer configServer)
	{
		super(process, null, true);
		this.configServer = configServer;
	}

	public void run()
	{
		builder = new StringBuilder();
		BufferedReader br = null;
		try
		{

			out = p.getInputStream();
			in = p.getOutputStream();

			byte[] buffer = new byte[4000];
			while (isAlive(p))
			{
				int no = out.available();
				if (no == 0 && !builder.toString().isEmpty())
				{
					// notify and reset
					configServer.notifyHandler(builder.toString());
					builder = new StringBuilder();
				}
				else if (no > 0)
				{
					int n = out.read(buffer, 0, Math.min(no, buffer.length));
					String string = new String(buffer, 0, n);
					System.out.println(string);
					builder.append(string);
				}

				int ni = System.in.available();
				if (ni > 0)
				{
					int n = System.in.read(buffer, 0, Math.min(ni, buffer.length));
					in.write(buffer, 0, n);
					in.flush();
				}

				try
				{
					Thread.sleep(10);
				}
				catch (InterruptedException e)
				{
				}
			}
		}
		
		catch (IOException e)
		{
			//ignore
		} finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (Exception e)
				{
				}
			}
			monitor.done();
		}

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

	public void destory()
	{
		if (p != null)
		{
			p.destroy();
		}
	}

	public void executeCommand(String command)
	{
		String msg = MessageFormat.format(Messages.JsonConfigServerRunnable_CmdToBeExecuted, command);
		IDFCorePlugin.getPlugin().getLog().log(new Status(IStatus.INFO, SDKConfigCorePlugin.PLUGIN_ID, msg));

		PrintWriter pwdWriter = new PrintWriter(in);
		pwdWriter.println(command);
		pwdWriter.flush();
	}

}
