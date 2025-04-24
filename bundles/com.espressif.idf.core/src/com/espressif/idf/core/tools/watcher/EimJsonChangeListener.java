/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.watcher;

import java.nio.file.Path;

/**
 * Classes that want to handle the eim_idf.json Changes must implement this listener
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public interface EimJsonChangeListener
{
	void onJsonFileChanged(Path file);
}
