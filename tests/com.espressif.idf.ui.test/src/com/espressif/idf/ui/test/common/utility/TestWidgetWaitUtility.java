/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.common.utility;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.BoolResult;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.internal.progress.JobInfo;
import org.eclipse.ui.internal.progress.ProgressInfoItem;
import org.eclipse.ui.internal.progress.ProgressView;

/**
 * Utility class to wait for UI operations that may take time longer than 5000ms (Default delay of SWTBot before
 * {@link WidgetNotFoundException} is thrown)
 * 
 * @author Ali Azam Rana
 *
 */
@SuppressWarnings("restriction")
public class TestWidgetWaitUtility
{
	public static void waitForOperationsInProgressToFinishAsync(SWTWorkbenchBot bot)
	{
		bot.viewById(IPageLayout.ID_PROGRESS_VIEW).show();
		ProgressView progressView = (ProgressView) bot.viewById(IPageLayout.ID_PROGRESS_VIEW).getViewReference()
				.getView(true);
		final OperationResponse operationResponse = getOperationResponse();
		bot.waitWhile(new DefaultCondition()
		{
			@Override
			public boolean test() throws Exception
			{
				Display.getDefault().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						progressView.setFocus();
						ProgressInfoItem[] progressInfoItems = progressView.getViewer().getProgressInfoItems();
						operationResponse.itemStillPending = progressInfoItems.length > 0;
					}
				});

				return operationResponse.itemStillPending;
			}

