/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.eimjson.model;

import java.util.Optional;

import com.espressif.idf.core.tools.eimjson.InstallationStatus;

/**
 * Version-neutral view of one {@code idfInstalled[]} entry.
 */
public interface EimInstallationModel
{
	String getId();

	String getName();

	String getPath();

	String getIdfToolsPath();

	Optional<String> getActivationScript();

	Optional<String> getPython();

	InstallationStatus getStatus();

	/** True when the IDE may run the activation script and wire toolchains. */
	boolean isActivatable();
}
