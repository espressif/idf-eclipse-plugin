package com.espressif.idf.tests.common;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public class TestSetupUtility
{
	public static void setupProject(String projectName, String category, String subCategory, SWTWorkbenchBot bot)
	{
		bot.shell().activate().bot().menu("File").menu("New").menu("Project...").click();
		SWTBotShell shell = bot.shell("New Project");
		shell.activate();

		bot.tree().expandNode(category).select(subCategory);
		bot.button("Finish").click();
		bot.textWithLabel("Project name:").setText(projectName);
		bot.button("Finish").click();
	}

	public static void closeProject(String projectName, SWTWorkbenchBot bot)
	{
		bot.viewByTitle("Project Explorer").show();
		Optional<SWTBotTreeItem> projectItem = Arrays.asList(bot.tree().getAllItems()).stream()
				.filter(project -> project.getText().equals(projectName)).findFirst();
		if (projectItem.isPresent())
		{
			projectItem.get().contextMenu("Close Project").click();
		}
	}

	public static void deleteProject(String projectName, SWTWorkbenchBot bot, boolean deleteFromDisk)
	{
		bot.viewByTitle("Project Explorer").show();
		Optional<SWTBotTreeItem> projectItem = Arrays.asList(bot.tree().getAllItems()).stream()
				.filter(project -> project.getText().equals(projectName)).findFirst();
		if (projectItem.isPresent())
		{
			projectItem.get().contextMenu("Delete").click();
			if (deleteFromDisk)
			{
				bot.checkBox("Delete project contents on disk (cannot be undone)").click();
			}

			bot.button("OK").click();
		}
	}

	public static void deleteProject(String projectName, SWTWorkbenchBot bot)
	{
		TestSetupUtility.deleteProject(projectName, bot, true);
	}
}
