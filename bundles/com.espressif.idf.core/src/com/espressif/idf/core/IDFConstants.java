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

	/**
	 * idf python file
	 */
	String IDF_PYTHON_SCRIPT = "idf.py"; //$NON-NLS-1$

	/**
	 * idf tools file
	 */
	String IDF_TOOLS_SCRIPT = "idf_tools.py"; //$NON-NLS-1$
	
	/**
	 * idf_monitor.py
	 */
	String IDF_MONITOR_SCRIPT = "idf_monitor.py"; //$NON-NLS-1$
	
	/**
	 * idf size file
	 */
	String IDF_SIZE_SCRIPT = "idf_size.py"; //$NON-NLS-1$

	/**
	 * <IDF_PATH>/tools
	 */
	String TOOLS_FOLDER = "tools"; //$NON-NLS-1$
	
	String FLASH_CMD = "flash"; //$NON-NLS-1$

	/**
	 * <IDF_PATH>/tools/cmake
	 */
	String CMAKE_FOLDER = "cmake"; //$NON-NLS-1$

	/**
	 * kconfig_menus.json file folder name
	 */
	String CONFIG_FOLDER = "config"; //$NON-NLS-1$

	/**
	 * idf.py confserver
	 */
	String CONF_SERVER_CMD = "confserver"; //$NON-NLS-1$

	/**
	 * Json config menu file name
	 */
	String KCONFIG_MENUS_JSON = "kconfig_menus.json"; //$NON-NLS-1$
	
	/**
	 * sdk config json file
	 */
	String SDKCONFIG_JSON_FILE_NAME = "sdkconfig.json"; //$NON-NLS-1$

	/**
	 * SDK configuration file
	 */
	String SDKCONFIG_FILE_NAME = "sdkconfig"; //$NON-NLS-1$

	/**
	 * build folder for idf projects
	 */
	String BUILD_FOLDER = "build"; //$NON-NLS-1$

	/**
	 * <IDF_PATH>/tools/idf_tools.py export
	 */
	String TOOLS_EXPORT_CMD = "export"; //$NON-NLS-1$

	String TOOLS_INSTALL_CMD = "install"; //$NON-NLS-1$

	String TOOLS_INSTALL_ALL_CMD = "all"; //$NON-NLS-1$
	
	String TOOLS_INSTALL_PYTHON_CMD = "install-python-env"; //$NON-NLS-1$
	
	String TOOLS_LIST_CMD = "list"; //$NON-NLS-1$

	String TOOLS_EXPORT_FORMAT_KEYVALUE = "key-value"; //$NON-NLS-1$

	String TOOLS_EXPORT_CMD_FORMAT = "--format"; //$NON-NLS-1$

	String TOOLS_EXPORT_CMD_FORMAT_VAL = TOOLS_EXPORT_CMD_FORMAT + "=" + TOOLS_EXPORT_FORMAT_KEYVALUE; //$NON-NLS-1$
	
	String PYTHON_CMD = "python"; //$NON-NLS-1$

}
