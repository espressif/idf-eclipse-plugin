package com.espressif.idf.tests.main;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class MyFirstTest
{
	private static SWTWorkbenchBot	bot;
	 
	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTWorkbenchBot();
		bot.viewByTitle("Welcome").close();
	}
 
 
	@Test
	public void canCreateANewJavaProject() throws Exception {
		bot.shell().activate().bot().menu("File").menu("New").menu("Project...").click();
 
		SWTBotShell shell = bot.shell("New Project");
		shell.activate();
		bot.tree().expandNode("Java").select("Java Project");
		bot.button("Next >").click();
 
		bot.textWithLabel("Project name:").setText("MyFirstProject");
 
		bot.button("Finish").click();
		// FIXME: assert that the project is actually created, for later
	}
 
 
	@AfterClass
	public static void sleep() {
		bot.sleep(2000);
	}

}
