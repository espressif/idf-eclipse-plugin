/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class KConfigMenuItem
{
	private List<KConfigMenuItem> children;
	private KConfigMenuItem parent;
	private String depends_on;
	private String help;
	private String name;
	private String range;
	private String title;
	private String type;

	public KConfigMenuItem(KConfigMenuItem parent)
	{
		children = new ArrayList<KConfigMenuItem>();
		this.parent = parent;
	}

	public List<KConfigMenuItem> getChildren()
	{
		return children;
	}

	public void setChildren(List<KConfigMenuItem> children)
	{
		this.children = children;
	}

	public void addChild(KConfigMenuItem item)
	{
		this.children.add(item);
	}

	public KConfigMenuItem getParent()
	{
		return parent;
	}

	public String getDepends_on()
	{
		return depends_on;
	}

	public void setDepends_on(String depends_on)
	{
		this.depends_on = depends_on;
	}

	public String getHelp()
	{
		return help;
	}

	public void setHelp(String help)
	{
		this.help = help;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getRange()
	{
		return range;
	}

	public void setRange(String range)
	{
		this.range = range;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public boolean hasChildren()
	{
		return this.children.size() > 0;
	}
}