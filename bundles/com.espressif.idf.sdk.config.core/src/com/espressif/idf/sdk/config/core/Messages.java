package com.espressif.idf.sdk.config.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.sdk.config.core.messages"; //$NON-NLS-1$
	public static String IJsonServerConfig_bool;
	public static String IJsonServerConfig_choice;
	public static String IJsonServerConfig_hex;
	public static String IJsonServerConfig_int;
	public static String IJsonServerConfig_load;
	public static String IJsonServerConfig_menu;
	public static String IJsonServerConfig_ranges;
	public static String IJsonServerConfig_save;
	public static String IJsonServerConfig_set;
	public static String IJsonServerConfig_string;
	public static String IJsonServerConfig_values;
	public static String IJsonServerConfig_version;
	public static String IJsonServerConfig_visible;
	public static String KConfigMenuProcessor_children;
	public static String KConfigMenuProcessor_depends_on;
	public static String KConfigMenuProcessor_help;
	public static String KConfigMenuProcessor_kconfigMenuNotfound;
	public static String KConfigMenuProcessor_title;
	public static String KConfigMenuProcessor_type;
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
