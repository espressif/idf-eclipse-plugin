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
 * Liviu Ionescu - initial version
 * Jonah Graham - fix for Neon
 *******************************************************************************/

package com.espressif.idf.debug.gdbjtag.openocd.dsf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.LaunchMessages;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.cdt.dsf.gdb.launching.ServicesLaunchSequence;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.embedcdt.core.StringUtils;
import org.eclipse.embedcdt.debug.gdbjtag.core.DebugUtils;
import org.eclipse.embedcdt.debug.gdbjtag.core.dsf.AbstractGnuMcuLaunchConfigurationDelegate;
import org.eclipse.embedcdt.debug.gdbjtag.core.dsf.GnuMcuServerServicesLaunchSequence;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.debug.gdbjtag.openocd.Activator;
import com.espressif.idf.debug.gdbjtag.openocd.Configuration;
import com.espressif.idf.debug.gdbjtag.openocd.ConfigurationAttributes;
import com.espressif.idf.debug.gdbjtag.openocd.ui.Messages;

@SuppressWarnings("restriction")
public class LaunchConfigurationDelegate extends AbstractGnuMcuLaunchConfigurationDelegate
{
	private static final String NON_STOP_FIRST_VERSION = "6.8.50"; //$NON-NLS-1$
	private static final int STATUS_DLL_NOT_FOUND = -1073741515;
	private static final int SERVER_STATUS_POLL_INTERVAL_MS = 250;
	private static final int SERVER_STATUS_POLL_TIMEOUT_MS = 30_000;
	private static final int COMPLETE_INIT_TIMEOUT_MS = 60_000;

	private static final ThreadLocal<LaunchOptions> pendingLaunchOptions = new ThreadLocal<>();

	ILaunchConfiguration fConfig = null;
	@SuppressWarnings("unused")
	private boolean fIsNonStopSession = false;

	@Override
	protected IDsfDebugServicesFactory newServiceFactory(ILaunchConfiguration config, String version)
	{
		fConfig = config;
		return new ServicesFactory(version, ILaunchManager.DEBUG_MODE);
	}

	protected IDsfDebugServicesFactory newServiceFactory(ILaunchConfiguration config, String version, String mode)
	{
		fConfig = config;
		return new ServicesFactory(version, mode);
	}

	/**
	 * Launch OpenOCD without starting the GDB client (application-level tracing).
	 */
	public void runOpenOcdOnlyLaunch(ILaunchConfiguration config, String mode, IProgressMonitor monitor)
			throws CoreException
	{
		pendingLaunchOptions.set(LaunchOptions.openOcdOnly());
		try
		{
			config.launch(mode, monitor != null ? monitor : new NullProgressMonitor());
		}
		finally
		{
			pendingLaunchOptions.remove();
		}
	}

	@Override
	protected GdbLaunch createGdbLaunch(ILaunchConfiguration configuration, String mode, ISourceLocator locator)
			throws CoreException
	{
		ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
		if (Configuration.getDoStartGdbServer(wc))
		{
			Configuration.allocateServerPorts(wc);
		}

		Launch launch = new Launch(wc, mode, locator);
		launch.setDoStartGdbServer(Configuration.getDoStartGdbServer(wc));

		LaunchOptions options = pendingLaunchOptions.get();
		if (options != null && options.isOpenOcdOnly())
		{
			wc.setAttribute(ConfigurationAttributes.DO_START_GDB_CLIENT, false);
			launch.setDoStartGdbClient(false);
		}
		else
		{
			launch.setDoStartGdbClient(Configuration.getDoStartGdbClient(wc));
		}

		DebugUtils.checkLaunchConfigurationStarted(wc);
		return launch;
	}

	@Override
	protected String getGDBVersion(ILaunchConfiguration config) throws CoreException
	{
		String gdbClientCommand = Configuration.getGdbClientCommand(config, null);
		return getGDBVersion(config, gdbClientCommand);
	}

