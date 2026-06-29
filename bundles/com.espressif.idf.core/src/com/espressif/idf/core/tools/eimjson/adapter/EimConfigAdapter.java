/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.eimjson.adapter;

import com.espressif.idf.core.tools.eimjson.model.EimConfigModel;

/**
 * Converts a version-specific {@code eim_idf.json} DTO into the neutral domain model.
 */
public interface EimConfigAdapter
{
	EimConfigModel toModel(Object rawDocument);
}
