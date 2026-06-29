/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools;

import java.io.IOException;

import com.espressif.idf.core.tools.eimjson.EimIdfJsonLoader;
import com.espressif.idf.core.tools.eimjson.model.EimConfigModel;
import com.espressif.idf.core.tools.exceptions.EimVersionMismatchException;

/**
 * Loads {@code eim_idf.json}. Delegates version detection and parsing to
 * {@link EimIdfJsonLoader}.
 */
public class EimIdfConfiguratinParser
{
	private final EimIdfJsonLoader loader = new EimIdfJsonLoader();
	private EimConfigModel configModel;

	public EimIdfConfiguratinParser()
	{
	}

	private void load() throws IOException, EimVersionMismatchException
	{
		configModel = loader.loadDefault();
	}

	public EimConfigModel getConfigModel(boolean reload) throws IOException, EimVersionMismatchException
	{
		if (reload || configModel == null)
		{
			load();
		}
		return configModel;
	}
}
