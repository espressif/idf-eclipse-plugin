/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.eimjson.presentation;

import com.espressif.idf.core.tools.eimjson.EimJsonVersion;

/**
 * Selects a presentation renderer for an {@code eim_idf.json} schema version.
 * <p>
 * Mirrors {@link com.espressif.idf.core.tools.eimjson.adapter.EimConfigAdapterFactory}: UI and
 * tests obtain a renderer from the file's schema version instead of hard-coding a concrete class,
 * so a future schema (e.g. V4) can supply different labels or button rules without changing
 * {@code ESPIDFMainTablePage}.
 * <p>
 * V2 and V3 currently share {@link DefaultEimInstallationPresentationRenderer} because adapters
 * already normalize both into {@link com.espressif.idf.core.tools.eimjson.model.EimInstallationModel}
 * and {@link com.espressif.idf.core.tools.eimjson.InstallationStatus}; the renderer only maps those
 * domain values to {@link EimInstallationPresentation}.
 */
public final class EimInstallationPresentationRendererFactory
{
	private static final EimInstallationPresentationRenderer RENDERER = new DefaultEimInstallationPresentationRenderer();

	private EimInstallationPresentationRendererFactory()
	{
	}

	/**
	 * @param version schema version from the loaded config; retained for API symmetry with adapters
	 *              and so a version-specific renderer can be returned when presentation rules diverge
	 */
	public static EimInstallationPresentationRenderer forSchema(EimJsonVersion version)
	{
		// version ignored until a schema needs presentation logic adapters cannot express
		return RENDERER;
	}
}