	private String getGDBVersion(final ILaunchConfiguration configuration, String gdbClientCommand) throws CoreException
	{
		String[] cmdArray = new String[2];
		cmdArray[0] = gdbClientCommand;
		cmdArray[1] = "--version";

		final Process process;
		try
		{
			process = ProcessFactory.getFactory().exec(cmdArray, DebugUtils.getLaunchEnvironment(configuration));
		}
		catch (IOException e)
		{
			throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, DebugException.REQUEST_FAILED,
					"Error while launching command: " + StringUtils.join(cmdArray, " "), e.getCause()));
		}

		Job timeoutJob = new Job("GDB version timeout job")
		{
			{
				setSystem(true);
			}
			@Override
			protected IStatus run(IProgressMonitor arg)
			{
				process.destroy();
				return Status.OK_STATUS;
			}
		};
		timeoutJob.schedule(10000);

		InputStream stream = null;
		StringBuilder cmdOutput = new StringBuilder(200);
		try
		{
			stream = process.getInputStream();
			Reader r = new InputStreamReader(stream);
			BufferedReader reader = new BufferedReader(r);

			String line;
			while ((line = reader.readLine()) != null)
			{
				cmdOutput.append(line).append('\n');
			}
		}
		catch (IOException e)
		{
			throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, DebugException.REQUEST_FAILED,
					"Error reading GDB STDOUT", e.getCause()));
		} finally
		{
			timeoutJob.cancel();
			if (stream != null)
			{
				try
				{
					stream.close();
				}
				catch (IOException e)
				{
				}
			}
			process.destroy();
		}

		String gdbVersion = LaunchUtils.getGDBVersionFromText(cmdOutput.toString());
		if (gdbVersion == null || gdbVersion.isEmpty())
		{
			String errorMessage = process.exitValue() == STATUS_DLL_NOT_FOUND ? Messages.DllNotFound_ExceptionMessage
					: cmdOutput.toString().trim();
			if (errorMessage.isEmpty())
			{
				errorMessage = "Could not determine GDB version";
			}
			throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, DebugException.REQUEST_FAILED,
					errorMessage, null));
		}
		return gdbVersion;
	}

	@Override
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException
	{
		org.eclipse.cdt.launch.LaunchUtils.enableActivity("org.eclipse.cdt.debug.dsfgdbActivity", true);

		final IProgressMonitor finalMonitor = monitor == null ? new NullProgressMonitor() : monitor;
		final Thread mainLaunchThread = Thread.currentThread();

		Thread cancelWatcher = new Thread(() -> {
			try
			{
				while (!finalMonitor.isCanceled() && !launch.isTerminated())
				{
					Thread.sleep(200);
				}

				if (finalMonitor.isCanceled())
				{
					if (!launch.isTerminated() && launch.canTerminate())
					{
						try
						{
							launch.terminate();
						}
						catch (Exception e)
						{
							Logger.log(e);
						}
					}
					mainLaunchThread.interrupt();
				}
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		});

		cancelWatcher.setDaemon(true);
		cancelWatcher.setName("Launch Cancel Watcher");
		cancelWatcher.start();

		try
		{
			if (mode.equals(ILaunchManager.DEBUG_MODE) || mode.equals(ILaunchManager.RUN_MODE))
			{
				launchDebugger(config, launch, finalMonitor);
			}
		} finally
		{
			cancelWatcher.interrupt();
			Thread.interrupted();
		}
	}

	private void launchDebugger(ILaunchConfiguration config, ILaunch launch, IProgressMonitor monitor)
			throws CoreException
	{
		Launch idfLaunch = (Launch) launch;
		int totalWork = idfLaunch.getDoStartGdbServer() ? 11 : 10;
		monitor.beginTask(LaunchMessages.getString("GdbLaunchDelegate.0"), totalWork);

		if (monitor.isCanceled())
		{
			cleanupLaunch(launch);
			return;
		}

		try
		{
			launchDebugSession(config, launch, monitor);
		} finally
		{
			monitor.done();
		}
	}

	@Override
	protected void launchDebugSession(final ILaunchConfiguration config, ILaunch l, IProgressMonitor monitor)
			throws CoreException
	{
		if (monitor.isCanceled())
		{
			cleanupLaunch(l);
			return;
		}

		SessionType sessionType = LaunchUtils.getSessionType(config);
		boolean attach = LaunchUtils.getIsAttach(config);
		final Launch launch = (Launch) l;

		if (sessionType == SessionType.REMOTE)
			monitor.subTask(LaunchMessages.getString("GdbLaunchDelegate.1"));
		else if (sessionType == SessionType.CORE)
			monitor.subTask(LaunchMessages.getString("GdbLaunchDelegate.2"));
		else
			monitor.subTask(LaunchMessages.getString("GdbLaunchDelegate.3"));

		if (!attach)
			checkBinaryDetails(config);

		monitor.worked(1);
		fIsNonStopSession = LaunchUtils.getIsNonStopMode(config);

		if (launch.getDoStartGdbClient())
		{
			String gdbVersion = getGDBVersion(config);

			if (LaunchUtils.getIsNonStopMode(config) && !isNonStopSupportedInGdbVersion(gdbVersion))
			{
				cleanupLaunch(launch);
				throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, DebugException.REQUEST_FAILED,
						"Non-stop mode is not supported", null));
			}

			if (LaunchUtils.getIsPostMortemTracing(config) && !isPostMortemTracingSupportedInGdbVersion(gdbVersion))
			{
				cleanupLaunch(launch);
				throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, DebugException.REQUEST_FAILED,
						"Post-mortem tracing is not supported", null));
			}

			launch.setServiceFactory(newServiceFactory(config, gdbVersion, launch.getLaunchMode()));
		}
		else
		{
			launch.setServiceFactory(newServiceFactory(config, "7.0", launch.getLaunchMode()));
		}

		launch.initialize();

		boolean succeed = false;
		IProgressMonitor subMonServer = new SubProgressMonitor(monitor, 4,
				SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		Sequence serverServicesLaunchSequence = getServerServicesSequence(launch.getSession(), launch, subMonServer);

		try
		{
			launch.getSession().getExecutor().execute(serverServicesLaunchSequence);
			serverServicesLaunchSequence.get();
			succeed = true;
		}
		catch (InterruptedException e1)
		{
			if (monitor.isCanceled())
				return;
			throw new DebugException(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR, "Interrupted", e1));
		}
		catch (ExecutionException e1)
		{
			if (e1.getMessage() != null && e1.getMessage().contains("Starting OpenOCD timed out."))
			{
				IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						Activator.OPENOCD_STARTUP_TIMEOUT_STATUS, "Timeout", e1.getCause());
				DebugPlugin.getDefault().getStatusHandler(status).handleStatus(status, null);
				throw new DebugException(Status.OK_STATUS);
			}
			throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED,
					"Error", e1.getCause()));
		}
		catch (CancellationException e1)
		{
			return;
		} finally
		{
			if (!succeed)
				cleanupLaunch(launch);
		}

		if (launch.getDoStartGdbServer())
		{
			launch.initializeServerConsole(monitor);
			IStatus serverStatus;
			try
			{
				Callable<IStatus> callable = new Callable<IStatus>()
				{
					@Override
					public IStatus call() throws CoreException
					{
						DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(),
								launch.getSession().getId());
						try
						{
							GdbServerBackend backend = tracker.getService(GdbServerBackend.class);
							if (backend != null)
								return backend.getServerExitStatus();
							throw new CoreException(
									new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Could not start GDB server."));
						}
						finally
						{
							tracker.dispose();
						}
					}
				};

				long deadline = System.currentTimeMillis() + SERVER_STATUS_POLL_TIMEOUT_MS;
				serverStatus = null;
				while (serverStatus == null)
				{
					if (monitor.isCanceled())
					{
						cleanupLaunch(launch);
						return;
					}
					if (System.currentTimeMillis() >= deadline)
					{
						cleanupLaunch(launch);
						throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
								Activator.OPENOCD_STARTUP_TIMEOUT_STATUS, "OpenOCD server status timed out.", null));
					}
					Thread.sleep(SERVER_STATUS_POLL_INTERVAL_MS);
					serverStatus = launch.getSession().getExecutor().submit(callable).get();
				}

				if (serverStatus != Status.OK_STATUS)
				{
					if ("TERMINATED".equals(serverStatus.getMessage()))
					{
						cleanupLaunch(launch);
						return;
					}
					throw new CoreException(serverStatus);
				}
			}
			catch (InterruptedException e)
			{
				if (monitor.isCanceled())
				{
					cleanupLaunch(launch);
					return;
				}
				Activator.log(e);
			}
			catch (ExecutionException e)
			{
				Activator.log(e);
			}
		}

		if (!launch.getDoStartGdbClient())
			return;

		IProgressMonitor subMon1 = new SubProgressMonitor(monitor, 4, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		Sequence servicesLaunchSequence = getServicesSequence(launch.getSession(), launch, subMon1);

		launch.getSession().getExecutor().execute(servicesLaunchSequence);
		succeed = false;
		try
		{
			servicesLaunchSequence.get();
			succeed = true;
		}
		catch (InterruptedException e1)
		{
			if (monitor.isCanceled())
				return;
			throw new DebugException(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, DebugException.INTERNAL_ERROR, "Interrupted", e1));
		}
		catch (ExecutionException e1)
		{
			throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, DebugException.REQUEST_FAILED,
					"Error", e1.getCause()));
		}
		catch (CancellationException e1)
		{
			return;
		} finally
		{
			if (!succeed)
				cleanupLaunch(launch);
		}

		if (monitor.isCanceled())
		{
			cleanupLaunch(launch);
			return;
		}

		launch.initializeControl();
		launch.initializeConsoles(monitor);

		final IProgressMonitor subMon2 = new SubProgressMonitor(monitor, 4,
				SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		Query<Object> completeLaunchQuery = new Query<Object>()
		{
			@Override
			protected void execute(final DataRequestMonitor<Object> rm)
			{
				DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(),
						launch.getSession().getId());
				IGDBControl control = tracker.getService(IGDBControl.class);
				tracker.dispose();
				control.completeInitialization(new RequestMonitorWithProgress(ImmediateExecutor.getInstance(), subMon2)
				{
					@Override
					protected void handleCompleted()
					{
						if (isCanceled())
							rm.cancel();
						else
							rm.setStatus(getStatus());
						rm.done();
					}
				});
			}
		};

		launch.getSession().getExecutor().execute(completeLaunchQuery);
		succeed = false;
		try
		{
			// Bounded wait: completeInitialization runs the init commands (e.g.
			// "mon reset halt"). If the target is unresponsive or stuck in a reset
			// loop these never return, which would otherwise hang the launch forever.
			completeLaunchQuery.get(COMPLETE_INIT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
			succeed = true;
		}
		catch (InterruptedException e1)
		{
			if (monitor.isCanceled())
				return;
			throw new DebugException(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, DebugException.INTERNAL_ERROR, "Interrupted", e1));
		}
		catch (TimeoutException e1)
		{
			completeLaunchQuery.cancel(true);
			throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, DebugException.REQUEST_FAILED,
					"Debugger initialization timed out. The target may be unresponsive or stuck in a reset loop.", e1));
		}
		catch (ExecutionException e1)
		{
			throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, DebugException.REQUEST_FAILED,
					"Error", e1.getCause()));
		}
		catch (CancellationException e1)
		{
			return;
		} finally
		{
			if (!succeed)
				cleanupLaunch(launch);
		}
	}

	@Override
	protected IPath checkBinaryDetails(final ILaunchConfiguration config) throws CoreException
	{
		boolean doStartServer = true;
		try
		{
			doStartServer = Configuration.getDoStartGdbServer(config);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}

		if (doStartServer)
		{
			String configOptions = "";
			try
			{
				configOptions = Configuration.getGdbServerOtherConfig(config);
			}
			catch (CoreException e)
			{
				Logger.log(e);
			}
			if (configOptions.isEmpty())
				throw new CoreException(
						new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Missing mandatory configuration."));
		}
		return super.checkBinaryDetails(config);
	}

	@Override
	protected Sequence getServicesSequence(DsfSession session, ILaunch launch, IProgressMonitor progressMonitor)
	{
		return new ServicesLaunchSequence(session, (GdbLaunch) launch, progressMonitor);
	}

	protected Sequence getServerServicesSequence(DsfSession session, ILaunch launch, IProgressMonitor progressMonitor)
	{
		return new GnuMcuServerServicesLaunchSequence(session, (GdbLaunch) launch, progressMonitor);
	}

	@Override
	protected void cleanupLaunch(final ILaunch launch)
	{
		try
		{
			LaunchProcessDictionary.getInstance().killAllProcessesInLaunch(launch);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}

		if (launch instanceof Launch)
		{
			((Launch) launch).clearProcessReferences();
		}

		Job cleanupJob = new Job("Terminating Launch")
		{
			@Override
			protected IStatus run(IProgressMonitor m)
			{
				try
				{
					LaunchConfigurationDelegate.super.cleanupLaunch(launch);
				}
				catch (RejectedExecutionException e)
				{
					// Expected: terminate() already started the DSF executor shutdown.
				}
				catch (Exception e)
				{
					Logger.log(e);
				}
				try
				{
					DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
				}
				catch (RejectedExecutionException e)
				{
					// Expected: the DSF session/executor is already shutting down.
				}
				catch (Exception e)
				{
					Logger.log(e);
				}
				return Status.OK_STATUS;
			}
		};
		cleanupJob.setSystem(true);
		cleanupJob.schedule();
	}
}
