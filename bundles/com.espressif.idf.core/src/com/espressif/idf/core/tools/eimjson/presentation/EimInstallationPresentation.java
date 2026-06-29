/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.eimjson.presentation;

/**
 * UI-neutral presentation hints for one installation row (text + interaction flags).
 * SWT-specific colours are resolved in the UI renderer layer.
 * Label text and status mapping live in {@link DefaultEimInstallationPresentationRenderer}.
 */
public final class EimInstallationPresentation
{
	public enum StatusKind
	{
		ACTIVE,
		INACTIVE,
		SETTING_UP,
		IN_PROGRESS,
		FAILED,
		BEING_REPAIRED,
		BROKEN
	}

	private final StatusKind statusKind;
	private final String statusText;
	private final boolean activateEnabled;
	private final boolean reinstallEnabled;

	public EimInstallationPresentation(StatusKind statusKind, String statusText, boolean activateEnabled,
			boolean reinstallEnabled)
	{
		this.statusKind = statusKind;
		this.statusText = statusText;
		this.activateEnabled = activateEnabled;
		this.reinstallEnabled = reinstallEnabled;
	}

	public StatusKind getStatusKind()
	{
		return statusKind;
	}

	public String getStatusText()
	{
		return statusText;
	}

	public boolean isActivateEnabled()
	{
		return activateEnabled;
	}

	public boolean isReinstallEnabled()
	{
		return reinstallEnabled;
	}
}
