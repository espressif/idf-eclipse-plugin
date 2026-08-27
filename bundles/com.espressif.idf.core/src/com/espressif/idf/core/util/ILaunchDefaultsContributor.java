package com.espressif.idf.core.util;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

public interface ILaunchDefaultsContributor
{
	/**
	 * Fills plugin-specific defaults for attributes that are missing or empty. Must not overwrite values the user
	 * already set. Safe on both new and existing launch configurations.
	 */
	void applyDefaults(ILaunchConfigurationWorkingCopy wc);
}
