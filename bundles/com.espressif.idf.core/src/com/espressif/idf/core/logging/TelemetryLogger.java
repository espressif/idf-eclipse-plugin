/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.logging;

import java.util.Map;

import com.microsoft.applicationinsights.TelemetryClient;

/**
 * Azure Telemetry logger
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class TelemetryLogger
{
	private static final TelemetryClient telemetryClient = new TelemetryClient();

	public static void logEvent(String eventName, Map<String, String> properties)
	{
		telemetryClient.trackEvent(eventName, properties, null);
		telemetryClient.flush();
	}
}
