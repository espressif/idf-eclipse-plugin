/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.eimjson.schema.v3;

import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.Expose;

/** Gson DTO for {@code eim_idf.json} schema 3.0. */
public class EimJsonV3
{
	@Expose
	private String version;
	@Expose
	private String eimPath;
	@Expose
	private String gitPath;
	@Expose
	private String idfSelectedId;
	@Expose
	private List<IdfInstalledV3> idfInstalled = Collections.emptyList();

	public String getVersion()
	{
		return version;
	}

	public String getEimPath()
	{
		return eimPath;
	}

	public String getGitPath()
	{
		return gitPath;
	}

	public String getIdfSelectedId()
	{
		return idfSelectedId;
	}

	public List<IdfInstalledV3> getIdfInstalled()
	{
		return idfInstalled;
	}
}
