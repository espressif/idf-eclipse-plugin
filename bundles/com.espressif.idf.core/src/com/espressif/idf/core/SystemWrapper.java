/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core;

public interface SystemWrapper
{
	String PATH = "PATH"; //$NON-NLS-1$
	String PATHEXT = "PATHEXT"; //$NON-NLS-1$
	
	public String getPathEnv();
	public String getEnvExecutables();
}
