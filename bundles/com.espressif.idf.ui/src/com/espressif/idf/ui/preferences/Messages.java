package com.espressif.idf.ui.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.ui.preferences.messages"; //$NON-NLS-1$
	public static String EspresssifPreferencesPage_BuildGroupTxt;
	public static String EspresssifPreferencesPage_CCacheToolTip;
	public static String EspresssifPreferencesPage_EnableCCache;
	public static String EspresssifPreferencesPage_IDFSpecificPrefs;
	public static String EspresssifPreferencesPage_SearchHintsCheckBtn;
	public static String EspresssifPreferencesPage_SearchHintsTooltip;
	public static String GDBServerTimeoutPage_TimeoutField;
	public static String SerialMonitorPage_Field_NumberOfLines;
	public static String SerialMonitorPage_Field_NumberOfCharsInLine;
	public static String GDBServerTimeoutGroup_Heading;
	public static String SerialMonitorPage_GroupHeading;
	public static String EspresssifPreferencesPage_HideErrprOnIdfComponentsBtn;
	public static String EspresssifPreferencesPage_HideErrprOnIdfComponentsToolTip;
	
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
