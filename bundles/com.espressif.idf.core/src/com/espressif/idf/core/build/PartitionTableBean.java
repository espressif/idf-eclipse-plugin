/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.build;

import com.opencsv.bean.CsvBindByPosition;

public class PartitionTableBean implements CsvBean
{

	@CsvBindByPosition(position = 0)
	private String name;

	@CsvBindByPosition(position = 1)
	private String type;

	@CsvBindByPosition(position = 2)
	private String subType;

	@CsvBindByPosition(position = 3)
	private String offset;

	@CsvBindByPosition(position = 4)
	private String size;

	@CsvBindByPosition(position = 5)
	private String flags;

	public PartitionTableBean()
	{
		this.name = ""; //$NON-NLS-1$
		this.type = ""; //$NON-NLS-1$
		this.subType = ""; //$NON-NLS-1$
		this.offset = ""; //$NON-NLS-1$
		this.size = ""; //$NON-NLS-1$
		this.flags = ""; //$NON-NLS-1$
	}

	public String getName()
	{
		return name.trim();
	}

	public String getType()
	{
		return type.trim();
	}

	public String getSubType()
	{
		return subType.trim();
	}

	public String getOffSet()
	{
		return offset.trim();
	}

	public String getSize()
	{
		return size.trim();
	}

	public String getFlag()
	{
		return flags.trim();
	}

	public void setName(String value)
	{
		name = value;
	}

	public void setType(String value)
	{
		type = value.trim();

	}

	public void setOffSet(String value)
	{
		offset = value.trim();
	}

	public void setSize(String value)
	{
		size = value.trim();
	}

	public void setFlag(String value)
	{
		flags = value.trim();
	}

	public void setSubType(String value)
	{
		subType = value.trim();
	}
}
