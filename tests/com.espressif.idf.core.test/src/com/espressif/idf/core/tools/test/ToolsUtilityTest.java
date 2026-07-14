/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.espressif.idf.core.tools.util.ToolsUtility;

/**
 * Unit tests for {@link ToolsUtility#parseVersionCMake(Path)}.
 */
public class ToolsUtilityTest
{
	@Test
	void full_version_includes_patch(@TempDir Path tempDir) throws IOException
	{
		Path file = writeVersionCMake(tempDir, """
				set(IDF_VERSION_MAJOR 6)
				set(IDF_VERSION_MINOR 0)
				set(IDF_VERSION_PATCH 1)
				""");
		Assertions.assertEquals("6.0.1", ToolsUtility.parseVersionCMake(file));
	}

	@Test
	void missing_patch_falls_back_to_major_minor(@TempDir Path tempDir) throws IOException
	{
		Path file = writeVersionCMake(tempDir, """
				set(IDF_VERSION_MAJOR 5)
				set(IDF_VERSION_MINOR 3)
				""");
		Assertions.assertEquals("5.3", ToolsUtility.parseVersionCMake(file));
	}

	@Test
	void missing_minor_returns_empty(@TempDir Path tempDir) throws IOException
	{
		Path file = writeVersionCMake(tempDir, "set(IDF_VERSION_MAJOR 6)\n");
		Assertions.assertEquals("", ToolsUtility.parseVersionCMake(file));
	}

	@Test
	void nonexistent_file_returns_empty(@TempDir Path tempDir)
	{
		Assertions.assertEquals("", ToolsUtility.parseVersionCMake(tempDir.resolve("version.cmake")));
	}

	@Test
	void null_file_returns_empty()
	{
		Assertions.assertEquals("", ToolsUtility.parseVersionCMake(null));
	}

	private static Path writeVersionCMake(Path dir, String content) throws IOException
	{
		Path file = dir.resolve("version.cmake");
		Files.writeString(file, content);
		return file;
	}
}
