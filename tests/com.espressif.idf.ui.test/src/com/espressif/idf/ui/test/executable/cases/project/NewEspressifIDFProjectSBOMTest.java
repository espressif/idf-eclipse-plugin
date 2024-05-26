/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.executable.cases.project;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.lang3.SystemUtils;
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
public class NewEspressifIDFProjectSBOMTest
{
	@BeforeClass
	public static void beforeTestClass() throws Exception
	{
		Fixture.loadEnv();
	}

	@After
	public void afterEachTest()
	{
		Fixture.cleanTestEnv();
	}

	@Test
	public void givenNewProjectCreatedRunSBOMtoolUsingContextMenu() throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewSbomProjectFirstTest");
		Fixture.whenNewProjectIsSelected();
		Fixture.runSBOMtoolBeforeProjectBuild();
	}

	@Test
	public void givenNewProjectCreatedBuilProjectThenRunSBOMtoolUsingContextMenu() throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewSbomProjectSecondTest");
		Fixture.whenNewProjectIsSelected();
		Fixture.whenProjectIsBuiltUsingContextMenu();
		Fixture.runSBOMtoolUsingContextMenu();
	}

	@Test
	public void runSBOMtoolRedirectOutputToFile() throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewSbomProjectThirdTest");
		Fixture.whenNewProjectIsSelected();
		Fixture.whenProjectIsBuiltUsingContextMenu();
		Fixture.runSBOMtoolUsingContextMenuRedirectOutputToFile();
	}

	@Test
	public void givenNewProjectCreatedBuilProjectThenRunSBOMtoolUsingContextMenuCheckingPath() throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewSbomProjectFourthTest");
		Fixture.whenNewProjectIsSelected();
		Fixture.whenProjectIsBuiltUsingContextMenu();
		Fixture.runSBOMtoolCheckingPathValidation();
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

		private static void cleanTestEnv()
		{
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}

		private static void runSBOMtoolBeforeProjectBuild() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "ESP-IDF: SBOM Tool");
			assertTrue(ProjectTestOperations.checkButtonIsDisabled(bot, "OK"));
			bot.checkBox("Redirect output to the file").click();
			assertTrue(ProjectTestOperations.checkButtonIsDisabled(bot, "OK"));
			bot.checkBox("Redirect output to the file").click();
			assertTrue(ProjectTestOperations.checkButtonIsDisabled(bot, "OK"));
			bot.button("Cancel").click();
		}

		private static void runSBOMtoolUsingContextMenu() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "ESP-IDF: SBOM Tool");
			bot.button("OK").click();
			ProjectTestOperations
					.joinJobByName(com.espressif.idf.ui.update.Messages.SbomCommandDialog_EspIdfSbomJobName);
			ProjectTestOperations.findInConsole(bot, "Espressif IDF Tools Console", "SPDXVersion");
		}

		private static void runSBOMtoolUsingContextMenuRedirectOutputToFile() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "ESP-IDF: SBOM Tool");
			bot.checkBox("Redirect output to the file").click();
			bot.button("OK").click();
			ProjectTestOperations
					.joinJobByName(com.espressif.idf.ui.update.Messages.SbomCommandDialog_EspIdfSbomJobName);
			ProjectTestOperations.findInConsole(bot, "Espressif IDF Tools Console",
					"The output was redirected to the file:");
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Refresh");
			bot.tree().getTreeItem(projectName).getNode("sbom.txt").select();
			bot.tree().getTreeItem(projectName).getNode("sbom.txt").doubleClick();
			assertTrue(ProjectTestOperations.checkTextEditorContentForPhrase("SPDXVersion", bot));
		}

		private static void runSBOMtoolCheckingPathValidation() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "ESP-IDF: SBOM Tool");
			bot.checkBox("Redirect output to the file").click();
			if (!SystemUtils.IS_OS_LINUX)
			{
				ProjectTestOperations.setTextFieldInShell(bot, "Software Bill of Materials Tool", "Output File Path",
						" ");
				assertTrue(ProjectTestOperations.checkButtonIsDisabled(bot, "OK"));
			}
			ProjectTestOperations.setTextFieldInShell(bot, "Software Bill of Materials Tool", "Output File Path", "");
			ProjectTestOperations.setTextFieldInShell(bot, "Software Bill of Materials Tool",
					"Project Description Path", "");
			assertTrue(ProjectTestOperations.checkButtonIsDisabled(bot, "OK"));
			ProjectTestOperations.setTextFieldInShell(bot, "Software Bill of Materials Tool",
					"Project Description Path", " ");
			assertTrue(ProjectTestOperations.checkButtonIsDisabled(bot, "OK"));
			ProjectTestOperations.setTextFieldInShell(bot, "Software Bill of Materials Tool",
					"Project Description Path", "\\");
			assertTrue(ProjectTestOperations.checkButtonIsDisabled(bot, "OK"));
			bot.button("Cancel").click();
		}
	}
}