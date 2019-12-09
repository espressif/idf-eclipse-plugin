/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.json.simple.JSONObject;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.sdk.config.core.IJsonServerConfig;
import com.espressif.idf.sdk.config.core.KConfigMenuItem;
import com.espressif.idf.sdk.config.core.server.ConfigServerManager;
import com.espressif.idf.sdk.config.core.server.JsonConfigServer;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ConfigContentProvider extends TreeNodeContentProvider
{
	private static Object[] EMPTY_ARRAY = new Object[0];
	protected TreeViewer viewer;
	private IProject project;

	public ConfigContentProvider(IProject project)
	{
		this.project = project;
	}

	/*
	 * @see IContentProvider#dispose()
	 */
	@Override
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
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
		this.viewer = (TreeViewer) viewer;
	}

	/*
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement)
	{
		if (parentElement instanceof KConfigMenuItem)
		{
			KConfigMenuItem element = (KConfigMenuItem) parentElement;

			List<KConfigMenuItem> children = element.getChildren();
			try
			{
				return getMenuItems(children).toArray();
			}
			catch (IOException e)
			{
				Logger.log(e);
			}
		}
		return EMPTY_ARRAY;
	}

	private List<KConfigMenuItem> getMenuItems(List<KConfigMenuItem> children) throws IOException
	{
		
		JsonConfigServer configServer = ConfigServerManager.INSTANCE.getServer(project);
		List<KConfigMenuItem> menuList = new ArrayList<KConfigMenuItem>();
		for (KConfigMenuItem kConfigMenuItem : children)
		{
			if (kConfigMenuItem.getType() != null && kConfigMenuItem.getType().equals(IJsonServerConfig.MENU_TYPE))
			{
				JSONObject visibleJsonMap = configServer.getOutput().getVisibleJsonMap();
				Logger.logTrace(SDKConfigUIPlugin.getDefault(), "item >" + kConfigMenuItem.getTitle() + " type >"+ kConfigMenuItem.getType()); //$NON-NLS-1$ //$NON-NLS-2$
				
				boolean visible = kConfigMenuItem.isVisible(visibleJsonMap);
				Logger.logTrace(SDKConfigUIPlugin.getDefault(), "visibility >" + kConfigMenuItem.isVisible(visibleJsonMap)); //$NON-NLS-1$
				if (visible)
				{
					menuList.add(kConfigMenuItem);
				}
			}
		}

		return menuList;
	}

	/*
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	@Override
	public boolean hasChildren(Object element)
	{
		if (element instanceof KConfigMenuItem)
		{
			KConfigMenuItem configMenuItem = (KConfigMenuItem) element;
			List<KConfigMenuItem> children = configMenuItem.getChildren();
			try
			{
				return getMenuItems(children).size() > 0;
			}
			catch (IOException e)
			{
				Logger.log(e);
			}
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
