/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.launch;

import java.io.IOException;
import java.util.OptionalLong;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.espressif.idf.core.logging.Logger;

/**
 * Utility class for waiting for process exit
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 */
public final class ProcessWaiter
{
	private ProcessWaiter()
	{
		// utility
	}

	public static IStatus waitForExitByPid(OptionalLong pidOpt, IProgressMonitor monitor)
	{
		if (pidOpt == null || pidOpt.isEmpty())
			return Status.OK_STATUS;

		long pid = pidOpt.getAsLong();
		if (pid <= 0)
			return Status.error("Invalid PID: " + pid); //$NON-NLS-1$

		try
		{
			while (!monitor.isCanceled() && ProcessHandle.of(pid).map(ProcessHandle::isAlive).orElse(false))
			{
				Thread.sleep(1000);
			}
			return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			return Status.CANCEL_STATUS;
		}
		catch (Exception e)
		{
			Logger.log(e);
			return Status.error(e.getMessage());
		}
	}

	/**
	 * Fallback waiter (macOS): poll for a process matching execPath using pgrep.
	 * This avoids relying on System Events / Accessibility permissions for PID lookup.
	 */
	public static IStatus waitForExitByExecPath(String execPath, IProgressMonitor monitor)
	{
		if (execPath == null || execPath.isBlank())
			return Status.error("execPath is null/blank"); //$NON-NLS-1$

		try
		{
			while (!monitor.isCanceled() && isAnyProcessMatchingExecPath(execPath))
			{
				Thread.sleep(1000);
			}
			return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			return Status.CANCEL_STATUS;
		}
		catch (Exception e)
		{
			Logger.log(e);
			return Status.error(e.getMessage());
		}
	}

	private static boolean isAnyProcessMatchingExecPath(String execPath) throws IOException, InterruptedException
	{
		// pgrep -f searches the full command line; quote it defensively for sh -lc
		String q = ProcessUtils.bashSingleQuote(execPath);
		String cmd = "pgrep -f " + q + " >/dev/null 2>&1; echo $?"; //$NON-NLS-1$ //$NON-NLS-2$

		Process p = new ProcessBuilder("bash", "-lc", cmd) //$NON-NLS-1$ //$NON-NLS-2$
				.redirectErrorStream(true).start();

		int exit = p.waitFor();
		String out = ProcessUtils.readAll(p.getInputStream());

		// If bash failed, treat as "not running" to avoid infinite waits.
		if (exit != 0 && (out == null || out.isBlank()))
			return false;

		// echo $? prints 0 if pgrep found something
		return out.trim().endsWith("0"); //$NON-NLS-1$
	}
}
