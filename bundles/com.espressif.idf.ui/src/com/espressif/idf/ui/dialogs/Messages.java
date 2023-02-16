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
	public static String HintsYmlNotFoundErrMsg;

	public static String TelemetryDialog_CacheLimitLbl;
	public static String TelemetryDialog_ColumnNumberLbl;
	public static String TelemetryDialog_DataFormatLbl;
	public static String TelemetryDialog_DefaultAxisY;
	public static String TelemetryDialog_DefaultCacheLimit;
	public static String TelemetryDialog_DefaultColumnsText;
	public static String TelemetryDialog_DefaultGraphName;
	public static String TelemetryDialog_DefaultSeparator;
	public static String TelemetryDialog_GraphNameLbl;
	public static String TelemetryDialog_Message;
	public static String TelemetryDialog_RegexLbl;
	public static String TelemetryDialog_SeparatorLbl;
	public static String TelemetryDialog_ShellText;
	public static String TelemetryDialog_Title;
	public static String TelemetryDialog_TotalColumnsLbl;
	public static String TelemetryDialog_YNameLbl;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
