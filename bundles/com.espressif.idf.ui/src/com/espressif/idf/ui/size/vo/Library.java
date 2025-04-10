/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size.vo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Main vo for holding all the data related to a memory object
 * 
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class Library
{
	private String name;
	
    private String abbrevName;

    private long size;

    private Map<String, MemoryType> memoryTypes;

    private long sizeDiff;
    
    private List<Library> children;
    
    public Library()
    {
    	children = new LinkedList<>();
    }

	public String getAbbrevName()
	{
		return abbrevName;
	}

	public void setAbbrevName(String abbrevName)
	{
		this.abbrevName = abbrevName;
	}

	public long getSize()
	{
		return size;
	}

	public void setSize(long size)
	{
		this.size = size;
	}

	public Map<String, MemoryType> getMemoryTypes()
	{
		return memoryTypes;
	}

	public void setMemoryTypes(Map<String, MemoryType> memoryTypes)
	{
		this.memoryTypes = memoryTypes;
	}

	public long getSizeDiff()
	{
		return sizeDiff;
	}

	public void setSizeDiff(long sizeDiff)
	{
		this.sizeDiff = sizeDiff;
	}

	public List<Library> getChildren()
	{
		return children;
	}

	public void setChildren(List<Library> children)
	{
		this.children = children;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
