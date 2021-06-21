/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.test.operations;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * Class to contain the common operations for the {@link SWTBotTree}
 * 
 * @author Ali Azam Rana
 *
 */
public class SWTBotTreeOperations
{
	/**
	 * Gets all the tree items from the given tree path
	 * 
	 * @param tree        the tree to look in
	 * @param subTreePath sub tree path can contain multiple hierarchies as in file path for example node1/node2/node3
	 * @return all the tree items in the given tree path
	 */
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

	/**
	 * Get the {@link SWTBotTreeItem} from the given tree
	 * 
	 * @param tree     the tree to search in
	 * @param itemPath the complete item path to look for for example node1/node2/itemToLook or only giving the
	 *                 itemToLook will look for the item in the tree root
	 * @return The tree item found or null if nothing is found.
	 */
	public static SWTBotTreeItem getTreeItem(SWTBotTree tree, String itemPath)
	{
		SWTBotTreeItem[] treeItems = null;
		String[] subNodes = itemPath.split("/");
		for (int i = 0; i < subNodes.length - 1; i++)
		{
			treeItems = SWTBotTreeOperations.getTreeItems(tree, subNodes[i]);
			if (treeItems == null)
			{
				return null;
			}
		}

		// Means the item was passed without path lookup directly inside the tree and return accordingly
		if (treeItems == null)
		{
			return tree.getTreeItem(itemPath);
		}

		return Arrays.asList(treeItems).stream().filter(tI -> tI.getText().equals(subNodes[subNodes.length - 1]))
				.findFirst().get();
	}

}
