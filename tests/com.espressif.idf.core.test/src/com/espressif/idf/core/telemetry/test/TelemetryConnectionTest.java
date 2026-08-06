/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.telemetry.test;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.espressif.idf.core.telemetry.TelemetryConnection;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class TelemetryConnectionTest
{
	@Test
	void test_parses_key_and_regional_endpoint()
	{
		Optional<TelemetryConnection> connection = TelemetryConnection
				.parse("InstrumentationKey=abc-123;IngestionEndpoint=https://eastasia-0.in.applicationinsights.azure.com/");

		Assertions.assertTrue(connection.isPresent());
		Assertions.assertEquals("abc-123", connection.get().getInstrumentationKey());
		Assertions.assertEquals("https://eastasia-0.in.applicationinsights.azure.com/v2/track",
				connection.get().getTrackUri().toString());
	}

	@Test
	void test_falls_back_to_global_endpoint_when_not_specified()
	{
		Optional<TelemetryConnection> connection = TelemetryConnection.parse("InstrumentationKey=abc-123");

		Assertions.assertTrue(connection.isPresent());
		Assertions.assertEquals("https://dc.services.visualstudio.com/v2/track",
				connection.get().getTrackUri().toString());
	}

	@Test
	void test_appends_missing_trailing_slash_to_endpoint()
	{
		Optional<TelemetryConnection> connection = TelemetryConnection
				.parse("InstrumentationKey=abc-123;IngestionEndpoint=https://example.invalid");

		Assertions.assertTrue(connection.isPresent());
		Assertions.assertEquals("https://example.invalid/v2/track", connection.get().getTrackUri().toString());
	}

	@Test
	void test_ignores_unknown_fields_and_is_case_insensitive()
	{
		Optional<TelemetryConnection> connection = TelemetryConnection
				.parse("LiveEndpoint=https://live.invalid/;instrumentationkey=abc-123;ApplicationId=xyz");

		Assertions.assertTrue(connection.isPresent());
		Assertions.assertEquals("abc-123", connection.get().getInstrumentationKey());
	}

	@ParameterizedTest(name = "connection string ''{0}'' is rejected")
	@ValueSource(strings = { "", "   ", "IngestionEndpoint=https://example.invalid/", "InstrumentationKey=",
			"garbage" })
	void test_rejects_connection_string_without_key(String connectionString)
	{
		Assertions.assertTrue(TelemetryConnection.parse(connectionString).isEmpty());
	}

	@Test
	void test_rejects_null_connection_string()
	{
		Assertions.assertTrue(TelemetryConnection.parse(null).isEmpty());
	}
}
