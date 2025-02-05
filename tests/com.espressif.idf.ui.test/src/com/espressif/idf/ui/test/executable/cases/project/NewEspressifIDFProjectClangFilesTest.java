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
 * Test class to test the project creation, build and basic operations
 * 
 * @author Andrii Filippov
 *
 */
@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NewEspressifIDFProjectClangFilesTest
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
	public void givenNewProjectIsCreatedThenTestFullSDKconfigWorkflowForNewProject() throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProjectClangFilesTest");
		Fixture.whenNewProjectIsSelected();
		Fixture.thenClangdFileIsPresent();
		Fixture.whenClangdFileOpenedUsingDoubleClick();
		Fixture.thenClangdFileContentChecked();
		Fixture.thenClangdShellClosed();
		Fixture.thenClangFormatFileIsPresent();
		Fixture.whenClangFormatFileOpenedUsingDoubleClick();
		Fixture.thenClangFormatContentChecked();
		Fixture.thenClangFormatShellClosed();
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
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);
			SWTBotView projectExplorView = bot.viewByTitle("Project Explorer");
			projectExplorView.show();
			projectExplorView.setFocus();
			bot.tree().getTreeItem(projectName).select();
			bot.tree().getTreeItem(projectName).expand();
			bot.sleep(1000);
		}

		private static void thenClangdFileIsPresent() throws IOException
		{
			assertTrue(bot.tree().getTreeItem(projectName).getNode(".clangd") != null);
		}

		private static void thenClangFormatFileIsPresent() throws IOException
		{
			assertTrue(bot.tree().getTreeItem(projectName).getNode(".clang-format") != null);
		}

//		private static void thenClangdFileIsAbsent() throws IOException
//		{
//			assertTrue(!bot.tree().getTreeItem(projectName).getNodes().contains(".clangd"));
//		}

		private static void whenClangdFileOpenedUsingDoubleClick() throws IOException
		{
			bot.tree().getTreeItem(projectName).getNode(".clangd").doubleClick();
			TestWidgetWaitUtility.waitForCTabToAppear(bot, ".clangd", 5000);
		}

		private static void whenClangFormatFileOpenedUsingDoubleClick() throws IOException
		{
			bot.tree().getTreeItem(projectName).getNode(".clang-format").doubleClick();
			TestWidgetWaitUtility.waitForCTabToAppear(bot, ".clang-format", 5000);
		}

		private static void thenClangdFileContentChecked() throws Exception
		{
			bot.cTabItem(".clangd").activate();
			assertTrue(ProjectTestOperations.checkExactMatchInTextEditor(
					"CompileFlags:\n" + "  CompilationDatabase: build\n" + "  Remove: [-m*, -f*]", bot));
		}

		private static void thenClangFormatContentChecked() throws Exception
		{
			bot.cTabItem(".clang-format").activate();
			assertTrue(ProjectTestOperations.checkExactMatchInTextEditor(
					"# We'll use defaults from the LLVM style, but with some modifications so that it's close to the CDT K&R style.\n"
							+ "BasedOnStyle: LLVM\n" + "UseTab: Always\n" + "IndentWidth: 4\n" + "TabWidth: 4\n"
							+ "PackConstructorInitializers: NextLineOnly\n"
							+ "BreakConstructorInitializers: AfterColon\n" + "IndentAccessModifiers: false\n"
							+ "AccessModifierOffset: -4",
					bot));
		}

//		private static void thenClangFormatFileContentEdited() throws Exception
//		{
//			bot.cTabItem(".clang-format").activate();
//
//		}

		private static void thenClangdShellClosed() throws IOException
		{
			bot.cTabItem(".clangd").close();
		}

		private static void thenClangFormatShellClosed() throws IOException
		{
			bot.cTabItem(".clang-format").close();
		}

//		private static void whenClangdFileIsSaved() throws IOException
//		{
//			bot.cTabItem("*.clangd").activate();
//			bot.cTabItem("*.clangd").close();
//			bot.sleep(1000);
//			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Save Resource", 5000);
//			bot.shell("Save Resource").bot().button("Save").click();
//		}
//
//		private static void whenClangFormatFileIsSaved() throws IOException
//		{
//			bot.cTabItem("*.clangd").activate();
//			bot.cTabItem("*.clangd").close();
//			bot.sleep(1000);
//			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Save Resource", 5000);
//			bot.shell("Save Resource").bot().button("Save").click();
//		}

//		private static void thenCheckChangesAreSaved() throws Exception
//		{
//			bot.cTabItem(".clangd").activate();
//
//		}

		private static void cleanTestEnv()
		{
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}
	}
}