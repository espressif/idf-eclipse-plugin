/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.executable.cases.project;

import static org.junit.Assert.assertNotEquals;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.build.IDFBuildConfigurationProvider;
import com.espressif.idf.ui.test.common.WorkBenchSWTBot;
import com.espressif.idf.ui.test.common.utility.TestWidgetWaitUtility;
import com.espressif.idf.ui.test.operations.EnvSetupOperations;
import com.espressif.idf.ui.test.operations.ProjectTestOperations;

/**
 * Verifies the CDT Core Build configuration lifecycle of IDF projects.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class IDFProjectBuildConfigurationTest
{
	private static final String PROJECT_NAME = "RecreatedProject"; //$NON-NLS-1$
	private static final String CONFIG_NAME_PREFIX = IDFBuildConfigurationProvider.ID + "/idf."; //$NON-NLS-1$

	private static SWTWorkbenchBot bot;

	@BeforeClass
	public static void beforeTestClass() throws Exception
	{
		bot = WorkBenchSWTBot.getBot();
		EnvSetupOperations.setupEspressifEnv(bot);
	}

	@AfterClass
	public static void tearDown()
	{
		TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
		ProjectTestOperations.closeAllProjects(bot);
		ProjectTestOperations.deleteAllProjects(bot);
	}

	/**
	 * Verifies that CDT assigns mode-specific configurations and that recreating a project under a name the tracker
	 * handled before still produces a usable configuration.
	 */
	@Test
	public void givenProjectWhenModeChangesAndProjectIsRecreatedThenModeSpecificConfigurationIsAssigned()
	{
		Fixture.givenProjectIsCreated();
		String runModeConfig = Fixture.thenCoreBuildConfigurationIsAssigned(ILaunchManager.RUN_MODE);

		Fixture.whenLaunchModeIsSelected(ILaunchManager.DEBUG_MODE);
		String debugModeConfig = Fixture.thenCoreBuildConfigurationIsAssigned(ILaunchManager.DEBUG_MODE);
		assertNotEquals("Run and Debug must have separate CDT Core Build configurations", runModeConfig, //$NON-NLS-1$
				debugModeConfig);

		Fixture.whenProjectIsDeletedAndCreatedAgain();

		Fixture.thenCoreBuildConfigurationIsAssigned(ILaunchManager.DEBUG_MODE);
	}

	private static class Fixture
	{
		private static void givenProjectIsCreated()
		{
			ProjectTestOperations.setupProject(PROJECT_NAME, "EspressIf", "Espressif IDF Project", bot); //$NON-NLS-1$ //$NON-NLS-2$
		}

		private static void whenProjectIsDeletedAndCreatedAgain()
		{
			ProjectTestOperations.deleteAllProjects(bot);
			givenProjectIsCreated();
		}

		private static void whenLaunchModeIsSelected(String launchMode)
		{
			ILaunchBarManager launchBarManager = IDFCorePlugin.getService(ILaunchBarManager.class);
			try
			{
				launchBarManager
						.setActiveLaunchMode(DebugPlugin.getDefault().getLaunchManager().getLaunchMode(launchMode));
			}
			catch (Exception e)
			{
				throw new AssertionError("Unable to select the " + launchMode + " launch mode", e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		private static String thenCoreBuildConfigurationIsAssigned(String launchMode)
		{
			bot.waitUntil(new DefaultCondition()
			{
				@Override
				public boolean test() throws Exception
				{
					return activeConfigName().startsWith(CONFIG_NAME_PREFIX + launchMode + '.');
				}

				@Override
				public String getFailureMessage()
				{
					return "No " + launchMode + " IDF Core Build configuration was assigned to " + PROJECT_NAME; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}, 30000, 500);

			return activeConfigName();
		}

		private static String activeConfigName()
		{
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
			if (!project.isAccessible())
			{
				return IBuildConfiguration.DEFAULT_CONFIG_NAME;
			}

			try
			{
				return project.getActiveBuildConfig().getName();
			}
			catch (Exception e)
			{
				return IBuildConfiguration.DEFAULT_CONFIG_NAME;
			}
		}
	}
}
