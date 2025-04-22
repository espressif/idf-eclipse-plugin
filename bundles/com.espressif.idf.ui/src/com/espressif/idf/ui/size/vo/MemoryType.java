/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size.vo;

import java.util.Map;

/**
 * Sub vo for holding different memory types in a library record
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class MemoryType
{
	private long size;

    private long sizeDiff;

    private Map<String, Section> sections;

	public Map<String, Section> getSections()
	{
		return sections;
	}

	public void setSections(Map<String, Section> sections)
	{
		this.sections = sections;
	}

	public long getSizeDiff()
	{
		return sizeDiff;
	}

	public void setSizeDiff(long sizeDiff)
	{
		this.sizeDiff = sizeDiff;
	}

	public long getSize()
	{
		return size;
	}

	public void setSize(long size)
	{
		this.size = size;
	}

}
