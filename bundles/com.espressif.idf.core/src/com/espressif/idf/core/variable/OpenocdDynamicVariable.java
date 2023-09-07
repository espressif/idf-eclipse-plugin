/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.variable;

public enum OpenocdDynamicVariable
{
	OPENOCD_PATH("openocd_path"), OPENOCD_EXE("openocd_exe"), OPENOCD_SCRIPTS; //$NON-NLS-1$ //$NON-NLS-2$

	private String value;

	OpenocdDynamicVariable(String value)
	{
		this.value = value;
	}

	OpenocdDynamicVariable()
	{
		this.value = name();
	}

	public String getValue()
	{
		return value;
	}

}
