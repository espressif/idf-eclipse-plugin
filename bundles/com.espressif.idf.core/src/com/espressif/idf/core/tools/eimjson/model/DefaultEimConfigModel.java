/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.eimjson.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.espressif.idf.core.tools.eimjson.EimJsonVersion;

public final class DefaultEimConfigModel implements EimConfigModel
{
	private final EimJsonVersion schemaVersion;
	private final String gitPath;
	private final String eimPath;
	private final String idfSelectedId;
	private final List<EimInstallationModel> installations;

	public DefaultEimConfigModel(EimJsonVersion schemaVersion, String gitPath, String eimPath,
			String idfSelectedId, List<EimInstallationModel> installations)
	{
		this.schemaVersion = schemaVersion;
		this.gitPath = gitPath;
		this.eimPath = eimPath;
		this.idfSelectedId = idfSelectedId;
		this.installations = installations != null ? List.copyOf(installations) : Collections.emptyList();
	}

	@Override
	public EimJsonVersion getSchemaVersion()
	{
		return schemaVersion;
	}

	@Override
	public String getGitPath()
	{
		return gitPath;
	}

	@Override
	public Optional<String> getEimPath()
	{
		return Optional.ofNullable(eimPath);
	}

	@Override
	public String getIdfSelectedId()
	{
		return idfSelectedId;
	}

	@Override
	public List<EimInstallationModel> getInstallations()
	{
		return installations;
	}
}
