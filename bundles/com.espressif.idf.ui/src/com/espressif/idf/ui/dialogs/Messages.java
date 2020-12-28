package com.espressif.idf.ui.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.ui.dialogs.messages"; //$NON-NLS-1$
	public static String CMakeBuildTab2_AdditionalCMakeArgs;
	public static String CMakeBuildTab2_BuildCmd;
	public static String CMakeBuildTab2_CleanCmd;
	public static String CMakeBuildTab2_CMakeSettings;
	public static String CMakeBuildTab2_Generator;
	public static String CMakeBuildTab2_Ninja;
	public static String CMakeBuildTab2_UnixMakeFiles;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
