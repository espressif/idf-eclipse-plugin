/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.executable.cases.project;
import java.io.IOException;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
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

/**
 * Test class to test the Serial Monitor feature
 * 
 * @author Andrii Filippov
 *
 */
@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NewEspressifIDFProjectSerialMonitorTest
{
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
	public void givenNewProjectCreatedBuiltWhenFlashProjectThenCheckSerialMonitorOutput()
			throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProjectSerialMonitorTest");
		Fixture.whenNewProjectIsSelected();
		Fixture.whenProjectIsBuiltUsingContextMenu();
		Fixture.whenFlashProject();
		Fixture.whenSelectSerialPort();
		Fixture.whenFlashProject();
		Fixture.openSerialMonitorandVerify();
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
		
		private static void whenFlashProject() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Run Configurations...");
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Run Configurations", 10000);
			SWTBotShell prefrencesShell = bot.shell("Run Configurations");
			prefrencesShell.bot().tree().getTreeItem("ESP-IDF Application").select();
			prefrencesShell.bot().tree().getTreeItem("ESP-IDF Application").expand();
			prefrencesShell.bot().tree().getTreeItem("ESP-IDF Application").getNode("NewProjectSerialMonitorTest").select();
			bot.sleep(1000);
			bot.button("Run").click();
		}
		
		private static void whenSelectSerialPort() throws IOException
		{
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Serial port not found", 30000);
			bot.button("OK").click();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "New ESP Target", 20000);
			bot.comboBoxWithLabel("Serial Port:").setSelection("COM5 USB Serial Port (COM5)");
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
			bot.button("Finish").click();
		}
		
		private static void openSerialMonitorandVerify() throws IOException {
			ProjectTestOperations.waitProjectFlash(bot);
			ProjectTestOperations.waitForProjectMonitorAndDisconnect(bot);
			ProjectTestOperations.findTextInSerialMonitorOutput(bot);
		}

		private static void cleanTestEnv()
		{
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}
	}
}
