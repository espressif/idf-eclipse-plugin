package com.espressif.idf.core.util;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

public interface ILaunchDefaultsContributor
{
	/**
	 * Applies plugin-specific default values to a newly created launch configuration.
	 */
	void applyDefaults(ILaunchConfigurationWorkingCopy wc);
}
