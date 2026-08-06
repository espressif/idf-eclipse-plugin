/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.telemetry.test;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import com.espressif.idf.core.telemetry.TelemetryService;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class TelemetrySessionIntervalTest
{
	private static final long NOW = TimeUnit.DAYS.toMillis(20000);
	private static final long ONE_DAY = TimeUnit.HOURS.toMillis(24);

	@Test
	void test_reports_when_never_reported_before()
	{
		Assertions.assertTrue(TelemetryService.shouldReportSession(0L, NOW));
	}

	@Test
	void test_reports_once_a_day()
	{
		Assertions.assertTrue(TelemetryService.shouldReportSession(NOW - ONE_DAY, NOW));
		Assertions.assertTrue(TelemetryService.shouldReportSession(NOW - ONE_DAY - 1, NOW));
	}

	@Test
	void test_skips_report_within_the_same_day()
	{
		Assertions.assertFalse(TelemetryService.shouldReportSession(NOW - TimeUnit.HOURS.toMillis(23), NOW));
		Assertions.assertFalse(TelemetryService.shouldReportSession(NOW, NOW));
	}

	@Test
	void test_reports_when_the_clock_moved_backwards()
	{
		Assertions.assertTrue(TelemetryService.shouldReportSession(NOW + ONE_DAY, NOW));
	}
}
