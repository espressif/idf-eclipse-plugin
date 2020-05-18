/*******************************************************************************
 * Copyright 2018-2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFSizeData
{
	private String name;
	private long total;
	private long bss;
	private long data;
	private long iram;
	private long flash_rodata;
	private long flash_text;
	private long diram;
	private long other;
	private List<IDFSizeData> children = new ArrayList<>();

	public IDFSizeData(String name, long data, long bss, long diram, long iram, long flash_text, long flash_rodata,
			long other, long total)
	{
		this.name = name;
		this.total = total;
		this.bss = bss;
		this.data = data;
		this.iram = iram;
		this.flash_rodata = flash_rodata;
		this.flash_text = flash_text;
		this.diram = diram;
		this.other = other;
	}

	public String getName()
	{
		return name;
	}

	public long getTotal()
	{
		return total;
	}

	public long getBss()
	{
		return bss;
	}

	public long getData()
	{
		return data;
	}

	public long getIram()
	{
		return iram;
	}

	public long getFlash_rodata()
	{
		return flash_rodata;
	}

	public long getFlash_text()
	{
		return flash_text;
	}

	public long getDiram()
	{
		return diram;
	}

	public long getOther()
	{
		return other;
	}

	public List<IDFSizeData> getChildren()
	{
		return children;
	}

}
