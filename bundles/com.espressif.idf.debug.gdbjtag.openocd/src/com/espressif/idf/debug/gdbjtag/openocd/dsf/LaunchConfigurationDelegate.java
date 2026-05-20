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
 *     Jonah Graham - fix for Neon
 *******************************************************************************/

package com.espressif.idf.debug.gdbjtag.openocd.dsf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.embedcdt.debug.gdbjtag.core.DebugUtils;
import org.eclipse.embedcdt.debug.gdbjtag.core.dsf.AbstractGnuMcuLaunchConfigurationDelegate;
import org.eclipse.embedcdt.debug.gdbjtag.core.dsf.GnuMcuServerServicesLaunchSequence;

import com.espressif.idf.debug.gdbjtag.openocd.Activator;
import com.espressif.idf.debug.gdbjtag.openocd.Configuration;

/**
 * This class is referred in the plugin.xml as an "org.eclipse.debug.core.launchDelegates" extension point.
 *
 * It inherits directly from the GDB Hardware Debug plug-in.
 *
 *
 */
@SuppressWarnings("restriction")
public class LaunchConfigurationDelegate extends AbstractGnuMcuLaunchConfigurationDelegate
{

	// ------------------------------------------------------------------------

	private final static String NON_STOP_FIRST_VERSION = "6.8.50"; //$NON-NLS-1$
	private final int STATUS_DLL_NOT_FOUND = -1073741515;

	ILaunchConfiguration fConfig = null;
	@SuppressWarnings("unused")
	private boolean fIsNonStopSession = false;
	private boolean fDoStartGdbServer = false;
	private boolean fDoStartGdbClient = true;
	private boolean fIgnoreGdbClient = false;

	// ------------------------------------------------------------------------

	@Override
	protected IDsfDebugServicesFactory newServiceFactory(ILaunchConfiguration config, String version)
	{

		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.LaunchConfigurationDelegate.newServiceFactory(" + config.getName() + ","
					+ version + ") " + this);
		}

