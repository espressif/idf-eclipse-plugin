/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.nvs.dialog;

import com.espressif.idf.core.util.StringUtil;

/**
 * A Data Transfer Object (DTO) that holds the settings for the NVS Editor page, implemented as an immutable Java
 * Record.
 */
public record NvsEditorSettings(String partitionSize, boolean encryptEnabled, boolean generateKeyEnabled,
		String encryptionKeyPath)
{

	public static final String DEFAULT_PARTITION_SIZE = "0x3000"; //$NON-NLS-1$

	/**
	 * Creates a new instance with default values.
	 */
	public static NvsEditorSettings createDefault()
	{
		return new NvsEditorSettings(DEFAULT_PARTITION_SIZE, false, true, StringUtil.EMPTY);
	}
}
