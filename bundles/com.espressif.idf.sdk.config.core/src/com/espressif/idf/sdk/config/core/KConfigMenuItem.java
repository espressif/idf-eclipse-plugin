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
	private String depends_on;
	private String help;
	private String name;
	private KConfigMenuItem parent;
	private String range;
	private String title;
	private String type;

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

	public String getDepends_on()
	{
		return depends_on;
	}

	public String getHelp()
	{
		return help;
	}

	public String getName()
	{
		return name;
	}

	public KConfigMenuItem getParent()
	{
		return parent;
	}

	public String getRange()
	{
		return range;
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

	public void setDepends_on(String depends_on)
	{
		this.depends_on = depends_on;
	}

	public void setHelp(String help)
	{
		this.help = help;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setRange(String range)
	{
		this.range = range;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public void setType(String type)
	{
		this.type = type;
	}
}