/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.operations;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import com.espressif.idf.ui.test.common.configs.DefaultPropertyFetcher;
import com.espressif.idf.ui.test.common.utility.TestWidgetWaitUtility;

/**
 * Class to contain the common operations related to project setup. The class can be used in different test classes to
 * setup the required projects
 * 
 * @author Ali Azam Rana
 *
 */
public class ProjectTestOperations
{
	private static final String DEFAULT_PROJECT_BUILD_WAIT_PROPERTY = "default.project.build.wait";

	/**
	 * Build a project using the context menu by right clicking on the project
	 * 
	 * @param projectName project name to build
	 * @param bot         current SWT bot reference
	 */
	public static void buildProjectUsingContextMenu(String projectName, SWTWorkbenchBot bot)
	{
		SWTBotTreeItem projectItem = fetchProjectFromProjectExplorer(projectName, bot);
		if (projectItem != null)
		{
			projectItem.select();
			projectItem.contextMenu("Build Project").click();
		}
	}

	/**
	 * Waits for the current build operation to be completed
	 * 
	 * @param bot current SWT bot reference
	 * @throws IOException
	 */
	public static void waitForProjectBuild(SWTWorkbenchBot bot) throws IOException
	{
		SWTBotView consoleView = viewConsole("CDT Build Console", bot); //bot.viewById("org.eclipse.ui.console.ConsoleView");
		consoleView.show();
		consoleView.setFocus();
		TestWidgetWaitUtility.waitUntilViewContains(bot, "Build complete", consoleView,
				DefaultPropertyFetcher.getLongPropertyValue(DEFAULT_PROJECT_BUILD_WAIT_PROPERTY, 60000));
	}

	public static SWTBotView viewConsole(String consoleType, SWTWorkbenchBot bot)
	{
		SWTBotView view = bot.viewByPartName("Console");
		view.setFocus();
		SWTBotToolbarDropDownButton b = view.toolbarDropDownButton("Display Selected Console");
		org.hamcrest.Matcher<MenuItem> withRegex = WidgetMatcherFactory.withRegex(".*" + consoleType + ".*");
//		focusMainShell(bot);
		b.menuItem(withRegex).click();
//		try
//		{
//			b.pressShortcut(KeyStroke.getInstance("ESC"));
//		}
//		catch (ParseException e)
//		{
//		}
		view.setFocus();
		return view;
	}

	/**
	 * Focus on the main window
	 */
	public static void focusMainShell(SWTWorkbenchBot bot)
	{
		SWTBotShell shell = getMainShell(bot);
		shell.activate();
	}

	private static SWTBotShell getMainShell(SWTWorkbenchBot bot)
	{
		for (SWTBotShell shellBot : bot.shells())
		{
			if (shellBot.getText().toLowerCase().contains("eclipse"))
			{
				return shellBot;
			}
		}
		return null;
	}

	/**
	 * Creates an espressif idf project from the template
	 * 
	 * @param projectName  name of the project, project will be created with this name
	 * @param category     category of projects
	 * @param subCategory  sub category from the projects window
	 * @param templatePath the template path to select a template
	 * @param bot          current SWT bot reference
	 */
	public static void setupProjectFromTemplate(String projectName, String category, String subCategory,
			String templatePath, SWTWorkbenchBot bot)
	{
		bot.shell().activate().bot().menu("File").menu("New").menu("Project...").click();
		SWTBotShell shell = bot.shell("New Project");
		shell.activate();

		bot.tree().expandNode(category).select(subCategory);
		bot.button("Finish").click();
		bot.textWithLabel("Project name:").setText(projectName);
		bot.button("Next >").click();
		bot.checkBox("Create a project using one of the templates").click();
		SWTBotTreeItem templateItem = SWTBotTreeOperations.getTreeItem(bot.tree(), templatePath);
		templateItem.select();
		bot.textWithLabel("&Project name:").setText(projectName);
		bot.button("Finish").click();

		TestWidgetWaitUtility.waitUntilViewContainsTheTreeItemWithName(projectName, bot.viewByTitle("Project Explorer"),
				7000);
	}

	/**
	 * Set up a project
	 * 
	 * @param projectName name of the project
	 * @param category    category of the project
	 * @param subCategory sub category of the project
	 * @param bot         current SWT bot reference
	 */
	public static void setupProject(String projectName, String category, String subCategory, SWTWorkbenchBot bot)
	{
		bot.shell().activate().bot().menu("File").menu("New").menu("Project...").click();
		SWTBotShell shell = bot.shell("New Project");
		shell.activate();

		bot.tree().expandNode(category).select(subCategory);
		bot.button("Finish").click();
		bot.textWithLabel("Project name:").setText(projectName);
		bot.button("Finish").click();
		TestWidgetWaitUtility.waitUntilViewContainsTheTreeItemWithName(projectName, bot.viewByTitle("Project Explorer"),
				5000);
	}

	/**
	 * Closes the project
	 * 
	 * @param projectName project name to close
	 * @param bot         current SWT bot reference
	 */
	public static void closeProject(String projectName, SWTWorkbenchBot bot)
	{
		SWTBotTreeItem projectItem = fetchProjectFromProjectExplorer(projectName, bot);
		if (projectItem != null)
		{
			projectItem.contextMenu("Close Project").click();
		}
	}

