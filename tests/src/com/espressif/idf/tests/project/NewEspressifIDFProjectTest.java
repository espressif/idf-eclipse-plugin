package com.espressif.idf.tests.project;

import static org.junit.Assert.assertTrue;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.espressif.idf.tests.common.TestAssertUtility;
import com.espressif.idf.tests.common.resources.DefaultFileContentsReader;
import com.espressif.idf.tests.operations.ProjectTestOperationUtility;
import com.espressif.idf.tests.operations.SWTBotTreeOperationsUtility;

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
		fixture.thenProjectHasTheFile("CMakeLists.txt", "/main");
		fixture.thenFileContentsMatchDefaultFile("/main", "CMakeLists.txt");
	}

	@Test
	public void givenNewIDFProjectIsCreatedAndBuiltThenProjectIsCreatedAndBuilt() throws Exception
	{
		fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		fixture.givenProjectNameIs("NewProjectTest");
		fixture.whenNewProjectIsSelected();
		fixture.whenProjectIsBuilt();
		fixture.thenConsoleShowsBuildSuccessful();
	}

	private class Fixture
	{
		private SWTWorkbenchBot bot;
		private String category;
		private String subCategory;
		private String projectName;

		private Fixture()
		{
			bot = new SWTWorkbenchBot();
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

		private void whenNewProjectIsSelected() throws Exception
		{
			ProjectTestOperationUtility.setupProject(projectName, category, subCategory, bot);
		}

		public void whenProjectIsBuilt() throws IOException
		{
			ProjectTestOperationUtility.buildProject(projectName, bot);
			ProjectTestOperationUtility.waitForProjectBuild(projectName, bot);
		}

		private void thenProjectIsAddedToProjectExplorer()
		{
			bot.viewByTitle("Project Explorer");
			bot.tree().expandNode(projectName).select();
		}

		private void thenProjectHasTheFile(String fileName, String path)
		{
			bot.viewByTitle("Project Explorer");
			String pathToPass = StringUtils.isNotEmpty(path) ? projectName.concat(path) : projectName;
			assertTrue(TestAssertUtility.treeContainsItem(fileName, pathToPass, bot.tree()));
		}

		private void thenFileContentsMatchDefaultFile(String path, String fileName) throws IOException
		{
			bot.viewByTitle("Project Explorer").show();
			String pathToPass = StringUtils.isNotEmpty(path) ? projectName.concat(path) : projectName;
			SWTBotTreeItem[] items = SWTBotTreeOperationsUtility.getTreeItems(bot.tree(), pathToPass);
			Optional<SWTBotTreeItem> file = Arrays.asList(items).stream().filter(i -> i.getText().equals(fileName))
					.findFirst();
			String defaultFileContents = DefaultFileContentsReader.getFileContents(pathToPass + "/" + fileName);
			if (file.isPresent())
			{
				file.get().doubleClick();
				bot.editorByTitle(fileName).show();
				assertTrue(bot.styledText().getText().equals(defaultFileContents));
			}
			else
			{
				throw new AssertionError("File not found: " + fileName);
			}
		}

		public void thenConsoleShowsBuildSuccessful()
		{
			SWTBotView consoleView = bot.viewById("org.eclipse.ui.console.ConsoleView");
			consoleView.show();
			consoleView.setFocus();
			String consoleTextString = consoleView.bot().styledText().getText();
			assertTrue(consoleTextString.contains("Build complete (0 errors"));
		}

		private void cleanTestEnv()
		{
			ProjectTestOperationUtility.closeProject(projectName, bot);
			ProjectTestOperationUtility.deleteProject(projectName, bot);
		}
	}
}
