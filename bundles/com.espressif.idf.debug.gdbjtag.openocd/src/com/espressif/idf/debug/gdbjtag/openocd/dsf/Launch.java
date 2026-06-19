/*******************************************************************************
 * Copyright (c) 2013 Liviu Ionescu.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Liviu Ionescu - initial version
 *******************************************************************************/

package com.espressif.idf.debug.gdbjtag.openocd.dsf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.embedcdt.debug.gdbjtag.core.dsf.GnuMcuLaunch;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.debug.gdbjtag.openocd.Activator;
import com.espressif.idf.debug.gdbjtag.openocd.Configuration;
import com.espressif.idf.debug.gdbjtag.openocd.ConfigurationAttributes;
import com.espressif.idf.debug.gdbjtag.openocd.dsf.process.CustomIdfProcessFactory;
import com.espressif.idf.debug.gdbjtag.openocd.preferences.DefaultPreferences;

@SuppressWarnings("restriction")
public class Launch extends GnuMcuLaunch
{

	ILaunchConfiguration fConfig = null;
	private IProcess openOcdServerProcess;
	private IProcess gdbIProcess;

	private boolean fDoStartGdbServer = true;
	private boolean fDoStartGdbClient = true;

	private static final int SAFETY_NET_CLEANUP_DELAY_MS = 3000;

	private static final String SERVER_PROC_KEY = "SERVER_PROC";
	private static final String GDB_PROC_KEY = "GDB_PROC";

	public Launch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator)
	{
		super(launchConfiguration, mode, locator);
		fConfig = launchConfiguration;
	}

	public void setDoStartGdbServer(boolean doStartGdbServer)
	{
		fDoStartGdbServer = doStartGdbServer;
	}

	public boolean getDoStartGdbServer()
	{
		return fDoStartGdbServer;
	}

	public void setDoStartGdbClient(boolean doStartGdbClient)
	{
		fDoStartGdbClient = doStartGdbClient;
	}

	public boolean getDoStartGdbClient()
	{
		return fDoStartGdbClient;
	}

	void clearProcessReferences()
	{
		openOcdServerProcess = null;
		gdbIProcess = null;
	}

	private void scheduleSafetyNetCleanup()
	{
		final ILaunch launch = this;
		Job cleanupJob = new Job("Force debug session cleanup")
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				if (launch instanceof GdbLaunch)
				{
					DsfSession session = ((GdbLaunch) launch).getSession();
					if (session != null && DsfSession.isSessionActive(session.getId()))
					{
						// Session is still active well after terminate(): the graceful
						// shutdown is stuck. Re-kill the OS processes and close the MI
						// pipes/consoles so CDT's own shutdown can finally complete.
						LaunchProcessDictionary.getInstance().forceKillOsProcesses(launch);
					}
				}
				return Status.OK_STATUS;
			}
		};
		cleanupJob.setSystem(true);
		cleanupJob.schedule(SAFETY_NET_CLEANUP_DELAY_MS);
	}

	@Override
	protected void provideDefaults(ILaunchConfigurationWorkingCopy config) throws CoreException
	{
		super.provideDefaults(config);

		if (!config.hasAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS))
			config.setAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS, "localhost");

		if (!config.hasAttribute(IGDBJtagConstants.ATTR_JTAG_DEVICE_ID))
			config.setAttribute(IGDBJtagConstants.ATTR_JTAG_DEVICE_ID, ConfigurationAttributes.JTAG_DEVICE);

		if (!config.hasAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER))
			config.setAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER,
					DefaultPreferences.GDB_SERVER_GDB_PORT_NUMBER_DEFAULT);

		if (!config.hasAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME))
			config.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME,
					Activator.getInstance().getDefaultPreferences().getGdbClientExecutable());

		if (Configuration.getDoStartGdbServer(config))
			config.setAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, DefaultPreferences.GDB_SERVER_GDB_PORT_NUMBER_DEFAULT);

		config.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, CustomIdfProcessFactory.ID);
	}

	public void initializeServerConsole(IProgressMonitor monitor) throws CoreException
	{
		if (Configuration.getDoAddServerConsole(fConfig))
		{
			openOcdServerProcess = addServerProcess(Configuration.getGdbServerCommandName(fConfig));
			LaunchProcessDictionary.getInstance().addProcessToDictionary(this, SERVER_PROC_KEY, openOcdServerProcess);
			monitor.worked(1);
		}
	}

	public void initializeConsoles(IProgressMonitor monitor) throws CoreException
	{
		gdbIProcess = addClientProcess(Configuration.getGdbClientCommandName(fConfig));
		gdbIProcess.setAttribute(IProcess.ATTR_CMDLINE, Configuration.getGdbClientCommandLine(fConfig));
		LaunchProcessDictionary.getInstance().addProcessToDictionary(this, GDB_PROC_KEY, gdbIProcess);
		monitor.worked(1);
	}

	public IProcess addServerProcess(String label) throws CoreException
	{
		IProcess newProcess = null;
		try
		{
			Process serverProc = getDsfExecutor().submit(new Callable<Process>()
			{
				@Override
				public Process call() throws CoreException
				{
					DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(),
							getSession().getId());
					try
					{
						GdbServerBackend backend = tracker.getService(GdbServerBackend.class);
						return backend != null ? backend.getServerProcess() : null;
					}
					finally
					{
						tracker.dispose();
					}
				}
			}).get();

			if (serverProc != null)
			{
				newProcess = DebugPlugin.newProcess(this, serverProc, label, new HashMap<String, String>());
			}
		}
		catch (Exception e)
		{
			throw new CoreException(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error adding server process.", e));
		}
		return newProcess;
	}

	@Override
	public void terminate() throws DebugException
	{
		// Start CDT's normal terminate first so the framework owns ending the DSF
		// session and shutting down its executor. Then force-kill the OS processes and
		// close the MI pipes/consoles: that is what unblocks the graceful shutdown when
		// gdb/openocd are stuck (e.g. a core-reset loop), without us pre-terminating the
		// executor and triggering RejectedExecutionException in the framework.
		try
		{
			if (!isTerminated())
			{
				super.terminate();
			}
		}
		catch (RejectedExecutionException e)
		{
			// DSF executor already terminating; OS processes are force-killed below.
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		finally
		{
			LaunchProcessDictionary.getInstance().forceKillOsProcesses(this);
			clearProcessReferences();
			scheduleSafetyNetCleanup();
		}
	}

	@Override
	public boolean canDisconnect()
	{
		return true;
	}

	@Override
	public boolean canTerminate()
	{
		return true;
	}

	@Override
	public IProcess[] getProcesses()
	{
		List<IProcess> processes = new ArrayList<>();
		if (openOcdServerProcess != null)
			processes.add(openOcdServerProcess);
		if (gdbIProcess != null)
			processes.add(gdbIProcess);
		return processes.toArray(new IProcess[0]);
	}
}
