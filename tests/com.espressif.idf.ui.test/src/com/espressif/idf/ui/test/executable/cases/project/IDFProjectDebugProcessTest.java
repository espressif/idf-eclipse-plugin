/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD.
 * All rights reserved. Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.test.executable.cases.project;

import java.io.IOException;

import org.apache.commons.lang3.SystemUtils;
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
import static org.eclipse.swtbot.swt.finder.waits.Conditions.*;
import static org.junit.Assert.assertTrue;

import com.espressif.idf.ui.test.common.utility.TestWidgetWaitUtility;
import com.espressif.idf.ui.test.operations.EnvSetupOperations;
import com.espressif.idf.ui.test.operations.ProjectTestOperations;
import com.espressif.idf.ui.test.operations.selectors.LaunchBarConfigSelector;
import com.espressif.idf.ui.test.operations.selectors.LaunchBarTargetSelector;

/**
 * Test class to test Debug Process
 * 
 * @author Andrii Filippov
 *
 */

@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IDFProjectDebugProcessTest
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
			Fixture.cleanTestEnv();
		}
		catch (Exception e)
		{
			System.err.println("Error during cleanup: " + e.getMessage());
		}
	}

	@Test
	public void givenNewProjectCreatedWhenSelectDebugWhenBuiltThenCheckDebugSuccessfully() throws Exception
	{
		if (SystemUtils.IS_OS_LINUX) //temporary solution until new ESP boards arrive for Windows
		{
			Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
			Fixture.givenProjectNameIs("NewProjecDebugTest");
			Fixture.whenNewProjectIsSelected();
			Fixture.whenSelectDebugConfig();
			Fixture.whenSelectLaunchTargetBoard();
			Fixture.whenProjectIsBuiltUsingContextMenu();
			Fixture.whenDebugProject();
			Fixture.thenVerifyJTAGflashDone();
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

		private static void whenDebugProject() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Debug Configurations...");
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Debug Configurations", 10000);
			bot.tree().getTreeItem("ESP-IDF GDB OpenOCD Debugging").select();
			bot.tree().getTreeItem("ESP-IDF GDB OpenOCD Debugging").expand();
			bot.tree().getTreeItem("ESP-IDF GDB OpenOCD Debugging").getNode(projectName + " Debug").select();
			bot.waitUntil(widgetIsEnabled(bot.button("Debug")), 5000);
			bot.button("Debug").click();
		}

		private static void whenSelectDebugConfig() throws Exception
		{
			LaunchBarConfigSelector configSelector = new LaunchBarConfigSelector(bot);
			configSelector.select(projectName + " Debug");
		}

		private static void whenSelectLaunchTargetBoard() throws Exception
		{
			LaunchBarTargetSelector targetSelector = new LaunchBarTargetSelector(bot);
			targetSelector.clickEdit();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "New ESP Target", 20000);
			SWTBotShell shell = bot.shell("New ESP Target");
			bot.comboBoxWithLabel("Board:").setSelection("ESP32-ETHERNET-KIT [usb://1-10]");
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);
			shell.setFocus();
			bot.button("Finish").click();
		}

		private static void thenVerifyJTAGflashDone() throws Exception
		{
			ProjectTestOperations.verifyTheConsoleOutput(bot, "** Flashing done for partition_table/partition-table.bin");
		}

		private static void cleanTestEnv()
		{
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}
	}
}
