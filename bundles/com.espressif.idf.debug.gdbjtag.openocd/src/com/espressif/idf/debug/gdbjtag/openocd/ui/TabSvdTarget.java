/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd.ui;

import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.embedcdt.debug.gdbjtag.core.ConfigurationAttributes;
import org.eclipse.embedcdt.debug.gdbjtag.ui.TabSvd;

/**
 * Svd target class for loading the svd files from the plugin directly
 * 
 * @author Ali Azam Rana
 *
 */
public class TabSvdTarget extends TabSvd
{
	private static final String ESP_SVD_PATH = "esp_svd_path"; //$NON-NLS-1$

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
	{
		configuration.setAttribute(ConfigurationAttributes.SVD_PATH,
				VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression(ESP_SVD_PATH, null));
		super.setDefaults(configuration);
	}

}
