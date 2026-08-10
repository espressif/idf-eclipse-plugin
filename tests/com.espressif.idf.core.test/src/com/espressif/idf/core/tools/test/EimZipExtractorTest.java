/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.Platform;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.espressif.idf.core.tools.EimZipExtractor;

class EimZipExtractorTest
{
	@TempDir
	Path tempDir;

	@Test
	void extractsPlainEimBinaryFromLegacyZip() throws Exception
	{
		Path zip = tempDir.resolve("legacy-eim.zip");
		Path dest = tempDir.resolve("out-legacy");
		// Payload larger than the symlink heuristic threshold and not a sibling path name.
		String payload = "A".repeat(1024);
		writeSingleFileZip(zip, "eim", payload);

		Path launchPath = EimZipExtractor.extract(zip, dest);

		Path eim = dest.resolve("eim");
		assertEquals(eim, launchPath);
		assertTrue(Files.isRegularFile(eim));
		assertFalse(Files.isSymbolicLink(eim));
		assertEquals(payload, Files.readString(eim));
		if (!Platform.OS_WIN32.equals(Platform.getOS()))
		{
			assertTrue(Files.isExecutable(eim));
		}
	}

	@Test
	void repairsMaterializedSymlinkFromVersionedZip() throws Exception
	{
		Path zip = tempDir.resolve("versioned-eim.zip");
		Path dest = tempDir.resolve("out-versioned");
		// Simulate ZipInputStream behavior: symlink stored as a tiny regular file with target text.
		writeTwoFileZip(zip, "eim_v0.17.4", "real-binary-bytes", "eim", "eim_v0.17.4");

		Path launchPath = EimZipExtractor.extract(zip, dest);

		Path versioned = dest.resolve("eim_v0.17.4");
		Path eim = dest.resolve("eim");
		assertTrue(Files.isRegularFile(versioned));
		assertEquals(eim, launchPath);
		assertEquals("real-binary-bytes", Files.readString(versioned));

		if (Platform.OS_WIN32.equals(Platform.getOS()))
		{
			// Fallback on Windows: copy of the versioned binary (symlink may be unavailable).
			assertTrue(Files.isRegularFile(eim));
			assertEquals("real-binary-bytes", Files.readString(eim));
		}
		else
		{
			assertTrue(Files.isSymbolicLink(eim));
			assertEquals(Path.of("eim_v0.17.4"), Files.readSymbolicLink(eim));
			assertEquals("real-binary-bytes", Files.readString(eim));
			assertTrue(Files.isExecutable(versioned));
		}
	}

	@Test
	void doesNotTreatLargeEimFileAsSymlinkPayload() throws Exception
	{
		Path dest = tempDir.resolve("probe");
		Files.createDirectories(dest);
		Path versioned = dest.resolve("eim_v0.17.4");
		Files.writeString(versioned, "real-binary");
		Path eim = dest.resolve("eim");
		Files.writeString(eim, "X".repeat(1024));

		assertFalse(EimZipExtractor.looksLikeMaterializedSymlinkPayload(eim, dest));
		EimZipExtractor.repairMaterializedEimSymlink(dest);
		assertFalse(Files.isSymbolicLink(eim));
		assertEquals("X".repeat(1024), Files.readString(eim));
	}

	@Test
	void detectsTinySiblingPathPayloadAsMaterializedSymlink() throws Exception
	{
		Path dest = tempDir.resolve("probe2");
		Files.createDirectories(dest);
		Files.writeString(dest.resolve("eim_v0.17.4"), "real-binary");
		Path eim = dest.resolve("eim");
		Files.writeString(eim, "eim_v0.17.4");

		assertTrue(EimZipExtractor.looksLikeMaterializedSymlinkPayload(eim, dest));
	}

	@Test
	void rejectsUnsafeSymlinkTargetNamesInDetectionAndRepair() throws Exception
	{
		Path dest = tempDir.resolve("probe-unsafe");
		Files.createDirectories(dest);
		Files.writeString(dest.resolve("eim_v..0"), "real-binary");

		Path eim = dest.resolve("eim");
		Files.writeString(eim, "eim_v..0");

		assertFalse(EimZipExtractor.isSafeSymlinkTargetName("eim_v..0"));
		assertFalse(EimZipExtractor.isSafeSymlinkTargetName("eim\r_v0.17.4"));
		assertFalse(EimZipExtractor.looksLikeMaterializedSymlinkPayload(eim, dest));

		EimZipExtractor.repairMaterializedEimSymlink(dest);
		assertFalse(Files.isSymbolicLink(eim));
		assertEquals("eim_v..0", Files.readString(eim));
	}

	private static void writeSingleFileZip(Path zipPath, String entryName, String payload) throws IOException
	{
		try (OutputStream out = Files.newOutputStream(zipPath); ZipOutputStream zos = new ZipOutputStream(out))
		{
			zos.putNextEntry(new ZipEntry(entryName));
			zos.write(payload.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
		}
	}

	private static void writeTwoFileZip(Path zipPath, String firstName, String firstPayload, String secondName,
			String secondPayload) throws IOException
	{
		try (OutputStream out = Files.newOutputStream(zipPath); ZipOutputStream zos = new ZipOutputStream(out))
		{
			zos.putNextEntry(new ZipEntry(firstName));
			zos.write(firstPayload.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();

			zos.putNextEntry(new ZipEntry(secondName));
			zos.write(secondPayload.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
		}
	}
}
