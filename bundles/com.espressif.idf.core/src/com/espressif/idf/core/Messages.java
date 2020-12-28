package com.espressif.idf.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.core.messages"; //$NON-NLS-1$
	public static String IDFHelpers_NoIDFBuildConfig;
	public static String IDFHelpers_ProjectNotCreatedCorrectly;
	public static String Version_InvalidVersion;
	public static String Version_VersionNotNull;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
