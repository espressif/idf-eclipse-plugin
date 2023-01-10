/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.common.configs;

/**
 * Default configuration constant interface to hold information about the properties or property files
 * 
 * @author Ali Azam Rana
 *
 */
public interface IDefaultConfigConstants
{
	/**
	 * The default property file for tests for Linux inside configs folder
	 */
	String DEFAULT_CONFIG_PROPERTY_FILE_LINUX = "default-test-linux.properties";
	
	/**
	 * The default property file for tests for Windows inside configs folder
	 */
	String DEFAULT_CONFIG_PROPERTY_FILE_WINDOWS = "default-test-win.properties";

	/**
	 * The default file directory inside resources folder
	 */
	String DEFAULT_FILE_DIRECTORY = "default-files";
}