	/**
	 * Deletes the project
	 * 
	 * @param projectName    name of the project
	 * @param bot            current SWT bot reference
	 * @param deleteFromDisk delete from disk or only delete from workspace
	 */
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
			projectExplorView.show();
			SWTBotTreeItem[] projects = projectExplorView.bot().tree().getAllItems();
			projectExplorView.bot().waitUntil(new DefaultCondition()
			{
				@Override
				public boolean test() throws Exception
				{
					return Arrays.asList(projects).stream().filter(project -> project.getText().equals(projectName))
							.count() == 0;
				}

				@Override
				public String getFailureMessage()
				{
					return "Project Explorer contains the project: " + projectName;
				}
			}, 60000);
		}
	}

	/**
	 * Deletes the project from disk and workspace
	 * 
	 * @param projectName name of the project to delete
	 * @param bot         current SWT bot reference
	 */
	public static void deleteProject(String projectName, SWTWorkbenchBot bot)
	{
		ProjectTestOperations.deleteProject(projectName, bot, true);
	}

	private static SWTBotTreeItem fetchProjectFromProjectExplorer(String projectName, SWTWorkbenchBot bot)
	{
		SWTBotView projectExplorView = bot.viewByTitle("Project Explorer");
		projectExplorView.show();
		projectExplorView.setFocus();
		SWTBotTreeItem[] items = projectExplorView.bot().tree().getAllItems();
		Optional<SWTBotTreeItem> project = Arrays.asList(items).stream().filter(i -> i.getText().equals(projectName))
				.findFirst();
		if (project.isPresent())
		{
			return project.get();
		}

		return null;
	}

	/**
	 * Copies the project to the existing workspace
	 * 
	 * @param projectName     name of the project to copy
	 * @param projectCopyName name of the project after copy
	 * @param bot             current SWT bot reference
	 * @param timeout         time to wait for in ms for the copy operation to be completed
	 */
	public static void copyProjectToExistingWorkspace(String projectName, String projectCopyName, SWTWorkbenchBot bot,
			long timeout)
	{
		SWTBotView projectExplorerBotView = bot.viewByTitle("Project Explorer");
		projectExplorerBotView.show();
		projectExplorerBotView.setFocus();
		SWTBotTreeItem projectItem = fetchProjectFromProjectExplorer(projectName, bot);
		if (projectItem != null)
		{
			projectItem.contextMenu("Copy").click();
			projectExplorerBotView.bot().tree().contextMenu("Paste").click();
			bot.textWithLabel("&Project name:").setText(projectCopyName);
			bot.button("Copy").click();
			TestWidgetWaitUtility.waitUntilViewContainsTheTreeItemWithName(projectCopyName, projectExplorerBotView,
					timeout);
			bot.sleep(3000);
		}
	}

	public static void renameProject(String projectName, String newProjectName, SWTWorkbenchBot bot)
	{
		SWTBotView projectExplorerBotView = bot.viewByTitle("Project Explorer");
		projectExplorerBotView.show();
		projectExplorerBotView.setFocus();
		SWTBotTreeItem projectItem = fetchProjectFromProjectExplorer(projectName, bot);
		if (projectItem != null)
		{
			projectItem.contextMenu("Rename...").click();
			bot.textWithLabel("New na&me:").setText(newProjectName);
			bot.button("OK").click();
			TestWidgetWaitUtility.waitUntilViewContainsTheTreeItemWithName(newProjectName, projectExplorerBotView,
					600000);
		}
	}

	public static void closeAllProjects(SWTWorkbenchBot bot)
	{
		SWTBotView projectExplorerBotView = bot.viewByTitle("Project Explorer");
		projectExplorerBotView.show();
		projectExplorerBotView.setFocus();
		try
		{
			for (SWTBotTreeItem project : projectExplorerBotView.bot().tree().getAllItems())
			{
				project.contextMenu("Close Project").click();
			}
		}
		catch (WidgetNotFoundException widgetNotFoundException)
		{
			// logging will be added to show no projects were found
		}
	}

	public static void deleteAllProjects(SWTWorkbenchBot bot)
	{
		SWTBotView projectExplorerBotView = bot.viewByTitle("Project Explorer");
		projectExplorerBotView.show();
		projectExplorerBotView.setFocus();
		try
		{
			for (SWTBotTreeItem project : projectExplorerBotView.bot().tree().getAllItems())
			{
				String projectName = project.getText();
				project.contextMenu("Delete").click();
				bot.checkBox("Delete project contents on disk (cannot be undone)").click();
				bot.button("OK").click();
				projectExplorerBotView.show();
				SWTBotTreeItem[] projects = projectExplorerBotView.bot().tree().getAllItems();
				projectExplorerBotView.bot().waitUntil(new DefaultCondition()
				{
					@Override
					public boolean test() throws Exception
					{
						return Arrays.asList(projects).stream().filter(project -> project.getText().equals(projectName))
								.count() == 0;
					}

					@Override
					public String getFailureMessage()
					{
						return "Project Explorer contains the project: " + projectName;
					}
				});
			}
		}
		catch (WidgetNotFoundException widgetNotFoundException)
		{
			// logging will be added to show no projects were found
		}
	}
}
