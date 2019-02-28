/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public interface IDFConstants
{
	/**
	 * CMake projects file identifier.
	 */
	String CMAKE_FILE = "CMakeLists.txt"; //$NON-NLS-1$

	/**
	 * Default IDF template manifest file which knows where to copy the default project artifacts.
	 */
	String IDF_TEMPLATE_MANIFEST_PATH = "templates/esp-idf-template/manifest.xml"; //$NON-NLS-1$

	/**
	 * IDF project description file.
	 */
	String PROJECT_DESC_IDENTIFIER = "README.md"; //$NON-NLS-1$

	/**
	 * IDF examples folder name
	 */
	String TEMPLATE_FOLDER_NAME = "examples"; //$NON-NLS-1$

	/**
	 * Default template from the IDF examples
	 */
	String DEFAULT_TEMPLATE_ID = "hello_world"; //$NON-NLS-1$

}
