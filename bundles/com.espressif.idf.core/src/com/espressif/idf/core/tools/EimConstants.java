package com.espressif.idf.core.tools;

public interface EimConstants
{
	String EIM_JSON = "eim_idf.json"; //$NON-NLS-1$
	
	String EIM_POSIX_DIR = System.getProperty("user.home").concat("/.espressif/tools/");  //$NON-NLS-1$//$NON-NLS-2$
	
	String EIM_WIN_DIR = "C:\\Espressif\\tools\\";  //$NON-NLS-1$
	
	String EIM_WIN_PATH = EIM_WIN_DIR + EIM_JSON;

	String EIM_URL = "https://dl.espressif.com/dl/eim/"; //$NON-NLS-1$
	
	String EIM_POSIX_PATH = EIM_POSIX_DIR + EIM_JSON;
	
	String INSTALL_TOOLS_FLAG = "INSTALL_TOOLS_FLAG"; //$NON-NLS-1$
	
	String TOOL_SET_CONFIG_LEGACY_CONFIG_FILE = "tool_set_config.json"; //$NON-NLS-1$
	
	String OLD_CONFIG_EXPORTED_FLAG = "OLD_CONFIG_EXPORTED_FLAG"; //$NON-NLS-1$
}
