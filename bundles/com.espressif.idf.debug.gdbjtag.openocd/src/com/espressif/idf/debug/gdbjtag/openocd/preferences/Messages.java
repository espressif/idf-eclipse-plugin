package com.espressif.idf.debug.gdbjtag.openocd.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.debug.gdbjtag.openocd.preferences.messages"; //$NON-NLS-1$
	public static String GDBServerTimeoutPage_Description;
	public static String GDBServerTimeoutPage_TimeoutField;
	
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
