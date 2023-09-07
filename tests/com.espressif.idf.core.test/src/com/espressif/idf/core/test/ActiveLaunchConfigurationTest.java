/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import com.espressif.idf.core.build.ActiveLaunchConfigurationProvider;
import com.espressif.idf.core.logging.Logger;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ActiveLaunchConfigurationTest
{

	private static final String EXPECTED_LAUNCH_CONFIG_NAME = "expected_launch_config_name";

	@Test
	void get_active_launch_configuration_returns_expected_config_when_init_launchbar_job_is_active()
			throws CoreException
	{
		ILaunchBarManager launchBarManager = mock(ILaunchBarManager.class);
		ILaunchConfiguration launchConfiguration = mock(ILaunchConfiguration.class);

		when(launchConfiguration.getName()).thenReturn(EXPECTED_LAUNCH_CONFIG_NAME);
		when(launchBarManager.getActiveLaunchConfiguration()).thenReturn(null);
		ActiveLaunchConfigurationProvider provider = new ActiveLaunchConfigurationProvider(launchBarManager);
		runInitLaunchBarJob(launchBarManager, launchConfiguration);
		ILaunchConfiguration configuration = provider.getActiveLaunchConfiguration();
		
		assertEquals(EXPECTED_LAUNCH_CONFIG_NAME, configuration.getName());
	}

	@Test
	void get_active_launch_configuration_returns_expected_config_when_init_launchbar_job_is_not_active()
			throws CoreException
	{
		ILaunchBarManager launchBarManager = mock(ILaunchBarManager.class);
		ILaunchConfiguration launchConfiguration = mock(ILaunchConfiguration.class);

		when(launchConfiguration.getName()).thenReturn(EXPECTED_LAUNCH_CONFIG_NAME);
		when(launchBarManager.getActiveLaunchConfiguration()).thenReturn(launchConfiguration);
		ActiveLaunchConfigurationProvider provider = new ActiveLaunchConfigurationProvider(launchBarManager);
		ILaunchConfiguration configuration = provider.getActiveLaunchConfiguration();

		assertEquals(EXPECTED_LAUNCH_CONFIG_NAME, configuration.getName());
	}

	private void runInitLaunchBarJob(ILaunchBarManager launchBarManager, ILaunchConfiguration launchConfiguration)
	{
		@SuppressWarnings("restriction")
		Job job = new Job(org.eclipse.launchbar.core.internal.Messages.LaunchBarManager_0)
		{

			protected IStatus run(IProgressMonitor monitor)
			{
				try
				{
					when(launchBarManager.getActiveLaunchConfiguration()).thenReturn(launchConfiguration);
				}
				catch (CoreException e)
				{
					Logger.log(e);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

}
