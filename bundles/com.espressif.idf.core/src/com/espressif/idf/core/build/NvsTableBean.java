/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.build;

import com.opencsv.bean.CsvBindByPosition;

public class NvsTableBean implements CsvBean
{
	@CsvBindByPosition(position = 0)
	private String key;

	@CsvBindByPosition(position = 1)
	private String type;

	@CsvBindByPosition(position = 2)
	private String encoding;

	@CsvBindByPosition(position = 3)
	private String value;

	// Default values for adding new Bean without breaking NVS table
	public NvsTableBean()
	{
		key = ""; //$NON-NLS-1$
		type = "namespace"; //$NON-NLS-1$
		encoding = ""; //$NON-NLS-1$
		value = ""; //$NON-NLS-1$
	}

	public String getKey()
	{
		return key.trim();
	}

	public String getType()
	{
		return type.trim();
	}

	public String getEncoding()
	{
		return encoding.trim();
	}

	public String getValue()
	{
		return value.trim();
	}

	public void setKey(String key)
	{
		this.key = key.trim();
	}

	public void setType(String type)
	{
		this.type = type.trim();
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding.trim();
	}

	public void setValue(String value)
	{
		this.value = value.trim();
	}
}
