/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.telemetry.test;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import com.espressif.idf.core.telemetry.TelemetryEnvelope;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class TelemetryEnvelopeTest
{
	private static final Instant TIMESTAMP = Instant.parse("2026-08-05T10:15:30.500Z");

	@Test
	void test_builds_application_insights_event_envelope()
	{
		String payload = TelemetryEnvelope.build("abc-123", "espressif-ide/session", Map.of("ai.user.id", "install-1"),
				Map.of("os", "Mac OS X"), TIMESTAMP);

		JsonObject envelope = JsonParser.parseString(payload).getAsJsonObject();
		Assertions.assertEquals("Microsoft.ApplicationInsights.Event", envelope.get("name").getAsString());
		Assertions.assertEquals("abc-123", envelope.get("iKey").getAsString());
		Assertions.assertEquals("2026-08-05T10:15:30.500Z", envelope.get("time").getAsString());
		Assertions.assertEquals("install-1",
				envelope.getAsJsonObject("tags").get("ai.user.id").getAsString());

		JsonObject data = envelope.getAsJsonObject("data");
		Assertions.assertEquals("EventData", data.get("baseType").getAsString());

		JsonObject baseData = data.getAsJsonObject("baseData");
		Assertions.assertEquals("espressif-ide/session", baseData.get("name").getAsString());
		Assertions.assertEquals(2, baseData.get("ver").getAsInt());
		Assertions.assertEquals("Mac OS X", baseData.getAsJsonObject("properties").get("os").getAsString());
	}

	@Test
	void test_builds_envelope_without_tags_and_properties()
	{
		String payload = TelemetryEnvelope.build("abc-123", "espressif-ide/install", null, null, TIMESTAMP);

		JsonObject envelope = JsonParser.parseString(payload).getAsJsonObject();
		Assertions.assertTrue(envelope.getAsJsonObject("tags").isEmpty());
		Assertions.assertTrue(envelope.getAsJsonObject("data").getAsJsonObject("baseData").getAsJsonObject("properties")
				.isEmpty());
	}
}
