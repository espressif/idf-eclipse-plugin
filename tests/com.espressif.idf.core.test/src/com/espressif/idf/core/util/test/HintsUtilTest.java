/*******************************************************************************
 * Copyright 2022-2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.espressif.idf.core.build.ReHintPair;
import com.espressif.idf.core.util.HintsUtil;
import com.espressif.idf.core.util.IDFUtil;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@TestInstance(Lifecycle.PER_CLASS)
class HintsUtilTest
{
	private static final int FIRST_SIMPLIFIED_ENTRY_INDEX = 1;
	private static final int SECOND_SIMPLIFIED_ENTRY_INDEX = 2;
	private static final Integer EXPECTED_SIZE = 86;
	private File tempTestYmlFile;

	@BeforeAll
	void setUp() throws IOException
	{
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("hints.yml");
		File tempFile = File.createTempFile("test", "yml", null);
		try (FileOutputStream fos = new FileOutputStream(tempFile))
		{
			fos.write(inputStream.readAllBytes());
		}
		tempTestYmlFile = tempFile;
	}

	@Test
	void getHintsYmlPath_returns_correct_hints_yml_path()
	{
		String hintsYmlPath = HintsUtil.getHintsYmlPath();
		String expectedResult = IDFUtil.getIDFPath() + File.separator + "tools" + File.separator + "idf_py_actions" //$NON-NLS-1$ //$NON-NLS-2$
				+ File.separator + "hints.yml";
		assertEquals(expectedResult, hintsYmlPath);
	}

	@Test
	void getReHintsList_returns_correct_parsed_list()
	{
		final String expectedFirstHint = "The parameter type of the function esp_secure_boot_read_key_digests() has been changed from ets_secure_boot_key_digests_t* to esp_secure_boot_key_digests_t*.";
		final String expectedFirstRe = "warning: passing argument 1 of 'esp_secure_boot_read_key_digests' from incompatible pointer type";

		List<ReHintPair> reHintsList = HintsUtil.getReHintsList(tempTestYmlFile);

		assertNotNull(reHintsList);
		assertEquals(EXPECTED_SIZE, reHintsList.size());
		assertEquals(expectedFirstHint, reHintsList.get(0).getHint());
		assertEquals(expectedFirstRe, reHintsList.get(0).getRe().get().pattern());
	}

	@Test
	void getReHintsList_returns_simplified_entries()
	{
		final String expectedFirstComplexHint = "Function 'bootloader_common_get_reset_reason()' has been removed. Please use the function 'esp_rom_get_reset_reason()' in the ROM component.";
		final String expectedFirstComplexRe = "error: implicit declaration of function 'bootloader_common_get_reset_reason'";
		final String expectedSecondComplexHint = "Function 'esp_efuse_get_chip_ver()' has been removed. Please use the function efuse_hal_get_major_chip_version().";
		final String expectedSecondComplexRe = "error: implicit declaration of function 'esp_efuse_get_chip_ver'";

		List<ReHintPair> reHintsList = HintsUtil.getReHintsList(tempTestYmlFile);

		assertNotNull(reHintsList);
		assertEquals(EXPECTED_SIZE, reHintsList.size());
		assertEquals(expectedFirstComplexHint, reHintsList.get(FIRST_SIMPLIFIED_ENTRY_INDEX).getHint());
		assertEquals(expectedFirstComplexRe, reHintsList.get(FIRST_SIMPLIFIED_ENTRY_INDEX).getRe().get().pattern());
		assertEquals(expectedSecondComplexHint, reHintsList.get(SECOND_SIMPLIFIED_ENTRY_INDEX).getHint());
		assertEquals(expectedSecondComplexRe, reHintsList.get(SECOND_SIMPLIFIED_ENTRY_INDEX).getRe().get().pattern());
	}

	@Test
	void getReHintsList_returns_empty_array_when_not_existing_path_is_provided()
	{
		List<ReHintPair> reHintsList = HintsUtil.getReHintsList(new File(null + "not_existing_path"));

		assertNotNull(reHintsList);
		assertEquals(new ArrayList<>(), reHintsList);
	}

	@Test
	void resolveHintsYmlFile_prefers_aggregated_hints_under_build_directory() throws IOException
	{
		File buildDir = Files.createTempDirectory("idf-build").toFile();
		File aggregated = new File(buildDir, "hints.yml");
		try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("hints.yml");
				FileOutputStream fos = new FileOutputStream(aggregated))
		{
			fos.write(inputStream.readAllBytes());
		}
		File resolved = HintsUtil.resolveHintsYmlFile(buildDir.toPath());
		assertEquals(aggregated.getCanonicalFile(), resolved.getCanonicalFile());
	}

	@Test
	void resolveHintsYmlFile_falls_back_to_legacy_path_when_build_dir_has_no_hints() throws IOException
	{
		File buildDir = Files.createTempDirectory("idf-build-empty").toFile();
		File legacy = new File(HintsUtil.getHintsYmlPath());
		if (!legacy.isFile())
		{
			return;
		}
		File resolved = HintsUtil.resolveHintsYmlFile(buildDir.toPath());
		assertEquals(legacy.getCanonicalFile(), resolved.getCanonicalFile());
	}

	@Test
	void resolveHintsYmlFile_without_build_dir_matches_legacy_when_present() throws IOException
	{
		File legacy = new File(HintsUtil.getHintsYmlPath());
		if (!legacy.isFile())
		{
			return;
		}
		File resolved = HintsUtil.resolveHintsYmlFile(null);
		assertTrue(resolved.isFile());
		assertEquals(legacy.getCanonicalFile(), resolved.getCanonicalFile());
	}

}