		fConfig = config;
		return new ServicesFactory(version, ILaunchManager.DEBUG_MODE);
		// return new GdbJtagDebugServicesFactory(version);
	}

	protected IDsfDebugServicesFactory newServiceFactory(ILaunchConfiguration config, String version, String mode)
	{

		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.LaunchConfigurationDelegate.newServiceFactory(" + config.getName() + ","
					+ version + "," + mode + ") " + this);
		}

		fConfig = config;
		return new ServicesFactory(version, mode);
		// return new GdbJtagDebugServicesFactory(version);
	}

	public void ignoreGdbClient()
	{
		fIgnoreGdbClient = true;
	}

	public void doNotIngoreGdbClient()
	{
		fIgnoreGdbClient = false;
	}

	/**
	 * This method is called first when starting a debug session.
	 */
	@Override
	protected GdbLaunch createGdbLaunch(ILaunchConfiguration configuration, String mode, ISourceLocator locator)
			throws CoreException
	{

		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.LaunchConfigurationDelegate.createGdbLaunch(" + configuration.getName() + ","
					+ mode + ") " + this);
		}

		fDoStartGdbServer = Configuration.getDoStartGdbServer(configuration);
		if (!fIgnoreGdbClient)
		{
			fDoStartGdbClient = Configuration.getDoStartGdbClient(configuration);
		}

		DebugUtils.checkLaunchConfigurationStarted(configuration);

		// return new GdbLaunch(configuration, mode, locator);
		return new Launch(configuration, mode, locator);
	}

	@Override
	protected String getGDBVersion(ILaunchConfiguration config) throws CoreException
	{
		var gdbClientCommand = Configuration.getGdbClientCommand(config, null);
		return getGDBVersion(config, gdbClientCommand);
	}

	private String getGDBVersion(final ILaunchConfiguration configuration, String gdbClientCommand) throws CoreException
	{
		String[] cmdArray = { gdbClientCommand, "--version" };
		Process process;

		try
		{
			process = ProcessFactory.getFactory().exec(cmdArray, DebugUtils.getLaunchEnvironment(configuration));
		}
		catch (IOException e)
		{
			throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, DebugException.REQUEST_FAILED,
					"Error launching command", e.getCause()));
		}

		String cmdOutput = "";
		try
		{
			boolean finishedOnTime = process.waitFor(10, TimeUnit.SECONDS);

			if (!finishedOnTime)
			{
				process.destroyForcibly();
				throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, DebugException.REQUEST_FAILED,
						"GDB version request timed out.", null));
			}

			cmdOutput = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

		}
		catch (
				InterruptedException
				| IOException e)
		{
			process.destroyForcibly();
			throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, DebugException.REQUEST_FAILED,
					"Error reading GDB STDOUT", e));
		}

		String gdbVersion = LaunchUtils.getGDBVersionFromText(cmdOutput);
		if (gdbVersion == null || gdbVersion.isEmpty())
		{
			throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, DebugException.REQUEST_FAILED,
					"Could not determine GDB version", null));
		}
		return gdbVersion;
	}

	public void launchWithoutGdbClient(ILaunchConfiguration config, String mode, ILaunch launch,
			IProgressMonitor monitor) throws CoreException
	{
		fDoStartGdbClient = false;
		launch(config, mode, launch, monitor);
	}

	@Override
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException
	{
		org.eclipse.cdt.launch.LaunchUtils.enableActivity("org.eclipse.cdt.debug.dsfgdbActivity", true);

		var finalMonitor = monitor == null ? new NullProgressMonitor() : monitor;
		var launchName = config.getName();
		var mainLaunchThread = Thread.currentThread();

		var cancelWatcher = Thread.ofVirtual().name("CancelWatcher-" + launchName).start(() -> {
			try
			{
				while (!finalMonitor.isCanceled() && !launch.isTerminated())
				{
					Thread.sleep(200);
				}

				if (!launch.isTerminated() && launch.canTerminate())
				{
					try
					{
						launch.terminate();
					}
					catch (Exception ignore)
					{
					}
				}

				mainLaunchThread.interrupt();
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		});

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

		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.LaunchConfigurationDelegate.launchDebugger(" + config.getName() + ") " + this);
		}

		int totalWork = 10;
		if (fDoStartGdbServer)
		{
			// Extra units due to server console
			totalWork += 1;
		}

		monitor.beginTask(LaunchMessages.getString("GdbLaunchDelegate.0"), totalWork); //$NON-NLS-1$
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

	/** @since 4.1 */
	@Override
	protected void launchDebugSession(final ILaunchConfiguration config, ILaunch l, IProgressMonitor monitor)
			throws CoreException
	{

		if (Activator.getInstance().isDebugging())
		{
			System.out.println(
					"openocd.LaunchConfigurationDelegate.launchDebugSession(" + config.getName() + ") " + this);
		}

		// From here it is almost identical with the system one, except
		// the console creation, explicitly marked with '+++++'.

		// --------------------------------------------------------------------

		if (monitor.isCanceled())
		{
			cleanupLaunch(l);
			return;
		}

		SessionType sessionType = LaunchUtils.getSessionType(config);
		boolean attach = LaunchUtils.getIsAttach(config);

		final GdbLaunch launch = (GdbLaunch) l;

		if (sessionType == SessionType.REMOTE)
		{
			monitor.subTask(LaunchMessages.getString("GdbLaunchDelegate.1")); //$NON-NLS-1$
		}
		else if (sessionType == SessionType.CORE)
		{
			monitor.subTask(LaunchMessages.getString("GdbLaunchDelegate.2")); //$NON-NLS-1$
		}
		else
		{
			assert sessionType == SessionType.LOCAL : "Unexpected session type: " + sessionType.toString(); //$NON-NLS-1$
			monitor.subTask(LaunchMessages.getString("GdbLaunchDelegate.3")); //$NON-NLS-1$
		}

		// An attach session does not need to necessarily have an
		// executable specified. This is because:
		// - In remote multi-process attach, there will be more than one
		// executable
		// In this case executables need to be specified differently.
		// The current solution is to use the solib-search-path to specify
		// the path of any executable we can attach to.
		// - In local single process, GDB has the ability to find the executable
		// automatically.
		if (!attach)
		{
			checkBinaryDetails(config);
		}

		monitor.worked(1);

		// Must set this here for users that call directly the deprecated
		// newServiceFactory(String)
		fIsNonStopSession = LaunchUtils.getIsNonStopMode(config);

		String gdbVersion = getGDBVersion(config);

		// First make sure non-stop is supported, if the user want to use this
		// mode
		if (LaunchUtils.getIsNonStopMode(config) && !isNonStopSupportedInGdbVersion(gdbVersion))
		{
			cleanupLaunch(launch);
			throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, DebugException.REQUEST_FAILED,
					"Non-stop mode is not supported for GDB " + gdbVersion + ", GDB " + NON_STOP_FIRST_VERSION //$NON-NLS-1$ //$NON-NLS-2$
							+ " or higher is required.", //$NON-NLS-1$
					null));
		}

		if (LaunchUtils.getIsPostMortemTracing(config) && !isPostMortemTracingSupportedInGdbVersion(gdbVersion))
		{
			cleanupLaunch(launch);
			throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, DebugException.REQUEST_FAILED,
					"Post-mortem tracing is not supported for GDB " + gdbVersion + ", GDB " + NON_STOP_FIRST_VERSION //$NON-NLS-1$ //$NON-NLS-2$
							+ " or higher is required.", //$NON-NLS-1$
					null));
		}

		launch.setServiceFactory(newServiceFactory(config, gdbVersion, launch.getLaunchMode()));

		// Time to start the DSF stuff. First initialize the launch.
		// We do this here to avoid having to cleanup in case
		// the launch is cancelled above.
		// This initialize() call is the first thing that requires cleanup
		// followed by the steps further down which also need cleanup.
		launch.initialize();

		// vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv

		boolean succeed = false;

		// Assign 4 work ticks.
		IProgressMonitor subMonServer = new SubProgressMonitor(monitor, 4,
				SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);

		Sequence serverServicesLaunchSequence = getServerServicesSequence(launch.getSession(), launch, subMonServer);

		try
		{
			// Execute on DSF thread and wait for it.
			launch.getSession().getExecutor().execute(serverServicesLaunchSequence);
			serverServicesLaunchSequence.get();
			succeed = true;
		}
		catch (InterruptedException e1)
		{
			throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR,
					"Interrupted Exception in dispatch thread", e1)); //$NON-NLS-1$
		}
		catch (ExecutionException e1)
		{
			if (e1.getMessage().contains("Starting OpenOCD timed out.")) //$NON-NLS-1$
			{
				IStatus status = new Status(IStatus.OK, Activator.PLUGIN_ID, DebugException.REQUEST_FAILED,
						"Error in services launch sequence", e1.getCause()); //$NON-NLS-1$
				DebugPlugin.getDefault().getStatusHandler(status).handleStatus(status, null);
				throw new DebugException(Status.OK_STATUS);
			}
			else
			{
				throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED,
						"Error in services launch sequence", e1.getCause())); //$NON-NLS-1$
			}

		}
		catch (CancellationException e1)
		{
			// Launch aborted, so exit cleanly
			if (Activator.getInstance().isDebugging())
			{
				System.out.println("openocd.LaunchConfigurationDelegate.launchDebugger() aborted, so exit cleanly");
			}
			return;
		} finally
		{
			if (!succeed)
			{
				cleanupLaunch(launch);
			}
		}

		if (fDoStartGdbServer)
		{

			// This contributes 1 work units to the monitor
			((Launch) launch).initializeServerConsole(monitor);

			// Wait for the server to be available, or to know it failed.
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
						GdbServerBackend backend = tracker.getService(GdbServerBackend.class);
						if (backend != null)
						{
							return backend.getServerExitStatus();
						}
						else
						{
							throw new CoreException(
									new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Could not start GDB server."));
						}
					}
				};

				// Wait to get the server status. Being endless should not be a
				// problem, the timeout will kill it if too long.
				serverStatus = null;
				while (serverStatus == null)
				{
					if (monitor.isCanceled())
					{
						if (Activator.getInstance().isDebugging())
						{
							System.out.println(
									"openocd.LaunchConfigurationDelegate.launchDebugSession() sleep cancelled" + this);
						}
						cleanupLaunch(launch);
						return;
					}
					Thread.sleep(10);
					serverStatus = launch.getSession().getExecutor().submit(callable).get();
					if (Activator.getInstance().isDebugging())
					{
						System.out.print('!');
					}
				}

				if (serverStatus != Status.OK_STATUS)
				{
					if ("TERMINATED".equals(serverStatus.getMessage()))
					{
						cleanupLaunch(launch);
						return;
					}
					if (Activator.getInstance().isDebugging())
					{
						System.out.println("openocd.LaunchConfigurationDelegate.launchDebugger() " + serverStatus);
					}
					throw new CoreException(serverStatus);
				}

			}
			catch (InterruptedException e)
			{
				Activator.log(e);
			}
			catch (ExecutionException e)
			{
				Activator.log(e);
			}

			if (Activator.getInstance().isDebugging())
			{
				System.out.println(
						"openocd.LaunchConfigurationDelegate.launchDebugSession() * Server start confirmed. *");
			}
		}

		if (!fDoStartGdbClient)
		{
			if (Activator.getInstance().isDebugging())
			{
				System.out.println(
						"openocd.LaunchConfigurationDelegate.launchDebugSession() No GDB client, abruptly return.");
			}
			return;
		}

		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		// Create and invoke the launch sequence to create the debug control and
		// services
		IProgressMonitor subMon1 = new SubProgressMonitor(monitor, 4, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		Sequence servicesLaunchSequence = getServicesSequence(launch.getSession(), launch, subMon1);

		launch.getSession().getExecutor().execute(servicesLaunchSequence);
		// boolean succeed = false;
		try
		{
			servicesLaunchSequence.get();
			succeed = true;
		}
		catch (InterruptedException e1)
		{
			throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, DebugException.INTERNAL_ERROR,
					"Interrupted Exception in dispatch thread", e1)); //$NON-NLS-1$
		}
		catch (ExecutionException e1)
		{
			throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, DebugException.REQUEST_FAILED,
					"Error in services launch sequence", e1.getCause())); //$NON-NLS-1$
		}
		catch (CancellationException e1)
		{
			// Launch aborted, so exit cleanly
			return;
		} finally
		{
			if (!succeed)
			{
				cleanupLaunch(launch);
			}
		}

		if (monitor.isCanceled())
		{
			cleanupLaunch(launch);
			return;
		}

		// The initializeControl method should be called after the
		// ICommandControlService
		// is initialised in the ServicesLaunchSequence above. This is because
		// it is that
		// service that will trigger the launch cleanup (if we need it during
		// this launch)
		// through an ICommandControlShutdownDMEvent
		launch.initializeControl();

		// Add the GDB process object to the launch.

		// vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
		// launch.addCLIProcess("gdb"); //$NON-NLS-1$
		// monitor.worked(1);

		// This contributes one work units for the GDB client console
		// and optionally one for the semihosting console.
		((Launch) launch).initializeConsoles(monitor);
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		// Create and invoke the final launch sequence to setup GDB
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
						{
							rm.cancel();
						}
						else
						{
							rm.setStatus(getStatus());
						}
						rm.done();
					}
				});
			}
		};

		launch.getSession().getExecutor().execute(completeLaunchQuery);
		succeed = false;
		try
		{
			completeLaunchQuery.get();
			succeed = true;
		}
		catch (InterruptedException e1)
		{
			throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, DebugException.INTERNAL_ERROR,
					"Interrupted Exception in dispatch thread", e1)); //$NON-NLS-1$
		}
		catch (ExecutionException e1)
		{
			throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, DebugException.REQUEST_FAILED,
					"Error in final launch sequence", e1.getCause())); //$NON-NLS-1$
		}
		catch (CancellationException e1)
		{
			// Launch aborted, so exit cleanly
			return;
		} finally
		{
			if (!succeed)
			{
				// finalLaunchSequence failed. Shutdown the session so that all
				// started
				// services including any GDB process are shutdown. (bug 251486)
				cleanupLaunch(launch);
			}
		}
		// --------------------------------------------------------------------
	}
	
	/**
	 * Perform some local validations before starting the debug session.
	 */
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
			;
		}

		if (doStartServer)
		{
			// If we should start the server, there must be a configuration
			// present, otherwise refuse to start.
			String configOptions = "";
			try
			{
				configOptions = Configuration.getGdbServerOtherConfig(config);
			}
			catch (CoreException e)
			{
				;
			}

			if (configOptions.isEmpty())
			{
				throw new CoreException(
						new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Missing mandatory configuration. "
								+ "Fill-in the 'Config options:' field in the Debugger tab.")); //$NON-NLS-1$
			}
		}

		IPath path = super.checkBinaryDetails(config);
		return path;
	}

	/**
	 * Get a custom launch sequence, that inserts a GDB server starter.
	 */
	@Override
	protected Sequence getServicesSequence(DsfSession session, ILaunch launch, IProgressMonitor progressMonitor)
	{

		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.LaunchConfigurationDelegate.getServicesSequence()");
		}

		return new ServicesLaunchSequence(session, (GdbLaunch) launch, progressMonitor);
	}

	protected Sequence getServerServicesSequence(DsfSession session, ILaunch launch, IProgressMonitor progressMonitor)
	{

		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.LaunchConfigurationDelegate.getServerServicesSequence()");
		}

		return new GnuMcuServerServicesLaunchSequence(session, (GdbLaunch) launch, progressMonitor);
	}

	@Override
	protected void cleanupLaunch(final ILaunch launch)
	{
		try
		{
			LaunchProcessDictionary.getInstance().killAllProcessesInLaunch(launch.getLaunchConfiguration().getName());
		}
		catch (Exception ignore)
		{
		}

		Thread.ofVirtual().name("LaunchCleanup-" + launch.getLaunchConfiguration().getName()).start(() -> {
			try
			{
				LaunchConfigurationDelegate.super.cleanupLaunch(launch);
			}
			catch (Exception ignore)
			{
			}

			if (!launch.isTerminated())
			{
				DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
			}
		});
	}

	// ------------------------------------------------------------------------

}
