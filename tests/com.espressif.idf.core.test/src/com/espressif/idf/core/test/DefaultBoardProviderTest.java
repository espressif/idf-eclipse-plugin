/*******************************************************************************
 * Copyright 2022-2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.espressif.idf.core.DefaultBoardProvider;

class DefaultBoardProviderTest
{

	private static final int ESP32C3_EXPECTED_INDEX = 1;
	private static final int ESP32S3_EXPECTED_INDEX = 2;
	private static final int ESP32_EXPECTED_INDEX = 0;
	private static final int ESP32S2_EXPECTED_INDEX = 0;
	private static final int ESP32C2_EXPECTED_INDEX = 0;
	private static final int ESP32C6_EXPECTED_INDEX = 0;
	private static final int ESP32H2_EXPECTED_INDEX = 0;

	@ParameterizedTest
	@MethodSource("argumentsAndExpectedResultProvider")
	void getIndexOfDefaultBoard_returns_expected_index_for_esp32s3_target(String target, String[] boardArray,
			int expectedIndex)
	{

		int index = new DefaultBoardProvider().getIndexOfDefaultBoard(target, boardArray);
		assertEquals(expectedIndex, index);
	}

	@ParameterizedTest
	@MethodSource("argumentsAndExpectedResultProvider")
	void getIndexOfDefaultBoard_ignores_case_for_target_and_returns_expected_result(String target, String[] boardArray,
			int expectedIndex)
	{
		target = target.toUpperCase();

		int index = new DefaultBoardProvider().getIndexOfDefaultBoard(target, boardArray);

		assertEquals(expectedIndex, index);
	}

	static Stream<Arguments> argumentsAndExpectedResultProvider()
	{
		return Stream.of(
				Arguments.of("esp32s3",
						new String[] { "ESP32-S3 chip (via ESP-PROG)", "ESP32-S3 chip (via ESP USB Bridge)",
								"ESP32-S3 chip (via builtin USB-JTAG)" },
						ESP32S3_EXPECTED_INDEX),
				Arguments.of("esp32c3",
						new String[] { "ESP32-C3 chip (via ESP USB Bridge)", "ESP32-C3 chip (via builtin USB-JTAG)",
								"ESP32-C3 chip (via ESP-PROG)" },
						ESP32C3_EXPECTED_INDEX),
				Arguments.of("esp32",
						new String[] { "ESP-WROVER-KIT 1.8V", "ESP32 chip (via ESP USB Bridge)",
								"ESP-WROVER-KIT 3.3V" },
						ESP32_EXPECTED_INDEX),
				Arguments.of("esp32s2",
						new String[] { "ESP32-S2 chip (via ESP USB Bridge)", "ESP32-S2-KALUGA-1",
								"ESP32-S2 chip (via ESP-PROG)" },
						ESP32S2_EXPECTED_INDEX),
				Arguments.of("esp32c2",
						new String[] { "ESP32-C2 chip (via ESP-PROG)", "ESP32-C2 chip (via ESP USB Bridge)"

						}, ESP32C2_EXPECTED_INDEX),
				Arguments.of("esp32c6",
						new String[] { "ESP32-C6 chip (via builtin USB-JTAG)", "ESP32-C6 chip (via ESP-PROG)",
								"ESP32-C6 chip (via ESP USB Bridge)" },
						ESP32C6_EXPECTED_INDEX),
				Arguments.of("esp32h2", new String[] { "ESP32-H2 chip (via ESP-PROG)",
						"ESP32-H2 chip (via ESP USB Bridge)", "ESP32-H2 chip (via builtin USB-JTAG)" },
						ESP32H2_EXPECTED_INDEX));
	}
}
