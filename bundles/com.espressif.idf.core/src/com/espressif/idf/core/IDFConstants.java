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
	 * idf.py list targets
	 */
	String IDF_LIST_TARGETS_CMD = "--list-targets";  //$NON-NLS-1$
	
	/**
	 * idf_monitor python file
	 */
	String IDF_MONITOR_PYTHON_SCRIPT = "idf_monitor.py"; //$NON-NLS-1$
	
	/**
	 * idf sysviewtrace_proc script file
	 */
	String IDF_SYSVIEW_TRACE_SCRIPT = "sysviewtrace_proc.py"; //$NON-NLS-1$
	
	/**
	 * idf app_trace_folder
	 */
	String IDF_APP_TRACE_FOLDER = "esp_app_trace"; //$NON-NLS-1$

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
	 * idf tools.json file for installable tools
	 */
	String IDF_TOOLS_JSON = "tools.json"; //$NON-NLS-1$

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
	
	String PYTHON3_CMD = "python3"; //$NON-NLS-1$

	/**
	 * <IDF_PATH>/components
	 */
	String COMPONENTS_FOLDER = "components"; //$NON-NLS-1$
	
	String ESP_CORE_DUMP_FOLDER = "espcoredump"; //$NON-NLS-1$
	
	String ESP_CORE_DUMP_SCRIPT = "espcoredump.py"; //$NON-NLS-1$

	/**
	 * COMPONENT_FOLDER/esptool_py
	 */
	String ESP_TOOL_FOLDER_PY = "esptool_py"; //$NON-NLS-1$

	/**
	 * ESP_TOOL_FOLDER_PY/esptool
	 */
	String ESP_TOOL_FOLDER = "esptool"; //$NON-NLS-1$

	/**
	 * <IDF_PATH>/components/esptool_py/esptool/esptool.py
	 */
	String ESP_TOOL_SCRIPT = "esptool.py"; //$NON-NLS-1$

	String ESP_TOOL_CHIP_ID_CMD = "chip_id"; //$NON-NLS-1$

	String ESP_TOOL_ERASE_FLASH_CMD = "erase_flash"; //$NON-NLS-1$
	
	String ESP_WRITE_FLASH_CMD = "write_flash"; //$NON-NLS-1$

	/**
	 * Property to store project custom build directory
	 */
	String BUILD_DIR_PROPERTY = "idf.buildDirectory.property"; //$NON-NLS-1$
	
	String PROECT_DESCRIPTION_JSON = "project_description.json"; //$NON-NLS-1$
}
