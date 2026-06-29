/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.eimjson;

import com.espressif.idf.core.tools.exceptions.EimVersionMismatchException;

/**
 * Known {@code eim_idf.json} schema versions and compatibility helpers.
 */
public enum EimJsonVersion
{
	V2("2.0"), //$NON-NLS-1$
	V3("3.0"); //$NON-NLS-1$

	/** Highest schema version this IDE build understands. */
	public static final String SUPPORTED_MAX = V3.schemaVersion;

	private final String schemaVersion;

	EimJsonVersion(String schemaVersion)
	{
		this.schemaVersion = schemaVersion;
	}

	public String getSchemaVersion()
	{
		return schemaVersion;
	}

	public static EimJsonVersion parse(String raw) throws EimVersionMismatchException
	{
		if (raw == null || raw.isBlank())
		{
			return V2;
		}

		double found = parseVersionNumber(raw);
		double maxSupported = parseVersionNumber(SUPPORTED_MAX);
		if (found > maxSupported)
		{
			throw new EimVersionMismatchException(SUPPORTED_MAX, raw);
		}

		if (found >= parseVersionNumber(V3.schemaVersion))
		{
			return V3;
		}
		return V2;
	}

	private static double parseVersionNumber(String version)
	{
		return Double.parseDouble(version.trim());
	}
}
