/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core;

public class DefaultSystemWrapper implements SystemWrapper
{
	private static final String PATH = "PATH"; //$NON-NLS-1$
	private static final String PATHEXT = "PATHEXT"; //$NON-NLS-1$

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
