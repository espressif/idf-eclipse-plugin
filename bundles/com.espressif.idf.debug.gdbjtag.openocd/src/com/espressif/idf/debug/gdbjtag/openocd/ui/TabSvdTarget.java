/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.embedcdt.debug.gdbjtag.core.ConfigurationAttributes;
import org.eclipse.embedcdt.debug.gdbjtag.ui.TabSvd;

import com.espressif.idf.core.logging.Logger;

/**
 * Svd target class for loading the svd files from the plugin directly
 * 
 * @author Ali Azam Rana
 *
 */
public class TabSvdTarget extends TabSvd implements ILaunchConfigurationTab
{
	private static final String ESP_SVD_PATH = "esp_svd_path"; //$NON-NLS-1$

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
	{
		configuration.setAttribute(ConfigurationAttributes.SVD_PATH,
				VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression(ESP_SVD_PATH, null));
		super.setDefaults(configuration);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration)
	{
		try
		{
			if (!configuration.getAttributes().containsKey(ConfigurationAttributes.SVD_PATH))
			{
				var wc = configuration.getWorkingCopy();
				wc.setAttribute(ConfigurationAttributes.SVD_PATH, VariablesPlugin
						.getDefault().getStringVariableManager().generateVariableExpression(ESP_SVD_PATH, null));
				wc.doSave();
			}

		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		super.initializeFrom(configuration);
	}

}
