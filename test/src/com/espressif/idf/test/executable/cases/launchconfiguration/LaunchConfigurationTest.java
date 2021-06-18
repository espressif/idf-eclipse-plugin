/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.test.executable.cases.launchconfiguration;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.internal.Activator;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.espressif.idf.test.operations.EnvSetupOperations;
import com.espressif.idf.test.operations.ProjectTestOperations;

/**
 * Test class to verify launch configurations and descriptors via cdt launchbar
 * 
 * @author Ali Azam Rana
 *
 */
@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
public class LaunchConfigurationTest
{
	private Fixture fixture;

	@Before
	public void beforeEachTest() throws Exception
	{
		fixture = new Fixture();
	}

	@After
	public void afterEachTest()
	{
		fixture.cleanTestEnv();
	}

	@Test
	public void givenNewProjectFromTemplateIsCreatedThenTheLaunchBarHasLaunchDescriptorAddedWithProjectName()
			throws Exception
	{
		fixture.givenProjectNameIs("TestProject");
		fixture.whenProjectIsCreatedFromTemplate();
		fixture.thenLaunchDescriptorContainsConfig("TestProject");
	}

	private class Fixture
	{
		private SWTWorkbenchBot bot;
		private String category = "EspressIf";
		private String subCategory = "Espressif IDF Project";
		private String projectTemplate = "blink";
		private String projectName;

		private Fixture() throws Exception
		{
			bot = new SWTWorkbenchBot();
			EnvSetupOperations.setupEspressifEnv(bot);
			bot.sleep(1000);
		}

		public void thenLaunchDescriptorContainsConfig(String configName) throws Exception
		{
			ILaunchBarManager manager = Activator.getService(ILaunchBarManager.class);
			ILaunchDescriptor[] launchDescriptors = manager.getLaunchDescriptors();
			assertEquals(1, Arrays.asList(launchDescriptors).stream()
					.filter(descriptor -> descriptor.getName().contains(configName)).count());
		}

		private void whenProjectIsCreatedFromTemplate()
		{
			ProjectTestOperations.setupProjectFromTemplate(projectName, category, subCategory, projectTemplate, bot);
		}

		public void whenProjectIsBuiltUsingContextMenu() throws IOException
		{
			ProjectTestOperations.buildProjectUsingContextMenu(projectName, bot);
			ProjectTestOperations.waitForProjectBuild(bot);
		}

		public void givenProjectNameIs(String projectName)
		{
			this.projectName = projectName;
		}

		private void cleanTestEnv()
		{
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}
	}
}
