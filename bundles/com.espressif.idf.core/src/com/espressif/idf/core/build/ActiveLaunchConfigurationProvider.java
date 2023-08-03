/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.ILaunchBarManager;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.logging.Logger;

/**
 * This provider allows you getting the active launch configuration from the ILaunchBarManager, even if it is not yet
 * initialized. In this case, we look for the initialization Job and join it.
 * 
 * @author Denys Almazov <denys.almazov@espressif.com>
 *
 */
public class ActiveLaunchConfigurationProvider
{
	private ILaunchBarManager launchBarManager;

	public ActiveLaunchConfigurationProvider(ILaunchBarManager launchBarManager)
	{
		this.launchBarManager = launchBarManager;
	}

	public ActiveLaunchConfigurationProvider()
	{
		this(IDFCorePlugin.getService(ILaunchBarManager.class));
	}

	public ILaunchConfiguration getActiveLaunchConfiguration() throws CoreException
	{
		ILaunchConfiguration configuration = launchBarManager.getActiveLaunchConfiguration();

		if (configuration == null)
		{
				Job[] jobs = Job.getJobManager().find(null);
				@SuppressWarnings("restriction")
				Optional<Job> launchBarInitJob = Stream.of(jobs)
						.filter(job -> job.getName()
								.equals(org.eclipse.launchbar.core.internal.Messages.LaunchBarManager_0))
						.findAny();
				launchBarInitJob.ifPresent(job -> {
					try
					{
						job.join();
					}
					catch (InterruptedException e)
					{
						Logger.log(e);
						Thread.currentThread().interrupt();
					}
				});
				configuration = launchBarManager.getActiveLaunchConfiguration();
		}
		return configuration;
	}
}
