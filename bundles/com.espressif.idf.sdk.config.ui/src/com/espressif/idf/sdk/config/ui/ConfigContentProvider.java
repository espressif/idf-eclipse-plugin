/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.espressif.idf.sdk.config.core.IJsonServerConfig;
import com.espressif.idf.sdk.config.core.KConfigMenuItem;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ConfigContentProvider extends TreeNodeContentProvider
{
	private static Object[] EMPTY_ARRAY = new Object[0];
	protected TreeViewer viewer;

	/*
	 * @see IContentProvider#dispose()
	 */
	public void dispose()
	{
	}

	/*
	 * @see IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	/**
	 * Notifies this content provider that the given viewer's input has been switched to a different element.
	 * <p>
	 * A typical use for this method is registering the content provider as a listener to changes on the new input
	 * (using model-specific means), and deregistering the viewer from the old input. In response to these change
	 * notifications, the content provider propagates the changes to the viewer.
	 * </p>
	 *
	 * @param viewer   the viewer
	 * @param oldInput the old input element, or <code>null</code> if the viewer did not previously have an input
	 * @param newInput the new input element, or <code>null</code> if the viewer does not have an input
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
		this.viewer = (TreeViewer) viewer;
	}

	/*
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parentElement)
	{
		if (parentElement instanceof KConfigMenuItem)
		{
			KConfigMenuItem element = (KConfigMenuItem) parentElement;

			List<KConfigMenuItem> children = element.getChildren();
			return getMenuItems(children).toArray();
		}
		return EMPTY_ARRAY;
	}

	private List<KConfigMenuItem> getMenuItems(List<KConfigMenuItem> children)
	{

		List<KConfigMenuItem> menuList = new ArrayList<KConfigMenuItem>();
		for (KConfigMenuItem kConfigMenuItem : children)
		{
			if (kConfigMenuItem.getType() != null && kConfigMenuItem.getType().equals(IJsonServerConfig.MENU_TYPE))
			{
				menuList.add(kConfigMenuItem);
			}
		}

		return menuList;
	}

	/*
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object element)
	{
		if (element instanceof KConfigMenuItem)
		{
			return ((KConfigMenuItem) element).getType().equals(IJsonServerConfig.MENU_TYPE);
		}

		return false;
	}

	/*
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object inputElement)
	{
		return getChildren(inputElement);
	}

	@Override
	public Object getParent(Object element)
	{
		if (element instanceof KConfigMenuItem)
		{
			return ((KConfigMenuItem) element).getParent();
		}
		return element;
	}

}
