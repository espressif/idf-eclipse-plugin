/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.test.common.utility;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import com.espressif.idf.test.operations.SWTBotTreeOperations;

/**
 * Utility class for tests to perform assertions on different objects of SWTBot
 * 
 * @author Ali Azam Rana
 *
 */
public class TestAssertUtility
{
	/**
	 * Checks if the provided SWTBotTree contains the item with provided name
	 * 
	 * @param nameOfItemToCheck name of the item to check in tree
	 * @param subTreeName       sub tree name if any else with use the treeToCheckIn you can provide a path like a
	 *                          directory structure separated by / for example src/main.c
	 * @param treeToCheckIn     main tree object to lookup the item
	 * @return true if found false if not
	 */
	public static boolean treeContainsItem(String nameOfItemToCheck, String subTreeName, SWTBotTree treeToCheckIn)
	{
		SWTBotTreeItem[] treeItems = null;

		if (StringUtils.isNotEmpty(subTreeName))
		{
			treeItems = SWTBotTreeOperations.getTreeItems(treeToCheckIn, subTreeName);
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
