/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.espressif.idf.core.util.SDKConfigUtil;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class KConfigMenuProcessor
{

	private IProject project;

	public KConfigMenuProcessor(IProject project)
	{
		this.project = project;
	}

	/**
	 * Process the kconfig_menus.json file and construct the hierarchical menu items tree
	 *    
	 * @return root to the kconfig_menus.json
	 * @throws Exception
	 */
	public KConfigMenuItem reader() throws Exception
	{

		SDKConfigUtil sdkConfigUtil = new SDKConfigUtil();
		String menuConfigPath = sdkConfigUtil.getConfigMenuFilePath(project);
		if (!new File(menuConfigPath).exists())
		{
			throw new Exception(MessageFormat.format(Messages.KconfMenuJsonNotFound, menuConfigPath));
		}

		JSONParser parser = new JSONParser();
		KConfigMenuItem root = new KConfigMenuItem(null);

		try
		{
			Object obj = parser.parse(new FileReader(menuConfigPath));
			read(obj, root);

		} catch (IOException | ParseException e)
		{
			throw new Exception(e);
		}
		return root;
	}

	
	/**
	 * @param obj
	 * @param menuItem
	 */
	protected void read(Object obj, KConfigMenuItem menuItem)
	{
		JSONArray jsonArray = (JSONArray) obj;
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> iterator = jsonArray.iterator();
		while (iterator.hasNext())
		{

			KConfigMenuItem childMenu = new KConfigMenuItem(menuItem);

			JSONObject jsonObject = (JSONObject) iterator.next();

			childMenu.setName((String) jsonObject.get("name")); //$NON-NLS-1$
			childMenu.setId((String) jsonObject.get("id")); //$NON-NLS-1$
			childMenu.setType((String) jsonObject.get("type")); //$NON-NLS-1$
			childMenu.setHelp((String) jsonObject.get("help")); //$NON-NLS-1$

			String title = (String) jsonObject.get("title"); //$NON-NLS-1$
			childMenu.setTitle(title);

			if (title != null)
			{
				menuItem.addChild(childMenu);
			}

			// loop array
			JSONArray children = (JSONArray) jsonObject.get("children"); //$NON-NLS-1$
			read(children, childMenu);

		}
	}
}
