/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.test.executable.cases.launchconfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.internal.Activator;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.espressif.idf.ui.test.common.utility.TestWidgetWaitUtility;
import com.espressif.idf.ui.test.operations.EnvSetupOperations;
import com.espressif.idf.ui.test.operations.ProjectTestOperations;
import com.espressif.idf.ui.test.operations.selectors.LaunchBarConfigSelector;
import com.espressif.idf.ui.test.operations.selectors.LaunchBarTargetSelector;

/**
 * Test class to verify launch configurations and descriptors via cdt launchbar
 * It also verifies various launch target creation from the cdt launch bar
 * 
 * @author Ali Azam Rana
 *
 */
@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
public class LaunchBarCDTConfigurationsTest
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

	@Test
	public void creatingNewEspLaunchTarget()
	{
		fixture.givenNewLaunchTargetIsSelected();
		fixture.givenTargetTypeSelectedIs("ESP Target");
		fixture.givenNewLaunchTargetName("TestESP");
		fixture.givenNewLaunchTargetComPortIsSetTo("COMTEST");
		fixture.givenNewLaunchTargetIdfTargetSetTo("esp32s2");
		fixture.whenFinishIsClicked();
		fixture.thenLaunchTargetContains("TestESP");
		fixture.thenLaunchTargetIsSelectedFromLaunchTargets("TestESP");
	}

	private class Fixture
	{
		private SWTWorkbenchBot bot;
		private String category = "EspressIf";
		private String subCategory = "Espressif IDF Project";
		private String projectTemplate = "blink";
		private String projectName;
		private LaunchBarConfigSelector launchBarConfigSelector;
		private LaunchBarTargetSelector launchBarTargetSelector;
		private final ILaunchTargetManager targetManager = Activator.getService(ILaunchTargetManager.class);
		private final ILaunchBarManager manager = Activator.getService(ILaunchBarManager.class);

		private Fixture() throws Exception
		{
			bot = new SWTWorkbenchBot();
			EnvSetupOperations.setupEspressifEnv(bot);
			bot.sleep(1000);
			launchBarConfigSelector = new LaunchBarConfigSelector(bot);
			try
			{
				launchBarTargetSelector = new LaunchBarTargetSelector(bot);	
			}
			catch(WidgetNotFoundException e)
			{
				launchBarTargetSelector = new LaunchBarTargetSelector(bot, false);	
			}
			
		}

		private void givenNewLaunchTargetIdfTargetSetTo(String espIdfTarget)
		{
			bot.comboBoxWithLabel("IDF Target").setSelection(espIdfTarget);
		}

		private void givenNewLaunchTargetComPortIsSetTo(String comPort)
		{
			bot.comboBoxWithLabel("Serial Port:").setText(comPort);
		}

		public void thenLaunchTargetIsSelectedFromLaunchTargets(String launchTargetName)
		{
			launchBarTargetSelector.select(launchTargetName);
		}

		public void thenLaunchTargetContains(String launchTargetName)
		{
			ILaunchTarget[] launchTargets = targetManager.getLaunchTargets();
			assertTrue(Arrays.asList(launchTargets).stream()
					.filter(launchTarget -> launchTarget.getId().equals(launchTargetName)).findAny().isPresent());
		}

		public void whenFinishIsClicked()
		{
			bot.button("Finish").click();
		}

		public void givenNewLaunchTargetName(String launchTargetName)
		{
			bot.textWithLabel("Name:").setText(launchTargetName);
		}

		private void givenTargetTypeSelectedIs(String targetType)
		{
			bot.table().select(targetType);
			bot.button("Next >").click();
		}

		private void givenNewLaunchTargetIsSelected()
		{
			launchBarTargetSelector.select("New Launch Target...");
		}

		private void selectProjectFromLaunchBar(String projectName)
		{
			launchBarConfigSelector.select(projectName);
		}

		private void thenLaunchDescriptorContainsConfig(String configName) throws Exception
		{
			ILaunchDescriptor[] launchDescriptors = manager.getLaunchDescriptors();
			assertEquals(1, Arrays.asList(launchDescriptors).stream()
					.filter(descriptor -> descriptor.getName().equals(configName)).count());
		}

		private void whenProjectIsCreatedFromTemplate()
		{
			ProjectTestOperations.setupProjectFromTemplate(projectName, category, subCategory, projectTemplate, bot);
		}

		private void whenProjectIsBuiltUsingToolbarButton() throws IOException
		{
			bot.toolbarButtonWithTooltip("Build").click();
			ProjectTestOperations.waitForProjectBuild(bot);
			TestWidgetWaitUtility.waitForOperationsInProgressToFinish(bot);
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
			TestWidgetWaitUtility.waitForOperationsInProgressToFinish(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}
	}
}
