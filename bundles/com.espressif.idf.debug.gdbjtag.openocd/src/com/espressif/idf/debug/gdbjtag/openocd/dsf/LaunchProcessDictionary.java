package com.espressif.idf.debug.gdbjtag.openocd.dsf;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.debug.gdbjtag.openocd.dsf.process.IdfRuntimeProcess;

/**
 * Tracks debug processes per {@link ILaunch} and terminates only those belonging
 * to the given launch. Does not use system-wide kill or DSF executor shutdown.
 */
public class LaunchProcessDictionary
{
	private static final int PROCESS_DESTROY_TIMEOUT_SECONDS = 5;

	private static final int BACKEND_QUERY_TIMEOUT_SECONDS = 5;

	private static final LaunchProcessDictionary instance = new LaunchProcessDictionary();
	private final Map<ILaunch, Map<String, IProcess>> processDictionary;
	private final Map<ILaunch, Map<String, Long>> backendPidDictionary;

	private LaunchProcessDictionary()
	{
		processDictionary = new ConcurrentHashMap<>();
		backendPidDictionary = new ConcurrentHashMap<>();
	}

	public static LaunchProcessDictionary getInstance()
	{
		return instance;
	}

	public void addProcessToDictionary(ILaunch launch, String procName, IProcess process)
	{
		if (launch == null || process == null)
		{
			return;
		}
		processDictionary.computeIfAbsent(launch, k -> new ConcurrentHashMap<>()).put(procName, process);
	}

	/**
	 * Records the OS pid when OpenOCD/GDB is spawned so cleanup can kill the process
	 * even if DSF services are already torn down.
	 */
	public void registerBackendProcess(ILaunch launch, String procName, Process process)
	{
		if (launch == null || process == null)
		{
			return;
		}
		backendPidDictionary.computeIfAbsent(launch, k -> new ConcurrentHashMap<>()).put(procName, process.pid());
	}

	/**
	 * Immediately kills OS processes for this launch (stream monitors, tracked pids,
	 * DSF backend processes). Does not shut down the DSF session.
	 */
	public void forceKillOsProcesses(ILaunch launch)
	{
		if (launch == null)
		{
			return;
		}

		if (launch instanceof GdbLaunch)
		{
			cancelSessionJobs((GdbLaunch) launch);
		}

		terminateTrackedProcesses(launch);
		forceDestroyRegisteredPids(launch);

		if (launch instanceof GdbLaunch)
		{
			GdbConsoleCleanup.stopConsolesForLaunch(launch);
			forceDestroyBackendProcessesOnExecutor((GdbLaunch) launch);
		}
	}

	/**
	 * Stops Eclipse stream monitors for tracked {@link IProcess} wrappers only.
	 */
	public void terminateProcessMonitors(ILaunch launch)
	{
		if (launch == null)
		{
			return;
		}

		Map<String, IProcess> processMap = processDictionary.remove(launch);
		if (processMap == null)
		{
			return;
		}

		for (IProcess process : processMap.values())
		{
			terminateProcessWrapper(process, false);
		}
	}

	/**
	 * Terminates OpenOCD/GDB processes tracked for this launch, force-kills any
	 * stuck OS processes, and shuts down the DSF session. Safe to call multiple times.
	 */
	public void killAllProcessesInLaunch(ILaunch launch)
	{
		forceTerminateLaunch(launch);
	}

	/**
	 * Force cleanup when graceful DSF shutdown is stuck (e.g. OpenOCD reset loop).
	 * Kills stream monitors, OS processes and GDB consoles, then lets the framework
	 * end the DSF session and shut down its executor.
	 * <p>
	 * This intentionally does <b>not</b> call {@code DsfSession.endSession} or
	 * {@code executor.shutdown()}. Those are owned by CDT
	 * ({@code GdbLaunch#launchRemoved} / {@code DsfTerminateCommand}); pre-terminating
	 * the executor here makes the framework's own teardown throw
	 * {@link RejectedExecutionException}. Force-killing the OS process and closing the
	 * MI pipes is what unblocks CDT's normal shutdown so it completes on its own.
	 */
	public void forceTerminateLaunch(ILaunch launch)
	{
		forceKillOsProcesses(launch);
	}

	private void terminateTrackedProcesses(ILaunch launch)
	{
		Map<String, IProcess> processMap = processDictionary.remove(launch);
		if (processMap == null)
		{
			return;
		}

		for (IProcess process : processMap.values())
		{
			terminateProcessWrapper(process, true);
		}
	}

	/**
	 * Stop {@link IProcess} stream monitors first so Eclipse "Input Stream Monitor"
	 * threads exit, then force-destroy the OS process if it is still alive.
	 */
	private void terminateProcessWrapper(IProcess process, boolean force)
	{
		if (process == null || process.isTerminated())
		{
			return;
		}

		if (process instanceof IdfRuntimeProcess)
		{
			((IdfRuntimeProcess) process).forceTerminateWithoutWait();
			return;
		}

		try
		{
			process.terminate();
		}
		catch (Exception e)
		{
			Logger.log(e);
		}

		if (force)
		{
			forceDestroySystemProcess(process.getAdapter(Process.class));
		}
	}

