package com.espressif.idf.terminal.connector.controls.themes;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = Messages.class.getPackageName() + ".messages"; //$NON-NLS-1$
	public static String EspressifDarkTheme_Name;
	public static String EspressifLightTheme_Name;
	public static String PowerShellTheme_Name;
	public static String ResetTheme_Name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
