/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.test.executable.cases.launchconfiguration;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.internal.Activator;
import org.eclipse.launchbar.ui.controls.internal.LaunchBarWidgetIds;
import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.espressif.idf.test.operations.EnvSetupOperations;
import com.espressif.idf.test.operations.ProjectTestOperations;
import com.espressif.idf.test.operations.selectors.LaunchBarConfigSelector;

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
		fixture.selectProjectFromLaunchBar("TestProject");
	}

	@Test
	public void givenTwoProjectsAreCreatedAndBuiltUsingToolbarButtonBySelectingOlderProjectFirstFromTheLaunchbarThenOlderProjectIsBuiltFirst()
			throws Exception
	{
		fixture.givenProjectNameIs("TestProject");
		fixture.whenProjectIsCreatedFromTemplate();
		fixture.givenProjectNameIs("TestProject2");
		fixture.whenProjectIsCreatedFromTemplate();
		fixture.thenLaunchDescriptorContainsConfig("TestProject");
		fixture.thenLaunchDescriptorContainsConfig("TestProject2");
		fixture.selectProjectFromLaunchBar("TestProject");
		fixture.whenProjectIsBuiltUsingToolbarButton();
		fixture.thenConsoleShowsBuildSuccessful();
		fixture.thenConsoleShowsProjectNameInConsole("TestProject");
		fixture.selectProjectFromLaunchBar("TestProject2");
		fixture.whenProjectIsBuiltUsingToolbarButton();
		fixture.thenConsoleShowsBuildSuccessful();
		fixture.thenConsoleShowsProjectNameInConsole("TestProject2");
	}

	private class Fixture
	{
		private SWTWorkbenchBot bot;
		private String category = "EspressIf";
		private String subCategory = "Espressif IDF Project";
		private String projectTemplate = "blink";
		private String projectName;
		private LaunchBarConfigSelector launchBarConfigSelector;

		private Fixture() throws Exception
		{
			bot = new SWTWorkbenchBot();
			EnvSetupOperations.setupEspressifEnv(bot);
			bot.sleep(1000);
			launchBarConfigSelector = new LaunchBarConfigSelector(bot);
		}

		public void selectProjectFromLaunchBar(String projectName)
		{
			launchBarConfigSelector.select(projectName);
		}

		public void thenLaunchDescriptorContainsConfig(String configName) throws Exception
		{
			ILaunchBarManager manager = Activator.getService(ILaunchBarManager.class);
			ILaunchDescriptor[] launchDescriptors = manager.getLaunchDescriptors();
			assertEquals(1, Arrays.asList(launchDescriptors).stream()
					.filter(descriptor -> descriptor.getName().equals(configName)).count());
		}

		private void whenProjectIsCreatedFromTemplate()
		{
			ProjectTestOperations.setupProjectFromTemplate(projectName, category, subCategory, projectTemplate, bot);
		}

		private void whenProjectIsBuiltUsingContextMenu() throws IOException
		{
			ProjectTestOperations.buildProjectUsingContextMenu(projectName, bot);
			ProjectTestOperations.waitForProjectBuild(bot);
		}

		private void whenProjectIsBuiltUsingToolbarButton() throws IOException
		{
			bot.toolbarButtonWithTooltip("Build").click();
			ProjectTestOperations.waitForProjectBuild(bot);
		}

		public void givenProjectNameIs(String projectName)
		{
			this.projectName = projectName;
		}

		private void thenConsoleShowsBuildSuccessful()
		{
			SWTBotView consoleView = bot.viewById("org.eclipse.ui.console.ConsoleView");
			consoleView.show();
			consoleView.setFocus();
			String consoleTextString = consoleView.bot().styledText().getText();
			assertTrue(consoleTextString.contains("Build complete (0 errors"));
		}

		private void thenConsoleShowsProjectNameInConsole(String projectName)
		{
			SWTBotView consoleView = bot.viewById("org.eclipse.ui.console.ConsoleView");
			consoleView.show();
			consoleView.setFocus();
			String consoleTextString = consoleView.bot().styledText().getText();
			assertTrue(consoleTextString.contains("Build complete (0 errors"));
			assertTrue(consoleTextString.contains(projectName.concat("/build")));
		}

		private void cleanTestEnv()
		{
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}
	}
}
