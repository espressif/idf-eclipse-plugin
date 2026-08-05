/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.telemetry;

import java.net.URI;
import java.util.Locale;
import java.util.Optional;

/**
 * Azure Application Insights connection string, holding the instrumentation key and the regional ingestion endpoint.
 *
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public final class TelemetryConnection
{
	private static final String INSTRUMENTATION_KEY = "instrumentationkey"; //$NON-NLS-1$
	private static final String INGESTION_ENDPOINT = "ingestionendpoint"; //$NON-NLS-1$
	private static final String DEFAULT_INGESTION_ENDPOINT = "https://dc.services.visualstudio.com/"; //$NON-NLS-1$
	private static final String TRACK_PATH = "v2/track"; //$NON-NLS-1$

	private final String instrumentationKey;
	private final String ingestionEndpoint;

	private TelemetryConnection(String instrumentationKey, String ingestionEndpoint)
	{
		this.instrumentationKey = instrumentationKey;
		this.ingestionEndpoint = ingestionEndpoint;
	}

	/**
	 * Parses an Application Insights connection string of the form
	 * <code>InstrumentationKey=&lt;key&gt;;IngestionEndpoint=&lt;url&gt;</code>. Unknown fields are ignored and the
	 * ingestion endpoint falls back to the global one when absent.
	 *
	 * @param connectionString connection string to parse, may be <code>null</code>
	 * @return the parsed connection or an empty optional when no instrumentation key is present
	 */
	public static Optional<TelemetryConnection> parse(String connectionString)
	{
		if (connectionString == null || connectionString.isBlank())
		{
			return Optional.empty();
		}

		String key = null;
		String endpoint = DEFAULT_INGESTION_ENDPOINT;
		for (String field : connectionString.split(";")) //$NON-NLS-1$
		{
			int separator = field.indexOf('=');
			if (separator <= 0)
			{
				continue;
			}
			String name = field.substring(0, separator).trim().toLowerCase(Locale.ENGLISH);
			String value = field.substring(separator + 1).trim();
			if (value.isEmpty())
			{
				continue;
			}
			if (INSTRUMENTATION_KEY.equals(name))
			{
				key = value;
			}
			else if (INGESTION_ENDPOINT.equals(name))
			{
				endpoint = value;
			}
		}

		if (key == null)
		{
			return Optional.empty();
		}
		if (!endpoint.endsWith("/")) //$NON-NLS-1$
		{
			endpoint = endpoint + "/"; //$NON-NLS-1$
		}
		return Optional.of(new TelemetryConnection(key, endpoint));
	}

	public String getInstrumentationKey()
	{
		return instrumentationKey;
	}

	public String getIngestionEndpoint()
	{
		return ingestionEndpoint;
	}

	/**
	 * @return endpoint accepting the telemetry envelopes
	 */
	public URI getTrackUri()
	{
		return URI.create(ingestionEndpoint + TRACK_PATH);
	}
}
