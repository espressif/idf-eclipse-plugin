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
import com.espressif.idf.core.tools.eimjson.adapter.EimConfigAdapter;
import com.espressif.idf.core.tools.eimjson.adapter.EimConfigAdapterFactory;
import com.espressif.idf.core.tools.eimjson.model.EimConfigModel;
import com.espressif.idf.core.tools.eimjson.schema.v3.EimJsonV3;
import com.espressif.idf.core.tools.exceptions.EimVersionMismatchException;
import com.espressif.idf.core.tools.vo.EimJson;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.GsonBuilder;

/**
 * Single entry point for loading {@code eim_idf.json}: detects schema version,
 * delegates to the matching adapter, returns the neutral domain model.
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
			EimConfigAdapter adapter = EimConfigAdapterFactory.forVersion(version);
			Object raw = parseRaw(root, version);
			return adapter.toModel(raw);
		}
	}

	public EimConfigModel loadDefault() throws IOException, EimVersionMismatchException
	{
		Path path = new EimIdfJsonPathResolver().resolveEimIdfJsonFile();
		return load(path);
	}

	private Object parseRaw(JsonObject root, EimJsonVersion version)
	{
		return switch (version)
		{
			case V2 -> gson.fromJson(root, EimJson.class);
			case V3 -> gson.fromJson(root, EimJsonV3.class);
		};
	}
}
