/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFSizeDataContentProvider implements ITreeContentProvider
{
	public Object[] getChildren(Object parentElement)
	{
		if (parentElement instanceof List)
			return ((List<?>) parentElement).toArray();
		if (parentElement instanceof IDFSizeData)
			return ((IDFSizeData) parentElement).getChildren().toArray();
		return new Object[0];
	}

	public Object getParent(Object element)
	{
		return null;
	}

	public boolean hasChildren(Object element)
	{
		if (element instanceof List)
			return ((List<?>) element).size() > 0;
		if (element instanceof IDFSizeData)
			return ((IDFSizeData) element).getChildren().size() > 0;
		return false;
	}

	public Object[] getElements(Object cities)
	{
		return getChildren(cities);
	}

	public void dispose()
	{
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}
}
