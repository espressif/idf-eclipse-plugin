package com.espressif.idf.tests.operations;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public class SWTBotTreeOperationsUtility
{
	public static SWTBotTreeItem[] getTreeItems(SWTBotTree tree, String subTreePath)
	{
		SWTBotTreeItem[] treeItems = tree.getAllItems();
		String[] subNodes = subTreePath.split("/");
		for (String subNode : subNodes)
		{
			Optional<SWTBotTreeItem> treeItemOptional = Arrays.asList(treeItems).stream()
					.filter(treeItem -> treeItem.getText().equals(subNode)).findFirst();
			if (treeItemOptional.isPresent())
			{
				treeItemOptional.get().expand();
				treeItems = treeItemOptional.get().getItems();
			}
			else
			{
				return null;
			}
		}

		return treeItems;
	}
	
}
