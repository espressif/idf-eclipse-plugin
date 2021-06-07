package com.espressif.idf.tests.common;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public class TestAssertUtility
{
	public static boolean treeContainsItem(String nameOfItemToCheck, String subTreeName, SWTBotTree treeToCheckIn) throws Exception 
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
