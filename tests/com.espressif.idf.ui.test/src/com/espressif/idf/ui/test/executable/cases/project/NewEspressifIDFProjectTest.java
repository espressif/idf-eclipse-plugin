/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.executable.cases.project;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.espressif.idf.ui.test.common.configs.DefaultPropertyFetcher;
import com.espressif.idf.ui.test.common.resources.DefaultFileContentsReader;
import com.espressif.idf.ui.test.common.utility.TestAssertUtility;
import com.espressif.idf.ui.test.common.utility.TestWidgetWaitUtility;
import com.espressif.idf.ui.test.operations.EnvSetupOperations;
import com.espressif.idf.ui.test.operations.ProjectTestOperations;
import com.espressif.idf.ui.test.operations.SWTBotTreeOperations;

/**
 * Test class to test the project creation, build and basic operations
 * 
 * @author Ali Azam Rana
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class NewEspressifIDFProjectTest
{
	private Fixture fixture;

	@Before
	public void beforeEachTest() throws Exception
	{
		fixture = new Fixture();
	}

	@After
	public void afterEachTest()
	{
		fixture.cleanTestEnv();
	}

	@Test
	public void givenNewIDFProjectIsSelectedThenProjectIsCreatedAndAddedToProjectExplorer() throws Exception
	{
		fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		fixture.givenProjectNameIs("NewProjectTest");
		fixture.whenNewProjectIsSelected();
		fixture.thenProjectIsAddedToProjectExplorer();
	}

	@Test
	public void givenNewIDFProjectIsSelectedFromTemplateThenProjectIsCreatedAndAddedToProjectExplorerWithRequiredFiles()
			throws Exception
	{
		fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		fixture.givenProjectNameIs("NewProjectTestTemplate");
		fixture.givenProjectTemplateIs("bluetooth/esp_hid_device");
		fixture.whenProjectIsCreatedFromTemplate();
		fixture.thenProjectIsAddedToProjectExplorer();
		fixture.thenProjectHasTheFile("esp_hid_device_main.c", "/main");
		fixture.thenProjectHasTheFile("esp_hid_gap.c", "/main");
		fixture.thenProjectHasTheFile("esp_hid_gap.h", "/main");
	}

	@Test
	public void givenNewProjectIsSelectedTheProjectHasTheRequiredFiles() throws Exception
	{
		fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		fixture.givenProjectNameIs("NewProjectTest");
		fixture.whenNewProjectIsSelected();
		fixture.thenProjectHasTheFile("CMakeLists.txt", "/main");
		fixture.thenFileContentsMatchDefaultFile("/main", "CMakeLists.txt");
		fixture.thenProjectHasTheFile(".project", null);
		fixture.thenFileContentsMatchDefaultFile(null, ".project");
	}

	@Test
	public void givenNewIDFProjectIsCreatedAndBuiltUsingContextMenuOnProjectThenProjectIsCreatedAndBuilt()
			throws Exception
	{
		fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		fixture.givenProjectNameIs("NewProjectTest");
		fixture.whenNewProjectIsSelected();
		fixture.whenProjectIsBuiltUsingContextMenu();
		fixture.thenConsoleShowsBuildSuccessful();
	}

	@Test
	public void givenNewIDFProjectIsCreatedAndBuiltUsingToolbarButtonThenProjectIsBuilt() throws Exception
	{
		fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		fixture.givenProjectNameIs("NewProjectTest");
		fixture.whenNewProjectIsSelected();
		fixture.whenProjectIsBuiltUsingToolbarButton("NewProjectTest");
		fixture.thenConsoleShowsBuildSuccessful();
	}

	@Test
	public void givenNewIDFProjectIsCreatedBuilAndCopiedAndOldProjectIsDeletedTheCopiedProjectIsBuiltSuccessfully()
			throws Exception
	{
		fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		fixture.givenProjectNameIs("NewProjectTest");
		fixture.whenNewProjectIsSelected();
		fixture.whenProjectIsBuiltUsingContextMenu();
		fixture.thenConsoleShowsBuildSuccessful();

		fixture.whenProjectIsCopied("NewProjectTest", "NewProjectTest2");
		
		fixture.closeProject("NewProjectTest");
		fixture.deleteProject("NewProjectTest");

		fixture.whenProjectIsBuiltUsingToolbarButton("NewProjectTest2");
		fixture.thenConsoleShowsBuildSuccessful();
		
		fixture.closeProject("NewProjectTest2");
		fixture.deleteProject("NewProjectTest2");
	}

	@Test
	public void givenNewIDFProjectIsCreatedAndCopiedTheCopiedProjectIsBuiltSuccessfully() throws Exception
	{
		fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		fixture.givenProjectNameIs("NewProjectTest");
		fixture.whenNewProjectIsSelected();
		fixture.whenProjectIsCopied("NewProjectTest", "NewProjectTest2");
		fixture.whenProjectIsBuiltUsingToolbarButton("NewProjectTest2");
		fixture.thenConsoleShowsBuildSuccessful();
		fixture.closeProject("NewProjectTest2");
		fixture.deleteProject("NewProjectTest2");
	}

	@Test
	public void givenNewProjectCreatedAndRenamedAfterThenProjectIsBuildSuccessfully() throws Exception
	{
		fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		fixture.givenProjectNameIs("NewProjectTest");
		fixture.whenNewProjectIsSelected();
		fixture.whenProjectIsRenamed("NewProjectTest2");
		fixture.whenProjectIsBuiltUsingContextMenu();
		fixture.thenConsoleShowsBuildSuccessful();
	}

	@Test
	public void givenNewProjectCreatedBuiltAndThenRenamedThenProjectIsBuildSuccessfully() throws Exception
	{
		fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		fixture.givenProjectNameIs("NewProjectTest");
		fixture.whenNewProjectIsSelected();
		fixture.whenProjectIsBuiltUsingContextMenu();
		
		fixture.whenProjectIsRenamed("NewProjectTest2");
		fixture.whenProjectIsBuiltUsingContextMenu();
		fixture.thenConsoleShowsBuildSuccessful();
	}

	private class Fixture
	{
		private SWTWorkbenchBot bot;
		private String category;
		private String subCategory;
		private String projectName;
		private String projectTemplate;

		private Fixture() throws Exception
		{
			bot = new SWTWorkbenchBot();
			EnvSetupOperations.setupEspressifEnv(bot);
			bot.sleep(1000);
		}

		private void givenNewEspressifIDFProjectIsSelected(String category, String subCategory)
		{
			this.category = category;
			this.subCategory = subCategory;
		}

		private void givenProjectNameIs(String projectName)
		{
			this.projectName = projectName;
		}

		private void givenProjectTemplateIs(String projectTemplate)
		{
			this.projectTemplate = projectTemplate;
		}

		public void whenProjectIsRenamed(String newProjectName)
		{
			ProjectTestOperations.renameProject(projectName, newProjectName, bot);
			this.projectName = newProjectName;
		}

		private void whenProjectIsCreatedFromTemplate()
		{
			ProjectTestOperations.setupProjectFromTemplate(projectName, category, subCategory, projectTemplate, bot);
		}

		private void whenNewProjectIsSelected() throws Exception
		{
			ProjectTestOperations.setupProject(projectName, category, subCategory, bot);
		}

		private void whenProjectIsCopied(String projectName, String projectCopyName) throws IOException
		{
			ProjectTestOperations.copyProjectToExistingWorkspace(projectName, projectCopyName, bot,
					DefaultPropertyFetcher.getLongPropertyValue("default.project.copy.wait", 60000));
			TestWidgetWaitUtility.waitForOperationsInProgressToFinish(bot);
		}

		private void whenProjectIsBuiltUsingContextMenu() throws IOException
		{
			ProjectTestOperations.buildProjectUsingContextMenu(projectName, bot);
			ProjectTestOperations.waitForProjectBuild(bot);
			TestWidgetWaitUtility.waitForOperationsInProgressToFinish(bot);
		}

		private void whenProjectIsBuiltUsingToolbarButton(String projectName) throws IOException
		{
			SWTBotView projectExplorView = bot.viewByTitle("Project Explorer");
			projectExplorView.show();
			projectExplorView.bot().tree().getTreeItem(projectName).select();
			bot.toolbarButtonWithTooltip("Build").click();
			ProjectTestOperations.waitForProjectBuild(bot);
			TestWidgetWaitUtility.waitForOperationsInProgressToFinish(bot);
		}

		private void thenProjectIsAddedToProjectExplorer()
		{
			bot.viewByTitle("Project Explorer").show();
			bot.viewByTitle("Project Explorer").setFocus();
			bot.viewByTitle("Project Explorer").bot().tree().expandNode(projectName).select();
		}

		private void thenProjectHasTheFile(String fileName, String path)
		{
			bot.viewByTitle("Project Explorer");
			String pathToPass = StringUtils.isNotEmpty(path) ? projectName.concat(path) : projectName;
			assertTrue(TestAssertUtility.treeContainsItem(fileName, pathToPass, bot.tree()));
		}

		private void thenFileContentsMatchDefaultFile(String path, String fileName)
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

		private void thenConsoleShowsBuildSuccessful()
		{
			TestWidgetWaitUtility.waitForOperationsInProgressToFinish(bot);
			SWTBotView consoleView = bot.viewById("org.eclipse.ui.console.ConsoleView");
			consoleView.show();
			consoleView.setFocus();
			String consoleTextString = consoleView.bot().styledText().getText();
			assertTrue(consoleTextString.contains("Build complete (0 errors"));
		}

		private void closeProject(String projectName)
		{
			TestWidgetWaitUtility.waitForOperationsInProgressToFinish(bot);
			ProjectTestOperations.closeProject(projectName, bot);
		}

		private void deleteProject(String projectName)
		{
			TestWidgetWaitUtility.waitForOperationsInProgressToFinish(bot);
			ProjectTestOperations.deleteProject(projectName, bot);
		}

		private void cleanTestEnv()
		{
			TestWidgetWaitUtility.waitForOperationsInProgressToFinish(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}

		private void switchEditorToSourceIfPresent(SWTBotEditor editor)
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
	}
}
