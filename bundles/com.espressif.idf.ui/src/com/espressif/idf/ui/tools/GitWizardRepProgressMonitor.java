/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import java.text.MessageFormat;
import java.util.Queue;

import org.eclipse.jgit.lib.BatchingProgressMonitor;

/**
 * Git cloning progress report class to add updated messages to the passed queue of messages which can be then consumed
 * by the initiator
 * 
 * @author Ali Azam Rana
 *
 */
public class GitWizardRepProgressMonitor extends BatchingProgressMonitor
{
	private Queue<String> logMessages;
	private boolean jobCancelled;

	public GitWizardRepProgressMonitor(Queue<String> logMessages)
	{
		this.logMessages = logMessages;
	}

	@Override
	protected void onUpdate(String taskName, int workCurr)
	{
		logMessages.add(MessageFormat.format("{0}  {1}", taskName, workCurr)); //$NON-NLS-1$
	}

	@Override
	protected void onEndTask(String taskName, int workCurr)
	{
		logMessages.add(MessageFormat.format("Finished {0}  {1}", taskName, workCurr)); //$NON-NLS-1$
	}

	@Override
	protected void onUpdate(String taskName, int workCurr, int workTotal, int percentDone)
	{
		logMessages.add(
				MessageFormat.format("{0} {1}, total {2} {3}% Completed", taskName, workCurr, workTotal, percentDone)); //$NON-NLS-1$
	}

	@Override
	protected void onEndTask(String taskName, int workCurr, int workTotal, int percentDone)
	{
		logMessages.add(MessageFormat.format("Finished {0} {1}, total {2} {3}% Completed", taskName, workCurr, //$NON-NLS-1$
				workTotal, percentDone));
	}

	@Override
	public boolean isCancelled()
	{
		if (jobCancelled)
		{
			logMessages.add("Cancelled by User"); //$NON-NLS-1$
		}
		return jobCancelled;
	}

	public void setJobCancelled(boolean jobCancelled)
	{
		this.jobCancelled = jobCancelled;
	}
}
