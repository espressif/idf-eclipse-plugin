/*******************************************************************************
* Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
* Use is subject to license terms.
*******************************************************************************/
package com.espressif.idf.ui.test.executable.cases.project;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import com.espressif.idf.core.ILSPConstants;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.ui.test.common.WorkBenchSWTBot;
import com.espressif.idf.ui.test.common.utility.TestWidgetWaitUtility;
import com.espressif.idf.ui.test.common.utility.WaitUtils;
import com.espressif.idf.ui.test.operations.EnvSetupOperations;
import com.espressif.idf.ui.test.operations.ProjectTestOperations;
import com.espressif.idf.ui.test.operations.selectors.LaunchBarConfigSelector;

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
	private static final String CLEAN_PROJECT1 = "Project1";
	private static final String CLEAN_PROJECT2 = "Project2";

	@BeforeClass
	public static void beforeTestClass() throws Exception
	{
		Fixture.loadEnv();
		Fixture.createProject(CLEAN_PROJECT1);
		Fixture.createProject(CLEAN_PROJECT2);
	}

	@After
	public void afterEachTest()
	{
		Fixture.resetWorkbenchState();
	}

	@AfterClass
	public static void tearDown()
	{
		Fixture.cleanupEnvironment();
	}

	@Test
	public void shouldHaveClangFilesPresentAndContentCorrectForNewProject() throws Exception
	{
		Fixture.thenClangdFileIsPresent(CLEAN_PROJECT1);
		Fixture.whenClangdFileOpenedUsingDoubleClick(CLEAN_PROJECT1);
		Fixture.thenClangdFileContentChecked(CLEAN_PROJECT1);
		Fixture.thenClangdFileClosed(CLEAN_PROJECT1);
		Fixture.thenClangFormatFileIsPresent(CLEAN_PROJECT1);
		Fixture.whenClangFormatFileOpenedUsingDoubleClick(CLEAN_PROJECT1);
		Fixture.thenClangFormatContentChecked(CLEAN_PROJECT1);
		Fixture.thenClangFormatFileClosed(CLEAN_PROJECT1);
	}

	@Test
	public void shouldRecreateClangdFileAfterDeletionAndVerifyContent() throws Exception
	{
		Fixture.whenClangdFileDeleted(CLEAN_PROJECT1);
		Fixture.thenClangdFileIsAbsent(CLEAN_PROJECT1);
		Fixture.thenCreateClangdFileUsingContextMenu(CLEAN_PROJECT1);
		Fixture.thenClangdFileIsPresent(CLEAN_PROJECT1);
		Fixture.whenClangdFileOpenedUsingDoubleClick(CLEAN_PROJECT1);
		Fixture.thenClangdFileContentChecked(CLEAN_PROJECT1);
		Fixture.thenClangdFileClosed(CLEAN_PROJECT1);
	}

	@Test
	public void shouldApplyClangFormatSettingsWhenAutoSaveIsEnabled() throws Exception
	{
		Fixture.setupAutoSave();
		Fixture.whenClangFormatFileOpenedUsingDoubleClick(CLEAN_PROJECT2);
		Fixture.thenClangFormatContentEdited(CLEAN_PROJECT2);
		Fixture.thenClangFormatFileSavedAndClosed(CLEAN_PROJECT2);
		Fixture.whenMainFileIsOpened(CLEAN_PROJECT2);
		Fixture.addSpaceToMainFile(CLEAN_PROJECT2);
		Fixture.thenMainFileSavedAndClosed(CLEAN_PROJECT2);
		Fixture.whenMainFileIsOpened(CLEAN_PROJECT2);
		Fixture.checkMainFileContentFormattedUnderActualSettings(CLEAN_PROJECT2);
	}

	@Test
	public void shouldMatchExpectedClangdArgumentsAfterBuildingProjects() throws Exception
	{
		Fixture.whenProjectIsBuiltUsingContextMenu(CLEAN_PROJECT2);
		Fixture.thenCheckClangdArgumentAfterProjectBuilt(CLEAN_PROJECT2);
		Fixture.whenSelectProjectInLaunchConfig();
		Fixture.whenProjectIsBuiltUsingContextMenu(CLEAN_PROJECT1);
		Fixture.thenCheckClangdArgumentAfterProjectBuilt(CLEAN_PROJECT1);
		Fixture.thenClangdDriversUpdateOnSelectedTarget();
	}

	private static class Fixture
	{
		private static final String MAIN_FILE_PATH = "main/main.c";
		private static final long DIALOG_TIMEOUT = 10000;
		private static final long EDITOR_TIMEOUT = 15000;
		private static final long RESOURCE_TIMEOUT = 15000;

		private static SWTWorkbenchBot bot;

		static void loadEnv() throws Exception
		{
			bot = WorkBenchSWTBot.getBot();
			EnvSetupOperations.setupEspressifEnv(bot);
			bot.sleep(1000);
		}

		private static String normalizeText(String text)
		{
			// Standardize all line endings to \n and trim outer whitespace
			return text.replace("\r\n", "\n").replace("\r", "\n").trim();
		}

		private static void assertTextEqualsNormalized(String message, String expected, String actual)
		{
			assertEquals(message, normalizeText(expected), normalizeText(actual));
		}

		private static void thenClangdDriversUpdateOnSelectedTarget() throws Exception
		{
			whenOpenClangdPreferences();
			try
			{
				thenCompareActualClangdDriversWithExpected();
			}
			finally
			{
				closePreferencesDialog();
			}
		}

		private static void createProject(String projectName) throws Exception
		{
			ProjectTestOperations.setupProject(projectName, "EspressIf", "Espressif IDF Project", bot);
			Fixture.whenNewProjectIsSelected(projectName);
		}

		private static void thenCheckClangdArgumentAfterProjectBuilt(String projectName) throws Exception
		{
			Fixture.whenOpenClangdPreferences();
			try
			{
				Fixture.thenCompareActualClangdArgumentWithExpected(projectName);
			}
			finally
			{
				Fixture.closePreferencesDialog();
			}
		}

		private static void whenNewProjectIsSelected(String projectName) throws Exception
		{
			projectTreeItem(projectName).select();
			bot.sleep(1000);
		}

		/**
		 * The tree of the Project Explorer is looked up through the view, so that a dialog left open by another test
		 * can never be mistaken for the Project Explorer.
		 */
		private static SWTBotTree projectExplorerTree()
		{
			SWTBotView projectExplorerView = bot.viewByTitle("Project Explorer");
			projectExplorerView.show();
			projectExplorerView.setFocus();
			return projectExplorerView.bot().tree();
		}

		private static SWTBotTreeItem projectTreeItem(String projectName)
		{
			SWTBotTreeItem projectItem = projectExplorerTree().getTreeItem(projectName);
			projectItem.expand();
			return projectItem;
		}

		private static boolean waitForProjectNode(String projectName, String nodeName, boolean shouldBePresent)
		{
			try
			{
				bot.waitUntil(new DefaultCondition()
				{
					@Override
					public boolean test() throws Exception
					{
						return projectTreeItem(projectName).getNodes().contains(nodeName) == shouldBePresent;
					}

					@Override
					public String getFailureMessage()
					{
						return MessageFormat.format("The {0} file of the project {1} is still {2}", nodeName,
								projectName, shouldBePresent ? "missing" : "present");
					}
				}, RESOURCE_TIMEOUT);
			}
			catch (TimeoutException timeoutException)
			{
				// the assertion of the caller reports the actual state of the tree
			}

			return projectTreeItem(projectName).getNodes().contains(nodeName);
		}

		private static void thenClangdFileIsPresent(String projectName) throws IOException
		{
			boolean isPresent = waitForProjectNode(projectName, ILSPConstants.CLANGD_CONFIG_FILE, true);
			assertEquals("The .clangd file should be present", true, isPresent);
		}

		private static void thenClangFormatFileIsPresent(String projectName) throws IOException
		{
			boolean isPresent = waitForProjectNode(projectName, ILSPConstants.CLANG_FORMAT_FILE, true);
			assertEquals("The .clang-format file should be present", true, isPresent);
		}

		private static void whenClangdFileDeleted(String projectName) throws IOException
		{
			SWTBotTreeItem clangdItem = projectTreeItem(projectName).getNode(ILSPConstants.CLANGD_CONFIG_FILE);
			clangdItem.select();
			clangdItem.contextMenu("Delete").click();
			bot.shell("Delete Resources").bot().button("OK").click();
			TestWidgetWaitUtility.waitWhileDialogIsVisible(bot, "Delete Resources", DIALOG_TIMEOUT);
			WaitUtils.waitForJobs();
		}

		private static void thenClangdFileIsAbsent(String projectName) throws IOException
		{
			boolean isPresent = waitForProjectNode(projectName, ILSPConstants.CLANGD_CONFIG_FILE, false);
			assertEquals("The .clangd file should be absent", false, isPresent);
		}

		private static void thenCreateClangdFileUsingContextMenu(String projectName) throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Create Clangd Config");
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Clangd Configuration", DIALOG_TIMEOUT);
			bot.shell("Clangd Configuration").bot().button("OK").click();
			TestWidgetWaitUtility.waitWhileDialogIsVisible(bot, "Clangd Configuration", DIALOG_TIMEOUT);
			WaitUtils.waitForJobs();
		}

		private static void whenClangdFileOpenedUsingDoubleClick(String projectName) throws IOException
		{
			whenFileOpenedUsingDoubleClick(projectName, ILSPConstants.CLANGD_CONFIG_FILE);
		}

		private static void whenClangFormatFileOpenedUsingDoubleClick(String projectName) throws IOException
		{
			whenFileOpenedUsingDoubleClick(projectName, ILSPConstants.CLANG_FORMAT_FILE);
		}

		private static void whenFileOpenedUsingDoubleClick(String projectName, String fileName) throws IOException
		{
			projectTreeItem(projectName).getNode(fileName).doubleClick();
			editorFor(projectName, fileName);
		}

		/**
		 * Editors are looked up by the file they are opened on, because the same file name is used by several projects
		 * and because a dirty editor is not necessarily marked with an asterisk in its tab.
		 */
		private static SWTBotEditor editorFor(String projectName, String projectRelativePath)
		{
			bot.waitUntil(new DefaultCondition()
			{
				@Override
				public boolean test() throws Exception
				{
					return findEditor(projectName, projectRelativePath).isPresent();
				}

				@Override
				public String getFailureMessage()
				{
					return MessageFormat.format("The editor for {0} of the project {1} did not open in time",
							projectRelativePath, projectName);
				}
			}, EDITOR_TIMEOUT);

			SWTBotEditor editor = findEditor(projectName, projectRelativePath).get();
			editor.show();
			editor.setFocus();
			return editor;
		}

		private static Optional<SWTBotEditor> findEditor(String projectName, String projectRelativePath)
		{
			String workspacePath = "/" + projectName + "/" + projectRelativePath;
			for (SWTBotEditor editor : bot.editors())
			{
				if (workspacePath.equals(fileOfEditor(editor)))
				{
					return Optional.of(editor);
				}
			}

			return Optional.empty();
		}

		private static String fileOfEditor(SWTBotEditor editor)
		{
			return UIThreadRunnable.syncExec((Result<String>) () ->
			{
				try
				{
					IEditorInput editorInput = editor.getReference().getEditorInput();
					IFile file = Adapters.adapt(editorInput, IFile.class);
					return file == null ? null : file.getFullPath().toString();
				}
				catch (PartInitException partInitException)
				{
					return null;
				}
			});
		}

		private static void waitForEditorContent(SWTBotEditor editor, String expectedText)
		{
			try
			{
				bot.waitUntil(new DefaultCondition()
				{
					@Override
					public boolean test() throws Exception
					{
						return normalizeText(expectedText).equals(normalizeText(editor.toTextEditor().getText()));
					}

					@Override
					public String getFailureMessage()
					{
						return "The editor content did not match the expected content in time";
					}
				}, EDITOR_TIMEOUT);
			}
			catch (TimeoutException timeoutException)
			{
				// the assertion of the caller reports the actual content of the editor
			}
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

		private static void thenClangdFileContentChecked(String projectName) throws Exception
		{
			String buildPath = getExpectedBuildFolderPATH(projectName);
			SWTBotEditor editor = editorFor(projectName, ILSPConstants.CLANGD_CONFIG_FILE);

			String expectedText = "CompileFlags:\n  CompilationDatabase: " + buildPath + "\n  Remove: [-m*, -f*]";
			waitForEditorContent(editor, expectedText);

			assertTextEqualsNormalized("Clangd file content with build path did not match", expectedText,
					editor.toTextEditor().getText());
		}

		private static void thenClangFormatContentChecked(String projectName) throws Exception
		{
			SWTBotEditor editor = editorFor(projectName, ILSPConstants.CLANG_FORMAT_FILE);

			String expectedText = """
					# We'll use defaults from the LLVM style, but with some modifications so that it's close to the CDT K&R style.
					BasedOnStyle: LLVM
					UseTab: Always
					IndentWidth: 4
					TabWidth: 4
					BreakConstructorInitializers: AfterColon
					IndentAccessModifiers: false
					AccessModifierOffset: -4
					""";

			// Using trim() to avoid mismatch purely due to trailing spaces/newlines from text block processing
			assertTextEqualsNormalized("ClangFormat content did not match", expectedText.trim(),
					editor.toTextEditor().getText().trim());
		}

		private static void thenClangFormatContentEdited(String projectName) throws Exception
		{
			SWTBotEditor textEditor = editorFor(projectName, ILSPConstants.CLANG_FORMAT_FILE);
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

		private static void addSpaceToMainFile(String projectName) throws Exception
		{
			SWTBotEditor textEditor = editorFor(projectName, MAIN_FILE_PATH);
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

		private static void checkMainFileContentFormattedUnderActualSettings(String projectName) throws Exception
		{
			SWTBotEditor editor = editorFor(projectName, MAIN_FILE_PATH);

			String expectedText = """
					#include <stdbool.h>
					#include <stdio.h>
					#include <unistd.h>

					void app_main(void) {
					while (true) {
					printf("Hello from app_main!\\n");
					sleep(1);
					}
					}
					""";

			waitForEditorContent(editor, expectedText);

			assertTextEqualsNormalized("Formatted main file content did not match", expectedText.trim(),
					editor.toTextEditor().getText().trim());
		}

		private static void setupAutoSave() throws Exception
		{
			bot.menu("Window").menu("Preferences...").click();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Preferences", DIALOG_TIMEOUT);
			SWTBotShell prefrencesShell = bot.shell("Preferences");
			prefrencesShell.bot().tree().getTreeItem("C/C++").select();
			prefrencesShell.bot().tree().getTreeItem("C/C++").expand();
			prefrencesShell.bot().tree().getTreeItem("C/C++").getNode("Editor (LSP)").select();
			prefrencesShell.bot().tree().getTreeItem("C/C++").getNode("Editor (LSP)").expand();
			prefrencesShell.bot().tree().getTreeItem("C/C++").getNode("Editor (LSP)").getNode("Save Actions").select();
			prefrencesShell.bot().checkBox("Format source code").click();
			prefrencesShell.bot().button("Apply and Close").click();
			TestWidgetWaitUtility.waitWhileDialogIsVisible(bot, "Preferences", DIALOG_TIMEOUT);
		}

		private static void whenMainFileIsOpened(String projectName) throws Exception
		{
			ProjectTestOperations.openMainFileInTextEditorUsingContextMenu(projectName, bot);
			editorFor(projectName, MAIN_FILE_PATH);
		}

		private static void thenClangdFileClosed(String projectName) throws IOException
		{
			closeEditor(projectName, ILSPConstants.CLANGD_CONFIG_FILE, false);
		}

		private static void thenClangFormatFileClosed(String projectName) throws IOException
		{
			closeEditor(projectName, ILSPConstants.CLANG_FORMAT_FILE, false);
		}

		private static void thenClangFormatFileSavedAndClosed(String projectName) throws IOException
		{
			closeEditor(projectName, ILSPConstants.CLANG_FORMAT_FILE, true);
		}

		private static void thenMainFileSavedAndClosed(String projectName) throws IOException
		{
			closeEditor(projectName, MAIN_FILE_PATH, true);
		}

		/**
		 * Saving through the editor itself does not raise the confirmation dialog, which keeps the save deterministic.
		 */
		private static void closeEditor(String projectName, String projectRelativePath, boolean save)
		{
			SWTBotEditor editor = editorFor(projectName, projectRelativePath);
			if (save && editor.isDirty())
			{
				editor.save();
				WaitUtils.waitForJobs();
			}
			editor.close();
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

		private static void thenCompareActualClangdArgumentWithExpected(String projectName) throws IOException
		{
			SWTBotShell prefrencesShell = bot.shell("Preferences");
			String actualClangdPath = prefrencesShell.bot().textWithLabel("Additional").getText();
			String expectedClangdPath = getExpectedBuildFolderPATHforClangdAdditionalArgument(projectName);
			assertEquals(expectedClangdPath, actualClangdPath);
		}

		private static void thenCompareActualClangdDriversWithExpected() throws IOException
		{
			SWTBotShell prefrencesShell = bot.shell("Preferences");
			String actualClangdPath = prefrencesShell.bot().textWithLabel("Path").getText();
			String expectedClangdPath = IDFUtil.findCommandFromBuildEnvPath(ILSPConstants.CLANGD_EXECUTABLE);
			assertEquals(expectedClangdPath, actualClangdPath);
		}

		private static void whenProjectIsBuiltUsingContextMenu(String projectName) throws IOException
		{
			ProjectTestOperations.buildProjectUsingContextMenu(projectName, bot);
			ProjectTestOperations.waitForProjectBuild(projectName, bot);
			// the clangd settings are updated by the build job once the build output has been printed
			WaitUtils.waitForJobs();
		}

		private static void whenOpenClangdPreferences() throws Exception
		{
			bot.menu("Window").menu("Preferences...").click();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Preferences", DIALOG_TIMEOUT);
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
			preferencesShell.bot().button("Cancel").click();
			TestWidgetWaitUtility.waitWhileDialogIsVisible(bot, "Preferences", DIALOG_TIMEOUT);
		}

		private static void whenSelectProjectInLaunchConfig() throws Exception
		{
			LaunchBarConfigSelector configSelector = new LaunchBarConfigSelector(bot);
			configSelector.select(CLEAN_PROJECT1);
		}

		/**
		 * Leaves the workbench without editors and dialogs, so that a failing test cannot break the ones running after
		 * it.
		 */
		static void resetWorkbenchState()
		{
			ProjectTestOperations.closeSecondaryShells(bot);
			bot.closeAllEditors();
		}

		static void cleanupEnvironment()
		{
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}
	}
}
