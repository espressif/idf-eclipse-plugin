/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.eimjson;

/**
 * Lifecycle status of a single ESP-IDF installation entry in {@code eim_idf.json}.
 * Introduced in schema 3.0; older schemas are treated as {@link #FINISHED}.
 */
public enum InstallationStatus
{
	IN_PROGRESS("in_progress"), //$NON-NLS-1$
	FAILED("failed"), //$NON-NLS-1$
	FINISHED("finished"), //$NON-NLS-1$
	BEING_REPAIRED("being_repaired"), //$NON-NLS-1$
	BROKEN("broken"); //$NON-NLS-1$

	private final String jsonValue;

	InstallationStatus(String jsonValue)
	{
		this.jsonValue = jsonValue;
	}

	public String getJsonValue()
	{
		return jsonValue;
	}

	public boolean isHealthy()
	{
		return this == FINISHED;
	}

	public boolean isActivatable()
	{
		return this == FINISHED;
	}

	public static InstallationStatus fromJson(String raw)
	{
		if (raw == null || raw.isBlank())
		{
			return FINISHED;
		}
		for (InstallationStatus status : values())
		{
			if (status.jsonValue.equalsIgnoreCase(raw))
			{
				return status;
			}
		}
		return FINISHED;
	}
}
