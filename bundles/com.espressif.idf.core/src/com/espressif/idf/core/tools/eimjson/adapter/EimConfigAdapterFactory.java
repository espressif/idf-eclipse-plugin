/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.eimjson.adapter;

import com.espressif.idf.core.tools.eimjson.EimJsonVersion;
import com.espressif.idf.core.tools.exceptions.EimVersionMismatchException;

public final class EimConfigAdapterFactory
{
	private EimConfigAdapterFactory()
	{
	}

	public static EimConfigAdapter forVersion(EimJsonVersion version)
	{
		return switch (version)
		{
			case V2 -> new EimConfigAdapterV2();
			case V3 -> new EimConfigAdapterV3();
		};
	}

	public static EimConfigAdapter forVersionString(String rawVersion) throws EimVersionMismatchException
	{
		return forVersion(EimJsonVersion.parse(rawVersion));
	}
}
