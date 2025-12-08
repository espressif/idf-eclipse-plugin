/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.executable.cases.project;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
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
 * Test class to test the project creation, build and basic operations
 * 
 * @author Andrii Filippov
 *
 */
@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NewEspressifIDFProjectSDKconfigTest
{
	@BeforeClass
	public static void beforeTestClass() throws Exception
	{
		Fixture.loadEnv();
	}

	@AfterClass
	public static void tearDown()
	{
		Fixture.cleanupEnvironment();
	}

	@Test
	public void givenNewProjectThenSDKconfigFileFunctionalTested() throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProjectSDKconfigTest");
		Fixture.whenNewProjectIsSelected();
		Fixture.whenProjectIsBuiltUsingContextMenu();
		Fixture.refreshProjectInProjectExplorer();
		Fixture.thenSDKconfigFileIsPresent();
		Fixture.whenSDKconfigFileOpenedEditedSaved();
		Fixture.whenProjectIsBuiltUsingContextMenu();
		Fixture.whenSDKconfigFileOpenedUsingContextMenu();
		Fixture.thenCheckChangesAreSaved();
		Fixture.thenSDKconfigShellClosed();
		Fixture.whenSDKconfigFileDeletedWhenBuildProjectThenSDKconfigFileGeneratedAndVerified();
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

		public static void whenSDKconfigFileOpenedViaContextMenuthenVerifiedthenClosed() throws Exception
		{
			whenSDKconfigFileOpenedUsingContextMenu();
			thenSDKconfigFileContentChecked();
			thenSDKconfigShellClosed();
		}

		private static void whenSDKconfigFileOpenedEditedSaved() throws Exception
		{
			whenSDKconfigFileOpenedUsingContextMenu();
			thenSDKconfigFileContentEdited();
			whenSDKconfigFileIsSaved();
		}

		private static void whenSDKconfigFileDeletedWhenBuildProjectThenSDKconfigFileGeneratedAndVerified()
				throws Exception
		{
			bot.tree().getTreeItem(projectName).getNode("sdkconfig").select();
			bot.tree().getTreeItem(projectName).getNode("sdkconfig").contextMenu("Delete").click();
			bot.shell("Delete Resources").bot().button("OK").click();
			bot.sleep(1000);
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Refresh");
			thenSDKconfigFileIsAbsent();
			whenProjectIsBuiltUsingContextMenu();
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Refresh");
			thenSDKconfigFileIsPresent();
			whenSDKconfigFileOpenedViaContextMenuthenVerifiedthenClosed();
		}

		private static void refreshProjectInProjectExplorer() throws Exception
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Refresh");
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
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);
			SWTBotView projectExplorView = bot.viewByTitle("Project Explorer");
			projectExplorView.show();
			projectExplorView.setFocus();
			bot.tree().getTreeItem(projectName).select();
			bot.tree().getTreeItem(projectName).expand();
			bot.sleep(1000);
		}

		private static void whenProjectIsBuiltUsingContextMenu() throws IOException
		{
			ProjectTestOperations.buildProjectUsingContextMenu(projectName, bot);
			ProjectTestOperations.waitForProjectBuild(bot);
		}

		private static void thenSDKconfigFileIsPresent() throws IOException
		{
			assertTrue("SDKconfig file was not found",
					bot.tree().getTreeItem(projectName).getNode("sdkconfig") != null);
		}

		private static void thenSDKconfigFileIsAbsent() throws IOException
		{
			assertTrue("SDKconfig file is still present",
					!bot.tree().getTreeItem(projectName).getNodes().contains("sdkconfig"));
		}

		private static void whenSDKconfigFileOpenedUsingContextMenu() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Menu Config");
			TestWidgetWaitUtility.waitWhileDialogIsVisible(bot, "Progress Information", 40000);
			TestWidgetWaitUtility.waitForCTabToAppear(bot, "SDK Configuration (sdkconfig)", 10000);
		}

		private static void thenSDKconfigFileContentChecked() throws Exception
		{
			bot.cTabItem("SDK Configuration (sdkconfig)").activate();
			TestWidgetWaitUtility.waitForTreeItem("Partition Table", bot.tree(1), bot);
			bot.tree(1).getTreeItem("Partition Table").click();
			bot.sleep(1000);
			assertTrue("'Offset of partition table (hex)' does not match '0x8000'",
					bot.textWithLabel("Offset of partition table (hex)").getText().matches("0x8000"));
		}

		private static void thenSDKconfigFileContentEdited() throws Exception
		{
			bot.cTabItem("SDK Configuration (sdkconfig)").activate();
			TestWidgetWaitUtility.waitForTreeItem("Partition Table", bot.tree(1), bot);
			bot.tree(1).getTreeItem("Partition Table").click();
			bot.sleep(1000);
			bot.textWithLabel("Offset of partition table (hex)").setText("0x4000");
			bot.sleep(1000);
			bot.comboBoxWithLabel("Partition Table").setSelection("Custom partition table CSV");
			bot.sleep(1000);
			bot.checkBox("Generate an MD5 checksum for the partition table").click();
		}

		private static void thenSDKconfigShellClosed() throws IOException
		{
			bot.cTabItem("SDK Configuration (sdkconfig)").close();
		}

		private static void whenSDKconfigFileIsSaved() throws IOException
		{
			bot.cTabItem("*SDK Configuration (sdkconfig)").activate();
			bot.cTabItem("*SDK Configuration (sdkconfig)").close();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Save Resource", 10000);
			bot.shell("Save Resource").bot().button("Save").click();
		}

		private static void thenCheckChangesAreSaved() throws Exception
		{
			bot.cTabItem("SDK Configuration (sdkconfig)").activate();
			TestWidgetWaitUtility.waitForTreeItem("Partition Table", bot.tree(1), bot);
			bot.tree(1).getTreeItem("Partition Table").click();
			bot.sleep(1000);
			assertTrue("'Offset of partition table (hex)' does not match '0x4000'",
					bot.textWithLabel("Offset of partition table (hex)").getText().matches("0x4000"));
			bot.sleep(1000);
			assertTrue("'Partition Table' does not match 'Custom partition table CSV'",
					bot.comboBoxWithLabel("Partition Table").selection().equals("Custom partition table CSV"));
			bot.sleep(1000);
			assertTrue("'Generate an MD5 checksum for the partition table' checkbox is still checked!",
					!bot.checkBox("Generate an MD5 checksum for the partition table").isChecked());
		}

		static void cleanupEnvironment()
		{
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}
	}
}