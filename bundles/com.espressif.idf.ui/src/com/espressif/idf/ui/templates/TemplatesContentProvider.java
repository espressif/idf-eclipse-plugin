/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.templates;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class TemplatesContentProvider implements ITreeContentProvider
{
	private static Object[] EMPTY_ARRAY = new Object[0];

	/*
	 * @see IContentProvider#dispose()
	 */
	@Override
	public void dispose()
	{
	}

	@Override
	public Object[] getChildren(Object parentElement)
	{
		if (parentElement instanceof ITemplateNode)
		{
			ITemplateNode element = (ITemplateNode) parentElement;

			List<ITemplateNode> children = element.getChildren();
			return children.toArray(new ITemplateNode[children.size()]);
		}
		return EMPTY_ARRAY;
	}

	/*
	 * @see ITreeContentProvider#getParent(Object)
	 */
	@Override
	public Object getParent(Object element)
	{
		if (element instanceof ITemplateNode)
		{
			return ((ITemplateNode) element).getParent();
		}
		return null;
	}

	/*
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	@Override
	public boolean hasChildren(Object element)
	{
		if (element instanceof ITemplateNode)
		{
			return ((ITemplateNode) element).getChildren().size() > 0;
		}

		return false;
	}

	/*
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	@Override
	public Object[] getElements(Object inputElement)
	{
		return getChildren(inputElement);
	}

}
