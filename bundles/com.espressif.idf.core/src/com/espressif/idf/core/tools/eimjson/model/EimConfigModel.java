/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.eimjson.model;

import java.util.List;
import java.util.Optional;

import com.espressif.idf.core.tools.eimjson.EimJsonVersion;

/**
 * Version-neutral view of the full {@code eim_idf.json} document.
 */
public interface EimConfigModel
{
	EimJsonVersion getSchemaVersion();

	String getGitPath();

	Optional<String> getEimPath();

	String getIdfSelectedId();

	List<EimInstallationModel> getInstallations();
}
