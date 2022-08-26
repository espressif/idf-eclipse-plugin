/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.tracing;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Content provider for the tree viewer used in consolidated callers view
 * 
 * @author Ali Azam Rana
 *
 */
public class ConsolidatedCallersViewContentProvider implements ITreeContentProvider
{

	@Override
	public Object[] getElements(Object inputElement)
	{
		return (TreeItem[]) inputElement;
	}

	@Override
	public Object[] getChildren(Object parentElement)
	{
		TreeItem consolidatedCallersVO = (TreeItem) parentElement;
		return consolidatedCallersVO.getItems();
	}

	@Override
	public Object getParent(Object element)
	{
		TreeItem consolidatedCallersVO = (TreeItem) element;
		return consolidatedCallersVO.getParentItem();
	}

	@Override
	public boolean hasChildren(Object element)
	{
		TreeItem consolidatedCallersVO = (TreeItem) element;
		if (consolidatedCallersVO.getItemCount() != 0)
		{
			return true;
		}

		return false;
	}
}
