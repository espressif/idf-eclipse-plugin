/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.espressif.idf.core.tools.eimjson.EimIdfJsonLoader;
import com.espressif.idf.core.tools.eimjson.EimJsonVersion;
import com.espressif.idf.core.tools.eimjson.InstallationStatus;
import com.espressif.idf.core.tools.eimjson.model.EimConfigModel;
import com.espressif.idf.core.tools.eimjson.model.EimInstallationModel;
import com.espressif.idf.core.tools.eimjson.presentation.EimInstallationPresentation;
import com.espressif.idf.core.tools.eimjson.presentation.EimInstallationPresentationRendererFactory;

class EimIdfJsonLoaderTest
{
	@TempDir
	Path tempDir;

	@Test
	void loadsV2AsFinishedInstallations() throws Exception
	{
		Path json = tempDir.resolve("eim_idf.json");
		Files.writeString(json, """
				{
				  "version": "2.0",
				  "gitPath": "/usr/bin/git",
				  "idfSelectedId": "id-1",
				  "idfInstalled": [{
				    "id": "id-1",
				    "name": "v5.4",
				    "path": "/esp/v5.4/esp-idf",
				    "idfToolsPath": "/esp/tools",
				    "activationScript": "/esp/tools/activate.sh",
				    "python": "/esp/tools/python/bin/python3"
				  }]
				}
				""");

		EimConfigModel model = new EimIdfJsonLoader().load(json);
		assertEquals(EimJsonVersion.V2, model.getSchemaVersion());
		EimInstallationModel inst = model.getInstallations().get(0);
		assertEquals(InstallationStatus.FINISHED, inst.getStatus());
		assertTrue(inst.isActivatable());
	}

	@Test
	void loadsV3BrokenInstallationAsNotActivatable() throws Exception
	{
		Path json = tempDir.resolve("eim_idf.json");
		Files.writeString(json, """
				{
				  "version": "3.0",
				  "gitPath": "/usr/bin/git",
				  "idfSelectedId": "id-2",
				  "idfInstalled": [{
				    "id": "id-2",
				    "name": "v5.4",
				    "path": "/esp/v5.4/esp-idf",
				    "idfToolsPath": "/esp/tools",
				    "status": "broken"
				  }]
				}
				""");

		EimConfigModel model = new EimIdfJsonLoader().load(json);
		assertEquals(EimJsonVersion.V3, model.getSchemaVersion());
		EimInstallationModel inst = model.getInstallations().get(0);
		assertEquals(InstallationStatus.BROKEN, inst.getStatus());
		assertFalse(inst.isActivatable());

		var renderer = EimInstallationPresentationRendererFactory.forSchema(EimJsonVersion.V3);
		EimInstallationPresentation presentation = renderer.render(inst, false, false);
		assertEquals(EimInstallationPresentation.StatusKind.BROKEN, presentation.getStatusKind());
		assertFalse(presentation.isActivateEnabled());
	}
}
