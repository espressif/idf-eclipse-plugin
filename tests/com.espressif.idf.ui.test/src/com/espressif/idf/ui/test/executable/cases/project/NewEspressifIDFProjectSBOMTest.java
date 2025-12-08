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
	private static final String BUILT_PROJECT = "Project1";
	private static final String CLEAN_PROJECT = "Project2";
	private static final String SBOM_OUTPUT = "SPDXVersion";

	@BeforeClass
	public static void beforeTestClass() throws Exception
	{
		Fixture.loadEnv();
		Fixture.createBuiltProject(BUILT_PROJECT);
		Fixture.createCleanProject(CLEAN_PROJECT);
	}

	@AfterClass
	public static void tearDown()
	{
		Fixture.cleanupEnvironment();
	}

	@Test
	public void sbomToolShouldBeDisabledWhenProjectIsNotBuilt() throws Exception
	{
		Fixture.openSbomTool(CLEAN_PROJECT);
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
		Fixture.openSbomTool(BUILT_PROJECT);
		Fixture.runSbomTool();
		Fixture.shouldSeeInConsole(SBOM_OUTPUT);
	}

	@Test
	public void sbomShouldRedirectToFileAndConsoleShouldConfirm() throws Exception
	{
		Fixture.openSbomTool(BUILT_PROJECT);
		Fixture.toggleRedirectOutput();
		Fixture.runSbomTool();
		Fixture.shouldSeeInConsole("The output was redirected to the file:");
		Fixture.refreshProject(BUILT_PROJECT);
		Fixture.openSbomFile(BUILT_PROJECT);
		Fixture.shouldSeeSbomFileContains(SBOM_OUTPUT);
	}

	@Test
	public void emptyDescriptionPathShouldDisableRun() throws Exception
	{
		Fixture.openSbomTool(BUILT_PROJECT);
		Fixture.setProjectDescriptionPath("");
		Fixture.shouldDisableRunButton();
		Fixture.closeSbomTool();
	}

	@Test
	public void spaceInDescriptionPathShouldDisableRun() throws Exception
	{
		Fixture.openSbomTool(BUILT_PROJECT);
		Fixture.setProjectDescriptionPath(" ");
		Fixture.shouldDisableRunButton();
		Fixture.closeSbomTool();
	}

	@Test
	public void backslashInDescriptionPathShouldDisableRun() throws Exception
	{
		Fixture.openSbomTool(BUILT_PROJECT);
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
		Fixture.openSbomTool(BUILT_PROJECT);
		Fixture.toggleRedirectOutput();
		Fixture.setOutputFilePath(" ");
		Fixture.shouldDisableRunButton();
		Fixture.closeSbomTool();
	}

	private static class Fixture
	{
		private static SWTWorkbenchBot bot;
		private static final String SBOM_DIALOG = "Software Bill of Materials Tool";

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

		private static void createCleanProject(String projectName) throws Exception
		{
			ProjectTestOperations.setupProject(projectName, "EspressIf", "Espressif IDF Project", bot);
		}

		private static void createBuiltProject(String projectName) throws Exception
		{
			ProjectTestOperations.setupProject(projectName, "EspressIf", "Espressif IDF Project", bot);
			ProjectTestOperations.buildProjectUsingContextMenu(projectName, bot);
			ProjectTestOperations.waitForProjectBuild(bot);
		}

		static void refreshProject(String projectName) throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Refresh");
		}

		static void openSbomTool(String projectName) throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "SBOM Tool");
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

		static void shouldSeeInConsole(String text) throws IOException
		{
			ProjectTestOperations.findInConsole(bot, "Espressif IDF Tools Console", text);
		}

		static void openSbomFile(String projectName)
		{
			bot.tree().getTreeItem(projectName).getNode("sbom.txt").doubleClick();
		}

		static void shouldSeeSbomFileContains(String text) throws IOException
		{
			assertTrue(ProjectTestOperations.checkTextEditorContentForPhrase(text, bot));
		}
	}
}