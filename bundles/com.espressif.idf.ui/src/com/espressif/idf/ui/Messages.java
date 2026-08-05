package com.espressif.idf.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.ui.messages"; //$NON-NLS-1$
	public static String LaunchBarListener_TargetDontMatch_Msg;
	public static String LaunchBarListener_TargetChanged_Msg;
	public static String LaunchBarListener_TargetChanged_Title;
	public static String TelemetryNotice_Title;
	public static String TelemetryNotice_Message;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
