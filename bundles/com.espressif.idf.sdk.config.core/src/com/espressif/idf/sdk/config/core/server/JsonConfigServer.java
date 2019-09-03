/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.json.simple.parser.ParseException;

import com.aptana.core.ShellExecutable;
import com.aptana.core.util.ProcessRunner;
import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.sdk.config.core.SDKConfigCorePlugin;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class JsonConfigServer implements IMessagesHandlerNotifier
{

	private List<IMessageHandlerListener> listeners;
	private IProject project;
	private JsonConfigServerRunnable runnable;
	private JsonConfigOutput configOutput;

	public JsonConfigServer(IProject project)
	{
		this.project = project;
		listeners = new ArrayList<IMessageHandlerListener>();
		configOutput = new JsonConfigOutput();
	}

	public void addListener(IMessageHandlerListener listener)
	{
		listeners.add(listener);
	}

	public void destroy()
	{
		runnable.destory();
	}

	public void execute(String command, CommandType type)
	{
		runnable.executeCommand(command, type);
	}

	public void notifyHandler(String message, CommandType type)
	{
		for (IMessageHandlerListener listener : listeners)
		{
			listener.notifyRequestServed(message, type);
		}
	}

	public void removeListener(IMessageHandlerListener listener)
	{
		listeners.remove(listener);
	}

	public void start()
	{
		IPath workingDir = project.getLocation();

		File idfPythonScriptFile = IDFUtil.getIDFPythonScriptFile();
		String pythonPath = IDFUtil.findCommandFromBuildEnvPath(IDFConstants.PYTHON_CMD);
		List<String> arguments = new ArrayList<String>(
				Arrays.asList(pythonPath, idfPythonScriptFile.getAbsolutePath(), IDFConstants.CONF_SERVER_CMD));
		ProcessRunner processRunner = new ProcessRunner();
		Process process;
		try
		{
			process = processRunner.run(workingDir, getEnvironment(workingDir),
					arguments.toArray(new String[arguments.size()]));

			runnable = new JsonConfigServerRunnable(process, this);
			Thread t = new Thread(runnable);
			t.start();

		}
		catch (Exception e)
		{
			Logger.log(SDKConfigCorePlugin.getPlugin(), e);

		}
	}

	protected Map<String, String> getEnvironment(IPath location)
	{
		return ShellExecutable.getEnvironment(location);
	}

	protected ILaunchManager getLaunchManager()
	{
		return DebugPlugin.getDefault().getLaunchManager();
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
}
