/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.espressif.idf.core.tools.vo.EimJson;
import com.espressif.idf.core.tools.vo.IdfInstalled;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Tests for EIM JSON parsing to verify that the JSON format matches the Java VOs
 * 
 * @author Copilot
 */
class EimJsonParserTest
{
	private Gson gson;

	@BeforeEach
	void setUp()
	{
		gson = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization()
				.excludeFieldsWithoutExposeAnnotation().create();
	}

	@Test
	void testParseEimJson()
	{
		String json = """
				{
				  "gitPath": "/opt/homebrew/bin/git",
				  "idfInstalled": [
				    {
				      "activationScript": "/tmp/esp-new/activate_idf_v5.4.sh",
				      "id": "esp-idf-5705c12db93b4d1a8b084c6986173c1b",
				      "idfToolsPath": "/tmp/esp-new/v5.4/tools",
				      "name": "ESP-IDF v5.4",
				      "path": "/tmp/esp-new/v5.4/esp-idf",
				      "python": "/tmp/esp-new/v5.4/tools/python/bin/python3"
				    }
				  ],
				  "idfSelectedId": "esp-idf-5705c12db93b4d1a8b084c6986173c1b",
				  "eimPath": "/Applications/eim.app/Contents/MacOS/eim",
				  "version": "1.0"
				}
				""";

		EimJson eimJson = gson.fromJson(new StringReader(json), EimJson.class);

		assertNotNull(eimJson, "EimJson should not be null");
		assertEquals("1.0", eimJson.getVersion());
		assertEquals("/opt/homebrew/bin/git", eimJson.getGitPath());
		assertEquals("/Applications/eim.app/Contents/MacOS/eim", eimJson.getEimPath());
		assertEquals("esp-idf-5705c12db93b4d1a8b084c6986173c1b", eimJson.getIdfSelectedId());

		assertNotNull(eimJson.getIdfInstalled(), "idfInstalled should not be null");
		assertEquals(1, eimJson.getIdfInstalled().size(), "Should have 1 installed IDF");

		IdfInstalled installed = eimJson.getIdfInstalled().get(0);
		assertEquals("esp-idf-5705c12db93b4d1a8b084c6986173c1b", installed.getId());
		assertEquals("ESP-IDF v5.4", installed.getName());
		assertEquals("/tmp/esp-new/v5.4/esp-idf", installed.getPath());
		assertEquals("/tmp/esp-new/activate_idf_v5.4.sh", installed.getActivationScript());
		assertEquals("/tmp/esp-new/v5.4/tools", installed.getIdfToolsPath());
		assertEquals("/tmp/esp-new/v5.4/tools/python/bin/python3", installed.getPython());
	}

	@Test
	void testParseEimJsonMultipleInstallations()
	{
		String json = """
				{
				  "gitPath": "/usr/bin/git",
				  "idfInstalled": [
				    {
				      "activationScript": "/tmp/esp/v5.4/export.sh",
				      "id": "esp-idf-1",
				      "idfToolsPath": "/tmp/esp/v5.4/tools",
				      "name": "ESP-IDF v5.4",
				      "path": "/tmp/esp/v5.4/esp-idf",
				      "python": "/tmp/esp/v5.4/tools/python/bin/python3"
				    },
				    {
				      "activationScript": "/tmp/esp/v5.1/export.sh",
				      "id": "esp-idf-2",
				      "idfToolsPath": "/tmp/esp/v5.1/tools",
				      "name": "ESP-IDF v5.1",
				      "path": "/tmp/esp/v5.1/esp-idf",
				      "python": "/tmp/esp/v5.1/tools/python/bin/python3"
				    }
				  ],
				  "idfSelectedId": "esp-idf-1",
				  "eimPath": "/usr/local/bin/eim",
				  "version": "1.0"
				}
				""";

		EimJson eimJson = gson.fromJson(new StringReader(json), EimJson.class);

		assertNotNull(eimJson, "EimJson should not be null");
		assertEquals(2, eimJson.getIdfInstalled().size(), "Should have 2 installed IDFs");

		IdfInstalled firstInstalled = eimJson.getIdfInstalled().get(0);
		assertEquals("esp-idf-1", firstInstalled.getId());
		assertEquals("ESP-IDF v5.4", firstInstalled.getName());

		IdfInstalled secondInstalled = eimJson.getIdfInstalled().get(1);
		assertEquals("esp-idf-2", secondInstalled.getId());
		assertEquals("ESP-IDF v5.1", secondInstalled.getName());
	}

	@Test
	void testParseEmptyIdfInstalled()
	{
		String json = """
				{
				  "gitPath": "/usr/bin/git",
				  "idfInstalled": [],
				  "idfSelectedId": "",
				  "eimPath": "/usr/local/bin/eim",
				  "version": "1.0"
				}
				""";

		EimJson eimJson = gson.fromJson(new StringReader(json), EimJson.class);

		assertNotNull(eimJson, "EimJson should not be null");
		assertNotNull(eimJson.getIdfInstalled(), "idfInstalled should not be null");
		assertEquals(0, eimJson.getIdfInstalled().size(), "Should have 0 installed IDFs");
	}

	@Test
	void testParseMinimalJson()
	{
		String json = """
				{
				  "version": "1.0"
				}
				""";

		EimJson eimJson = gson.fromJson(new StringReader(json), EimJson.class);

		assertNotNull(eimJson, "EimJson should not be null");
		assertEquals("1.0", eimJson.getVersion());
		assertNull(eimJson.getGitPath(), "gitPath should be null when not provided");
		// idfInstalled has a default value of Collections.emptyList()
		assertNotNull(eimJson.getIdfInstalled(), "idfInstalled should have default empty list");
	}
}
