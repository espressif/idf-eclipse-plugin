/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.install;

import java.text.MessageFormat;
import java.time.Duration;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jgit.lib.BatchingProgressMonitor;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.ui.IDFConsole;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class GitProgressMonitor extends BatchingProgressMonitor
{
	private IDFConsole idfConsole = new IDFConsole();
	private MessageConsoleStream console;
	private IProgressMonitor monitor;

	public GitProgressMonitor(IProgressMonitor monitor)
	{
		console = idfConsole.getConsoleStream();
		this.monitor = monitor;
	}

	protected void onUpdate(final String taskName, final int workCurr)
	{
		onUpdate(taskName, workCurr, null);
	}

	protected void onUpdate(final String taskName, final int workCurr, Duration duration)
	{
		console.println(MessageFormat.format("{0}  {1} ", taskName, workCurr)); //$NON-NLS-1$
	}

	protected void onUpdate(final String taskName, final int workCurr, final int workTotal, final int percentDone)
	{
		onUpdate(taskName, workCurr, workTotal, percentDone, null);
	}

	protected void onUpdate(final String taskName, final int workCurr, final int workTotal, final int percentDone,
			Duration duration)
	{
		console.println(
				MessageFormat.format("{0} {1}, total {2} {3}% Completed", taskName, workCurr, workTotal, percentDone)); //$NON-NLS-1$
	}

	protected void onEndTask(final String taskName, final int workCurr)
	{
		onEndTask(taskName, workCurr, null);
	}

	protected void onEndTask(final String taskName, final int workCurr, Duration duration)
	{
		console.println(MessageFormat.format("Finished {0}  {1}", taskName, workCurr)); //$NON-NLS-1$
	}

	protected void onEndTask(final String taskName, final int workCurr, final int workTotal, final int percentDone)
	{
		onEndTask(taskName, workCurr, workTotal, percentDone, null);
	}

	protected void onEndTask(final String taskName, final int workCurr, final int workTotal, final int percentDone,
			Duration duration)
	{
		console.println(MessageFormat.format("Finished {0} {1}, total {2} {3}% Completed", taskName, workCurr, //$NON-NLS-1$
				workTotal, percentDone));
	}

	@Override
	public boolean isCancelled()
	{
		if (monitor.isCanceled())
		{
			console.println("Download cancellation initiated by user!"); //$NON-NLS-1$
			return true;
		}
		return false;
	}

}
