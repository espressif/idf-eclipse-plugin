/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import java.text.MessageFormat;
import java.util.Queue;

import org.eclipse.jgit.lib.BatchingProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;

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
	private ProgressBar progressBar;
	private Display display;

	public GitWizardRepProgressMonitor(Queue<String> logMessages, ProgressBar progressBar)
	{
		this.logMessages = logMessages;
		this.progressBar = progressBar;
		display = progressBar.getDisplay();
	}

	@Override
	protected void onUpdate(String taskName, int workCurr)
	{
		updateProgressBar(workCurr);
		setProgressBarVisibility(false);
		logMessages.add(MessageFormat.format("{0}  {1}", taskName, workCurr)); //$NON-NLS-1$
	}

	@Override
	protected void onEndTask(String taskName, int workCurr)
	{
		updateProgressBar(workCurr);
		logMessages.add(MessageFormat.format("Finished {0}  {1}", taskName, workCurr)); //$NON-NLS-1$
	}

	@Override
	protected void onUpdate(String taskName, int workCurr, int workTotal, int percentDone)
	{
		initializeMaxProgressbar(workTotal);
		updateProgressBar(workCurr);
		logMessages.add(
				MessageFormat.format("{0} {1}, total {2} {3}% Completed", taskName, workCurr, workTotal, percentDone)); //$NON-NLS-1$
	}

	@Override
	protected void onEndTask(String taskName, int workCurr, int workTotal, int percentDone)
	{
		initializeMaxProgressbar(workTotal);
		updateProgressBar(workCurr);
		setProgressBarVisibility(false);
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
	
	private void initializeMaxProgressbar(int max)
	{
		display.asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				progressBar.setVisible(true);
				progressBar.setMaximum(max);
			}
		});
	}

	private void updateProgressBar(int updateValue)
	{
		display.asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				progressBar.setSelection(updateValue);
			}
		});
	}
	
	private void setProgressBarVisibility(boolean visible)
	{
		display.asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				progressBar.setVisible(visible);
			}
		});
	}		
	
}
