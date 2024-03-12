/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools;

/**
 * Interface to store the constants related to tools management wizard
 * 
 * @author Ali Azam Rana
 *
 */
public interface IToolsInstallationWizardConstants
{
	String PYTHON_PATH_NODE_KEY = "PYTHON_EXECUTABLE"; //$NON-NLS-1$

	String GIT_PATH_NODE_KEY = "GIT_EXECUTABLE"; //$NON-NLS-1$
	
	String ESPRESSIF_LOGO = "icons/tools/espressif-logo.png"; //$NON-NLS-1$ 
	
	String INSTALL_TOOLS_FLAG = "INSTALL_TOOLS_FLAG"; //$NON-NLS-1$
	
	String TOOL_SET_CONFIG_FILE = "tool_set_config.json"; //$NON-NLS-1$
}
