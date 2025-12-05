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
public class NewEspressifIDFProjectSBOMTest
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
	public void sbomToolShouldBeDisabledWhenProjectIsNotBuilt() throws Exception
	{
		Fixture.openCleanProjectSbomTool();
		Fixture.shouldDisableRunButton();
		Fixture.toggleRedirectOutput();
		Fixture.shouldDisableRunButton();
		Fixture.toggleRedirectOutput();
		Fixture.shouldDisableRunButton();
		Fixture.closeSbomTool();
	}

	@Test
	public void sbomShouldGenerateToConsoleWhenProjectIsBuilt() throws Exception
	{
		Fixture.openBuiltProjectSbomTool();
		Fixture.runSbomTool();
		Fixture.shouldSeeDataInConsole();
	}

	@Test
	public void sbomShouldRedirectToFileAndConsoleShouldConfirm() throws Exception
	{
		Fixture.openBuiltProjectSbomTool();
		Fixture.toggleRedirectOutput();
		Fixture.runSbomTool();
		Fixture.shouldSeeRedirectInConsole();
		Fixture.refreshProject();
		Fixture.openSbomFile();
		Fixture.shouldSeeSbomFileContains();
	}

	@Test
	public void emptyDescriptionPathShouldDisableRun() throws Exception
	{
		Fixture.openBuiltProjectSbomTool();
		Fixture.setProjectDescriptionPath("");
		Fixture.shouldDisableRunButton();
		Fixture.closeSbomTool();
	}

	@Test
	public void spaceInDescriptionPathShouldDisableRun() throws Exception
	{
		Fixture.openBuiltProjectSbomTool();
		Fixture.setProjectDescriptionPath(" ");
		Fixture.shouldDisableRunButton();
		Fixture.closeSbomTool();
	}

	@Test
	public void backslashInDescriptionPathShouldDisableRun() throws Exception
	{
		Fixture.openBuiltProjectSbomTool();
		Fixture.setProjectDescriptionPath("\\");
		Fixture.shouldDisableRunButton();
		Fixture.closeSbomTool();
	}

	@Test
	public void spaceInOutputFilePathShouldDisableRunOnNonLinux() throws Exception
	{
		if (SystemUtils.IS_OS_LINUX)
		{
			assertTrue(true);
			return;
		}
		Fixture.openBuiltProjectSbomTool();
		Fixture.toggleRedirectOutput();
		Fixture.setOutputFilePath(" ");
		Fixture.shouldDisableRunButton();
		Fixture.closeSbomTool();
	}

	private static class Fixture
	{

		private static SWTWorkbenchBot bot;

		private static final String SBOM_DIALOG = "Software Bill of Materials Tool";
		private static final String PROJECT_1 = "BuildProject";
		private static final String PROJECT_2 = "CleanProject";
		private static final String CONSOLE_OUTPUT = "The output was redirected to the file:";
		private static final String SBOM_DATA = "SPDXVersion";

		static void loadEnv() throws Exception
		{
			bot = WorkBenchSWTBot.getBot();
			EnvSetupOperations.setupEspressifEnv(bot);
			bot.sleep(1000);
		}

		static void cleanupEnvironment()
		{
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
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

		static void refreshProject() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(PROJECT_1, bot, "Refresh");
		}

		static void openBuiltProjectSbomTool() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(PROJECT_1, bot, "SBOM Tool");
		}

		static void openCleanProjectSbomTool() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(PROJECT_2, bot, "SBOM Tool");
		}

		static void closeSbomTool()
		{
			bot.button("Cancel").click();
		}

		static void toggleRedirectOutput()
		{
			bot.checkBox("Redirect output to the file").click();
		}

		static void runSbomTool() throws IOException
		{
			bot.button("OK").click();
			ProjectTestOperations
					.joinJobByName(com.espressif.idf.ui.update.Messages.SbomCommandDialog_EspIdfSbomJobName);
		}

		static void setProjectDescriptionPath(String value)
		{
			bot.shell(SBOM_DIALOG).bot().textWithLabel("Project Description Path:").setText(value);
		}

		static void setOutputFilePath(String value)
		{
			bot.shell(SBOM_DIALOG).bot().textWithLabel("Output File Path:").setText(value);
		}

		static void shouldDisableRunButton()
		{
			SWTBotButton ok = bot.button("OK");
			assertTrue("OK button should be disabled!", !ok.isEnabled());
		}

		static void shouldSeeRedirectInConsole() throws IOException
		{
			ProjectTestOperations.findInConsole(bot, "Espressif IDF Tools Console", CONSOLE_OUTPUT);
		}

		static void shouldSeeDataInConsole() throws IOException
		{
			ProjectTestOperations.findInConsole(bot, "Espressif IDF Tools Console", SBOM_DATA);
		}

		static void openSbomFile()
		{
			bot.tree().getTreeItem(PROJECT_1).getNode("sbom.txt").doubleClick();
		}

		static void shouldSeeSbomFileContains() throws IOException
		{
			assertTrue(ProjectTestOperations.checkTextEditorContentForPhrase(SBOM_DATA, bot));
		}
	}
}