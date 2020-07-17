/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class KConfigMenuItem
{
	private List<KConfigMenuItem> children;
	private String help;
	private String name;
	private KConfigMenuItem parent;
	private String title;
	private String type;
	private String id;

	public KConfigMenuItem(KConfigMenuItem parent)
	{
		children = new ArrayList<KConfigMenuItem>();
		this.parent = parent;
	}

	public void addChild(KConfigMenuItem item)
	{
		this.children.add(item);
	}

	public List<KConfigMenuItem> getChildren()
	{
		return children;
	}

	public String getHelp()
	{
		return help;
	}

	public String getName()
	{
		return name;
	}

	public String getId()
	{
		return id;
	}

	public KConfigMenuItem getParent()
	{
		return parent;
	}

	public String getTitle()
	{
		return title;
	}

	public String getType()
	{
		return type;
	}

	public boolean hasChildren()
	{
		return this.children.size() > 0;
	}

	public void setChildren(List<KConfigMenuItem> children)
	{
		this.children = children;
	}

	public void setHelp(String help)
	{
		this.help = help;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setId(String name)
	{
		this.id = name;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public boolean isVisible(JSONObject visibleJsonMap)
	{
		if (visibleJsonMap == null || visibleJsonMap.isEmpty())
		{
			return false;
		}

		if (getType() != null && getType().equals(IJsonServerConfig.MENU_TYPE))
		{
			return isVisible(children, visibleJsonMap);
		}
		return isVisible(visibleJsonMap, getId());
	}

	private boolean isVisible(List<KConfigMenuItem> children, JSONObject visibleJsonMap)
	{
		for (KConfigMenuItem kConfigMenuItem : children)
		{
			String type = kConfigMenuItem.getType();
			String configKey = kConfigMenuItem.getId();
			boolean isVisible = false;
			if (type.equals(IJsonServerConfig.STRING_TYPE) || type.equals(IJsonServerConfig.HEX_TYPE)
					|| type.equals(IJsonServerConfig.BOOL_TYPE) || type.equals(IJsonServerConfig.INT_TYPE))
			{
				isVisible = isVisible(visibleJsonMap, configKey);
				if (isVisible)
				{
					return true;
				}
			}
			else if (type.equals(IJsonServerConfig.CHOICE_TYPE))
			{
				List<KConfigMenuItem> choiceItems = kConfigMenuItem.getChildren();
				for (KConfigMenuItem item : choiceItems)
				{
					String localConfigKey = item.getId();
					isVisible = isVisible(visibleJsonMap, localConfigKey);
					if (isVisible)
					{
						return true;
					}
				}
			}
			else if (type.equals(IJsonServerConfig.MENU_TYPE))
			{

				return isVisible(kConfigMenuItem.getChildren(), visibleJsonMap);
			}
		}
		return false;
	}

	private boolean isVisible(JSONObject visibleJsonMap, String configKey)
	{
		return visibleJsonMap.get(configKey) != null ? (boolean) visibleJsonMap.get(configKey) : false;
	}
}