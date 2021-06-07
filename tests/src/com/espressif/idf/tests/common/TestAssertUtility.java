package com.espressif.idf.tests.common;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public class TestAssertUtility
{
	/**
	 * Checks if the provided SWTBotTree contains the item with provided name
	 * @param nameOfItemToCheck name of the item to check in tree
	 * @param subTreeName sub tree name if any else with use the treeToCheckIn
	 * @param treeToCheckIn main tree object to lookup the item
	 * @return true if found false if not
	 */
	public static boolean treeContainsItem(String nameOfItemToCheck, String subTreeName, SWTBotTree treeToCheckIn)
	{
		SWTBotTreeItem[] treeItems = null;
		if (StringUtils.isNotEmpty(subTreeName))
		{
			treeItems = treeToCheckIn.getTreeItem(subTreeName).getItems();
		}
		else
		{
			treeItems = treeToCheckIn.getAllItems();
		}
		 
		for (SWTBotTreeItem item : treeItems)
		{
			String text = item.getText();
			if (text.contains(nameOfItemToCheck))
			{
				return true;
			}
		}
		return false;
	}
}
