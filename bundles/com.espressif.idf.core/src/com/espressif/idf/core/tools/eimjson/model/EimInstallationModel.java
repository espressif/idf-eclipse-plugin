/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.eimjson.model;

import java.util.Optional;

import com.espressif.idf.core.tools.eimjson.InstallationStatus;
import com.espressif.idf.core.util.StringUtil;
import com.google.gson.annotations.Expose;

/**
 * One {@code idfInstalled[]} entry from {@code eim_idf.json}.
 * <p>
 * Deserialized directly by Gson. {@code eim_idf.json} evolves additively, so a single tolerant DTO
 * covers every schema version: fields absent in older files stay {@code null} and are defaulted here
 * (e.g. {@code status} arrived in schema 3.0; older files report {@link InstallationStatus#FINISHED}).
 */
public class EimInstallationModel
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

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getPath()
	{
		return path;
	}

	public String getIdfToolsPath()
	{
		return idfToolsPath;
	}

	public Optional<String> getActivationScript()
	{
		return Optional.ofNullable(activationScript).filter(s -> !StringUtil.isEmpty(s));
	}

	public Optional<String> getPython()
	{
		return Optional.ofNullable(python).filter(s -> !StringUtil.isEmpty(s));
	}

	public InstallationStatus getStatus()
	{
		return InstallationStatus.fromJson(status);
	}

	/** True when the IDE may run the activation script and wire toolchains. */
	public boolean isActivatable()
	{
		return getStatus().isActivatable() && getActivationScript().isPresent() && getPython().isPresent();
	}
}
