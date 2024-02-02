/*******************************************************************************
 * Copyright 2024-2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.handlers;

import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.launchbar.ui.internal.commands.StopActiveCommandHandler;

@SuppressWarnings("restriction")
public class StopLaunchBuildHandler extends StopActiveCommandHandler
{

	@Override
	public void stop()
	{
		super.stop();
		this.stopLaunchBuild();
	}

	protected void stopLaunchBuild()
	{
		Job job = new Job(Messages.StopLaunchBuildHandler_0)
		{
			@Override
			protected IStatus run(IProgressMonitor progress)
			{

				final IJobManager jobManager = Job.getJobManager();
				Stream.of(jobManager.find(null)).filter(job -> job.getName().contains("Launching")).findFirst() //$NON-NLS-1$
						.ifPresent(job -> job.cancel());

				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
}
