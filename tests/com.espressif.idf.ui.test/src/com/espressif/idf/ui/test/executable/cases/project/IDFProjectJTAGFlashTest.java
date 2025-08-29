/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD.
 * All rights reserved. Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.test.executable.cases.project;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.ui.test.common.WorkBenchSWTBot;
import com.espressif.idf.ui.test.common.utility.TestWidgetWaitUtility;
import com.espressif.idf.ui.test.operations.EnvSetupOperations;
import com.espressif.idf.ui.test.operations.ProjectTestOperations;
import com.espressif.idf.ui.test.operations.selectors.LaunchBarConfigSelector;
import com.espressif.idf.ui.test.operations.selectors.LaunchBarTargetSelector;

/**
 * Test class to test JTAG Flash Process
 * 
 * @author Andrii Filippov
 *
 */
@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IDFProjectJTAGFlashTest 
{
	@BeforeClass
	public static void beforeTestClass() throws Exception
	{
		Fixture.loadEnv();
	}

	@AfterClass
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
	public void givenNewProjectCreated() throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProject");
		Fixture.whenNewProjectIsSelected();
		Fixture.whenSelectJTAGFlashInLaunchConfig();
		Fixture.whenSelectLaunchTargetBoard();
//		Fixture.whenProjectIsBuiltUsingContextMenu();
	}

	private static class Fixture
	{
		private static SWTWorkbenchBot bot;
		private static String category;
		private static String subCategory;
		private static String projectName;
		private static LaunchBarConfigSelector launchBarConfigSelector;
		private static LaunchBarTargetSelector launchBarTargetSelector;

		private static void loadEnv() throws Exception
		{
			bot = WorkBenchSWTBot.getBot();
			EnvSetupOperations.setupEspressifEnv(bot);
			bot.sleep(1000);
			ProjectTestOperations.deleteAllProjects(bot);
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

		private static void whenSelectJTAGFlashInLaunchConfig() throws Exception {
			LaunchBarConfigSelector configSelector = new LaunchBarConfigSelector(bot);
			configSelector.clickEdit();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Edit Configuration", 20000);
			bot.cTabItem("Main").show();
			bot.cTabItem("Main").setFocus();
			bot.comboBoxWithLabel("Flash over:").setSelection("JTAG");
			bot.sleep(5000);
			bot.button("OK").click();
		}

		private static void whenSelectLaunchTargetBoard() throws Exception {
			LaunchBarTargetSelector targetSelector = new LaunchBarTargetSelector(bot);
			targetSelector.clickEdit();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "New ESP Target", 20000);
			bot.comboBoxWithLabel("Board:").setSelection("ESP32-P4 chip (via builtin USB-JTAG) [usb://2-5]");
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
			bot.button("Finish").click();
		}
		
		private static void cleanTestEnv()
		{
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}
	}
}
