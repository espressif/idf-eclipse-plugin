/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import com.espressif.idf.core.logging.Logger;

/**
 * Invokes {@link ILaunchDefaultsContributor} extensions so missing or empty launch attributes get plugin defaults.
 */
public final class LaunchDefaults
{
	public static final String EXTENSION_POINT_ID = "com.espressif.idf.core.launchDefaultsContributor"; //$NON-NLS-1$

	private LaunchDefaults()
	{
	}

	public static void apply(ILaunchConfigurationWorkingCopy workingCopy)
	{
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXTENSION_POINT_ID);
		for (IConfigurationElement element : elements)
		{
			try
			{
				Object obj = element.createExecutableExtension("class"); //$NON-NLS-1$
				if (obj instanceof ILaunchDefaultsContributor contributor)
				{
					contributor.applyDefaults(workingCopy);
				}
			}
			catch (CoreException e)
			{
				Logger.log(e);
			}
		}
	}
}
