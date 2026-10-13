/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.espressif.idf.core.build.IdfKconfigNoteParser;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class IdfKconfigNoteParserTest
{
	private static final String GCC_PARSER = "org.eclipse.cdt.core.GCCErrorParser";
	private static final String GAS_PARSER = "org.eclipse.cdt.core.GASErrorParser";

	private final IdfKconfigNoteParser parser = new IdfKconfigNoteParser();

	@ParameterizedTest
	@ValueSource(strings = {
			"NOTE: the value 'y' is invalid for SOC_MPI_OPERATIONS_NUM (defined at D:/espressif/.espressif/v6.1/esp-idf\\./components/soc/esp32/include/soc/Kconfig.soc_caps.in:642), which has type int -- assignment ignored",
			"NOTE: C:/esp/v6.1/esp-idf/components/esp_stdio/Kconfig:150: ESP_CONSOLE_UART_BAUDRATE has user-set value 115200 from C:/Users/andri/workspaceT123/blink/sdkconfig but is not visible (disabled dependency)",
			"  NOTE: assignment ignored" })
	void process_line_consumes_kconfig_notes(String line)
	{
		assertTrue(parser.processLine(line, null));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"main/hello_world_main.c:14:10: fatal error: spiram.h: No such file or directory",
			"main/hello_world_main.c:14:10: note: previous declaration of 'foo' was here",
			"main/hello_world_main.c:14:10: NOTE: previous declaration of 'foo' was here",
			"NOTE:14:10: fatal error: spiram.h: No such file or directory",
			"note: each undeclared identifier is reported only once", "" })
	void process_line_leaves_compiler_diagnostics(String line)
	{
		assertFalse(parser.processLine(line, null));
	}

	@Test
	void process_line_ignores_null()
	{
		assertFalse(parser.processLine(null, null));
	}

	@Test
	void prepend_puts_kconfig_parser_ahead_of_toolchain_parsers()
	{
		String[] parserIds = IdfKconfigNoteParser
				.prepend(new String[] { GCC_PARSER, GAS_PARSER, IdfKconfigNoteParser.ID });

		assertArrayEquals(new String[] { IdfKconfigNoteParser.ID, GCC_PARSER, GAS_PARSER }, parserIds);
	}

	@Test
	void prepend_handles_missing_parser_list()
	{
		assertArrayEquals(new String[] { IdfKconfigNoteParser.ID }, IdfKconfigNoteParser.prepend(null));
	}
}
