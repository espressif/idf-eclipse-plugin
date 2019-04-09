package com.espressif.idf.sdk.config.core;

import java.util.ArrayList;
import java.util.List;

public class KConfigMenuItem
{

	public List<KConfigMenuItem> children;
	public KConfigMenuItem parent;
	public String depends_on;
	public String help;
	public String name;
	public String range;
	public String title;
	public String type;

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

}