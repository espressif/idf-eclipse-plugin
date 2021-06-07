package com.espressif.idf.tests.common;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public class TestWidgetWaitUtility
{
	public static void waitForConnection(final String name, final SWTBotTree currentTree, SWTWorkbenchBot bot) throws Exception 
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
}
