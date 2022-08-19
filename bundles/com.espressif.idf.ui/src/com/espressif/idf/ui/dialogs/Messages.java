package com.espressif.idf.ui.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.ui.dialogs.messages"; //$NON-NLS-1$
	public static String DeleteResourcesWizard_project_deleteConfigurations;
	public static String CMakeBuildTab2_AdditionalCMakeArgs;
	public static String CMakeBuildTab2_BuildCmd;
	public static String CMakeBuildTab2_CleanCmd;
	public static String CMakeBuildTab2_CMakeSettings;
	public static String CMakeBuildTab2_Generator;
	public static String CMakeBuildTab2_Ninja;
	public static String CMakeBuildTab2_UnixMakeFiles;
	public static String EraseFlashDialog_Title;
	public static String EraseFlashDialog_OkButton;
	public static String EraseFlashDialog_InformationMessage;
	public static String EraseFlashDialog_ComPortLabel;
	public static String EraseFlashDialog_DeviceInformationAreaInitialText;
	public static String EraseFlashDialog_LoadingMessage;
	public static String EraseFlashDialog_EraseFlashInProcessMessage;
	public static String EraseFlashDialog_EraseFlashInProcessMessageTitle;
	public static String EraseFlashDialog_EraseFlashInProcessMessageQuestion;
	public static String HintDetailsTitle;
	public static String FilterMessage;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
