package com.espressif.idf.tests.project;

import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.espressif.idf.tests.common.TestAssertUtility;
import com.espressif.idf.tests.common.TestSetupUtility;

@RunWith(SWTBotJunit4ClassRunner.class)
public class NewEspressifIDFProjectTest
{
	private static Fixture fixture;
	
	@BeforeClass
	public static void beforeEachTest() throws Exception
	{
		fixture = new Fixture();
	}
	
	@AfterClass
	public static void afterEachTest()
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
		fixture.thenProjectHasTheFile("CMakeLists.txt");
	}
	
	private static class Fixture
	{
		private SWTWorkbenchBot	bot;
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
			TestSetupUtility.setupProject(projectName, category, subCategory, bot);
		}
		
		private void thenProjectIsAddedToProjectExplorer()
		{
			bot.viewByTitle("Project Explorer");
			bot.tree().expandNode(projectName).select();
		}
		
		private void thenProjectHasTheFile(String fileName)
		{
			bot.viewByTitle("Project Explorer");
			assertTrue(TestAssertUtility.treeContainsItem(fileName, projectName, bot.tree()));
		}
		
		private void cleanTestEnv()
		{
			TestSetupUtility.closeProject(projectName, bot);
			TestSetupUtility.deleteProject(projectName, bot);
		}
	}
}
