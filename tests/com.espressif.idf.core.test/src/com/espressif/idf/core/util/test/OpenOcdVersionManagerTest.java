/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.espressif.idf.core.util.OpenOcdVersionManager;
import com.espressif.idf.core.util.OpenOcdVersionManager.OpenOcdVersion;

class OpenOcdVersionManagerTest {

	// ========================================================================
	// Regex Parsing Tests
	// ========================================================================

	@Test
	void testParseStandardVersion() {
		String output = "Open On-Chip Debugger 0.11.0\nLicensed under GNU GPL v2...";
		OpenOcdVersion version = OpenOcdVersionManager.parseVersionString(output);

		Assertions.assertEquals(0, version.major);
		Assertions.assertEquals(11, version.minor);
		Assertions.assertEquals(0, version.patch);
		Assertions.assertEquals(0, version.buildDate);
	}

	@Test
	void testParseVendorVersionWithBuildDate() {
		String output = "Open On-Chip Debugger v0.12.0-esp32-20240228\nLicensed under...";
		OpenOcdVersion version = OpenOcdVersionManager.parseVersionString(output);

		Assertions.assertEquals(0, version.major);
		Assertions.assertEquals(12, version.minor);
		Assertions.assertEquals(0, version.patch);
		Assertions.assertEquals(20240228, version.buildDate);
	}

	@Test
	void testParseAlternativeVendor() {
		String output = "Open On-Chip Debugger 0.10.3-custom123-20230101";
		OpenOcdVersion version = OpenOcdVersionManager.parseVersionString(output);

		Assertions.assertEquals(0, version.major);
		Assertions.assertEquals(10, version.minor);
		Assertions.assertEquals(3, version.patch);
		Assertions.assertEquals(20230101, version.buildDate);
	}

	@Test
	void testParseMissingPatchVersion() {
		// The regex allows the patch version to be optional
		String output = "Open On-Chip Debugger v1.2-esp32-20260424";
		OpenOcdVersion version = OpenOcdVersionManager.parseVersionString(output);

		Assertions.assertEquals(1, version.major);
		Assertions.assertEquals(2, version.minor);
		Assertions.assertEquals(0, version.patch); // Defaults to 0
		Assertions.assertEquals(20260424, version.buildDate);
	}

	@Test
	void testParseInvalidInput() {
		String output = "Some random error string or command not found";
		OpenOcdVersion version = OpenOcdVersionManager.parseVersionString(output);

		Assertions.assertEquals(0, version.major);
		Assertions.assertEquals(0, version.minor);
		Assertions.assertEquals(0, version.patch);
		Assertions.assertEquals(0, version.buildDate);
	}

	@Test
	void testParseNullAndEmptyInput() {
		OpenOcdVersion nullVersion = OpenOcdVersionManager.parseVersionString(null);
		Assertions.assertEquals(0, nullVersion.major);

		OpenOcdVersion emptyVersion = OpenOcdVersionManager.parseVersionString("   \n  ");
		Assertions.assertEquals(0, emptyVersion.major);
	}

	// ========================================================================
	// Version Logic Tests: isAtLeast(major, minor, [patch])
	// ========================================================================

	@Test
	void testIsAtLeastMajorMinor() {
		OpenOcdVersion version = new OpenOcdVersion(0, 11, 0, 0);

		Assertions.assertTrue(version.isAtLeast(0, 10)); // Newer minor
		Assertions.assertTrue(version.isAtLeast(0, 11)); // Exact match
		Assertions.assertFalse(version.isAtLeast(0, 12)); // Older minor
		Assertions.assertFalse(version.isAtLeast(1, 0)); // Older major
	}

	@Test
	void testIsAtLeastWithPatch() {
		OpenOcdVersion version = new OpenOcdVersion(0, 12, 2, 0);

		Assertions.assertTrue(version.isAtLeast(0, 12, 1)); // Newer patch
		Assertions.assertTrue(version.isAtLeast(0, 12, 2)); // Exact patch match
		Assertions.assertFalse(version.isAtLeast(0, 12, 3)); // Older patch
		Assertions.assertTrue(version.isAtLeast(0, 11, 5)); // Newer minor overrides patch
	}

	// ========================================================================
	// Version Logic Tests: isBuildDateAtLeast
	// ========================================================================

	@Test
	void testIsBuildDateAtLeast_ExactVersionNewerDate() {
		OpenOcdVersion version = new OpenOcdVersion(0, 12, 0, 20260424);

		Assertions.assertTrue(version.isBuildDateAtLeast(0, 12, 20230101), 
				"Should pass because 20260424 >= 20230101");
	}

	@Test
	void testIsBuildDateAtLeast_ExactVersionOlderDate() {
		OpenOcdVersion version = new OpenOcdVersion(0, 12, 0, 20230101);

		Assertions.assertFalse(version.isBuildDateAtLeast(0, 12, 20260424), 
				"Should fail because 20230101 < 20260424");
	}

	@Test
	void testIsBuildDateAtLeast_ExactSameDate() {
		OpenOcdVersion version = new OpenOcdVersion(0, 12, 0, 20260424);

		Assertions.assertTrue(version.isBuildDateAtLeast(0, 12, 20260424), 
				"Should pass because dates are exactly equal");
	}

	@Test
	void testIsBuildDateAtLeast_StrictlyNewerBaseVersion() {
		OpenOcdVersion version = new OpenOcdVersion(0, 13, 0, 20220101);

		Assertions.assertTrue(version.isBuildDateAtLeast(0, 12, 20260424), 
				"Should pass immediately because 0.13.0 > 0.12.0, build date is ignored");
	}

	@Test
	void testIsBuildDateAtLeast_OlderBaseVersion() {
		OpenOcdVersion version = new OpenOcdVersion(0, 11, 0, 20290101);

		Assertions.assertFalse(version.isBuildDateAtLeast(0, 12, 20260424), 
				"Should fail immediately because 0.11.0 < 0.12.0, despite newer build date");
	}

	// ========================================================================
	// API Null Safety Tests
	// ========================================================================

	@Test
	void testGetVersionNullAndEmptyPaths() {
		OpenOcdVersion nullPathVersion = OpenOcdVersionManager.getVersion(null);
		Assertions.assertEquals(0, nullPathVersion.major);

		OpenOcdVersion emptyPathVersion = OpenOcdVersionManager.getVersion("");
		Assertions.assertEquals(0, emptyPathVersion.major);
	}

	// ========================================================================
	// Model ToString Test
	// ========================================================================

	@Test
	void testToString() {
		OpenOcdVersion version = new OpenOcdVersion(0, 12, 1, 20240101);
		Assertions.assertEquals("0.12.1 (Build: 20240101)", version.toString());
	}
}
