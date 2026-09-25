/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.telemetry.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.espressif.idf.core.telemetry.TelemetryMachineIdentity;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class TelemetryMachineIdentityTest
{
	private static final byte[] VALID_MAC = { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55 };
	private static final String VALID_MAC_HASH = "66fd6ef831e4ec98957030be94189a78ca3c2986dbed57a3b97985d862c217af";

	@TempDir
	Path temporaryDirectory;

	@Test
	void test_hashes_the_first_valid_mac_like_vscode()
	{
		TelemetryMachineIdentity.Identity identity = TelemetryMachineIdentity
				.fromHardwareAddresses(List.of(new byte[6], VALID_MAC), temporaryDirectory.resolve("machine-id"));

		Assertions.assertEquals(VALID_MAC_HASH, identity.id());
		Assertions.assertEquals(TelemetryMachineIdentity.MAC_SOURCE, identity.source());
	}

	@Test
	void test_skips_mac_addresses_rejected_by_vscode()
	{
		byte[] broadcast = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
		byte[] privateMac = { (byte) 0xac, (byte) 0xde, 0x48, 0x00, 0x11, 0x22 };

		TelemetryMachineIdentity.Identity identity = TelemetryMachineIdentity.fromHardwareAddresses(
				List.of(new byte[6], broadcast, privateMac, VALID_MAC), temporaryDirectory.resolve("machine-id"));

		Assertions.assertEquals(VALID_MAC_HASH, identity.id());
		Assertions.assertEquals(TelemetryMachineIdentity.MAC_SOURCE, identity.source());
	}

	@Test
	void test_persists_and_reuses_fallback_when_no_mac_is_available() throws Exception
	{
		Path fallbackFile = temporaryDirectory.resolve("nested/machine-id");

		TelemetryMachineIdentity.Identity first = TelemetryMachineIdentity.fromHardwareAddresses(List.of(),
				fallbackFile);
		TelemetryMachineIdentity.Identity second = TelemetryMachineIdentity.fromHardwareAddresses(List.of(),
				fallbackFile);

		Assertions.assertEquals(TelemetryMachineIdentity.FALLBACK_SOURCE, first.source());
		Assertions.assertEquals(first, second);
		Assertions.assertEquals(first.id(), Files.readString(fallbackFile).trim());
		Assertions.assertDoesNotThrow(() -> java.util.UUID.fromString(first.id()));
	}

	@Test
	void test_repairs_an_invalid_fallback_file() throws Exception
	{
		Path fallbackFile = temporaryDirectory.resolve("machine-id");
		Files.writeString(fallbackFile, "not-a-uuid");

		TelemetryMachineIdentity.Identity identity = TelemetryMachineIdentity.fromHardwareAddresses(List.of(),
				fallbackFile);

		Assertions.assertEquals(TelemetryMachineIdentity.FALLBACK_SOURCE, identity.source());
		Assertions.assertDoesNotThrow(() -> java.util.UUID.fromString(identity.id()));
		Assertions.assertEquals(identity.id(), Files.readString(fallbackFile).trim());
	}

	@Test
	void test_marks_an_unpersisted_fallback_as_ephemeral() throws Exception
	{
		Path regularFile = temporaryDirectory.resolve("not-a-directory");
		Files.writeString(regularFile, "content");

		TelemetryMachineIdentity.Identity identity = TelemetryMachineIdentity.fromHardwareAddresses(List.of(),
				regularFile.resolve("machine-id"));

		Assertions.assertEquals(TelemetryMachineIdentity.EPHEMERAL_SOURCE, identity.source());
		Assertions.assertFalse(identity.isPersistent());
	}
}
