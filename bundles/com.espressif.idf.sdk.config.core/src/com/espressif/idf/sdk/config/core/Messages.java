package com.espressif.idf.sdk.config.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.sdk.config.core.messages"; //$NON-NLS-1$

	public static String KconfMenuJsonNotFound;

	public static String SDKConfigUtil_CouldNotFindBuildDir;
	public static String SDKConfigUtil_ProjectNull;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
