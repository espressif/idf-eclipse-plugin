/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class StringUtil
{
	public static String EMPTY = ""; //$NON-NLS-1$

	public static boolean isEmpty(String text)
	{
		return text == null || text.trim().length() == 0;
	}
}
