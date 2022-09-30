/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd.ui;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.embedcdt.debug.gdbjtag.core.ConfigurationAttributes;
import org.eclipse.embedcdt.debug.gdbjtag.ui.TabSvd;
import org.eclipse.launchbar.core.ILaunchBarManager;

import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.debug.gdbjtag.openocd.Activator;

/**
 * Svd target class for loading the svd files from the plugin directly
 * @author Ali Azam Rana
 *
 */
public class TabSvdTarget extends TabSvd
{
	public TabSvdTarget()
	{
		super();
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration)
	{
		ILaunchConfigurationWorkingCopy wc = ((ILaunchConfigurationWorkingCopy) configuration);
		String selectedTarget = StringUtil.EMPTY;
		try
		{
			selectedTarget = wc.getAttribute(IDFLaunchConstants.TARGET_FOR_JTAG, StringUtil.EMPTY);
			if (StringUtil.isEmpty(selectedTarget))
			{
				selectedTarget = Activator.getService(ILaunchBarManager.class).getActiveLaunchTarget()
						.getAttribute(IDFLaunchConstants.ATTR_IDF_TARGET, StringUtil.EMPTY);
			}
			URL svdUrl = Platform.getBundle(Activator.PLUGIN_ID).getEntry("svd/".concat(selectedTarget.concat(".svd")));
			String selectedTargetPath = new File(FileLocator.resolve(svdUrl).toURI()).getPath();
			String currentSvdPath = configuration.getAttribute(ConfigurationAttributes.SVD_PATH, StringUtil.EMPTY);
			if (StringUtil.isEmpty(currentSvdPath) && !currentSvdPath.equals(selectedTargetPath))
			{
				((ILaunchConfigurationWorkingCopy) configuration).setAttribute(ConfigurationAttributes.SVD_PATH,
						selectedTargetPath);
			}
		}
		catch (Exception e)
		{
			selectedTarget = StringUtil.EMPTY;
			Logger.log(e);
		}

		super.initializeFrom(configuration);
	}
}
