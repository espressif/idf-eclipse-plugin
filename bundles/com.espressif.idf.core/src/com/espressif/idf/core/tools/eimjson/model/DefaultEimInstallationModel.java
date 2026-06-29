/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.eimjson.model;

import java.util.Optional;

import com.espressif.idf.core.tools.eimjson.InstallationStatus;
import com.espressif.idf.core.util.StringUtil;

public final class DefaultEimInstallationModel implements EimInstallationModel
{
	private final String id;
	private final String name;
	private final String path;
	private final String idfToolsPath;
	private final String activationScript;
	private final String python;
	private final InstallationStatus status;

	public DefaultEimInstallationModel(String id, String name, String path, String idfToolsPath,
			String activationScript, String python, InstallationStatus status)
	{
		this.id = id;
		this.name = name;
		this.path = path;
		this.idfToolsPath = idfToolsPath;
		this.activationScript = activationScript;
		this.python = python;
		this.status = status != null ? status : InstallationStatus.FINISHED;
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getPath()
	{
		return path;
	}

	@Override
	public String getIdfToolsPath()
	{
		return idfToolsPath;
	}

	@Override
	public Optional<String> getActivationScript()
	{
		return Optional.ofNullable(activationScript).filter(s -> !StringUtil.isEmpty(s));
	}

	@Override
	public Optional<String> getPython()
	{
		return Optional.ofNullable(python).filter(s -> !StringUtil.isEmpty(s));
	}

	@Override
	public InstallationStatus getStatus()
	{
		return status;
	}

	@Override
	public boolean isActivatable()
	{
		return status.isActivatable() && getActivationScript().isPresent();
	}
}
