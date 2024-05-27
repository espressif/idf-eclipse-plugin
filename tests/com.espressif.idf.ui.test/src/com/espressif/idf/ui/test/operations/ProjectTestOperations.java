/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.operations;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IPageLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espressif.idf.ui.test.common.configs.DefaultPropertyFetcher;
import com.espressif.idf.ui.test.common.utility.TestWidgetWaitUtility;
import com.espressif.idf.ui.test.common.utility.WaitUtils;

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

	private static final Logger logger = LoggerFactory.getLogger(ProjectTestOperations.class);

	private static final int DELETE_PROJECT_TIMEOUT = 240000;

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
			projectItem.expand();
			projectItem.select("build");
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
		SWTBotView consoleView = viewConsole("CDT Build Console", bot);
		consoleView.show();
		consoleView.setFocus();
		TestWidgetWaitUtility.waitUntilViewContains(bot, "Build complete", consoleView,
				DefaultPropertyFetcher.getLongPropertyValue(DEFAULT_PROJECT_BUILD_WAIT_PROPERTY, 600000));
	}

	public static void waitForProjectNewComponentInstalled(SWTWorkbenchBot bot) throws IOException
	{
		SWTBotView consoleView = viewConsole("ESP-IDF Console", bot);
		consoleView.show();
		consoleView.setFocus();
		TestWidgetWaitUtility.waitUntilViewContains(bot, "Successfully added dependency", consoleView,
				DefaultPropertyFetcher.getLongPropertyValue("Install New Component", 10000));
	}

	public static SWTBotView viewConsole(String consoleType, SWTWorkbenchBot bot)
	{
		SWTBotView view = bot.viewByPartName("Console");
		view.setFocus();
		SWTBotToolbarDropDownButton b = view.toolbarDropDownButton("Display Selected Console");
		org.hamcrest.Matcher<MenuItem> withRegex = WidgetMatcherFactory.withRegex(".*" + consoleType + ".*");
		b.menuItem(withRegex).click();
		view.setFocus();
		return view;
	}

	public static void createDebugConfiguration(String projectName, SWTWorkbenchBot bot)
	{
		SWTBotView projectExplorerBotView = bot.viewByTitle("Project Explorer");
		projectExplorerBotView.show();
		projectExplorerBotView.setFocus();
		SWTBotTreeItem projectItem = fetchProjectFromProjectExplorer(projectName, bot);
		if (projectItem != null)
		{
			projectItem.select().contextMenu("Debug As").menu("Debug Configurations...").click();
			bot.tree().getTreeItem("ESP-IDF GDB OpenOCD Debugging").select();
			bot.tree().getTreeItem("ESP-IDF GDB OpenOCD Debugging").doubleClick();
			bot.button("Close").click();
		}

	}

	public static void openProjectComponentYMLFileInTextEditorUsingContextMenu(String projectName, SWTWorkbenchBot bot)
	{
		SWTBotTreeItem projectItem = fetchProjectFromProjectExplorer(projectName, bot);
		if (projectItem != null)
		{
			projectItem.select();
			projectItem.expand();
			projectItem.getNode("main").expand();

			int maxAttempts = 2;
			for (int attempt = 0; attempt <= maxAttempts; attempt++)
			{
				SWTBotTreeItem fileToOpenItem = findTreeItem(projectItem.getNode("main"), "idf_component.yml");

				if (fileToOpenItem != null)
				{
					fileToOpenItem.select();
					fileToOpenItem.doubleClick();
					return;
				}

				else
				{
					try
					{
						Thread.sleep(3000);
					}
					catch (InterruptedException e)
					{
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
	}

	public static boolean findProjectCleanedFilesInBuildFolder(String projectName, SWTWorkbenchBot bot)
	{
		SWTBotTreeItem projectItem = fetchProjectFromProjectExplorer(projectName, bot);
		if (projectItem != null)
		{
			projectItem.select();
			projectItem.getNode("build").expand();

			boolean fileToBeAbsentItem = isFileAbsent(projectItem.getNode("build"), ".elf");
			SWTBotTreeItem fileToFindItem = findTreeItem(projectItem.getNode("build"), "bootloader");
			if (fileToFindItem != null && fileToBeAbsentItem)
			{
				return true;
			}
			return false;
		}
		return false;
	}

	public static boolean findProjectFullCleanedFilesInBuildFolder(String projectName, SWTWorkbenchBot bot)
	{
		SWTBotTreeItem projectItem = fetchProjectFromProjectExplorer(projectName, bot);
		if (projectItem != null)
		{
			projectItem.select();
			projectItem.getNode("build").expand();

			boolean fileToBeAbsentItem = isFileAbsent(projectItem.getNode("build"), ".elf");
			boolean fileToBeAbsentItem1 = isFileAbsent(projectItem.getNode("build"), "bootloader");
			if (fileToBeAbsentItem && fileToBeAbsentItem1)
			{
				return true;
			}
			return false;
		}
		return false;
	}

	private static SWTBotTreeItem findTreeItem(SWTBotTreeItem parent, String itemName)
	{
		for (SWTBotTreeItem child : parent.getItems())
		{
			if (child.getText().equals(itemName))
			{
				return child;
			}
			SWTBotTreeItem found = findTreeItem(child, itemName);
			if (found != null)
			{
				return found;
			}
		}
		return null;
	}

	private static boolean isFileAbsent(SWTBotTreeItem parent, String itemName)
	{
		for (SWTBotTreeItem child : parent.getItems())
		{
			if (child.getText().contains(itemName))
			{
				return false; // File found, return false
			}
			if (!isFileAbsent(child, itemName))
			{
				return false; // File found in a child, return false
			}
		}
		return true; // File not found, return true
	}

	public static boolean checkFolderExistanceAfterPythonClean(File folderExists)
	{
		boolean folder = folderExists.exists();
		if (folder == false)
		{
			return true;
		}
		return false;
	}

	public static boolean checkTextEditorContentForPhrase(String phrase, SWTWorkbenchBot bot)
	{
		SWTBotEditor textEditor = bot.activeEditor();
		String editorText = textEditor.toTextEditor().getText();

		return editorText.contains(phrase);
	}

	public static void openProjectNewComponentUsingContextMenu(String projectName, SWTWorkbenchBot bot)
	{
		SWTBotTreeItem projectItem = fetchProjectFromProjectExplorer(projectName, bot);
		if (projectItem != null)
		{
			projectItem.select();
			projectItem.contextMenu("Install New Component").click();
		}
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
		bot.button("Finish").click(); // Finish for the project wizard from eclipse

		bot.textWithLabel("Project name:").setText(projectName);
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
		SWTBotShell shell1 = bot.shell("New IDF Project");
		shell1.activate();
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
	 * Deletes a project
	 *
	 * @param projectName     the name of the tracing project
	 * @param deleteResources whether or not to deleted resources under the project
	 * @param bot             the workbench bot
	 */
	public static void deleteProject(final String projectName, boolean deleteResources, SWTWorkbenchBot bot)
	{
		// Wait for any analysis to complete because it might create
		// supplementary files
		WaitUtils.waitForJobs();
		try
		{
			ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).refreshLocal(IResource.DEPTH_INFINITE,
					null);
		}
		catch (CoreException e)
		{
		}

		WaitUtils.waitForJobs();

		closeSecondaryShells(bot);
		WaitUtils.waitForJobs();

		if (!ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).exists())
		{
			return;
		}

		focusMainWindow(bot.shells());

		final SWTBotView projectViewBot = bot.viewById(IPageLayout.ID_PROJECT_EXPLORER);
		projectViewBot.setFocus();

		SWTBotTree treeBot = projectViewBot.bot().tree();
		SWTBotTreeItem treeItem = treeBot.getTreeItem(projectName);
		SWTBotMenu contextMenu = treeItem.contextMenu("Delete");
		contextMenu.click();

		handleDeleteDialog(deleteResources, bot);
		WaitUtils.waitForJobs();
	}

	/**
	 * Close all non-main shells that are visible.
	 *
	 * @param bot the workbench bot
	 */
	public static void closeSecondaryShells(SWTWorkbenchBot bot)
	{
		SWTBotShell[] shells = bot.shells();
		SWTBotShell mainShell = getMainShell(shells);
		if (mainShell == null)
		{
			return;
		}

		// Close all non-main shell but make sure we don't close an invisible
		// shell such the special "limbo shell" that Eclipse needs to work
		Arrays.stream(shells).filter(shell -> shell != mainShell).filter(s -> !s.widget.isDisposed())
				.filter(SWTBotShell::isVisible)
				.peek(shell -> logger
						.info(MessageFormat.format("Closing lingering shell with title {0}", shell.getText())))
				.forEach(SWTBotShell::close);
	}

	private static void handleDeleteDialog(boolean deleteResources, SWTWorkbenchBot bot)
	{
		SWTBotShell parentShell = bot.shell("Delete Resources");
		if (deleteResources)
		{
			parentShell.setFocus();
			final SWTBotCheckBox checkBox = parentShell.bot().checkBox();
			checkBox.click();
		}

		final SWTBotButton okButton = parentShell.bot().button("OK");
		okButton.click();

		// If the out of sync shell appears, press continue to delete the project
		bot.waitWhile(new DefaultCondition()
		{
			@Override
			public boolean test() throws Exception
			{
				// If no delete resources shells are found, we can assume that the project has been deleted
				boolean deleteShellFound = false;
				for (SWTBotShell shell : bot.shells())
				{
					if (shell.getText().equals("Delete Resources"))
					{
						deleteShellFound = true;
						if (shell.widget != parentShell.widget)
						{
							shell.bot().button("Continue").click();
						}
					}
				}
				return deleteShellFound;
			}

			@Override
			public String getFailureMessage()
			{
				return "Delete Resources shell did not close";
			}
		}, DELETE_PROJECT_TIMEOUT);
	}

	/**
	 * Focus on the main window
	 *
	 * @param shellBots SWTBotShell for all the shells
	 * @return the main shell
	 */
	public static SWTBotShell focusMainWindow(SWTBotShell[] shellBots)
	{
		SWTBotShell mainShell = getMainShell(shellBots);
		if (mainShell != null)
		{
			mainShell.activate();
		}
		return mainShell;
	}

	private static SWTBotShell getMainShell(SWTBotShell[] shellBots)
	{
		SWTBotShell mainShell = null;
		for (SWTBotShell shellBot : shellBots)
		{
			if (shellBot.getText().toLowerCase().contains("eclipse")
					|| shellBot.getText().toLowerCase().contains("workspace"))
			{
				mainShell = shellBot;
			}
		}
		return mainShell;
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
				project.contextMenu("Refresh").click();
				bot.sleep(2000);
				project.contextMenu("Close Project").click();
				bot.sleep(2000);
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
			SWTBotTreeItem[] projectsBotTreeItems = projectExplorerBotView.bot().tree().getAllItems();
			for (SWTBotTreeItem project : projectsBotTreeItems)
			{
				deleteProject(project.getText(), true, bot);
				bot.sleep(2000);
				projectExplorerBotView.show();
			}
		}
		catch (WidgetNotFoundException widgetNotFoundException)
		{
			// logging will be added to show no projects were found
		}
	}

	public static void launchCommandUsingContextMenu(String projectName, SWTWorkbenchBot bot, String contextMenuLabel)
	{
		SWTBotTreeItem projectItem = fetchProjectFromProjectExplorer(projectName, bot);
		if (projectItem != null)
		{
			projectItem.select();
			projectItem.contextMenu(contextMenuLabel).click();
		}
		bot.sleep(3000);
	}

	public static void findInConsole(SWTWorkbenchBot bot, String consoleName, String findText) throws IOException
	{
		SWTBotView consoleView = viewConsole(consoleName, bot);
		consoleView.show();
		consoleView.setFocus();
		TestWidgetWaitUtility.waitUntilViewContains(bot, findText, consoleView, 3000);
	}

	public static boolean checkButtonIsDisabled(SWTWorkbenchBot bot, String buttonLabel) throws IOException
	{
		bot.button(buttonLabel);
		return !bot.button(buttonLabel).isEnabled(); // return false if active
	}

	public static void setTextFieldInShell(SWTWorkbenchBot bot, String shellName, String textWithLabel, String setText)
	{
		bot.shell(shellName).bot().textWithLabel(textWithLabel + ":").setText(setText);
	}

	public static void joinJobByName(String jobName)
	{
		Job[] jobs = Job.getJobManager().find(null);
		@SuppressWarnings("restriction")
		Optional<Job> lookingJob = Stream.of(jobs).filter(job -> job.getName().equals(jobName)).findAny();

		if (lookingJob.isPresent())
		{
			try
			{
				lookingJob.get().join();
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}
}
