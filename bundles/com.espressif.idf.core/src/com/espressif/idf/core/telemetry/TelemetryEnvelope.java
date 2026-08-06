/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.telemetry;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Builds the JSON payload accepted by the Application Insights ingestion endpoint.
 *
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public final class TelemetryEnvelope
{
	private static final String ENVELOPE_NAME = "Microsoft.ApplicationInsights.Event"; //$NON-NLS-1$
	private static final String EVENT_BASE_TYPE = "EventData"; //$NON-NLS-1$
	private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter
			.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC); //$NON-NLS-1$

	private static final Gson GSON = new Gson();

	private TelemetryEnvelope()
	{
	}

	/**
	 * @param instrumentationKey Application Insights instrumentation key
	 * @param eventName          name of the custom event, for instance <code>espressif-ide/session</code>
	 * @param tags               Application Insights context tags, such as <code>ai.user.id</code>
	 * @param properties         custom string properties reported along with the event
	 * @param timestamp          time the event occurred
	 * @return the serialized envelope
	 */
	public static String build(String instrumentationKey, String eventName, Map<String, String> tags,
			Map<String, String> properties, Instant timestamp)
	{
		JsonObject baseData = new JsonObject();
		baseData.addProperty("ver", 2); //$NON-NLS-1$
		baseData.addProperty("name", eventName); //$NON-NLS-1$
		baseData.add("properties", toJsonObject(properties)); //$NON-NLS-1$

		JsonObject data = new JsonObject();
		data.addProperty("baseType", EVENT_BASE_TYPE); //$NON-NLS-1$
		data.add("baseData", baseData); //$NON-NLS-1$

		JsonObject envelope = new JsonObject();
		envelope.addProperty("name", ENVELOPE_NAME); //$NON-NLS-1$
		envelope.addProperty("time", TIMESTAMP_FORMAT.format(timestamp)); //$NON-NLS-1$
		envelope.addProperty("iKey", instrumentationKey); //$NON-NLS-1$
		envelope.add("tags", toJsonObject(tags)); //$NON-NLS-1$
		envelope.add("data", data); //$NON-NLS-1$

		return GSON.toJson(envelope);
	}

	private static JsonObject toJsonObject(Map<String, String> values)
	{
		JsonObject object = new JsonObject();
		if (values != null)
		{
			values.forEach((key, value) -> {
				if (key != null && value != null)
				{
					object.addProperty(key, value);
				}
			});
		}
		return object;
	}
}
