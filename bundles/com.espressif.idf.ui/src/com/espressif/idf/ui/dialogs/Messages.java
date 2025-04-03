package com.espressif.idf.ui.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.ui.dialogs.messages"; //$NON-NLS-1$
	public static String BuildView_ErrorMsgLbl;
	public static String BuildView_HintMsgLbl;
	public static String BuildView_NoAvailableHintsMsg;
	public static String DeleteResourcesWizard_project_deleteConfigurations;
	public static String CMakeBuildTab2_AdditionalCMakeArgs;
	public static String CMakeBuildTab2_BuildCmd;
	public static String CMakeBuildTab2_BuildFolderTextLbl;
	public static String CMakeBuildTab2_BuildFolderTextMsg;
	public static String CMakeBuildTab2_BuildFolderTextToolTip;
	public static String CMakeBuildTab2_CleanCmd;
	public static String CMakeBuildTab2_CMakeSettings;
	public static String CMakeBuildTab2_Generator;
	public static String CMakeBuildTab2_Ninja;
	public static String CMakeBuildTab2_SelectBuildFolderMsg;
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
	public static String SelectDebugConfigDialog_LableText;
	public static String SelectDebugConfigDialog_Text;
	public static String SelectDebugConfigDialog_Title;
	public static String SelectLaunchConfigDialog_LableText;
	public static String SelectLaunchConfigDialog_Text;
	public static String SelectLaunchConfigDialog_Title;
	public static String WriteFlashDialog_Bin_Path_Lbl;
	public static String WriteFlashDialog_BinFileErrFormatErrMsg;
	public static String WriteFlashDialog_Browse_Btn;
	public static String WriteFlashDialog_ErrorExitCodeMsg;
	public static String WriteFlashDialog_ErrorOutputMsg;
	public static String WriteFlashDialog_Flash_Btn_Lbl;
	public static String WriteFlashDialog_Offset_Lbl;
	public static String WriteFlashDialog_OffsetErrMsg;
	public static String WriteFlashDialog_Information_Msg;
	public static String WriteFlashDialog_SerialPortErrMsg;
	public static String WriteFlashDialog_Title;
	public static String WriteFlashDialog_WritingBinsToFlashMsg0;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
