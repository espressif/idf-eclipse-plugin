/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.console.MessageConsoleStream;
import org.json.simple.parser.ParseException;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.sdk.config.core.SDKConfigCorePlugin;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class JsonConfigServer implements IMessagesHandlerNotifier
{

	protected MessageConsoleStream console;
	private List<IMessageHandlerListener> listeners;
	private IProject project;
	private JsonConfigServerRunnable runnable;
	private JsonConfigOutput configOutput;
	private Process process;
	private IFile file;

	public JsonConfigServer(IProject project, IFile file)
	{
		this.project = project;
		listeners = new ArrayList<IMessageHandlerListener>();
		configOutput = new JsonConfigOutput();
		this.file = file;
	}

	@Override
	public void addListener(IMessageHandlerListener listener)
	{
		listeners.add(listener);
	}

	public void destroy()
	{
		if (runnable != null)
		{
			runnable.destory();
		}
	}

	public void execute(String command, CommandType type)
	{
		if (runnable != null)
		{
			runnable.executeCommand(command, type);
		}
	}

	@Override
	public void notifyHandler(String message, CommandType type)
	{
		for (IMessageHandlerListener listener : listeners)
		{
			listener.notifyRequestServed(message, type);
		}
	}

	@Override
	public void removeListener(IMessageHandlerListener listener)
	{
		listeners.remove(listener);
	}

	public void start() throws IOException
	{
		IPath workingDir = project.getLocation();
		Map<String, String> env = new HashMap<String, String>(System.getenv());
		
		prepEnvMap(env);
		List<String> arguments = new ArrayList<>();
		File idfPythonScriptFile = IDFUtil.getIDFPythonScriptFile();
		if (!idfPythonScriptFile.exists())
		{
			throw new FileNotFoundException("File Not found:" + idfPythonScriptFile); //$NON-NLS-1$
		}
		
		try
		{
			String pythonPath = IDFUtil.getIDFPythonEnvPath();
			arguments.add(pythonPath);
			arguments.add(idfPythonScriptFile.getAbsolutePath());
			arguments.add("-B"); //$NON-NLS-1$
			arguments.add(IDFUtil.getBuildDir(project));
			arguments.add("-DSDKCONFIG=".concat(file.getName())); //$NON-NLS-1$
			arguments.add(IDFConstants.CONF_SERVER_CMD);
			Logger.log(arguments.toString());
			
			ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
			
			String oldSdkconfigValue = StringUtil.EMPTY;
			oldSdkconfigValue = getCmakeCacheSdkconfigValue();
			
			
			process = processRunner.run(arguments, workingDir, env);
			runnable = new JsonConfigServerRunnable(process, this, project, oldSdkconfigValue);
			Thread t = new Thread(runnable);
			t.start();
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}

	private void prepEnvMap(Map<String, String> env)
	{
		env.put("PYTHONUNBUFFERED", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		env.put("IDF_CCACHE_ENABLE", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		loadIdfPathWithSystemPath(env);
	}
	
	private void loadIdfPathWithSystemPath(Map<String, String> systemEnv)
	{
		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
		String idfExportPath = idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.PATH);
		String pathVar = "PATH"; // for Windows //$NON-NLS-1$
		String pathEntry = systemEnv.get(pathVar); // $NON-NLS-1$
		if (pathEntry == null)
		{
			pathVar = "Path"; //$NON-NLS-1$
			pathEntry = systemEnv.get(pathVar);
			if (pathEntry == null) // no idea
			{
				Logger.log(new Exception("No PATH found in the system environment variables")); //$NON-NLS-1$
			}
		}

		if (!StringUtil.isEmpty(pathEntry))
		{
			idfExportPath = idfExportPath.replace("$PATH", pathEntry); // macOS //$NON-NLS-1$
			idfExportPath = idfExportPath.replace("%PATH%", pathEntry); // Windows //$NON-NLS-1$
		}
		
		systemEnv.put(pathVar, idfExportPath);
		for (Entry<String, String> entry : idfEnvironmentVariables.getEnvMap().entrySet())
		{
			if (entry.getKey().equals(IDFEnvironmentVariables.PATH))
				continue;

			systemEnv.put(entry.getKey(), entry.getValue());
		}
	}

	private String getCmakeCacheSdkconfigValue() throws CoreException
	{
		File cmakeCacheFile = new File(IDFUtil.getBuildDir(project).concat("/CMakeCache.txt"));
		if (cmakeCacheFile.exists())
		{
			try (BufferedReader reader = new BufferedReader(new FileReader(cmakeCacheFile)))
			{
				String line;
				while ((line = reader.readLine()) != null)
				{
					// Check if the line starts with the specific prefix
					if (line.startsWith("SDKCONFIG:UNINITIALIZED=")) //$NON-NLS-1$
					{
						// Replace the line
						return line;
					}
				}
			}
			catch (IOException e)
			{
				Logger.log(e);
			}
		}

		return StringUtil.EMPTY;
	}

	public IJsonConfigOutput getOutput(String response, boolean isUpdate)
	{
		try
		{
			configOutput.parse(response, isUpdate);
		}
		catch (ParseException e)
		{
			Logger.log(SDKConfigCorePlugin.getPlugin(), e);
		}

		return configOutput;
	}

	public IJsonConfigOutput getOutput()
	{
		return configOutput;
	}

	public void addConsole(MessageConsoleStream console)
	{
		this.console = console;
	}

	public boolean isAlive()
	{
		return runnable.isAlive(process);
	}
}
