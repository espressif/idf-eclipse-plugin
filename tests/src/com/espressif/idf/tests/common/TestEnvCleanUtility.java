package com.espressif.idf.tests.common;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public class TestEnvCleanUtility
{
	public static void closeAndDeleteAllProjectsInEnv(SWTWorkbenchBot bot)
	{
		bot.viewByTitle("Project Explorer").show();
		SWTBotTreeItem[] projects = bot.tree().getAllItems();
		for(SWTBotTreeItem project : projects)
		{
			project.select();
			project.contextMenu("Close Project").click();
			project.contextMenu("Delete").click();
			bot.checkBox("Delete project contents on disk (cannot be undone)").click();
			bot.button("OK").click();
		}
	}
}
