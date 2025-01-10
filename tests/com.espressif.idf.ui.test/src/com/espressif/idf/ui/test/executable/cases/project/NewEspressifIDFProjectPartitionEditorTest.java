/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.executable.cases.project;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
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

/**
 * Test class to test the SBOM feature
 * 
 * @author Andrii Filippov
 *
 */
@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NewEspressifIDFProjectPartitionEditorTest
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
	public void givenNewProjectCreatedBuiltWhenOpenPartitionTableEditorWhenDeleteSelectedRowThenCheckRowDeleted()
			throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProjectPartitionTableEditor1Test");
		Fixture.whenNewProjectIsSelected();
		Fixture.whenProjectIsBuiltUsingContextMenu();
		Fixture.whenOpenPartitionTableEditor();
		Fixture.whenDeleteRowFromPartitionTable();
		Fixture.ThenCheckRowDeleted();
	}

	@Test
	public void givenNewProjectCreatedBuiltWhenOpenPartitionTableEditorWhenDeleteSelectedRowWhenSaveAndQuitwhenReopenPartitionTableThenCheckChangesSaved()
			throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProjectPartitionTableEditor2Test");
		Fixture.whenNewProjectIsSelected();
		Fixture.whenProjectIsBuiltUsingContextMenu();
		Fixture.whenOpenPartitionTableEditor();
		Fixture.whenDeleteRowFromPartitionTable();
		Fixture.whenSaveAndQuit();
		Fixture.whenOpenPartitionTableEditor();
		Fixture.thenCheckChangesSaved();
	}

	@Test
	public void givenNewProjectCreatedBuiltWhenOpenPartitionTableEditorWhenDeleteSelectedRowWhenSaveAndCancelwhenReopenPartitionTableThenCheckChangesSaved()
			throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProjectPartitionTableEditor3Test");
		Fixture.whenNewProjectIsSelected();
		Fixture.whenProjectIsBuiltUsingContextMenu();
		Fixture.whenOpenPartitionTableEditor();
		Fixture.whenDeleteRowFromPartitionTable();
		Fixture.whenSavePartitionTable();
		Fixture.whenCancel();
		Fixture.whenOpenPartitionTableEditor();
		Fixture.thenCheckChangesSaved();
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

		private static void whenOpenPartitionTableEditor() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Partition Table Editor");
		}

		private static void whenDeleteRowFromPartitionTable() throws IOException
		{
			ProjectTestOperations.deletePartitionTableRow(bot);
		}

		private static void ThenCheckRowDeleted() throws IOException
		{
			assertTrue(ProjectTestOperations.comparePartitionTableRows(bot, -1));
		}

		private static void whenSaveAndQuit() throws IOException
		{
			bot.button("Save and Quit").click();
			bot.button("OK").click();
		}

		private static void whenSavePartitionTable() throws IOException
		{
			bot.toolbarButton("Save").click();
			bot.button("OK").click();
		}

		private static void whenCancel() throws IOException
		{
			bot.button("Cancel").click();
		}

		private static void thenCheckChangesSaved() throws IOException
		{
			assertTrue(ProjectTestOperations.comparePartitionTableRows(bot, -1));
		}

		private static void cleanTestEnv()
		{
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}
	}
}
