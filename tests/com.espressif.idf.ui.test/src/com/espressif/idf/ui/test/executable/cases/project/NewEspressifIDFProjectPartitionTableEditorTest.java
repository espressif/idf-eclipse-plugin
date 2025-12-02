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
public class NewEspressifIDFProjectPartitionTableEditorTest {

	@BeforeClass
	public static void beforeTestClass() throws Exception {
		Fixture.loadEnv();
		Fixture.createAndBuildProject("Project1");
		Fixture.createProject("Project2"); // not built
	}

	@AfterClass
	public static void tearDown() {
		Fixture.cleanupEnvironment();
	}

	@Test
	public void shouldShowInfoMessageWhenOpeningEditorOnUnbuiltProject() throws Exception {
		Fixture.fullClean("Project2");
		Fixture.openEditorExpectingInfo("Project2");
		Fixture.assertInfoPopupShown();
		Fixture.confirmOk();
	}

	@Test
	public void shouldDisplayBuiltInTableWhenProjectIsBuilt() throws Exception {
		Fixture.rebuild("Project2");
		Fixture.openEditor("Project2");
		Fixture.assertBuiltInTableVisible();
		Fixture.closeEditor();
	}

	@Test
	public void shouldAddRowToPartitionTable() throws Exception {
		Fixture.openEditor("Project1");
		Fixture.addRow();
		Fixture.assertRowAdded();
		Fixture.closeEditor();
	}

	@Test
	public void shouldDeleteRowFromPartitionTable() throws Exception {
		Fixture.openEditor("Project1");
		Fixture.deleteRow();
		Fixture.assertRowDeleted();
		Fixture.closeEditor();
	}

	@Test
	public void shouldPersistChangesAfterSaveAndReopen() throws Exception {
		Fixture.rebuild("Project2");
		Fixture.openEditor("Project2");
		Fixture.deleteRow();
		Fixture.saveAndQuit();

		Fixture.openEditor("Project2");
		Fixture.assertChangesPersisted();
		Fixture.closeEditor();

		Fixture.deletePartitionCsv("Project2");
	}

	private static class Fixture {

		private static SWTWorkbenchBot bot;

		static void loadEnv() throws Exception {
			bot = WorkBenchSWTBot.getBot();
			EnvSetupOperations.setupEspressifEnv(bot);
			bot.sleep(1000);
		}

		private static void createProject(String projectName) throws Exception {
			ProjectTestOperations.setupProject(projectName, "EspressIf", "Espressif IDF Project", bot);
		}

		private static void createAndBuildProject(String projectName) throws Exception {
			createProject(projectName);
			ProjectTestOperations.buildProjectUsingContextMenu(projectName, bot);
			ProjectTestOperations.waitForProjectBuild(bot);
		}

		static void fullClean(String projectName) throws IOException {
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Project Full Clean");
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);
		}

		static void rebuild(String projectName) throws IOException {
			fullClean(projectName);
			ProjectTestOperations.buildProjectUsingContextMenu(projectName, bot);
			ProjectTestOperations.waitForProjectBuild(bot);
		}

		static void openEditor(String projectName) throws IOException {
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Partition Table Editor");
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Partition Table Editor", 10000);
		}

		static void openEditorExpectingInfo(String projectName) throws IOException {
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Partition Table Editor");
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Information", 10000);
		}

		static void assertInfoPopupShown() throws IOException {
			assertTrue(ProjectTestOperations.checkShellContent(bot, "Information",
					"Failed to get partition CSV file name from sdkconfig. Make sure your project is compiled and has sdkconfig."));
		}

		static void assertBuiltInTableVisible() throws IOException {
			assertTrue(ProjectTestOperations.checkPartitionTableContent(bot));
		}

		static void addRow() {
			bot.toolbarButton("Add Row").click();
		}

		static void assertRowAdded() throws IOException {
			assertTrue(ProjectTestOperations.comparePartitionTableRows(bot, 1));
		}

		static void deleteRow() throws IOException {
			ProjectTestOperations.deletePartitionTableRow(bot);
		}

		static void assertRowDeleted() throws IOException {
			assertTrue(ProjectTestOperations.comparePartitionTableRows(bot, -1));
		}

		static void saveAndQuit() {
			bot.button("Save and Quit").click();
			bot.button("OK").click();
		}

		static void closeEditor() {
			bot.button("Cancel").click();
		}

		static void confirmOk() {
			bot.button("OK").click();
		}

		static void assertChangesPersisted() throws IOException {
			assertTrue(ProjectTestOperations.comparePartitionTableRows(bot, -1));
		}

		static void deletePartitionCsv(String projectName) {
			SWTBotTreeItem csv = bot.tree().getTreeItem(projectName).getNode("partitions.csv");
			csv.select();
			csv.contextMenu("Delete").click();
			bot.shell("Delete Resources").bot().button("OK").click();
		}

		static void cleanupEnvironment() {
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}
	}
}