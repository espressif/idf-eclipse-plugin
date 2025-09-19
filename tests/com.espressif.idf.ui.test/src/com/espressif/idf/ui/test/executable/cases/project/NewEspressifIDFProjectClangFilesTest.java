/*******************************************************************************
* Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
* Use is subject to license terms.
*******************************************************************************/
package com.espressif.idf.ui.test.executable.cases.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.ui.test.common.WorkBenchSWTBot;
import com.espressif.idf.ui.test.common.utility.TestWidgetWaitUtility;
import com.espressif.idf.ui.test.operations.EnvSetupOperations;
import com.espressif.idf.ui.test.operations.ProjectTestOperations;
import com.espressif.idf.ui.test.operations.selectors.LaunchBarTargetSelector;

/**
 * Test class to test Clangd / Clang-Format files functionality
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
	public void givenNewProjectIsCreatedThenTestClangFilesPresenceAndContentForNewProject() throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProjectClangFilesTest");
		Fixture.whenNewProjectIsSelected("NewProjectClangFilesTest");
		Fixture.thenClangdFileIsPresent();
		Fixture.whenClangdFileOpenedUsingDoubleClick();
		Fixture.thenClangdFileContentChecked();
		Fixture.thenClangdShellClosed();
		Fixture.thenClangFormatFileIsPresent();
		Fixture.whenClangFormatFileOpenedUsingDoubleClick();
		Fixture.thenClangFormatContentChecked();
		Fixture.thenClangFormatShellClosed();
	}

	@Test
	public void givenNewProjectIsCreatedWhenClangdFileIsDeletedThenTestClangdFileCreatedUsingContextMenuThenClangdFileContentChecked()
			throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProjectClangFilesTest2");
		Fixture.whenNewProjectIsSelected("NewProjectClangFilesTest2");
		Fixture.whenClangdFileDeleted();
		Fixture.thenClangdFileIsAbsent();
		Fixture.thenCreateClangdFileUsingContextMenu();
		Fixture.thenClangdFileIsPresent();
		Fixture.whenClangdFileOpenedUsingDoubleClick();
		Fixture.thenClangdFileContentCheckedAgain("NewProjectClangFilesTest2");
	}

	@Test
	public void givenNewProjectIsCreatedThenTestClangFormatFileSettingsAreBeingApplied() throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProjectClangFilesTest3");
		Fixture.setupAutoSave();
		Fixture.whenNewProjectIsSelected("NewProjectClangFilesTest3");
		Fixture.whenClangFormatFileOpenedUsingDoubleClick();
		Fixture.thenClangFormatContentEdited();
		Fixture.thenEditedClangFormatShellClosed();
		Fixture.whenMainFileIsOpened();
		Fixture.addSpaceToMainFile();
		Fixture.thenMainFileShellClosed();
		Fixture.whenMainFileIsOpened();
		Fixture.checkMainFileContentFormattedUnderActualSettings();
	}

	@Test
	public void givenNewProjectsAreCreatedAndBuiltWhenPreferencesOpenedThenClangdArgumentMatchesExpected()
			throws Exception
	{
		Fixture.givenNewProjectIsCreated("NewProjectClangFilesTest4");
		Fixture.whenProjectIsBuiltUsingContextMenu("NewProjectClangFilesTest4");
		Fixture.thenCheckClangdArgumentAfterProjectBuilt("NewProjectClangFilesTest4");
		Fixture.givenNewProjectIsCreated("NewProjectClangFilesTest5");
		Fixture.whenProjectIsBuiltUsingContextMenu("NewProjectClangFilesTest5");
		Fixture.thenCheckClangdArgumentAfterProjectBuilt("NewProjectClangFilesTest5");
	}

	@Test
	public void givenNewProjectIsCreatedWhenBuiltThenClangdDriversUpdateOnTargetWhenChangeTargetAndBuiltThenClangdDriversUpdateOnTargetChange()
			throws Exception
	{
		Fixture.givenNewProjectIsCreated("NewProjectClangFilesTest6");
		Fixture.whenProjectIsBuiltUsingContextMenu("NewProjectClangFilesTest6");
		Fixture.thenClangdDriversUpdateOnSelectedTarget();
		Fixture.whenChangeTarget();
		Fixture.whenProjectIsBuiltUsingContextMenu("NewProjectClangFilesTest6");
		Fixture.thenClangdDriversUpdateOnSelectedTarget();
	}

	private static class Fixture
	{
		private static SWTWorkbenchBot bot;
		private static String category;
		private static String subCategory;
		private static String projectName;
		private static LaunchBarTargetSelector launchBarTargetSelector;

		private static void loadEnv() throws Exception
		{
			bot = WorkBenchSWTBot.getBot();
			EnvSetupOperations.setupEspressifEnv(bot);
			bot.sleep(1000);
			ProjectTestOperations.deleteAllProjects(bot);
			try
			{
				launchBarTargetSelector = new LaunchBarTargetSelector(bot);
			}
			catch (WidgetNotFoundException e)
			{
				launchBarTargetSelector = new LaunchBarTargetSelector(bot, false);
			}
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

		private static void givenNewProjectIsCreated(String projectName) throws Exception
		{
			Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
			Fixture.givenProjectNameIs(projectName);
			Fixture.whenNewProjectIsSelected(projectName);
		}

		private static void thenCheckClangdArgumentAfterProjectBuilt(String projectName) throws Exception
		{
			Fixture.whenOpenClangdPreferences();
			Fixture.thenCompareActualClangdArgumentWithExpected(projectName);
			Fixture.closePreferencesDialog();
		}

		private static void whenNewProjectIsSelected(String projectName) throws Exception
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

		private static void thenClangdDriversUpdateOnSelectedTarget() throws Exception
		{
			whenOpenClangdPreferences();
			thenCompareActualClangdDriversWithExpected("NewProjectClangFilesTest6");
			closePreferencesDialog();
		}

		private static void whenChangeTarget() throws Exception
		{
			Fixture.thenLaunchTargetIsSelectedFromLaunchTargets("esp32s2");
			Fixture.closeTargetDialog();
		}

		private static void thenClangdFileIsPresent() throws IOException
		{
			assertTrue(bot.tree().getTreeItem(projectName).getNode(".clangd") != null);
		}

		private static void thenClangFormatFileIsPresent() throws IOException
		{
			assertTrue(bot.tree().getTreeItem(projectName).getNode(".clang-format") != null);
		}

		private static void whenClangdFileDeleted() throws IOException
		{
			bot.tree().getTreeItem(projectName).getNode(".clangd").select();
			bot.tree().getTreeItem(projectName).getNode(".clangd").contextMenu("Delete").click();
			bot.shell("Delete Resources").bot().button("OK").click();
			TestWidgetWaitUtility.waitWhileDialogIsVisible(bot, "Delete Resources", 10000);
		}

		private static void thenClangdFileIsAbsent() throws IOException
		{
			assertTrue(!bot.tree().getTreeItem(projectName).getNodes().contains(".clangd"));
		}

		private static void thenCreateClangdFileUsingContextMenu() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Create Clangd Config");
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Clangd Configuration", 5000);
			bot.shell("Clangd Configuration").bot().button("OK").click();
		}

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

		private static String getExpectedBuildFolderPATH(String projectName) throws IOException
		{
			try
			{
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				String buildFolder = IDFUtil.getBuildDir(project);
				Path buildFolderPath = Paths.get(buildFolder);
				return buildFolderPath.toAbsolutePath().toString();
			}
			catch (CoreException e)
			{
				throw new IOException("Failed to get build directory for project: " + projectName, e);
			}
		}

		private static void thenClangdFileContentChecked() throws Exception
		{
			bot.cTabItem(".clangd").activate();
			assertTrue(ProjectTestOperations.checkExactMatchInTextEditor(
					"CompileFlags:\n" + "  CompilationDatabase: build\n" + "  Remove: [-m*, -f*]", bot));
		}

		private static void thenClangdFileContentCheckedAgain(String projectName) throws Exception
		{
			String buildPath = getExpectedBuildFolderPATH(projectName);
			bot.cTabItem(".clangd").activate();
			assertTrue(ProjectTestOperations.checkExactMatchInTextEditor(
					"CompileFlags:\n" + "  CompilationDatabase: " + buildPath + "\n" + "  Remove: [-m*, -f*]", bot));
		}

		public static void thenLaunchTargetIsSelectedFromLaunchTargets(String launchTargetName)
		{
			launchBarTargetSelector.select(launchTargetName);
		}

		private static void thenClangFormatContentChecked() throws Exception
		{
			bot.cTabItem(".clang-format").activate();
			assertTrue(ProjectTestOperations.checkExactMatchInTextEditor(
					"""
							# We'll use defaults from the LLVM style, but with some modifications so that it's close to the CDT K&R style.
							BasedOnStyle: LLVM
							UseTab: Always
							IndentWidth: 4
							TabWidth: 4
							BreakConstructorInitializers: AfterColon
							IndentAccessModifiers: false
							AccessModifierOffset: -4
							""",
					bot));
		}

		private static void thenClangFormatContentEdited() throws Exception
		{
			SWTBotEditor textEditor = bot.activeEditor();
			textEditor.toTextEditor().setText(
					"""
							# We'll use defaults from the LLVM style, but with some modifications so that it's close to the CDT K&R style.
							BasedOnStyle: LLVM
							UseTab: Always
							IndentWidth: 0
							TabWidth: 0
							BreakConstructorInitializers: AfterColon
							IndentAccessModifiers: false
							AccessModifierOffset: -4
							""");
		}

		private static void addSpaceToMainFile() throws Exception
		{
			SWTBotEditor textEditor = bot.activeEditor();
			textEditor.toTextEditor().setText("""
					#include <stdbool.h>
					#include <stdio.h>
					#include <unistd.h>

					void app_main(void) {
							while (true) {
								printf("Hello from app_main!\\n");
								sleep(1   );
							}
					}
					""");
		}

		private static void checkMainFileContentFormattedUnderActualSettings() throws Exception
		{
			bot.sleep(1000);
			assertTrue(ProjectTestOperations.checkExactMatchInTextEditorwithWhiteSpaces("""
					#include <stdbool.h>
					#include <stdio.h>
					#include <unistd.h>

					void app_main(void) {
					while (true) {
					printf("Hello from app_main!\\n");
					sleep(1);
					}
					}
					""", bot));
		}

		private static void setupAutoSave() throws Exception
		{
			bot.menu("Window").menu("Preferences").click();
			SWTBotShell prefrencesShell = bot.shell("Preferences");
			prefrencesShell.bot().tree().getTreeItem("C/C++").select();
			prefrencesShell.bot().tree().getTreeItem("C/C++").expand();
			prefrencesShell.bot().tree().getTreeItem("C/C++").getNode("Editor (LSP)").select();
			prefrencesShell.bot().tree().getTreeItem("C/C++").getNode("Editor (LSP)").expand();
			prefrencesShell.bot().tree().getTreeItem("C/C++").getNode("Editor (LSP)").getNode("Save Actions").select();
			prefrencesShell.bot().checkBox("Format source code").click();
			prefrencesShell.bot().button("Apply and Close").click();
		}

		private static void whenMainFileIsOpened() throws Exception
		{
			ProjectTestOperations.openMainFileInTextEditorUsingContextMenu(projectName, bot);
		}

		private static void thenClangdShellClosed() throws IOException
		{
			bot.cTabItem(".clangd").close();
		}

		private static void thenClangFormatShellClosed() throws IOException
		{
			bot.cTabItem(".clang-format").close();
		}

		private static void thenEditedClangFormatShellClosed() throws IOException
		{
			bot.cTabItem("*.clang-format").close();
			bot.shell("Save Resource").bot().button("Save").click();
		}

		private static void thenMainFileShellClosed() throws IOException
		{
			bot.cTabItem("*main.c").close();
			bot.shell("Save Resource").bot().button("Save").click();
		}

		private static String getExpectedBuildFolderPATHforClangdAdditionalArgument(String projectName)
				throws IOException
		{
			try
			{
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				String buildFolder = IDFUtil.getBuildDir(project);
				Path buildFolderPath = Paths.get(buildFolder);
				return "--compile-commands-dir=" + buildFolderPath.toAbsolutePath().toString();
			}
			catch (CoreException e)
			{
				throw new IOException("Failed to get build directory for project: " + projectName, e);
			}
		}

		private static String getXtensaToolchainPathBasedOnTheTargetConfigured() throws IOException
		{
			String toolchain = IDFUtil.getToolchainExePathForActiveTarget();
			Path toolchainPath = Paths.get(toolchain);
			return toolchainPath.toAbsolutePath().toString();
		}

		private static void thenCompareActualClangdArgumentWithExpected(String projectName) throws IOException
		{
			SWTBotShell prefrencesShell = bot.shell("Preferences");
			String actualClangdPath = prefrencesShell.bot().textWithLabel("Additional").getText();
			String expectedClangdPath = getExpectedBuildFolderPATHforClangdAdditionalArgument(projectName);
			assertTrue(expectedClangdPath.equals(actualClangdPath));
		}

		private static void thenCompareActualClangdDriversWithExpected(String projectName) throws IOException
		{
			SWTBotShell prefrencesShell = bot.shell("Preferences");
			String actualDriversPath = prefrencesShell.bot().textWithLabel("Drivers").getText();
			String expectedDriversPath = "**";
			assertEquals(expectedDriversPath, actualDriversPath);
		}

		private static void whenProjectIsBuiltUsingContextMenu(String projectName) throws IOException
		{
			ProjectTestOperations.buildProjectUsingContextMenu(projectName, bot);
			ProjectTestOperations.waitForProjectBuild(bot);
		}

		private static void whenOpenClangdPreferences() throws Exception
		{
			bot.menu("Window").menu("Preferences").click();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Preferences", 10000);
			SWTBotShell prefrencesShell = bot.shell("Preferences");
			prefrencesShell.bot().tree().getTreeItem("C/C++").select();
			prefrencesShell.bot().tree().getTreeItem("C/C++").expand();
			prefrencesShell.bot().tree().getTreeItem("C/C++").getNode("Editor (LSP)").select();
			prefrencesShell.bot().tree().getTreeItem("C/C++").getNode("Editor (LSP)").expand();
			prefrencesShell.bot().tree().getTreeItem("C/C++").getNode("Editor (LSP)").getNode("clangd").select();
		}

		private static void closePreferencesDialog()
		{
			SWTBotShell preferencesShell = bot.shell("Preferences");
			preferencesShell.bot().button("Cancel").click(); // Or "Apply and Close"
			TestWidgetWaitUtility.waitWhileDialogIsVisible(bot, "Preferences", 10000);
		}

		private static void closeTargetDialog()
		{
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "IDF Launch Target Changed", 10000);
			SWTBotShell preferencesShell = bot.shell("IDF Launch Target Changed");
			preferencesShell.bot().button("Yes").click(); // Or "Apply and Close"
			TestWidgetWaitUtility.waitWhileDialogIsVisible(bot, "IDF Launch Target Changed", 10000);
		}

		private static void cleanTestEnv()
		{
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}
	}
}