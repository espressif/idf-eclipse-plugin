/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.executable.cases.project;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.ui.handlers.Messages;
import com.espressif.idf.ui.test.common.WorkBenchSWTBot;
import com.espressif.idf.ui.test.common.resources.DefaultFileContentsReader;
import com.espressif.idf.ui.test.common.utility.TestAssertUtility;
import com.espressif.idf.ui.test.common.utility.TestWidgetWaitUtility;
import com.espressif.idf.ui.test.operations.EnvSetupOperations;
import com.espressif.idf.ui.test.operations.ProjectTestOperations;
import com.espressif.idf.ui.test.operations.SWTBotTreeOperations;
import com.espressif.idf.ui.test.operations.selectors.LaunchBarConfigSelector;
import com.espressif.idf.ui.test.operations.selectors.LaunchBarTargetSelector;

/**
 * Test class to test the project creation, build and basic operations
 * 
 * @author Ali Azam Rana
 *
 */
@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NewEspressifIDFProjectTest
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
	public void givenNewIDFProjectIsSelectedThenProjectIsCreatedAndAddedToProjectExplorer() throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProjectTest");
		Fixture.whenNewProjectIsSelected();
		Fixture.thenProjectIsAddedToProjectExplorer();
	}

	@Test
	public void givenNewIDFProjectIsSelectedFromTemplateThenProjectIsCreatedAndAddedToProjectExplorerWithRequiredFiles()
			throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProjectTestTemplate");
		Fixture.givenProjectTemplateIs("bluetooth/esp_hid_device");
		Fixture.whenProjectIsCreatedFromTemplate();
		Fixture.thenProjectIsAddedToProjectExplorer();
		Fixture.thenProjectHasTheFile("esp_hid_device_main.c", "/main");
		Fixture.thenProjectHasTheFile("esp_hid_gap.c", "/main");
		Fixture.thenProjectHasTheFile("esp_hid_gap.h", "/main");
	}

	@Test
	public void givenNewIDFProjectIsCreatedAndBuiltUsingContextMenuOnProjectThenProjectIsCreatedAndBuilt()
			throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProjectForContextMenuBuildTest");
		Fixture.whenNewProjectIsSelected();
		Fixture.whenProjectIsBuiltUsingContextMenu();
		Fixture.thenConsoleShowsBuildSuccessful();
	}

	@Test
	public void givenNewIDFProjectIsCreatedAndBuiltUsingToolbarButtonThenProjectIsBuilt() throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProjectToolbarBuildButtonTest");
		Fixture.whenNewProjectIsSelected();
		Fixture.whenProjectIsBuiltUsingContextMenu();
		Fixture.thenConsoleShowsBuildSuccessful();
	}

