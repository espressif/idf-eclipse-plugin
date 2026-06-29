/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.eimjson.presentation;

import com.espressif.idf.core.tools.eimjson.model.EimInstallationModel;

/**
 * Builds {@link EimInstallationPresentation} for a table row based on schema version.
 */
public interface EimInstallationPresentationRenderer
{
	EimInstallationPresentation render(EimInstallationModel installation, boolean isActive, boolean isSettingUp);
}
