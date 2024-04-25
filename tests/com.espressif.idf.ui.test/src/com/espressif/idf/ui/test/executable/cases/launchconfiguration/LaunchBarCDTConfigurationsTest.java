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
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import com.espressif.idf.ui.test.common.WorkBenchSWTBot;
import com.espressif.idf.ui.test.common.utility.TestWidgetWaitUtility;
import com.espressif.idf.ui.test.operations.EnvSetupOperations;
import com.espressif.idf.ui.test.operations.ProjectTestOperations;
import com.espressif.idf.ui.test.operations.selectors.LaunchBarConfigSelector;
import com.espressif.idf.ui.test.operations.selectors.LaunchBarTargetSelector;

/**
 * Test class to verify launch configurations and descriptors via cdt launchbar It also verifies various launch target
 * creation from the cdt launch bar
 * 
 * @author Ali Azam Rana
 *
 */
@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LaunchBarCDTConfigurationsTest
{
	@BeforeClass
	public static void beforeEachTest() throws Exception
	{
		Fixture.loadEnv();
	}

	@After
	public void afterEachTest()
	{
		Fixture.cleanTestEnv();
	}

	@Test
	public void givenNewProjectFromTemplateIsCreatedThenTheLaunchBarHasLaunchDescriptorAddedWithProjectName()
			throws Exception
	{
		Fixture.givenProjectNameIs("TestProject");
		Fixture.whenProjectIsCreatedFromTemplate();
		Fixture.thenLaunchDescriptorContainsConfig("TestProject");
		Fixture.selectProjectFromLaunchBar("TestProject");
	}

	@Test
	public void creatingNewEspLaunchTarget()
	{
		Fixture.givenNewLaunchTargetIsSelected();
		Fixture.givenTargetTypeSelectedIs("ESP Target");
		Fixture.givenNewLaunchTargetName("TestESP");
		Fixture.givenNewLaunchTargetComPortIsSetTo("COMTEST");
		Fixture.givenNewLaunchTargetIdfTargetSetTo("esp32s2");
		Fixture.whenFinishIsClicked();
		Fixture.thenLaunchTargetContains("TestESP");
		Fixture.thenLaunchTargetIsSelectedFromLaunchTargets("TestESP");
	}

	private static class Fixture
	{
		private static SWTWorkbenchBot bot;
		private static String category = "EspressIf";
		private static String subCategory = "Espressif IDF Project";
		private static String projectTemplate = "blink";
		private static String projectName;
		private static LaunchBarConfigSelector launchBarConfigSelector;
		private static LaunchBarTargetSelector launchBarTargetSelector;
		private static final ILaunchTargetManager targetManager = Activator.getService(ILaunchTargetManager.class);
		private static final ILaunchBarManager manager = Activator.getService(ILaunchBarManager.class);

		private static void loadEnv() throws Exception
		{
			bot = WorkBenchSWTBot.getBot();
			EnvSetupOperations.setupEspressifEnv(bot);
			bot.sleep(1000);
			launchBarConfigSelector = new LaunchBarConfigSelector(bot);
			try
			{
				launchBarTargetSelector = new LaunchBarTargetSelector(bot);
			}
			catch (WidgetNotFoundException e)
			{
				launchBarTargetSelector = new LaunchBarTargetSelector(bot, false);
			}

		}

		private static void givenNewLaunchTargetIdfTargetSetTo(String espIdfTarget)
		{
			bot.comboBoxWithLabel("IDF Target").setSelection(espIdfTarget);
		}

		private static void givenNewLaunchTargetComPortIsSetTo(String comPort)
		{
			
			if (bot.comboBoxWithLabel("Serial Port:").itemCount() != 0)
			{
				bot.comboBoxWithLabel("Serial Port:").setSelection(0);
			}
		}

		private static void thenLaunchTargetIsSelectedFromLaunchTargets(String launchTargetName)
		{
			launchBarTargetSelector.selectTarget(launchTargetName);
		}

		private static void thenLaunchTargetContains(String launchTargetName)
		{
			ILaunchTarget[] launchTargets = targetManager.getLaunchTargets();
			assertTrue(Arrays.asList(launchTargets).stream()
					.filter(launchTarget -> launchTarget.getId().equals(launchTargetName)).findAny().isPresent());
		}

		private static void whenFinishIsClicked()
		{
			bot.button("Finish").click();
		}

		private static void givenNewLaunchTargetName(String launchTargetName)
		{
			bot.textWithLabel("Name:").setText(launchTargetName);
		}

		private static void givenTargetTypeSelectedIs(String targetType)
		{
			bot.table().select(targetType);
			bot.button("Next >").click();
		}

		private static void givenNewLaunchTargetIsSelected()
		{
			launchBarTargetSelector.select("New Launch Target...");
		}

		private static void selectProjectFromLaunchBar(String projectName)
		{
			launchBarConfigSelector.select(projectName);
		}

		private static void thenLaunchDescriptorContainsConfig(String configName) throws Exception
		{
			ILaunchDescriptor[] launchDescriptors = manager.getLaunchDescriptors();
			assertEquals(1, Arrays.asList(launchDescriptors).stream()
					.filter(descriptor -> descriptor.getName().equals(configName)).count());
		}

		private static void whenProjectIsCreatedFromTemplate()
		{
			ProjectTestOperations.setupProjectFromTemplate(projectName, category, subCategory, projectTemplate, bot);
		}

		private static void whenProjectIsBuiltUsingToolbarButton() throws IOException
		{
			bot.toolbarButtonWithTooltip("Build").click();
			ProjectTestOperations.waitForProjectBuild(bot);
//			TestWidgetWaitUtility.waitForOperationsInProgressToFinish(bot);
		}

		public static void givenProjectNameIs(String projectName)
		{
			Fixture.projectName = projectName;
		}

		private static void thenConsoleShowsBuildSuccessful()
		{
//			TestWidgetWaitUtility.waitForOperationsInProgressToFinish(bot);
			SWTBotView consoleView = ProjectTestOperations.viewConsole("CDT Build Console", bot);
			consoleView.show();
			consoleView.setFocus();
			String consoleTextString = consoleView.bot().styledText().getText();
			assertTrue(consoleTextString.contains("Build complete (0 errors"));
		}

		private static void thenConsoleShowsProjectNameInConsole(String projectName)
		{
			SWTBotView consoleView = bot.viewById("org.eclipse.ui.console.ConsoleView");
			consoleView.show();
			consoleView.setFocus();
			String consoleTextString = consoleView.bot().styledText().getText();
			assertTrue(consoleTextString.contains("Build complete (0 errors"));
			assertTrue(consoleTextString.contains(projectName.concat("/build")));
		}

		private static void cleanTestEnv()
		{
//			TestWidgetWaitUtility.waitForOperationsInProgressToFinish(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}
	}
}
