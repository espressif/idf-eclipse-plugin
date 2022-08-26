/*******************************************************************************
 * Copyright 2022-2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

/**
 * Utility class for getting size in bytes. Size can be specified as decimal numbers, hex numbers with the prefix 0x, or
 * size multipliers K or M (1024 and 1024*1024 bytes).
 * 
 * @author Denys Almazov
 *
 */
public class DataSizeUtil
{
	private static final long K = 1024;
	private static final long M = K * K;
	private static final String M_MULTIPIER = "M"; //$NON-NLS-1$
	private static final String K_MULTIPIER = "K"; //$NON-NLS-1$
	private static final String HEX_PREFIX = "0x"; //$NON-NLS-1$

	public static long parseSize(String size) {

		long sizeInBytes;
		if (size.contains(M_MULTIPIER))
		{
			sizeInBytes = Integer.parseInt(size.replace(M_MULTIPIER, "")) * M; //$NON-NLS-1$
		}
		else if (size.contains(K_MULTIPIER))
		{
			sizeInBytes = Integer.parseInt(size.replace(K_MULTIPIER, "")) * K; //$NON-NLS-1$
		}
		else if (size.contains(HEX_PREFIX))
		{
			sizeInBytes = Integer.parseInt(size.substring(2), 16);
		}
		else
		{
			sizeInBytes = Integer.parseInt(size);
		}
			
		return sizeInBytes;
	}
}
