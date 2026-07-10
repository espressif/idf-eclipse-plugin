/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.eimjson;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.EimIdfJsonPathResolver;
import com.espressif.idf.core.tools.eimjson.model.EimConfigModel;
import com.espressif.idf.core.tools.exceptions.EimVersionMismatchException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Single entry point for loading {@code eim_idf.json}.
 * <p>
 * {@code eim_idf.json} changes are additive, so one tolerant {@link EimConfigModel} parses every
 * known schema: Gson ignores unknown fields and leaves missing ones {@code null} (defaulted in the
 * model). {@link EimJsonVersion#parse(String)} only guards against files newer than this build.
 */
public final class EimIdfJsonLoader
{
	private final Gson gson;

	public EimIdfJsonLoader()
	{
		gson = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization()
				.excludeFieldsWithoutExposeAnnotation().create();
	}

	public EimConfigModel load(Path jsonPath) throws IOException, EimVersionMismatchException
	{
		File file = jsonPath.toFile();
		if (!file.exists())
		{
			Logger.log("EIM config file not found: " + jsonPath); //$NON-NLS-1$
			return null;
		}

		try (Reader reader = new FileReader(file))
		{
			JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
			String versionField = root.has("version") ? root.get("version").getAsString() : null; //$NON-NLS-1$ //$NON-NLS-2$
			EimJsonVersion version = EimJsonVersion.parse(versionField);
			EimConfigModel model = gson.fromJson(root, EimConfigModel.class);
			if (model != null)
			{
				model.setSchemaVersion(version);
			}
			return model;
		}
	}

	public EimConfigModel loadDefault() throws IOException, EimVersionMismatchException
	{
		Path path = new EimIdfJsonPathResolver().resolveEimIdfJsonFile();
		return load(path);
	}
}
