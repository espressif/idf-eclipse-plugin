/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.templates;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class TemplateNode implements ITemplateNode
{

	private String projectName;
	private File file;
	private ITemplateNode parentNode;
	private List<ITemplateNode> children = new ArrayList<ITemplateNode>();
	private int type;

	public TemplateNode(String projectName, File file, ITemplateNode parentNode, int type)
	{
		this.projectName = projectName;
		this.file = file;
		this.parentNode = parentNode;
		this.type = type;
	}

	@Override
	public String getName()
	{
		return projectName;
	}

	@Override
	public File getFilePath()
	{
		return file;
	}

	@Override
	public ITemplateNode getParent()
	{
		return parentNode;
	}

	public void add(TemplateNode templateNode)
	{
		children.add(templateNode);
	}
	
	public void addFirst(TemplateNode templateNode)
	{
		children.add(0, templateNode);
	}

	@Override
	public int getType()
	{
		return type;
	}

	@Override
	public List<ITemplateNode> getChildren()
	{
		return children;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((projectName == null) ? 0 : projectName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TemplateNode other = (TemplateNode) obj;
		if (projectName == null)
		{
			if (other.projectName != null)
				return false;
		} else
			if (!projectName.equals(other.projectName))
				return false;
		return true;
	}

}
