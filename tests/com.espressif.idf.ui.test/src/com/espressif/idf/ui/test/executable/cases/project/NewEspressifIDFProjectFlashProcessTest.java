/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.executable.cases.project;

import java.io.IOException;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.*;
import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
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
 * Test class to test the Flash process
 * 
 * @author Andrii Filippov
 *
 */
@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class NewEspressifIDFProjectFlashProcessTest {
	@BeforeClass
	public static void beforeTestClass() throws Exception
	{
		Fixture.loadEnv();
	}

	@After
	public void afterEachTest()
	{
		try
		{
			Fixture.cleanTestEnv(); // Make sure test environment is always cleaned up
		}
		catch (Exception e)
		{
			System.err.println("Error during cleanup: " + e.getMessage());
		}
	}


	@Test
	public void givenNewProjectCreatedBuiltWhenSelectSerialPortWhenFlashThenCheckFlashedSuccessfully()
			throws Exception
	{
		if (SystemUtils.IS_OS_LINUX) //temporary solution until new ESP boards arrive for Windows
		{
			Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
			Fixture.givenProjectNameIs("NewProjectFlashTest");
			Fixture.whenNewProjectIsSelected();
			Fixture.whenTurnOffOpenSerialMonitorAfterFlashingInLaunchConfig();
			Fixture.whenProjectIsBuiltUsingContextMenu();
			Fixture.whenSelectLaunchTargetSerialPort();
			Fixture.whenFlashProject();
			Fixture.thenVerifyFlashDoneSuccessfully();
		}
		else
		{
			assertTrue(true);
		}
	}

	private static class Fixture
	{
		private static SWTWorkbenchBot bot;
		private static String category;
		private static String subCategory;
		private static String projectName;

		private static void loadEnv() throws Exception
		{
			bot = WorkBenchSWTBot.getBot();
			EnvSetupOperations.setupEspressifEnv(bot);
			bot.sleep(1000);
			ProjectTestOperations.deleteAllProjects(bot);
		}

		private static void givenNewEspressifIDFProjectIsSelected(String category, String subCategory)
		{
			Fixture.category = category;
			Fixture.subCategory = subCategory;
		}

		private static void givenProjectNameIs(String projectName)
		{
			Fixture.projectName = projectName;
		}

		private static void whenNewProjectIsSelected() throws Exception
		{
			ProjectTestOperations.setupProject(projectName, category, subCategory, bot);
		}

		private static void whenProjectIsBuiltUsingContextMenu() throws IOException
		{
			ProjectTestOperations.buildProjectUsingContextMenu(projectName, bot);
			ProjectTestOperations.waitForProjectBuild(bot);
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
		}

		private static void cleanTestEnv()
		{
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}

		private static void whenSelectLaunchTargetSerialPort() throws Exception
		{
			LaunchBarTargetSelector targetSelector = new LaunchBarTargetSelector(bot);
			targetSelector.clickEdit();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "New ESP Target", 20000);
			SWTBotShell shell = bot.shell("New ESP Target");
			bot.comboBoxWithLabel("Serial Port:").setSelection("/dev/ttyUSB1 Dual RS232-HS");
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);
			shell.setFocus();
			bot.button("Finish").click();
		}
		
		private static void whenTurnOffOpenSerialMonitorAfterFlashingInLaunchConfig() throws Exception
		{
			LaunchBarConfigSelector configSelector = new LaunchBarConfigSelector(bot);
			configSelector.clickEdit();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Edit Configuration", 20000);
			bot.cTabItem("Main").show();
			bot.cTabItem("Main").setFocus();
			SWTBotCheckBox checkBox = bot.checkBox("Open Serial Monitor After Flashing");
			if (checkBox.isChecked()) {
			checkBox.click();
			}
			bot.button("OK").click();
		}
		
		private static void whenFlashProject() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Run Configurations...");
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Run Configurations", 10000);
			bot.tree().getTreeItem("ESP-IDF Application").select();
			bot.tree().getTreeItem("ESP-IDF Application").expand();
			bot.tree().getTreeItem("ESP-IDF Application").getNode(projectName).select();
			bot.waitUntil(widgetIsEnabled(bot.button("Run")), 5000);
			bot.button("Run").click();
		}
		
		private static void thenVerifyFlashDoneSuccessfully() throws Exception
		{
			ProjectTestOperations.waitForProjectFlash(bot);
		}
	}
}
