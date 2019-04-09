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

import com.aptana.core.ShellExecutable;
import com.aptana.core.util.ProcessRunner;
import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.sdk.config.core.IJsonServerConfig;

public class JsonConfigServer extends Thread implements IMessagesHandlerNotifier
{

	private IProject project;
	private ConfigServerProcessRunnable runnable;
	private List<IMessageHandlerListener> listeners;

	public JsonConfigServer(IProject project)
	{
		this.project = project;
		listeners = new ArrayList<IMessageHandlerListener>();
	}

	@Override
	public void run()
	{
		IPath workingDir = project.getLocation();

		File idfPythonScriptFile = IDFUtil.getIDFPythonScriptFile();
		List<String> arguments = new ArrayList<String>(
				Arrays.asList(idfPythonScriptFile.getAbsolutePath(), IDFConstants.CONF_SERVER_CMD));
		ProcessRunner processRunner = new ProcessRunner();
		Process process;
		try
		{
			process = processRunner.run(workingDir, getEnvironment(workingDir),
					arguments.toArray(new String[arguments.size()]));

//			Launch fLaunch = new Launch(null, ILaunchManager.RUN_MODE, null);
//			fLaunch.setAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP, Long.toString(System.currentTimeMillis()));
//			getLaunchManager().addLaunch(fLaunch);
//
//			DebugPlugin.newProcess(fLaunch, process,  "JSON Configuration Server - "+ project.getName()); //$NON-NLS-1$ ;

			runnable = new ConfigServerProcessRunnable(process, this);
			Thread t = new Thread(runnable);
			t.start();

		} catch (Exception e)
		{
			IDFCorePlugin.log(e);
		}
	}

	protected ILaunchManager getLaunchManager()
	{
		return DebugPlugin.getDefault().getLaunchManager();
	}

	protected Map<String, String> getEnvironment(IPath location)
	{
		return ShellExecutable.getEnvironment(location);
	}

	public void execute(String command)
	{
		runnable.executeCommand(command);
	}

	public void destroy()
	{
		runnable.destory();
	}
	
	public void addListener(IMessageHandlerListener listener)
	{
		listeners.add(listener);
	}

	public void notifyHandler(String message)
	{
		for (IMessageHandlerListener listener : listeners)
		{
			listener.notifyRequestServed(message);
		}
	}

	public void removeListener(IMessageHandlerListener listener)
	{
		listeners.remove(listener);
	}
}
