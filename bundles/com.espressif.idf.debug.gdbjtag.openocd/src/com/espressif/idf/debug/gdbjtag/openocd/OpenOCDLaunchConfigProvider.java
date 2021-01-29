/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;

import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.launch.serial.core.IDFCoreLaunchConfigProvider;

public class OpenOCDLaunchConfigProvider extends IDFCoreLaunchConfigProvider
{

	@Override
	public boolean supports(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException
	{
		return target != null && target.getTypeId().equals(IDFLaunchConstants.ESP_LAUNCH_TARGET_TYPE);
	}

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException
	{
		return DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfigurationType(IDFLaunchConstants.DEBUG_LAUNCH_CONFIG_TYPE);
	}

}
