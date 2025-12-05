/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.executable.cases.project;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
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

/**
 * Test class to test the SBOM feature
 * 
 * @author Andrii Filippov
 *
 */
@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NewEspressifIDFProjectPartitionTableEditorTest
{

	@BeforeClass
	public static void beforeTestClass() throws Exception
	{
		Fixture.loadEnv();
		Fixture.createAndBuildProject();
		Fixture.createCleanProject();
	}

	@AfterClass
	public static void tearDown()
	{
		Fixture.cleanupEnvironment();
	}

	@Test
	public void shouldShowInfoMessageWhenOpeningEditorOnUnbuiltProject() throws Exception
	{
		Fixture.fullClean();
		Fixture.openEditorExpectingInfo();
		Fixture.assertInfoPopupShown();
		Fixture.confirmOk();
	}

	@Test
	public void shouldDisplayBuiltInTableWhenProjectIsBuilt() throws Exception
	{
		Fixture.rebuild();
		Fixture.openCleanProjectEditor();
		Fixture.assertBuiltInTableVisible();
		Fixture.closeEditor();
	}

	@Test
	public void shouldAddRowToPartitionTable() throws Exception
	{
		Fixture.openBuiltProjectEditor();
		Fixture.addRow();
		Fixture.assertRowAdded();
		Fixture.closeEditor();
	}

	@Test
	public void shouldDeleteRowFromPartitionTable() throws Exception
	{
		Fixture.openBuiltProjectEditor();
		Fixture.deleteRow();
		Fixture.assertRowDeleted();
		Fixture.closeEditor();
	}

	@Test
	public void shouldPersistChangesAfterSaveAndReopen() throws Exception
	{
		Fixture.rebuild();
		Fixture.openCleanProjectEditor();
		Fixture.deleteRow();
		Fixture.saveAndQuit();

		Fixture.openCleanProjectEditor();
		Fixture.assertChangesPersisted();
		Fixture.closeEditor();

		Fixture.deletePartitionCsv();
	}

	private static class Fixture
	{

		private static SWTWorkbenchBot bot;

		private static final String PROJECT_1 = "BuildProject";
		private static final String PROJECT_2 = "CleanProject";

		static void loadEnv() throws Exception
		{
			bot = WorkBenchSWTBot.getBot();
			EnvSetupOperations.setupEspressifEnv(bot);
			bot.sleep(1000);
		}

		private static void createCleanProject() throws Exception
		{
			ProjectTestOperations.setupProject(PROJECT_2, "EspressIf", "Espressif IDF Project", bot);
		}

		private static void createAndBuildProject() throws Exception
		{
			ProjectTestOperations.setupProject(PROJECT_1, "EspressIf", "Espressif IDF Project", bot);
			ProjectTestOperations.buildProjectUsingContextMenu(PROJECT_1, bot);
			ProjectTestOperations.waitForProjectBuild(bot);
		}

		static void fullClean() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(PROJECT_2, bot, "Project Full Clean");
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);
		}

		static void rebuild() throws IOException
		{
			fullClean();
			ProjectTestOperations.buildProjectUsingContextMenu(PROJECT_2, bot);
			ProjectTestOperations.waitForProjectBuild(bot);
		}

		static void openCleanProjectEditor() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(PROJECT_2, bot, "Partition Table Editor");
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Partition Table Editor", 10000);
		}

		static void openBuiltProjectEditor() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(PROJECT_2, bot, "Partition Table Editor");
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Partition Table Editor", 10000);
		}

		static void openEditorExpectingInfo() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(PROJECT_2, bot, "Partition Table Editor");
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Information", 10000);
		}

		static void assertInfoPopupShown() throws IOException
		{
			assertTrue(ProjectTestOperations.checkShellContent(bot, "Information",
					"Failed to get partition CSV file name from sdkconfig. Make sure your project is compiled and has sdkconfig."));
		}

		static void assertBuiltInTableVisible() throws IOException
		{
			assertTrue(ProjectTestOperations.checkPartitionTableContent(bot));
		}

		static void addRow()
		{
			bot.toolbarButton("Add Row").click();
		}

		static void assertRowAdded() throws IOException
		{
			assertTrue(ProjectTestOperations.comparePartitionTableRows(bot, 1));
		}

		static void deleteRow() throws IOException
		{
			ProjectTestOperations.deletePartitionTableRow(bot);
		}

		static void assertRowDeleted() throws IOException
		{
			assertTrue(ProjectTestOperations.comparePartitionTableRows(bot, -1));
		}

		static void saveAndQuit()
		{
			bot.button("Save and Quit").click();
			bot.button("OK").click();
		}

		static void closeEditor()
		{
			bot.button("Cancel").click();
		}

		static void confirmOk()
		{
			bot.button("OK").click();
		}

		static void assertChangesPersisted() throws IOException
		{
			assertTrue(ProjectTestOperations.comparePartitionTableRows(bot, -1));
		}

		static void deletePartitionCsv()
		{
			SWTBotTreeItem csv = bot.tree().getTreeItem(PROJECT_2).getNode("partitions.csv");
			csv.select();
			csv.contextMenu("Delete").click();
			bot.shell("Delete Resources").bot().button("OK").click();
		}

		static void cleanupEnvironment()
		{
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}
	}
}