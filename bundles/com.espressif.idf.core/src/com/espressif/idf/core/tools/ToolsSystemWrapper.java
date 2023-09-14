/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools;

import com.espressif.idf.core.SystemWrapper;

/**
 * Tools System wrapper to make sure to avoid the 
 * system path when verifying for validation after tools installation
 * @author Ali Azam Rana
 *
 */
public class ToolsSystemWrapper implements SystemWrapper
{
	private String path;
	
	public ToolsSystemWrapper(String path)
	{
		this.path = path;
	}

	@Override
	public String getPathEnv()
	{
		return path;
	}

	@Override
	public String getEnvExecutables()
	{
		return System.getenv(PATHEXT);
	}

}
