/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.core.variable.JtagVariableResolver;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JtagVariableResolverTest
{

	@Test
	void formatFlashVoltageArgument_wraps_the_tcl_command_and_the_value()
	{
		assertEquals("-c 'set ESP32_FLASH_VOLTAGE 3.3' ", //$NON-NLS-1$
				JtagVariableResolver.formatFlashVoltageArgument("3.3", true)); //$NON-NLS-1$
		assertEquals("-c 'set ESP32_FLASH_VOLTAGE 1.8' ", //$NON-NLS-1$
				JtagVariableResolver.formatFlashVoltageArgument("1.8", true)); //$NON-NLS-1$
	}

	@Test
	void formatFlashVoltageArgument_omits_default_and_unsupported_targets()
	{
		assertEquals(StringUtil.EMPTY, JtagVariableResolver.formatFlashVoltageArgument("default", true)); //$NON-NLS-1$
		assertEquals(StringUtil.EMPTY, JtagVariableResolver.formatFlashVoltageArgument("3.3", false)); //$NON-NLS-1$
		assertEquals(StringUtil.EMPTY, JtagVariableResolver.formatFlashVoltageArgument(null, true));
		assertEquals(StringUtil.EMPTY, JtagVariableResolver.formatFlashVoltageArgument("  ", true)); //$NON-NLS-1$
	}
}
