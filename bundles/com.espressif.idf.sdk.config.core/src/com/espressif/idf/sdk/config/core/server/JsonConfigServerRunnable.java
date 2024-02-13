/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.sdk.config.core.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.sdk.config.core.IJsonServerConfig;
import com.espressif.idf.sdk.config.core.SDKConfigCorePlugin;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class JsonConfigServerRunnable implements Runnable
{

	private JsonConfigServer configServer;
	private OutputStream in;
	private InputStream out;
	private CommandType type;
	private Process process;
	private IProject project;
	private String oldSdkconfigValue;
	
	public JsonConfigServerRunnable(Process process, JsonConfigServer configServer, IProject project, String oldSdkconfigValue)
	{
		this.process = process;
		this.configServer = configServer;
		this.project = project;
		this.oldSdkconfigValue = oldSdkconfigValue;
	}

	public void destory()
	{
		if (process != null)
		{
			stopProcess(process);
		}
	}
	
	private void stopProcess(Process process) {
	    process.descendants().forEach(new Consumer<ProcessHandle>() {
	        @Override
	        public void accept(ProcessHandle t) {
	            t.destroy();
	        }
	    });
	    process.destroy();
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
		return p.isAlive();
	}

	public void run()
	{
		StringBuilder builder = new StringBuilder();

		try
		{
			out = process.getInputStream();
			in = process.getOutputStream();

			byte[] buffer = new byte[4000];

			// sleep to make process.getErrorStream()/getInputStream() to return an available stream.
			process.waitFor(3000, TimeUnit.MILLISECONDS);
			boolean isAlive = true;
			while (isAlive)
			{
				int no = out.available();
				String output = builder.toString();
				if (no == 0 && !output.isEmpty() && isValidJson(output))
				{
					// notify and reset
					configServer.notifyHandler(output, type);
					builder = new StringBuilder();
				}
				else if (no > 0)
				{
					int n = out.read(buffer, 0, Math.min(no, buffer.length));
					String string = new String(buffer, 0, n);
					configServer.console.print(string);
					configServer.console.flush();
					builder.append(string);
					if (string.contains("Server running")) //$NON-NLS-1$
					{
						try
						{
							replaceOldCmakeCache();
						}
						catch (CoreException e)
						{
							Logger.log(e);
						}
					}
				}

				process.waitFor(100, TimeUnit.MILLISECONDS);
				isAlive = process.isAlive();

			}

			configServer.notifyHandler("Server connection closed", CommandType.CONNECTION_CLOSED); //$NON-NLS-1$

		}

		catch (IOException e)
		{
			Logger.log(e);
		}
		catch (InterruptedException e1)
		{
		}

	}
	
	private void replaceOldCmakeCache() throws CoreException
	{
		// SDKCONFIG:UNINITIALIZED=

		File cmakeCacheFile = new File(IDFUtil.getBuildDir(project).concat("/CMakeCache.txt")); //$NON-NLS-1$
		if (cmakeCacheFile.exists() && !StringUtil.isEmpty(oldSdkconfigValue))
		{
			StringBuilder contentBuilder = new StringBuilder();
			String lineSeparator = System.lineSeparator();

			try (BufferedReader reader = new BufferedReader(new FileReader(cmakeCacheFile)))
			{
				String line;
				while ((line = reader.readLine()) != null)
				{
					// Check if the line starts with the specific prefix for sdkconfig
					if (line.startsWith("SDKCONFIG:UNINITIALIZED=")) //$NON-NLS-1$
					{
						// Replace the line
						contentBuilder.append(oldSdkconfigValue).append(lineSeparator);
					}
					else
					{
						// Keep the line unchanged
						contentBuilder.append(line).append(lineSeparator);
					}
				}
			}
			catch (IOException e)
			{
				Logger.log(e); 
			}

			// Write the modified content back to the file
			try (PrintWriter writer = new PrintWriter(new FileWriter(cmakeCacheFile)))
			{
				writer.print(contentBuilder.toString());
			}
			catch (IOException e)
			{
				Logger.log(e);
			}
		}

	}
	
	protected boolean isValidJson(String output)
	{
		String jsonOutput = new JsonConfigProcessor().getInitialOutput(output);
		if (StringUtil.isEmpty(jsonOutput))
		{
			return false;
		}
		try
		{
			JSONObject jsonObj = (JSONObject) new JSONParser().parse(jsonOutput);
			if (jsonObj != null)
			{
				if (jsonObj.get(IJsonServerConfig.VISIBLE) != null && jsonObj.get(IJsonServerConfig.VALUES) != null
						&& jsonObj.get(IJsonServerConfig.RANGES) != null)
				{
					return true;
				}
			}
		}
		catch (ParseException e)
		{
			return false;
		}

		return false;
	}

}