//	@Test
//	public void givenNewProjectCreatedAndRenamedAfterThenProjectIsBuildSuccessfully() throws Exception
//	{
//		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
//		Fixture.givenProjectNameIs("NewProjectForRenameTest");
//		Fixture.whenNewProjectIsSelected();
//		Fixture.whenProjectIsRenamed("NewProjectForRenameTest2");
//		Fixture.whenProjectIsBuiltUsingContextMenu();
//		Fixture.thenConsoleShowsBuildSuccessful();
//	}

	@Test
	public void givenNewProjectCreatedBuiltAndThenProjectPythonCleanUsingContextMenu() throws Exception
	{
		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProjectPythonCleanTest");
		Fixture.whenNewProjectIsSelected();
		Fixture.whenProjectIsBuiltUsingContextMenu();
		Fixture.whenProjectPythonCleanUsingContextMenu();
		Fixture.checkPythonCLeanCommandDeleteFolder();
	}

	private static class Fixture
	{
		private static SWTWorkbenchBot bot;
		private static String category;
		private static String subCategory;
		private static String projectName;
		private static String projectTemplate;
		private static LaunchBarTargetSelector launchBarTargetSelector;
		private static LaunchBarConfigSelector launchBarConfigSelector;

		private static void loadEnv() throws Exception
		{
			bot = WorkBenchSWTBot.getBot();
			EnvSetupOperations.setupEspressifEnv(bot);
			bot.sleep(1000);
			ProjectTestOperations.deleteAllProjects(bot);
			launchBarConfigSelector = new LaunchBarConfigSelector(bot);
			try
			{
				launchBarTargetSelector = new LaunchBarTargetSelector(bot);
			}
			catch (WidgetNotFoundException e)
			{
				launchBarTargetSelector = new LaunchBarTargetSelector(bot, false);
			}

		}

		public static void thenLaunchTargetIsSelectedFromLaunchTargets(String launchTargetName)
		{
			launchBarTargetSelector.select(launchTargetName);
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

		private static void givenProjectTemplateIs(String projectTemplate)
		{
			Fixture.projectTemplate = projectTemplate;
		}

		public static void whenProjectIsRenamed(String newProjectName)
		{
			ProjectTestOperations.renameProject(projectName, newProjectName, bot);
			projectName = newProjectName;
		}

		private static void whenProjectIsCreatedFromTemplate()
		{
			ProjectTestOperations.setupProjectFromTemplate(projectName, category, subCategory, projectTemplate, bot);
		}

		private static void whenNewProjectIsSelected() throws Exception
		{
			ProjectTestOperations.setupProject(projectName, category, subCategory, bot);
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);
		}

		public static void turnOffDfu()
		{
			launchBarConfigSelector.clickEdit();
			bot.comboBox().setSelection("UART");
			bot.button("OK").click();
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);
		}

		private static void turnOnDfu()
		{
			launchBarConfigSelector.clickEdit();
			bot.comboBox().setSelection("DFU");
			bot.button("OK").click();
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);
		}

		private static void whenProjectIsBuiltUsingContextMenu() throws IOException
		{
			ProjectTestOperations.buildProjectUsingContextMenu(projectName, bot);
			ProjectTestOperations.waitForProjectBuild(bot);
		}

		private static void whenInstallNewComponentUsingContextMenu() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Install New Component");
			bot.editorByTitle(projectName).show();
			bot.button("Install").click();
			ProjectTestOperations.waitForProjectNewComponentInstalled(bot);
			bot.editorByTitle(projectName).close();
		}

		private static void whenRefreshProject() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Refresh");
		}

		private static void checkPythonCLeanCommandDeleteFolder() throws IOException
		{
			String pychache = IDFUtil.getIDFPath() + IPath.SEPARATOR + IDFConstants.TOOLS_FOLDER + IPath.SEPARATOR
					+ "__pychache__";
			Path pycachePath = Paths.get(pychache);
			String folderPATH = pycachePath.toAbsolutePath().toString();
			File folder = new File(folderPATH);
			assertTrue(ProjectTestOperations.checkFolderExistanceAfterPythonClean(folder));
		}

		private static void checkIfNewComponentIsInstalledUsingContextMenu() throws IOException
		{
			ProjectTestOperations.openProjectComponentYMLFileInTextEditorUsingContextMenu(projectName, bot);
			assertTrue(ProjectTestOperations.checkTextEditorContentForPhrase("espressif/mdns", bot));
		}

		private static void thenProjectIsAddedToProjectExplorer()
		{
			bot.viewByTitle("Project Explorer").show();
			bot.viewByTitle("Project Explorer").setFocus();
			bot.viewByTitle("Project Explorer").bot().tree().expandNode(projectName).select();
		}

		private static void thenProjectHasTheFile(String fileName, String path)
		{
			bot.viewByTitle("Project Explorer");
			String pathToPass = StringUtils.isNotEmpty(path) ? projectName.concat(path) : projectName;
			assertTrue(TestAssertUtility.treeContainsItem(fileName, pathToPass, bot.tree()));
		}

		private static void thenFileContentsMatchDefaultFile(String path, String fileName)
		{
			bot.viewByTitle("Project Explorer").show();
			String pathToPass = StringUtils.isNotEmpty(path) ? projectName.concat(path) : projectName;
			SWTBotTreeItem[] items = SWTBotTreeOperations.getTreeItems(bot.tree(), pathToPass);
			Optional<SWTBotTreeItem> file = Arrays.asList(items).stream().filter(i -> i.getText().equals(fileName))
					.findFirst();
			String defaultFileContents = DefaultFileContentsReader.getFileContents(pathToPass + "/" + fileName);
			if (file.isPresent())
			{
				file.get().doubleClick();
				SWTBotEditor editor = bot.editorByTitle(fileName);
				editor.show();
				bot.sleep(1000);
				switchEditorToSourceIfPresent(editor);
				assertTrue(editor.toTextEditor().getText().equals(defaultFileContents));
			}
			else
			{
				throw new AssertionError("File not found: " + fileName);
			}
		}

		private static void thenConsoleShowsBuildSuccessful() throws IOException
		{
			ProjectTestOperations.findInConsole(bot, "CDT Build Console [" + Fixture.projectName + "]",
					"Build complete (0 errors");
		}

		private static void cleanTestEnv()
		{
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}

		private static void switchEditorToSourceIfPresent(SWTBotEditor editor)
		{
			try
			{
				editor.toTextEditor().bot().cTabItem("Source").activate();
			}
			catch (WidgetNotFoundException e)
			{
				// do nothing
			}

		}

		private static void whenProjectCleanUsingContextMenu() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Project Clean");
			ProjectTestOperations.joinJobByName(Messages.ProjectCleanCommandHandler_RunningProjectCleanJobName);
			ProjectTestOperations.findInConsole(bot, "Espressif IDF Tools Console", "Done");
		}

		private static void whenProjectFullCleanUsingContextMenu() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Project Full Clean");
			ProjectTestOperations.joinJobByName(Messages.ProjectFullCleanCommandHandler_RunningFullcleanJobName);
			ProjectTestOperations.findInConsole(bot, "Espressif IDF Tools Console", "Done");
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);
		}

		private static void whenProjectPythonCleanUsingContextMenu() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Python Clean");
			ProjectTestOperations.joinJobByName(Messages.PythonCleanCommandHandler_RunningPythonCleanJobName);
		}

		private static void checkIfProjectCleanedFilesInBuildFolder() throws IOException
		{
			assertTrue(ProjectTestOperations.findProjectCleanedFilesInBuildFolder(projectName, bot));
		}

		private static void checkIfProjectFullCleanedFilesInBuildFolder() throws IOException
		{
			assertTrue(ProjectTestOperations.findProjectFullCleanedFilesInBuildFolder(projectName, bot));
		}
	}
}
