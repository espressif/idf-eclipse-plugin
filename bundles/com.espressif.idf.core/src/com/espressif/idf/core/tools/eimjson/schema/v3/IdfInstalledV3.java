/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.eimjson.schema.v3;

import com.google.gson.annotations.Expose;

/** Gson DTO for one {@code idfInstalled[]} entry in schema 3.0. */
public class IdfInstalledV3
{
	@Expose
	private String activationScript;
	@Expose
	private String id;
	@Expose
	private String idfToolsPath;
	@Expose
	private String name;
	@Expose
	private String path;
	@Expose
	private String python;
	@Expose
	private String status;

	public String getActivationScript()
	{
		return activationScript;
	}

	public String getId()
	{
		return id;
	}

	public String getIdfToolsPath()
	{
		return idfToolsPath;
	}

	public String getName()
	{
		return name;
	}

	public String getPath()
	{
		return path;
	}

	public String getPython()
	{
		return python;
	}

	public String getStatus()
	{
		return status;
	}
}
