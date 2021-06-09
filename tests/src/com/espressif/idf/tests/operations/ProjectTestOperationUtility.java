package com.espressif.idf.tests.operations;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import com.espressif.idf.tests.common.configs.DefaultPropertyFetcher;
import com.espressif.idf.tests.common.utility.TestWidgetWaitUtility;

public class ProjectTestOperationUtility
{
	private static final String DEFAULT_PROJECT_BUILD_WAIT_PROPERTY = "default.project.build.wait";

	public static void buildProject(String projectName, SWTWorkbenchBot bot)
	{
		SWTBotTreeItem projectItem = fetchProjectFromProjectExplorer(projectName, bot);
		if (projectItem != null)
		{
			projectItem.contextMenu("Build Project").click();
		}
	}
	
	public static void waitForProjectBuild(String projectName, SWTWorkbenchBot bot) throws IOException
	{
		SWTBotView consoleView = bot.viewById("org.eclipse.ui.console.ConsoleView");
		consoleView.show();
		consoleView.setFocus();
		TestWidgetWaitUtility.waitUntilViewContains(bot, "Build complete", consoleView, 
				DefaultPropertyFetcher.getLongPropertyValue(DEFAULT_PROJECT_BUILD_WAIT_PROPERTY, 60000));
	}
	
	public static void setupProject(String projectName, String category, String subCategory, SWTWorkbenchBot bot)
	{
		bot.shell().activate().bot().menu("File").menu("New").menu("Project...").click();
		SWTBotShell shell = bot.shell("New Project");
		shell.activate();

		bot.tree().expandNode(category).select(subCategory);
		bot.button("Finish").click();
		bot.textWithLabel("Project name:").setText(projectName);
		bot.button("Finish").click();
		SWTWorkbenchBot botReferenceBot = bot;
		bot.waitUntil(new DefaultCondition()
		{
			@Override
			public boolean test() throws Exception
			{
				botReferenceBot.viewByTitle("Project Explorer").show();
				Optional<SWTBotTreeItem> projectItem = Arrays.asList(botReferenceBot.tree().getAllItems()).stream()
						.filter(project -> project.getText().equals(projectName)).findFirst();
				return projectItem.isPresent();
			}

			@Override
			public String getFailureMessage()
			{
				return "Project Explorer does not contain the project: " + projectName;
			}
		});
	}

	public static void closeProject(String projectName, SWTWorkbenchBot bot)
	{
		SWTBotTreeItem projectItem = fetchProjectFromProjectExplorer(projectName, bot);
		if (projectItem != null)
		{
			projectItem.contextMenu("Close Project").click();
		}
	}

	public static void deleteProject(String projectName, SWTWorkbenchBot bot, boolean deleteFromDisk)
	{
		SWTBotTreeItem projectItem = fetchProjectFromProjectExplorer(projectName, bot);
		if (projectItem != null)
		{
			projectItem.contextMenu("Delete").click();
			if (deleteFromDisk)
			{
				bot.checkBox("Delete project contents on disk (cannot be undone)").click();
			}

			bot.button("OK").click();
			SWTBotView projectExplorView = bot.viewByTitle("Project Explorer");
			SWTBotTreeItem[] projects = projectExplorView.bot().tree().getAllItems();
			projectExplorView.bot().waitUntil(new DefaultCondition()
			{
				@Override
				public boolean test() throws Exception
				{
					return Arrays.asList(projects).stream()
							.filter(project -> project.getText().equals(projectName)).count() == 0;
				}

				@Override
				public String getFailureMessage()
				{
					return "Project Explorer contains the project: " + projectName;
				}
			});
		}
	}

	public static void deleteProject(String projectName, SWTWorkbenchBot bot)
	{
		ProjectTestOperationUtility.deleteProject(projectName, bot, true);
	}
	
	private static SWTBotTreeItem fetchProjectFromProjectExplorer(String projectName, SWTWorkbenchBot bot)
	{
		bot.viewByTitle("Project Explorer").show();
		Optional<SWTBotTreeItem> projectItem = Arrays.asList(bot.tree().getAllItems()).stream()
				.filter(project -> project.getText().equals(projectName)).findFirst();
		if (projectItem.isPresent())
		{
			return projectItem.get();
		}
		
		return null;
	}
}
