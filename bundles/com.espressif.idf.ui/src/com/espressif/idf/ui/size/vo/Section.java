/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size.vo;

/**
 * Sub vo for memory types that contain the memory section details.
 * 
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class Section
{
	private long size;

    private String abbrevName;

    private long sizeDiff;

	public long getSize()
	{
		return size;
	}

	public void setSize(long size)
	{
		this.size = size;
	}

	public String getAbbrevName()
	{
		return abbrevName;
	}

	public void setAbbrevName(String abbrevName)
	{
		this.abbrevName = abbrevName;
	}

	public long getSizeDiff()
	{
		return sizeDiff;
	}

	public void setSizeDiff(long sizeDiff)
	{
		this.sizeDiff = sizeDiff;
	}

}