			@Override
			public String getFailureMessage()
			{

				return "Operations taking longer to finish";
			}
		}, 99000000, 3000);
	}

	public static void waitForOperationsInProgressToFinishSync(SWTWorkbenchBot bot)
	{
		bot.viewById(IPageLayout.ID_PROGRESS_VIEW).show();
		ProgressView progressView = (ProgressView) bot.viewById(IPageLayout.ID_PROGRESS_VIEW).getViewReference()
				.getView(true);
		bot.waitWhile(new DefaultCondition()
		{
			@Override
			public boolean test() throws Exception
			{
				return UIThreadRunnable.syncExec(new BoolResult()
				{
					@Override
					public Boolean run()
					{
						progressView.setFocus();
						ProgressInfoItem[] progressInfoItems = progressView.getViewer().getProgressInfoItems();
						for (ProgressInfoItem progressInfoItem : progressInfoItems)
						{
							JobInfo[] jobInfos = progressInfoItem.getJobInfos();
							for (JobInfo jobInfo : jobInfos)
							{
								boolean onlyRefresh = progressInfoItem.getJobNameAndStatus(jobInfo)
										.contains("Refreshing Projects")
										|| progressInfoItem.getJobNameAndStatus(jobInfo).toLowerCase()
												.contains("refresh");
								if (!onlyRefresh)
								{
									return true;
								}
							}
						}
						return false;
					}
				});
			}

			@Override
			public String getFailureMessage()
			{

				return "Operations taking longer to finish";
			}
		}, 99000000, 500);
	}

	/**
	 * Waits until the specified view contains the provided text, the view must contain a styled text
	 * 
	 * @param bot     current SWTWorkBenchBot reference
	 * @param text    the text to check
	 * @param view    the view to check text in
	 * @param timeOut time to wait for in ms before {@link WidgetNotFoundException} is thrown
	 */
	public static void waitUntilViewContains(SWTWorkbenchBot bot, String text, SWTBotView view, long timeOut)
	{
		view.bot().waitUntil(new DefaultCondition()
		{
			@Override
			public boolean test() throws Exception
			{
				view.show();
				view.setFocus();
				String textString = view.bot().styledText().getText();
				return textString.toLowerCase().contains(text.toLowerCase());
			}

			@Override
			public String getFailureMessage()
			{
				return "Text not found!";
			}
		}, timeOut, 3000);
	}

	/**
	 * Waits until the tree contains an item which contains the name in its text
	 * 
	 * @param name        name to look for
	 * @param currentTree current tree to look in
	 * @param bot         current SWTWorkBenchBot reference
	 * @throws Exception
	 */
	public static void waitForTreeItem(final String name, final SWTBotTree currentTree, SWTWorkbenchBot bot)
			throws Exception
	{
		bot.waitUntil(new DefaultCondition()
		{
			public boolean test() throws Exception
			{
				SWTBotTreeItem[] TreeItems = currentTree.getAllItems();
				for (SWTBotTreeItem item : TreeItems)
				{

					String text = item.getText();
					if (text.contains(name))
					{
						return true;
					}
				}
				return false;
			}

			@Override
			public String getFailureMessage()
			{
				return "Connection with " + name + " not visible in view.";
			}
		});
	}

	/**
	 * Waits until the provided view has the tree with an item name given
	 * 
	 * @param name       name of the {@link SWTBotTreeItem} to look for
	 * @param swtBotView SWT view to look in
	 * @param timeout    time to wait in ms before the {@link WidgetNotFoundException} is thrown
	 */
	public static void waitUntilViewContainsTheTreeItemWithName(String name, SWTBotView swtBotView, long timeout)
	{
		swtBotView.bot().waitUntil(new DefaultCondition()
		{
			@Override
			public boolean test() throws Exception
			{
				swtBotView.show();
				swtBotView.setFocus();
				Optional<SWTBotTreeItem> itemLookedUp = Arrays.asList(swtBotView.bot().tree().getAllItems()).stream()
						.filter(project -> project.getText().equals(name)).findFirst();
				return itemLookedUp.isPresent();
			}

			@Override
			public String getFailureMessage()
			{
				return swtBotView.getTitle() + " does not contain the : " + name;
			}
		}, timeout);
	}

	/**
	 * Waits until the provided bot has a dialog with the title visible
	 * 
	 * @param workbenchBot workbench bot
	 * @param dialogTitle  The title of the dialog to look for
	 * @param timeout      Time to wait in ms before throwing {@link WidgetNotFoundException}
	 */
	public static void waitForDialogToAppear(SWTWorkbenchBot workbenchBot, String dialogTitle, long timeout)
	{
		workbenchBot.waitUntil(new DefaultCondition()
		{
			@Override
			public boolean test() throws Exception
			{
				for (SWTBotShell shell : workbenchBot.shells())
				{
					if (shell.getText().contains(dialogTitle))
					{
						return true; // Dialog is now visible
					}
				}
				return false; // Dialog is not yet visible
			}

			@Override
			public String getFailureMessage()
			{
				return "Dialog with title: " + dialogTitle + " did not appear in time.";
			}
		}, timeout);
	}

	/**
	 * Waits while the provided bot has a dialog with the title visible
	 * 
	 * @param workbenchBot workbench bot
	 * @param dialogTitle  The title of the dialog to look for
	 * @param timeout      Time to wait in ms before throwing {@link WidgetNotFoundException}
	 */
	public static void waitWhileDialogIsVisible(SWTWorkbenchBot workbenchBot, String dialogTitle, long timeout)
	{
		workbenchBot.waitWhile(new DefaultCondition()
		{
			@Override
			public boolean test() throws Exception
			{
				for (SWTBotShell shell : workbenchBot.shells())
				{
					if (shell.getText().contains(dialogTitle))
					{
						return true;
					}
				}
				return false;
			}

			@Override
			public String getFailureMessage()
			{
				return "Dialog with title: " + dialogTitle + " did not close in time.";
			}
		}, timeout);
	}

	public static void waitForCTabToAppear(SWTWorkbenchBot workbenchBot, String tabName, long timeout)
	{
		workbenchBot.waitUntil(new DefaultCondition()
		{
			@Override
			public boolean test() throws Exception
			{
				try
				{
					return workbenchBot.cTabItem(tabName) != null;
				}
				catch (WidgetNotFoundException e)
				{
					return false;
				}
			}

			@Override
			public String getFailureMessage()
			{
				return "Tab '" + tabName + "' did not open in time.";
			}
		}, timeout);
	}

	private static OperationResponse getOperationResponse()
	{
		TestWidgetWaitUtility testWidgetWaitUtility = new TestWidgetWaitUtility();
		return testWidgetWaitUtility.getOperationResponseObject();
	}

	private OperationResponse getOperationResponseObject()
	{
		return new OperationResponse();
	}

	private class OperationResponse
	{
		private boolean itemStillPending = true;
	}
}
