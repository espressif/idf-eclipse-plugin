/*******************************************************************************
 * Copyright 2022-2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import com.espressif.idf.core.build.EspIdfErrorParser;
import com.espressif.idf.core.build.ReHintPair;
import com.espressif.idf.core.resources.OpenDialogListenerSupport;
import com.espressif.idf.core.resources.PopupDialog;
import com.espressif.idf.core.util.StringUtil;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class EspIdfErrorParserTest
{

	private static final String NON_EXISTING_ERROR_REGEX = "non existing error regex";
	private static final String ERROR_LINE = "/hello_world/main/hello_world_main.c:14:10: fatal error: spiram.h: No such file or directory";
	private static final String EXPECTED_HINT = "EXPECTED_HINT";
	private static final String ERROR_REGEX = "fatal error: (spiram.h|esp_spiram.h): No such file or directory";

	@Test
	void process_line_returns_true_if_hint_available_for_error_line()
	{
		List<ReHintPair> reHintPairs = new ArrayList<>();
		reHintPairs.add(new ReHintPair(ERROR_REGEX, StringUtil.EMPTY));
		String errorLine = ERROR_LINE;
		EspIdfErrorParser ep = new EspIdfErrorParser(reHintPairs);

		boolean actualResult = ep.processLine(errorLine);

		assertTrue(actualResult);
	}

	@Test
	void process_line_returns_false_if_no_hint_found_for_error_line()
	{
		List<ReHintPair> reHintPairs = new ArrayList<>();
		reHintPairs.add(new ReHintPair(NON_EXISTING_ERROR_REGEX, StringUtil.EMPTY));
		String errorLine = ERROR_LINE;
		EspIdfErrorParser ep = new EspIdfErrorParser(reHintPairs);

		boolean actualResult = ep.processLine(errorLine);

		assertFalse(actualResult);
	}

	@SuppressWarnings("unchecked")
	@Test
	void shutdown_should_trigger_property_change_listener_with_error_hints_pairs_as_new_value()
	{
		List<ReHintPair> actualReHintPair = new ArrayList<>();
		OpenDialogListenerSupport.getSupport().addPropertyChangeListener(evt -> {
			PopupDialog popupDialog = PopupDialog.valueOf(evt.getPropertyName());
			if (popupDialog.equals(PopupDialog.AVAILABLE_HINTS))
				actualReHintPair.addAll((List<ReHintPair>) evt.getNewValue());
		});
		List<ReHintPair> reHintPairs = new ArrayList<>();
		String expectedHint = EXPECTED_HINT;
		reHintPairs.add(new ReHintPair(ERROR_REGEX, expectedHint));
		String errorLine = ERROR_LINE;

		EspIdfErrorParser ep = new EspIdfErrorParser(reHintPairs);
		boolean actualResult = ep.processLine(errorLine);
		ep.shutdown();

		assertTrue(actualResult);
		assertEquals(expectedHint, actualReHintPair.get(0).getHint());
		assertEquals(errorLine, actualReHintPair.get(0).getRe().get().pattern());
	}

}
