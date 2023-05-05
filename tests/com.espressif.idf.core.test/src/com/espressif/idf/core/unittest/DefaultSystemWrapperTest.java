/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.unittest;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.espressif.idf.core.DefaultSystemWrapper;

class DefaultSystemWrapperTest
{
	private static final String PATH = "PATH"; //$NON-NLS-1$
	private static final String PATHEXT = "PATHEXT"; //$NON-NLS-1$

	@Test
	void testGetPathEnv()
	{
		assertEquals(System.getenv(PATH), new DefaultSystemWrapper().getPathEnv());
	}

	@Test
	void testGetEnvExecutables()
	{

		assertEquals(System.getenv(PATHEXT), new DefaultSystemWrapper().getEnvExecutables());

	}

}
