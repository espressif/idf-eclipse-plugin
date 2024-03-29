/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core;

public class DefaultSystemWrapper implements SystemWrapper
{
	@Override
	public String getPathEnv()
	{
		return System.getenv(PATH);
	}

	@Override
	public String getEnvExecutables()
	{
		return System.getenv(PATHEXT);
	}

}
