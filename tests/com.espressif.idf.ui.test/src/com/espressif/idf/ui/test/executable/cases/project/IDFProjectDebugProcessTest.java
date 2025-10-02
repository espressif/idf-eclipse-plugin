/*******************************************************************************
* Copyright 2025 Espressif Systems (Shanghai) PTE LTD.
* All rights reserved. Use is subject to license terms.
*******************************************************************************/

package com.espressif.idf.ui.test.executable.cases.project;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.widgetIsEnabled;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
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
	public void givenNewProjectCreatedWhenFlashedAndDebuggedThenDebuggingWorks() throws Exception
	{
		if (SystemUtils.IS_OS_LINUX) // Temporary solution until new ESP boards arrive for Windows
		{
			Fixture.createNewEspressifProject("EspressIf", "Espressif IDF Project", "NewProjecDebugTest");
			Fixture.buildAndFlashProject();
			Fixture.debugProject();
		}
		else
		{
			assertTrue(true);
		}
	}

	private static class Fixture
	{
		private static SWTWorkbenchBot bot;
		private static String projectName;

		private static void loadEnv() throws Exception
		{
			bot = WorkBenchSWTBot.getBot();
			EnvSetupOperations.setupEspressifEnv(bot);
			bot.sleep(1000);
			ProjectTestOperations.deleteAllProjects(bot);
		}

		private static void createNewEspressifProject(String category, String subCategory, String projectName)
		{
			Fixture.projectName = projectName;
			ProjectTestOperations.setupProject(projectName, category, subCategory, bot);
		}

		private static void buildAndFlashProject() throws Exception
		{
			whenTurnOffOpenSerialMonitorAfterFlashingInLaunchConfig();
			whenSelectLaunchTargetSerialPort();
			whenProjectIsBuiltUsingContextMenu();
			whenFlashProject();
			thenVerifyFlashDoneSuccessfully();
		}

		private static void debugProject() throws Exception
		{
			whenSelectDebugConfig();
			whenSelectLaunchTargetBoard();
			whenDebugProject();
			whenSwitchPerspective();
			thenOpenOCDexeIsPresent();
			thenGDBexeIsPresent();
			thenDebugStoppedUsingContextMenu();
		}

		private static void whenTurnOffOpenSerialMonitorAfterFlashingInLaunchConfig() throws Exception
		{
			modifyLaunchConfig("Open Serial Monitor After Flashing", false);
		}

		private static void modifyLaunchConfig(String checkboxLabel, boolean check) throws Exception
		{
			LaunchBarConfigSelector configSelector = new LaunchBarConfigSelector(bot);
			configSelector.clickEdit();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Edit Configuration", 20000);
			bot.cTabItem("Main").show();
			SWTBotCheckBox checkBox = bot.checkBox(checkboxLabel);
			if (checkBox.isChecked() != check)
			{
				checkBox.click();
			}
			bot.button("OK").click();
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

		private static void whenSelectDebugConfig() throws Exception
		{
			LaunchBarConfigSelector configSelector = new LaunchBarConfigSelector(bot);
			configSelector.select(projectName + " Debug");
		}

		private static void whenSelectLaunchTargetBoard() throws Exception
		{
			selectLaunchTarget("Board:", "ESP32-ETHERNET-KIT [usb://1-10]");
		}

		private static void whenSelectLaunchTargetSerialPort() throws Exception
		{
			selectLaunchTarget("Serial Port:", "/dev/ttyUSB1 Dual RS232-HS");
		}

		private static void selectLaunchTarget(String label, String target) throws Exception
		{
			LaunchBarTargetSelector targetSelector = new LaunchBarTargetSelector(bot);
			targetSelector.clickEdit();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "New ESP Target", 20000);
			SWTBotShell shell = bot.shell("New ESP Target");
			bot.comboBoxWithLabel(label).setSelection(target);
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);
			shell.setFocus();
			bot.button("Finish").click();
		}

		private static void whenSwitchPerspective() throws Exception
		{
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Confirm Perspective Switch", 20000);
			bot.button("Switch").click();
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

		private static void thenOpenOCDexeIsPresent() throws IOException
		{
			fetchProjectFromDebugView();
			assertTrue("OpenOCD.exe process was not found",
					bot.tree().getTreeItem(projectName + " Debug [ESP-IDF GDB OpenOCD Debugging]").getNodes()
							.contains("openocd"));
		}

		private static void thenGDBexeIsPresent() throws IOException
		{
			fetchProjectFromDebugView();
			assertTrue("riscv32-esp-elf-gdb.exe process was not found",
					bot.tree().getTreeItem(projectName + " Debug [ESP-IDF GDB OpenOCD Debugging]").getNodes()
							.contains("riscv32-esp-elf-gdb"));
		}

		private static void waitForDebugView()
		{
			long timeout = System.currentTimeMillis() + 10000;
			while (System.currentTimeMillis() < timeout)
			{
				try
				{
					bot.viewByTitle("Debug");
					bot.sleep(2000);
					return;
				}
				catch (Exception e)
				{
				}
				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException ex)
				{
					Thread.currentThread().interrupt();
				}
			}
			throw new RuntimeException("Debug view did not appear within the given time");
		}

		private static SWTBotTreeItem fetchProjectFromDebugView()
		{
			waitForDebugView();
			SWTBotView debugView = bot.viewByTitle("Debug");
			debugView.show();
			debugView.setFocus();
			SWTBotTreeItem[] items = debugView.bot().tree().getAllItems();
			Optional<SWTBotTreeItem> project = Arrays.asList(items).stream()
					.filter(i -> i.getText().equals(projectName + " Debug [ESP-IDF GDB OpenOCD Debugging]"))
					.findFirst();
			return project.orElse(null);
		}

		private static void launchCommandUsingDebugContextMenu(String projectName, SWTWorkbenchBot bot,
				String contextMenuLabel)
		{
			SWTBotTreeItem projectItem = fetchProjectFromDebugView();
			if (projectItem != null)
			{
				projectItem.select();
				projectItem.contextMenu(contextMenuLabel).click();
			}
		}

		private static void thenDebugStoppedUsingContextMenu() throws IOException
		{
			launchCommandUsingDebugContextMenu(projectName + " Debug [ESP-IDF GDB OpenOCD Debugging]", bot,
					"Terminate/Disconnect All");
			ProjectTestOperations.findInConsole(bot, "IDF Process Console", "dropped 'gdb'");
		}

		private static void cleanTestEnv()
		{
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}
	}
}
