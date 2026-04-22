/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.espressif.idf.core.tools.EimIdfJsonPathResolver;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class EimIdfJsonPathResolverTest
{

	@Test
	void empty_preference_uses_default_path()
	{
		EimIdfJsonPathResolver r = new EimIdfJsonPathResolver();
		Path result = r.resolveEimIdfJsonFileFromPreferenceString("");
		Assertions.assertEquals(r.getDefaultEimIdfJsonFile(), result);
	}

	@Test
	void null_preference_uses_default_path()
	{
		EimIdfJsonPathResolver r = new EimIdfJsonPathResolver();
		Path result = r.resolveEimIdfJsonFileFromPreferenceString(null);
		Assertions.assertEquals(r.getDefaultEimIdfJsonFile(), result);
	}

	@Test
	void existing_custom_file_is_used(@TempDir Path tempDir) throws IOException
	{
		Path json = tempDir.resolve("eim_idf.json");
		Files.createFile(json);
		EimIdfJsonPathResolver r = new EimIdfJsonPathResolver();
		Path result = r.resolveEimIdfJsonFileFromPreferenceString(json.toString());
		Assertions.assertEquals(json.toAbsolutePath().normalize(), result.toAbsolutePath().normalize());
	}

	@Test
	void nonexistent_custom_falls_back_to_default()
	{
		EimIdfJsonPathResolver r = new EimIdfJsonPathResolver();
		Path result = r.resolveEimIdfJsonFileFromPreferenceString("/no/such/path/eim_idf.json");
		Assertions.assertEquals(r.getDefaultEimIdfJsonFile(), result);
	}
}
