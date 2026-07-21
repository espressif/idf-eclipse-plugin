/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.operations;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
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
	
	private static final String DEFAULT_FLASH_WAIT_PROPERTY = "default.project.flash.wait";

	private static final String CDT_PERSPECTIVE_ID = "org.eclipse.cdt.ui.CPerspective";

	private static final String DEBUG_PERSPECTIVE_ID = "org.eclipse.debug.ui.DebugPerspective";

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
		try {
		TestWidgetWaitUtility.waitUntilViewContains(bot, "Build complete", consoleView,
				DefaultPropertyFetcher.getLongPropertyValue(DEFAULT_PROJECT_BUILD_WAIT_PROPERTY, 300000));
		} catch (Exception e) {
			throw new AssertionError("Project Build failed", e);
		}
	}

	public static void waitForProjectFlash(SWTWorkbenchBot bot) throws IOException
	{
		SWTBotView view = bot.viewByPartName("Console");
		view.setFocus();
		TestWidgetWaitUtility.waitUntilViewContains(bot, "Hard resetting via RTS pin...", view,
				DefaultPropertyFetcher.getLongPropertyValue(DEFAULT_FLASH_WAIT_PROPERTY, 120000));
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
		String regex = ".*" + Pattern.quote(consoleType) + "( \\[.*\\])?.*";
		org.hamcrest.Matcher<MenuItem> withRegex = WidgetMatcherFactory.withRegex(regex);
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

	/**
	 * Starts debugging via Project Explorer context menu: Debug As → Debug Configurations...,
	 * selects the ESP-IDF OpenOCD debug config, clicks Debug, and accepts the perspective switch if prompted.
	 *
	 * @param projectName project whose debug configuration should be launched
	 * @param bot         current SWT bot reference
	 */
	public static void startDebuggingUsingContextMenu(String projectName, SWTWorkbenchBot bot)
	{
		SWTBotTreeItem projectItem = fetchProjectFromProjectExplorer(projectName, bot);
		if (projectItem == null)
		{
			throw new WidgetNotFoundException("Project not found in Project Explorer: " + projectName);
		}

		projectItem.select();
		projectItem.contextMenu("Debug As").menu("Debug Configurations...").click();

		TestWidgetWaitUtility.waitForDialogToAppear(bot, "Debug Configurations", 10000);

		bot.tree().getTreeItem("ESP-IDF GDB OpenOCD Debugging").select();
		bot.tree().getTreeItem("ESP-IDF GDB OpenOCD Debugging").expand();

		String primaryDebugConfig = projectName + " Debug";
		String fallbackDebugConfig = projectName + " Configuration";
		try
		{
			bot.tree().getTreeItem("ESP-IDF GDB OpenOCD Debugging").getNode(primaryDebugConfig).select();
		}
		catch (WidgetNotFoundException e)
		{
			try
			{
				bot.tree().getTreeItem("ESP-IDF GDB OpenOCD Debugging").getNode(fallbackDebugConfig).select();
			}
			catch (WidgetNotFoundException e2)
			{
				// Last resort: use the first child config under the OpenOCD type.
				bot.tree().getTreeItem("ESP-IDF GDB OpenOCD Debugging").getNode(0).select();
			}
		}

		bot.waitUntil(Conditions.widgetIsEnabled(bot.button("Debug")), 5000);
		bot.button("Debug").click();
		// Dialog usually appears later, when GDB suspends — also handled in waitForDebugSessionStarted.
		acceptDebugPerspectiveSwitchIfPresent(bot, 5000);
	}

	/**
	 * Accepts the Eclipse "Confirm Perspective Switch" dialog when it appears after the debug
	 * session suspends. Checks "Remember my decision" so CI is less likely to see it again.
	 *
	 * @param bot     current SWT bot reference
	 * @param timeout how long to wait for the dialog in milliseconds
	 * @return {@code true} if the dialog was found and dismissed
	 */
	public static boolean acceptDebugPerspectiveSwitchIfPresent(SWTWorkbenchBot bot, long timeout)
	{
		try
		{
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Confirm Perspective Switch", timeout);
			SWTBotShell shell = bot.shell("Confirm Perspective Switch");
			shell.activate();
			shell.setFocus();

			try
			{
				SWTBotCheckBox remember = shell.bot().checkBox("Remember my decision");
				if (!remember.isChecked())
				{
					remember.click();
				}
			}
			catch (WidgetNotFoundException ignored)
			{
			}

			try
			{
				shell.bot().button("Switch").click();
			}
			catch (WidgetNotFoundException e)
			{
				shell.bot().button("Yes").click();
			}

			// Give the Debug perspective time to finish opening before further toolbar clicks.
			bot.sleep(2000);
			return true;
		}
		catch (Exception ignored)
		{
			// Perspective switch may already be remembered / suppressed.
			return false;
		}
	}

	/**
	 * @see #acceptDebugPerspectiveSwitchIfPresent(SWTWorkbenchBot, long)
	 */
	public static void acceptDebugPerspectiveSwitchIfPresent(SWTWorkbenchBot bot)
	{
		acceptDebugPerspectiveSwitchIfPresent(bot, 30000);
	}

	/**
	 * Waits until GDB has suspended at {@code app_main} (not merely OpenOCD "Target halted" during
	 * reset) and opens the Debug perspective. Does not require the Step Over toolbar button —
	 * that control is often missing/unreliable under SWTBot even when the session is suspended.
	 * <p>
	 * Prefers the Eclipse debug model over console-page switching. Repeatedly opening
	 * "Display Selected Console" leaves the dropdown open and stalls the UI under SWTBot.
	 *
	 * @param bot current SWT bot reference
	 * @throws IOException if property lookup fails
	 */
	public static void waitForDebugSessionStarted(SWTWorkbenchBot bot) throws IOException
	{
		// Prefer a bounded debug timeout; the shared flash wait property is often hours-long.
		long timeout = Math.min(
				DefaultPropertyFetcher.getLongPropertyValue(DEFAULT_FLASH_WAIT_PROPERTY, 120000),
				180000);
		long deadline = System.currentTimeMillis() + timeout;
		boolean perspectiveHandled = false;

		while (System.currentTimeMillis() < deadline)
		{
			if (!perspectiveHandled)
			{
				perspectiveHandled = acceptDebugPerspectiveSwitchIfPresent(bot, 1000);
			}

			// Do not flip Console pages while polling — that opens a sticky dropdown menu.
			String consoleText = readVisibleConsoleText(bot);

			if (consoleText.contains("shutdown command invoked")
					|| consoleText.contains("dropped 'gdb' connection"))
			{
				throw new AssertionError(
						"Debug session shut down before a breakpoint suspend was observed.\nConsole:\n"
								+ consoleText);
			}

			if (isSuspendedAtBreakpoint(bot, consoleText))
			{
				if (!perspectiveHandled)
				{
					perspectiveHandled = acceptDebugPerspectiveSwitchIfPresent(bot, 30000);
				}
				openDebugPerspective(bot);
				if (!hasActiveLaunch())
				{
					throw new AssertionError(
							"Debug launch terminated immediately after suspend at app_main.\nConsole:\n"
									+ consoleText);
				}
				return;
			}

			bot.sleep(1000);
		}

		String lastConsole = readVisibleConsoleText(bot);
		throw new AssertionError(
				"Debug session did not suspend at app_main within timeout (debug model or visible console).\nLast console text:\n"
						+ lastConsole);
	}

	/**
	 * Reads text from the Console view page that is currently visible (no console-page switching).
	 * Switching via "Display Selected Console" leaves a sticky dropdown open under SWTBot and
	 * stalls the debug wait loop.
	 */
	public static String readVisibleConsoleText(SWTWorkbenchBot bot)
	{
		try
		{
			SWTBotView view = bot.viewByPartName("Console");
			view.show();
			view.setFocus();
			String text = view.bot().styledText().getText();
			return text != null ? text : "";
		}
		catch (Exception e)
		{
			logger.debug("Could not read visible Console view: {}", e.getMessage());
			return "";
		}
	}

	/**
	 * Reads console text used for debug assertions from the currently visible Console page only.
	 */
	public static String readDebugRelatedConsoleText(SWTWorkbenchBot bot)
	{
		return readVisibleConsoleText(bot);
	}

	private static boolean isSuspendedAtBreakpoint(SWTWorkbenchBot bot, String consoleText)
	{
		// Prefer the debug model only while polling. Expanding the Debug view tree every
		// second can leave SWTBot stuck if a Surefire timeout interrupts mid-expand.
		return isSuspendedAtAppMainInDebugModel() || isSuspendedAtBreakpointInConsole(consoleText);
	}

	private static boolean isSuspendedAtBreakpointInConsole(String consoleText)
	{
		if (consoleText == null || consoleText.isEmpty())
		{
			return false;
		}
		return consoleText.contains("hit Temporary breakpoint")
				|| consoleText.contains("hit Breakpoint")
				|| consoleText.contains("hit breakpoint")
				|| (consoleText.contains("Temporary breakpoint") && consoleText.contains("app_main"));
	}

	/**
	 * True when an active debug target has a suspended thread whose stack includes {@code app_main}.
	 * Prefer this over OpenOCD console text — GDB often suspends in the UI without printing
	 * {@code hit Temporary breakpoint} on the IDF Process Console.
	 */
	public static boolean isSuspendedAtAppMainInDebugModel()
	{
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches = launchManager.getLaunches();
		if (launches == null)
		{
			return false;
		}

		for (ILaunch launch : launches)
		{
			if (launch == null || launch.isTerminated())
			{
				continue;
			}

			IDebugTarget[] targets = launch.getDebugTargets();
			if (targets == null)
			{
				continue;
			}

			for (IDebugTarget target : targets)
			{
				if (target == null || target.isTerminated())
				{
					continue;
				}

				try
				{
					if (!target.hasThreads())
					{
						continue;
					}
					for (IThread thread : target.getThreads())
					{
						if (thread == null || !thread.isSuspended() || !thread.hasStackFrames())
						{
							continue;
						}
						for (IStackFrame frame : thread.getStackFrames())
						{
							if (frame == null)
							{
								continue;
							}
							String name = frame.getName();
							if (name != null && name.toLowerCase(Locale.ENGLISH).contains("app_main"))
							{
								return true;
							}
						}
					}
				}
				catch (DebugException e)
				{
					logger.debug("Could not inspect debug model for app_main suspend: {}", e.getMessage());
				}
			}
		}
		return false;
	}

	/**
	 * Performs Step Over using the Debug toolbar button, or Project Explorer context menu
	 * {@code Step Over} on the project. Does not use keyboard shortcuts.
	 *
	 * @param projectName project to use for the context-menu fallback
	 * @param bot         current SWT bot reference
	 */
	public static void performDebugStepOver(String projectName, SWTWorkbenchBot bot)
	{
		acceptDebugPerspectiveSwitchIfPresent(bot, 2000);
		openDebugPerspective(bot);

		if (!hasActiveLaunch())
		{
			throw new AssertionError("Cannot Step Over — no active debug launch");
		}

		final SWTWorkbenchBot workbenchBot = bot;
		workbenchBot.waitUntil(new DefaultCondition()
		{
			@Override
			public boolean test() throws Exception
			{
				if (!hasActiveLaunch())
				{
					throw new AssertionError(
							"Debug launch terminated before Step Over became available (OpenOCD/GDB already stopped)");
				}
				// Ready when the thread can step, or the toolbar button is already visible.
				return canStepOverInDebugModel()
						|| isToolbarButtonPresent(workbenchBot, "Step Over (F6)")
						|| isToolbarButtonPresent(workbenchBot, "Step Over");
			}

			@Override
			public String getFailureMessage()
			{
				return "Debug Step Over not ready — debug session may have terminated or is not suspended";
			}
		}, 30000, 500);

		if (clickToolbarStepOver(bot))
		{
			bot.sleep(3000);
			return;
		}

		if (clickProjectContextMenuStepOver(projectName, bot))
		{
			bot.sleep(3000);
			return;
		}

		throw new AssertionError(
				"Failed to perform Step Over via toolbar button or Project Explorer context menu");
	}

	private static boolean clickToolbarStepOver(SWTWorkbenchBot bot)
	{
		try
		{
			bot.toolbarButtonWithTooltip("Step Over (F6)").click();
			return true;
		}
		catch (WidgetNotFoundException ignored)
		{
		}

		try
		{
			bot.toolbarButtonWithTooltip("Step Over").click();
			return true;
		}
		catch (WidgetNotFoundException ignored)
		{
			return false;
		}
	}

	private static boolean clickProjectContextMenuStepOver(String projectName, SWTWorkbenchBot bot)
	{
		try
		{
			SWTBotTreeItem projectItem = fetchProjectFromProjectExplorer(projectName, bot);
			if (projectItem == null)
			{
				return false;
			}
			projectItem.select();
			try
			{
				projectItem.contextMenu("Step Over").click();
				return true;
			}
			catch (WidgetNotFoundException e)
			{
				projectItem.contextMenu("Step Over (F6)").click();
				return true;
			}
		}
		catch (Exception e)
		{
			logger.debug("Project context menu Step Over failed: {}", e.getMessage());
			return false;
		}
	}

	private static boolean canStepOverInDebugModel()
	{
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches = launchManager.getLaunches();
		if (launches == null)
		{
			return false;
		}

		for (ILaunch launch : launches)
		{
			if (launch == null || launch.isTerminated())
			{
				continue;
			}
			IDebugTarget[] targets = launch.getDebugTargets();
			if (targets == null)
			{
				continue;
			}
			for (IDebugTarget target : targets)
			{
				if (target == null || target.isTerminated())
				{
					continue;
				}
				try
				{
					if (!target.hasThreads())
					{
						continue;
					}
					for (IThread thread : target.getThreads())
					{
						if (thread != null && thread.isSuspended() && thread.canStepOver())
						{
							return true;
						}
					}
				}
				catch (DebugException e)
				{
					logger.debug("canStepOverInDebugModel: {}", e.getMessage());
				}
			}
		}
		return false;
	}

	private static boolean isToolbarButtonPresent(SWTWorkbenchBot bot, String tooltip)
	{
		try
		{
			bot.toolbarButtonWithTooltip(tooltip);
			return true;
		}
		catch (WidgetNotFoundException e)
		{
			return false;
		}
	}

	/**
	 * Opens the Eclipse Debug perspective via Platform UI API (no menus/dialogs).
	 *
	 * @param bot current SWT bot reference (may be {@code null})
	 */
	public static void openDebugPerspective(SWTWorkbenchBot bot)
	{
		try
		{
			UIThreadRunnable.syncExec(new VoidResult()
			{
				@Override
				public void run()
				{
					IWorkbench workbench = PlatformUI.getWorkbench();
					IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
					if (window == null || window.getActivePage() == null)
					{
						return;
					}
					IPerspectiveDescriptor descriptor = workbench.getPerspectiveRegistry()
							.findPerspectiveWithId(DEBUG_PERSPECTIVE_ID);
					if (descriptor != null)
					{
						window.getActivePage().setPerspective(descriptor);
					}
				}
			});
			if (bot != null)
			{
				bot.sleep(1000);
			}
		}
		catch (Exception e)
		{
			logger.warn("Failed to open Debug perspective", e);
		}
	}

	/**
	 * Returns {@code true} if an active (non-terminated) Eclipse launch still exists.
	 */
	public static boolean hasActiveLaunch()
	{
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches = launchManager.getLaunches();
		if (launches == null)
		{
			return false;
		}
		for (ILaunch launch : launches)
		{
			if (launch != null && !launch.isTerminated())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Stops the active launch / debug session using the Launch Bar Stop button.
	 *
	 * @param bot current SWT bot reference
	 */
	public static void stopLaunchUsingLaunchBar(SWTWorkbenchBot bot)
	{
		try
		{
			bot.toolbarButtonWithTooltip("Stop").click();
			bot.sleep(2000);
		}
		catch (WidgetNotFoundException e)
		{
			logger.warn("Stop button not found while trying to stop launch/debug session");
		}
	}

	/**
	 * Best-effort cleanup of an active debug session via the debug API and process kill.
	 * Avoids clicking Launch Bar Stop / Debug Terminate toolbars — those tooltips are ambiguous
	 * under SWTBot and can race with an active session during perspective changes.
	 *
	 * @param bot current SWT bot reference (may be {@code null} if UI is unavailable)
	 */
	public static void stopDebugSessionAndKillProcesses(SWTWorkbenchBot bot)
	{
		try
		{
			terminateAllLaunches();
		}
		catch (Exception e)
		{
			logger.warn("Failed to terminate Eclipse launches during debug cleanup", e);
		}

		killDebugProcesses();
	}

	/**
	 * Switches the workbench back to the C/C++ perspective via the Platform UI API.
	 * Avoids Window → Perspective menus / "Open Perspective" dialogs, which can leave a
	 * modal shell open under SWTBot and block {@code @AfterClass} cleanup.
	 *
	 * @param bot current SWT bot reference (unused; kept for call-site consistency)
	 */
	public static void openCCppPerspective(SWTWorkbenchBot bot)
	{
		try
		{
			UIThreadRunnable.syncExec(new VoidResult()
			{
				@Override
				public void run()
				{
					IWorkbench workbench = PlatformUI.getWorkbench();
					IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
					if (window == null)
					{
						return;
					}
					IWorkbenchPage page = window.getActivePage();
					if (page == null)
					{
						return;
					}
					IPerspectiveDescriptor descriptor = workbench.getPerspectiveRegistry()
							.findPerspectiveWithId(CDT_PERSPECTIVE_ID);
					if (descriptor != null)
					{
						page.setPerspective(descriptor);
					}
				}
			});
			if (bot != null)
			{
				closeSecondaryShells(bot);
				focusMainWindow(bot.shells());
			}
		}
		catch (Exception e)
		{
			logger.warn("Failed to switch back to C/C++ perspective", e);
		}
	}

	/**
	 * Force-cleans workbench state after a debug test (including timeout/failure).
	 * Uses only Platform/debug APIs — no menus, perspective dialogs, or
	 * {@code WaitUtils.waitForJobs()} — so cleanup cannot hang the Surefire session
	 * and poison later UI tests.
	 *
	 * @param bot current SWT bot reference (may be {@code null})
	 */
	public static void forceCleanWorkbenchAfterDebugTest(SWTWorkbenchBot bot)
	{
		try
		{
			terminateAllLaunches();
		}
		catch (Exception e)
		{
			logger.warn("forceClean: terminateAllLaunches failed", e);
		}

		killDebugProcesses();

		try
		{
			openCCppPerspective(bot);
		}
		catch (Exception e)
		{
			logger.warn("forceClean: openCCppPerspective failed", e);
		}

		try
		{
			if (bot != null)
			{
				closeSecondaryShells(bot);
				focusMainWindow(bot.shells());
			}
		}
		catch (Exception e)
		{
			logger.warn("forceClean: closeSecondaryShells failed", e);
		}

		try
		{
			closeAllEditorsViaApi();
		}
		catch (Exception e)
		{
			logger.warn("forceClean: closeAllEditorsViaApi failed", e);
		}

		try
		{
			deleteAllProjectsViaWorkspaceApi();
		}
		catch (Exception e)
		{
			logger.warn("forceClean: deleteAllProjectsViaWorkspaceApi failed", e);
		}

		killDebugProcesses();
	}

	/**
	 * Closes all open editors without prompting (UI-thread API).
	 */
	public static void closeAllEditorsViaApi()
	{
		UIThreadRunnable.syncExec(new VoidResult()
		{
			@Override
			public void run()
			{
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window == null || window.getActivePage() == null)
				{
					return;
				}
				window.getActivePage().closeAllEditors(false);
			}
		});
	}

	/**
	 * Deletes every workspace project via the resources API (no Project Explorer UI,
	 * no {@code WaitUtils.waitForJobs()}). Safe for {@code @AfterClass} cleanup.
	 */
	public static void deleteAllProjectsViaWorkspaceApi()
	{
		UIThreadRunnable.syncExec(new VoidResult()
		{
			@Override
			public void run()
			{
				IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
				if (projects == null)
				{
					return;
				}
				for (IProject project : projects)
				{
					if (project == null || !project.exists())
					{
						continue;
					}
					try
					{
						if (project.isOpen())
						{
							project.close(null);
						}
					}
					catch (CoreException e)
					{
						logger.debug("Could not close project {}: {}", project.getName(), e.getMessage());
					}
					try
					{
						project.delete(true, true, null);
					}
					catch (CoreException e)
					{
						logger.warn("Could not delete project {}: {}", project.getName(), e.getMessage());
					}
				}
			}
		});
	}

	/**
	 * Terminates every non-terminated launch registered with the Eclipse debug framework.
	 */
	public static void terminateAllLaunches()
	{
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches = launchManager.getLaunches();
		if (launches == null)
		{
			return;
		}

		for (ILaunch launch : launches)
		{
			if (launch == null || launch.isTerminated())
			{
				continue;
			}
			try
			{
				launch.terminate();
			}
			catch (DebugException e)
			{
				logger.warn("Failed to terminate launch: " + launch, e);
			}
		}
	}

	/**
	 * Force-terminates OpenOCD and ESP GDB processes left behind by a debug session.
	 * Mirrors the VS Code UI-test {@code killDebugProcesses} helper. Exit status from
	 * {@code pkill} when no process matches is ignored.
	 */
	public static void killDebugProcesses()
	{
		String[] patterns = new String[] { "openocd", "xtensa-esp.*-gdb", "riscv32-esp.*-gdb" };
		for (String pattern : patterns)
		{
			try
			{
				Process process = new ProcessBuilder("pkill", "-f", pattern).redirectErrorStream(true).start();
				process.waitFor(5, TimeUnit.SECONDS);
			}
			catch (Exception e)
			{
				logger.debug("pkill for pattern '{}' skipped or failed: {}", pattern, e.getMessage());
			}
		}

		try
		{
			Thread.sleep(1500);
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
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

	public static void openMainFileInTextEditorUsingContextMenu(String projectName, SWTWorkbenchBot bot)
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
				SWTBotTreeItem fileToOpenItem = findTreeItem(projectItem.getNode("main"), "main.c");

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

	public static boolean checkExactMatchInTextEditor(String phrase, SWTWorkbenchBot bot)
	{
		SWTBotEditor textEditor = bot.activeEditor();
		String editorText = textEditor.toTextEditor().getText();

		// Normalize line endings
		String normalizedEditorText = editorText.replace("\r\n", "\n").trim();
		String normalizedPhrase = phrase.replace("\r\n", "\n").trim();

		// Trim leading and trailing spaces for each line to ignore indentation differences
		normalizedEditorText = normalizeWhitespace(normalizedEditorText);
		normalizedPhrase = normalizeWhitespace(normalizedPhrase);

		return normalizedEditorText.equals(normalizedPhrase);
	}

	private static String normalizeWhitespace(String text)
	{
		return Arrays.stream(text.split("\n")).map(String::trim) // Trim each line
				.collect(Collectors.joining("\n")); // Reconstruct text with normalized lines
	}

	public static boolean checkExactMatchInTextEditorwithWhiteSpaces(String phrase, SWTWorkbenchBot bot)
	{
		// Get the text from the active editor
		SWTBotEditor textEditor = bot.activeEditor();
		String editorText = textEditor.toTextEditor().getText();

		// Normalize line endings to a consistent format (e.g., using '\n')
		String normalizedEditorText = normalizeLineEndings(editorText);
		String normalizedPhrase = normalizeLineEndings(phrase);

		// Check for exact match, including spaces, tabs, and newlines
		return normalizedEditorText.equals(normalizedPhrase);
	}

	/**
	 * Normalizes line endings to '\n' to ensure consistency across platforms.
	 * 
	 * @param input The input string to normalize.
	 * @return The normalized string with consistent line endings.
	 */
	private static String normalizeLineEndings(String input)
	{
		// Normalize to '\n' for consistency across platforms (Mac, Linux, Windows)
		return input.replace("\r\n", "\n").replace("\r", "\n");
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
		bot.button("Next >").click();
		bot.activeShell().activate();

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
		bot.button("Next >").click();

		bot.activeShell().activate();
		bot.checkBox("Run idf.py reconfigure after project creation to initialize the CMake build configuration")
				.click();
		bot.textWithLabel("Project name:").setText(projectName);
		bot.button("Finish").click();
		TestWidgetWaitUtility.waitUntilViewContainsTheTreeItemWithName(projectName, bot.viewByTitle("Project Explorer"),
				5000);
	}

	/**
	 * Set up a project
	 * 
	 * @param projectName name of the project
	 * @param category    category of the project
	 * @param subCategory sub category of the project
	 * @param bot         current SWT bot reference
	 */
	public static void setupProjectWithReconfigureCommand(String projectName, String category, String subCategory,
			SWTWorkbenchBot bot)
	{
		bot.shell().activate().bot().menu("File").menu("New").menu("Project...").click();
		SWTBotShell shell = bot.shell("New Project");
		shell.activate();

		bot.tree().expandNode(category).select(subCategory);
		bot.button("Next >").click();

		bot.activeShell().activate();
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
		WaitUtils.waitForJobs();
	}

	public static void findInConsole(SWTWorkbenchBot bot, String consoleName, String findText) throws IOException
	{
		SWTBotView consoleView = viewConsole(consoleName, bot);
		consoleView.show();
		consoleView.setFocus();
		TestWidgetWaitUtility.waitUntilViewContains(bot, findText, consoleView, 30000);
	}

	public static boolean checkShellContent(SWTWorkbenchBot bot, String shellName, String expectedText)
	{
		SWTBotShell shell = bot.shell(shellName);
		shell.activate();
		SWTBotLabel label = bot.label(expectedText);
		String actualText = label.getText();
		return expectedText.equals(actualText);
	}

	public static boolean checkPartitionTableContent(SWTWorkbenchBot bot)
	{
		String[] builtInPartitionArray = { "nvs", "phy_init", "factory", "data", "data", "app", "nvs", "phy", "factory",
				"0x9000", "0xf000", "0x10000", "0x6000", "0x1000", "1M", "", "", "" };
		int builtInIndex = 0;
		SWTBotTable table = bot.table();
		int columns = table.columnCount();
		int rows = table.rowCount();
		if (columns != 6 && rows != 3)
		{
			return false;
		}
		for (int col = 0; col < columns; col++)
		{
			for (int row = 0; row < rows; row++)
			{
				String tableContent = table.cell(row, col);

				if (!builtInPartitionArray[builtInIndex].equals(tableContent))
				{
					return false;
				}
				builtInIndex++;
			}
		}
		return true;
	}

	public static boolean comparePartitionTableRows(SWTWorkbenchBot bot, int expectedDifference) throws IOException
	{
		SWTBotTable table = bot.table();
		int defaultRows = 3;
		int actualRows = table.rowCount();
		return (actualRows - defaultRows) == expectedDifference;
	}

	public static void deletePartitionTableRow(SWTWorkbenchBot bot) throws IOException
	{
		SWTBotTable table = bot.table();
		table.select(1);
		bot.toolbarButton("Delete Selected").click();
		bot.button("OK").click();
	}

	
	public static void verifyTheConsoleOutput(SWTWorkbenchBot bot, String text) throws IOException
	{
		SWTBotView view = bot.viewByPartName("Console");
		view.setFocus();
		TestWidgetWaitUtility.waitUntilViewContains(bot, text, view,
				DefaultPropertyFetcher.getLongPropertyValue(DEFAULT_FLASH_WAIT_PROPERTY, 120000));
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
