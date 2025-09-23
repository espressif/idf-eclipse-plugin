/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.executable.cases.project;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.widgetIsEnabled;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import com.espressif.idf.ui.test.common.WorkBenchSWTBot;
import com.espressif.idf.ui.test.common.utility.TestWidgetWaitUtility;
import com.espressif.idf.ui.test.operations.EnvSetupOperations;
import com.espressif.idf.ui.test.operations.ProjectTestOperations;
import com.espressif.idf.ui.test.operations.selectors.LaunchBarTargetSelector;

/**
 * Test class to test the Launch Target Editor
 * 
 * @author Andrii Filippov
 *
 */
@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class IDFProjectLaunchTargetEditorFunctionalityTest {
	@BeforeClass
	public static void beforeTestClass() throws Exception
	{
		Fixture.loadEnv();
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("LaunchTargetEditorTest");
		Fixture.whenNewProjectIsSelected();
	}

	@AfterClass
	public static void afterEachTest()
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
	public void givenANewProjectCreatedBuiltWhenSelectNewTargetWhenPopUpAppearsThenBuildFolderDeletedSuccessfully()
			throws Exception
	{	
		Fixture.whenProjectIsBuiltUsingContextMenu();
		Fixture.whenChangeLaunchTarget();
		Fixture.whenRefreshProject();
		Fixture.thenBuildFolderDeletedSuccessfully();
	}

	@Test
	public void givenBNewProjectCreatedWhenCreateNewLaunchTargetThenProjectBuiltSuccessfully()
			throws Exception
	{
		Fixture.whenCreateNewLaunchTarget();
		Fixture.whenProjectIsBuiltUsingContextMenu();
	}

	@Test
	public void givenCNewProjectCreatedWhenDeleteSelectedLaunchTargetThenDeletedSuccessfully()
			throws Exception
	{
		Fixture.whenDeleteSelectedLaunchTarget();
		Fixture.thenLaunchTargetDeletedSuccessfully();
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

		private static void whenChangeLaunchTarget() throws Exception
		{
			LaunchBarTargetSelector targetSelector = new LaunchBarTargetSelector(bot);
			targetSelector.selectTarget("esp32c2");
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "IDF Launch Target Changed", 20000);
			SWTBotShell shell = bot.shell("IDF Launch Target Changed");
			shell.setFocus();
			bot.button("Yes").click();
		}

		private static void whenSelectLaunchTarget() throws Exception
		{
			LaunchBarTargetSelector targetSelector = new LaunchBarTargetSelector(bot);
			targetSelector.selectTarget("target");
		}

		private static void whenDeleteLaunchTarget() throws Exception
		{
			bot.sleep(500);
			LaunchBarTargetSelector targetSelector = new LaunchBarTargetSelector(bot);
			targetSelector.clickEdit();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "New ESP Target", 20000);
			SWTBotShell shell = bot.shell("New ESP Target");
			shell.setFocus();
			bot.button("Delete").click();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "IDF Launch Target Changed", 5000);
			bot.button("Yes").click();
			bot.sleep(500);		
		}

		private static void whenDeleteSelectedLaunchTarget() throws Exception
		{
			whenSelectLaunchTarget();
			whenDeleteLaunchTarget();
		}

		private static void thenLaunchTargetDeletedSuccessfully() throws Exception
		{
			bot.sleep(500);
			LaunchBarTargetSelector targetSelector = new LaunchBarTargetSelector(bot);
			assertTrue("Launch Target was not deleted successfully!", !targetSelector.isTargetPresent("target"));
		}

		private static void selectNewLaunchTarget()
		{
			LaunchBarTargetSelector targetSelector = new LaunchBarTargetSelector(bot);
			targetSelector.select("New Launch Target...");
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "New Launch Target", 20000);
			assertTrue("'New Launch Target' dialog did not appear", bot.shell("New Launch Target").isActive());
		}

		private static void handleNewLaunchTargetDialog() throws Exception
		{
			SWTBotShell shell = bot.shell("New Launch Target");
			bot.table().select("ESP Target");
			shell.setFocus();
			bot.waitUntil(widgetIsEnabled(bot.button("Next >")), 5000);
			bot.button("Next >").click();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "New ESP Target",10000);
		}

		private static void handleNewEspTargetDialog() throws Exception
		{
			bot.textWithLabel("Name:").setText("target");
			bot.button("Finish").click();
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);
		}

		private static void whenCreateNewLaunchTarget() throws Exception
		{
			selectNewLaunchTarget();
			handleNewLaunchTargetDialog();
			handleNewEspTargetDialog();
		}

		private static void whenRefreshProject() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Refresh");
		}

		private static void thenBuildFolderDeletedSuccessfully() throws Exception
		{
			assertTrue("Build folder was not deleted successfully!", ProjectTestOperations.findProjectFullCleanedFilesInBuildFolder(projectName, bot));
		}

		private static void cleanTestEnv()
		{
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}
	}
}

