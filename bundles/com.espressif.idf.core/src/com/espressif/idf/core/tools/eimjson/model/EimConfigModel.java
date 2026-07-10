/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.eimjson.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.espressif.idf.core.tools.eimjson.EimJsonVersion;
import com.google.gson.annotations.Expose;

/**
 * The full {@code eim_idf.json} document.
 * <p>
 * Deserialized directly by Gson. The schema evolves additively, so this single DTO parses every
 * known version; {@link EimJsonVersion} is only used as a forward-compatibility guard in
 * {@link com.espressif.idf.core.tools.eimjson.EimIdfJsonLoader} and is stored here for reference.
 */
public class EimConfigModel
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
	private List<EimInstallationModel> idfInstalled = Collections.emptyList();

	private transient EimJsonVersion schemaVersion;

	public EimJsonVersion getSchemaVersion()
	{
		return schemaVersion;
	}

	public void setSchemaVersion(EimJsonVersion schemaVersion)
	{
		this.schemaVersion = schemaVersion;
	}

	public String getGitPath()
	{
		return gitPath;
	}

	public Optional<String> getEimPath()
	{
		return Optional.ofNullable(eimPath);
	}

	public String getIdfSelectedId()
	{
		return idfSelectedId;
	}

	public List<EimInstallationModel> getInstallations()
	{
		return idfInstalled != null ? idfInstalled : Collections.emptyList();
	}
}
