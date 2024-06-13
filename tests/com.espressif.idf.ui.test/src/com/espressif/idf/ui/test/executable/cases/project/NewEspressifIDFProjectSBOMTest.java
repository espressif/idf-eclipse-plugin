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
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
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
	public void givenNewProjectCreatedNotBuiltWhenOpenSbomThenSbomIsDisabled() throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProjectSbomFirstTest");
		Fixture.whenNewProjectIsSelected();
		Fixture.whenOpenSbomTool();
		Fixture.thenRunOKbuttonDisabled(Fixture.bot);
		Fixture.whenRedirectOutputToTheFileClicked();
		Fixture.thenRunOKbuttonDisabled(Fixture.bot);
		Fixture.whenRedirectOutputToTheFileClicked();
		Fixture.thenRunOKbuttonDisabled(Fixture.bot);
	}

	@Test
	public void givenNewProjectCreatedBuiltWhenRunSbomThenSbomIsGeneratedInConsole() throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProjectSbomSecondTest");
		Fixture.whenNewProjectIsSelected();
		Fixture.whenProjectIsBuiltUsingContextMenu();
		Fixture.whenOpenSbomTool();
		Fixture.whenRunSbomTool();
		Fixture.thenCheckResultInConsole();
	}

	@Test
	public void givenNewProjectCreatedBuiltWhenRunSBOMtoolRedirectOutputToFileThenCheckConsoleAndSbomFile()
			throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProjectSbomThirdTest");
		Fixture.whenNewProjectIsSelected();
		Fixture.whenProjectIsBuiltUsingContextMenu();
		Fixture.whenOpenSbomTool();
		Fixture.whenRedirectOutputToTheFileClicked();
		Fixture.whenRunSbomTool();
		Fixture.thenCheckInConsole();
		Fixture.whenRefreshProject();
		Fixture.thenOpenSbomFile();
		Fixture.thenCheckSbomFile();
	}

	@Test
	public void givenNewProjectCreatedBuiltWhenOpenSbomAndCleanProjectDescriptionPathThenCheckPathValidation()
			throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProjectSbomFourthTest");
		Fixture.whenNewProjectIsSelected();
		Fixture.whenProjectIsBuiltUsingContextMenu();
		Fixture.whenOpenSbomTool();
		Fixture.whenEmptyProjectDescriptionPath();
		Fixture.thenRunOKbuttonDisabled(Fixture.bot);
	}

	@Test
	public void givenNewProjectCreatedBuiltWhenOpenSbomAndAddSpaceToProjectDescriptionPathThenCheckPathValidation()
			throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProjectSbomFifthTest");
		Fixture.whenNewProjectIsSelected();
		Fixture.whenProjectIsBuiltUsingContextMenu();
		Fixture.whenOpenSbomTool();
		Fixture.whenAddSpaceToProjectDescriptionPath();
		Fixture.thenRunOKbuttonDisabled(Fixture.bot);
	}

	@Test
	public void givenNewProjectCreatedBuiltWhenOpenSbomAndAddBackSlashToProjectDescriptionPathThenCheckPathValidation()
			throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProjectSbomSixthTest");
		Fixture.whenNewProjectIsSelected();
		Fixture.whenProjectIsBuiltUsingContextMenu();
		Fixture.whenOpenSbomTool();
		Fixture.whenAddBackSlashToProjectDescriptionPath();
		Fixture.thenRunOKbuttonDisabled(Fixture.bot);
	}

	@Test
	public void givenNewProjectCreatedBuiltWhenOpenSbomAndAddSpaceToOutputFilePathThenCheckPathValidation()
			throws Exception
	{
		if (!SystemUtils.IS_OS_LINUX)
		{
			Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
			Fixture.givenProjectNameIs("NewProjectSbomSeventhTest");
			Fixture.whenNewProjectIsSelected();
			Fixture.whenProjectIsBuiltUsingContextMenu();
			Fixture.whenOpenSbomTool();
			Fixture.whenRedirectOutputToTheFileClicked();
			Fixture.whenAddSpaceToOutputFilePath(Fixture.bot);
			Fixture.thenRunOKbuttonDisabled(Fixture.bot);
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

		private static void whenOpenSbomTool() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "SBOM Tool");
		}

		private static void thenRunOKbuttonDisabled(SWTWorkbenchBot bot) throws IOException
		{
			SWTBotButton okButton = bot.button("OK");
			assertTrue(!okButton.isEnabled());
		}

		private static void whenRedirectOutputToTheFileClicked() throws IOException
		{
			bot.checkBox("Redirect output to the file").click();
		}

		private static void whenRunSbomTool() throws IOException
		{
			bot.button("OK").click();
			ProjectTestOperations
					.joinJobByName(com.espressif.idf.ui.update.Messages.SbomCommandDialog_EspIdfSbomJobName);
		}

		private static void thenCheckResultInConsole() throws IOException
		{
			ProjectTestOperations.findInConsole(bot, "Espressif IDF Tools Console", "SPDXVersion");
		}

		private static void thenCheckInConsole() throws IOException
		{
			ProjectTestOperations.findInConsole(bot, "Espressif IDF Tools Console",
					"The output was redirected to the file:");
		}

		private static void whenRefreshProject() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Refresh");
		}

		private static void thenOpenSbomFile() throws IOException
		{
			bot.tree().getTreeItem(projectName).getNode("sbom.txt").select();
			bot.tree().getTreeItem(projectName).getNode("sbom.txt").doubleClick();
		}

		private static void thenCheckSbomFile() throws IOException
		{
			assertTrue(ProjectTestOperations.checkTextEditorContentForPhrase("SPDXVersion", bot));
		}

		private static void whenAddSpaceToOutputFilePath(SWTWorkbenchBot bot) throws IOException
		{
			bot.shell("Software Bill of Materials Tool").bot().textWithLabel("Output File Path:").setText(" ");
		}

		private static void whenEmptyProjectDescriptionPath() throws IOException
		{
			bot.shell("Software Bill of Materials Tool").bot().textWithLabel("Project Description Path:").setText("");
		}

		private static void whenAddSpaceToProjectDescriptionPath() throws IOException
		{
			bot.shell("Software Bill of Materials Tool").bot().textWithLabel("Project Description Path:").setText(" ");
		}

		private static void whenAddBackSlashToProjectDescriptionPath() throws IOException
		{
			bot.shell("Software Bill of Materials Tool").bot().textWithLabel("Project Description Path:").setText("\\");
		}
	}
}