	private void cancelSessionJobs(GdbLaunch launch)
	{
		DsfSession session = launch.getSession();
		if (session == null)
		{
			return;
		}

		Job.getJobManager().cancel(session);
		Job.getJobManager().cancel(session.getId());
		Job.getJobManager().cancel(launch);
	}

	private void forceDestroyRegisteredPids(ILaunch launch)
	{
		Map<String, Long> pidMap = backendPidDictionary.remove(launch);
		if (pidMap == null)
		{
			return;
		}

		for (Long pid : pidMap.values())
		{
			if (pid != null)
			{
				forceDestroyPid(pid.longValue());
			}
		}
	}

	private void forceDestroyBackendProcessesOnExecutor(GdbLaunch launch)
	{
		DsfSession session = launch.getSession();
		if (session == null || session.getExecutor() == null)
		{
			return;
		}

		try
		{
			session.getExecutor().submit(() -> {
				DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), session.getId());
				try
				{
					forceDestroyBackendProcesses(tracker);
				}
				finally
				{
					tracker.dispose();
				}
			}).get(BACKEND_QUERY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		}
		catch (TimeoutException e)
		{
			Logger.log(new Exception("Timed out querying DSF backend processes for termination", e)); //$NON-NLS-1$
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			Logger.log(e);
		}
		catch (RejectedExecutionException e)
		{
			// Executor already terminating; registered pids were force-killed above.
		}
		catch (ExecutionException e)
		{
			Logger.log(e);
		}
	}

	private void forceDestroyBackendProcesses(DsfServicesTracker tracker)
	{
		GdbServerBackend serverBackend = tracker.getService(GdbServerBackend.class);
		if (serverBackend != null)
		{
			forceDestroySystemProcess(serverBackend.getServerProcess());
		}

		IGDBBackend gdbBackend = tracker.getService(IGDBBackend.class);
		if (gdbBackend != null)
		{
			forceDestroySystemProcess(gdbBackend.getProcess());
		}
	}

	private void forceDestroySystemProcess(Process process)
	{
		if (process == null || !process.isAlive())
		{
			return;
		}

		forceDestroyPid(process.pid());
		closeStreamSafely(process.getInputStream());
		closeStreamSafely(process.getOutputStream());
		closeStreamSafely(process.getErrorStream());
	}

	private void forceDestroyPid(long pid)
	{
		if (pid <= 0)
		{
			return;
		}

		ProcessHandle handle = ProcessHandle.of(pid).orElse(null);
		if (handle == null)
		{
			return;
		}

		// Snapshot the whole process tree up front. Once a process dies its
		// parent/child links are gone, and on Windows children are never killed
		// together with the parent. An orphaned openocd would keep holding the
		// JTAG/USB adapter and break the next debug session.
		List<ProcessHandle> tree = new ArrayList<>();
		handle.descendants().forEach(tree::add);
		tree.add(handle);

		// Cross-platform termination via the JVM: destroy() maps to SIGTERM /
		// TerminateProcess, destroyForcibly() to SIGKILL / TerminateProcess.
		destroyTree(tree, false);
		if (isAnyAlive(tree))
		{
			destroyTree(tree, true);
		}

		// Last resort: OS-native tree kill for anything ProcessHandle missed.
		if (isAnyAlive(tree))
		{
			killTreeViaOsCommand(pid);
		}

		if (isAnyAlive(tree))
		{
			Logger.log(new Exception("Debug process tree (root pid " + pid + ") still alive after forced termination")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private void destroyTree(List<ProcessHandle> tree, boolean forcibly)
	{
		for (ProcessHandle ph : tree)
		{
			if (!ph.isAlive())
			{
				continue;
			}
			if (forcibly)
			{
				ph.destroyForcibly();
			}
			else
			{
				ph.destroy();
			}
		}

		for (ProcessHandle ph : tree)
		{
			try
			{
				ph.onExit().get(PROCESS_DESTROY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			}
			catch (TimeoutException e)
			{
				// Escalation is handled by the caller (destroyForcibly / OS fallback).
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				return;
			}
			catch (ExecutionException e)
			{
				Logger.log(e);
			}
		}
	}

	private boolean isAnyAlive(List<ProcessHandle> tree)
	{
		for (ProcessHandle ph : tree)
		{
			if (ph.isAlive())
			{
				return true;
			}
		}
		return false;
	}

	private void killTreeViaOsCommand(long pid)
	{
		String[] command;
		if (Platform.OS_WIN32.equals(Platform.getOS()))
		{
			// /T terminates the process tree, /F forces it.
			command = new String[] { "taskkill", "/F", "/T", "/PID", Long.toString(pid) }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		else
		{
			command = new String[] { "kill", "-9", Long.toString(pid) }; //$NON-NLS-1$ //$NON-NLS-2$
		}

		try
		{
			Process killProcess = Runtime.getRuntime().exec(command);
			killProcess.waitFor(2, TimeUnit.SECONDS);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}

	private void closeStreamSafely(Object stream)
	{
		if (stream == null)
		{
			return;
		}
		try
		{
			if (stream instanceof InputStream)
			{
				((InputStream) stream).close();
			}
			else if (stream instanceof OutputStream)
			{
				((OutputStream) stream).close();
			}
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}
}
