/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.install;

import java.text.MessageFormat;

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

	@Override
    protected void onUpdate(final String taskName, final int workCurr) {
		console.println(MessageFormat.format("{0}  {1}", taskName, workCurr)); //$NON-NLS-1$
    }

    @Override
    protected void onUpdate(final String taskName, final int workCurr, final int workTotal, final int percentDone) {
    	console.println(MessageFormat.format("{0} {1}, total {2} {3}% Completed", taskName, workCurr, workTotal, percentDone)); //$NON-NLS-1$
    }

    @Override
    protected void onEndTask(final String taskName, final int workCurr) {
    	console.println(MessageFormat.format("Finished {0}  {1}", taskName, workCurr)); //$NON-NLS-1$
    }

    @Override
    protected void onEndTask(final String taskName, final int workCurr, final int workTotal, final int percentDone) {
    	console.println(MessageFormat.format("Finished {0} {1}, total {2} {3}% Completed", taskName, workCurr, workTotal, percentDone)); //$NON-NLS-1$
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
