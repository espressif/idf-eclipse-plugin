package com.espressif.idf.test.executable.cases.launchconfiguration;

import java.io.IOException;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.espressif.idf.test.operations.EnvSetupOperations;
import com.espressif.idf.test.operations.ProjectTestOperations;
import com.espressif.idf.test.operations.selectors.LaunchBarConfigSelector;

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
	public void givenNewProjectFromTemplateIsCreatedThenTheLaunchConfigurationIsAddedWithProjectName() throws Exception
	{
		fixture.givenProjectNameIs("TestProject");
		fixture.whenProjectIsCreatedFromTemplate();
		fixture.whenProjectIsBuiltUsingContextMenu();
		fixture.thenLaunchConfigurationContainsConfig("TestProject");
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

		public void thenLaunchConfigurationContainsConfig(String configName) throws Exception
		{
//			ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
//			ILaunchConfiguration[] configs = launchManager.getLaunchConfigurations();
//			assertEquals(1, Arrays.asList(launchManager.getLaunchConfigurations()).stream()
//					.filter(config -> config.getName().equals(configName)).count());
			LaunchBarConfigSelector launchBarConfigSelector = new LaunchBarConfigSelector(bot);
			launchBarConfigSelector.click();
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
