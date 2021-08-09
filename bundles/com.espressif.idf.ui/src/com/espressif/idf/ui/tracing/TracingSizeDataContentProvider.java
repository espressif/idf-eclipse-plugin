/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.tracing;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider class for tracing data in details view table
 * 
 * @author Ali Azam Rana
 *
 */
public class TracingSizeDataContentProvider implements ITreeContentProvider
{
	public Object[] getChildren(Object parentElement)
	{
		if (parentElement instanceof List)
			return ((List<?>) parentElement).toArray();
		if (parentElement instanceof DetailsVO)
			return null;
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
		if (element instanceof DetailsVO)
			return false;
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
