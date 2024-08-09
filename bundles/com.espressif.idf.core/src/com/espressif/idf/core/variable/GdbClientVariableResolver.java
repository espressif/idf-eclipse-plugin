/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.variable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.launchbar.core.ILaunchBarManager;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.LaunchBarTargetConstants;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;

public class GdbClientVariableResolver implements IDynamicVariableResolver
{

	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException
	{
		ILaunchBarManager launchBarManager = IDFCorePlugin.getService(ILaunchBarManager.class);
		var targetName = launchBarManager.getActiveLaunchTarget().getAttribute(LaunchBarTargetConstants.TARGET,
				StringUtil.EMPTY);
		return IDFUtil.getXtensaToolchainExecutablePathByTarget(targetName);
	}

}
