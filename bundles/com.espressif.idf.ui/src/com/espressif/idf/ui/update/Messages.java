/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.update;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.ui.update.messages"; //$NON-NLS-1$
	public static String AbstractToolsHandler_ExecutingMsg;
	public static String DirectorySelectionDialog_Browse;
	public static String DirectorySelectionDialog_CantbeEmpty;
	public static String DirectorySelectionDialog_CheckTools;
	public static String DirectorySelectionDialog_ChoosePyVersion;
	public static String DirectorySelectionDialog_GitExecutableLocation;
	public static String DirectorySelectionDialog_GitExeLocation;
	public static String DirectorySelectionDialog_IDFDirLabel;
	public static String DirectorySelectionDialog_IDFToolsInstallationDialog;
	public static String DirectorySelectionDialog_InstallTools;
	public static String DirectorySelectionDialog_ProvideIDFDirectory;
	public static String DirectorySelectionDialog_PyExecutableLocation;
	public static String DirectorySelectionDialog_PyExeLocation;
	public static String DirectorySelectionDialog_SelectIDFDirMessage;
	public static String IDFToolsHandler_ToolsManagerConsole;
	public static String InstallToolsHandler_AutoConfigureToolchain;
	public static String InstallToolsHandler_ConfiguredBuildEnvVarMsg;
	public static String InstallToolsHandler_ConfiguredCMakeMsg;
	public static String InstallToolsHandler_ExportingPathsMsg;
	public static String InstallToolsHandler_InstallingPythonMsg;
	public static String InstallToolsHandler_InstallingWebscoketMsg;
	public static String InstallToolsHandler_InstallingToolsMsg;
	public static String InstallToolsHandler_ItWilltakeTimeMsg;
	public static String InstallToolsHandler_ToolsCompleted;
	public static String InstallToolsHandler_CopyingOpenOCDRules;
	public static String InstallToolsHandler_OpenOCDRulesCopied;
	public static String InstallToolsHandler_OpenOCDRulesCopyError;
	public static String InstallToolsHandler_OpenOCDRulesCopyPaths;
	public static String InstallToolsHandler_OpenOCDRulesCopyWarning;
	public static String InstallToolsHandler_OpenOCDRulesCopyWarningMessage;
	public static String InstallToolsHandler_OpenOCDRulesNotCopied;
	public static String ListInstalledTools_MessageTitle;
	public static String ListInstalledTools_MissingIdfPathMsg;
	public static String JavaRuntimeVersionMsg;
	public static String OperatingSystemMsg;
	public static String EclipseCDTMsg;
	public static String IdfEclipseMsg;
	public static String EclipseMsg;
	public static String PythonIdfEnvMsg;
	public static String MissingIdfPathMsg;
	public static String NotFoundMsg;
	
	public static String SbomCommandDialog_BrowseBtnTxt;
	public static String SbomCommandDialog_ConsoleRedirectedOutputFormatString;
	public static String SbomCommandDialog_EspIdfSbomJobName;
	public static String SbomCommandDialog_OutputFileNotWritabbleErrorMsg;
	public static String SbomCommandDialog_OutputFilePathLbl;
	public static String SbomCommandDialog_ProjectDescDoesntExistDefaultErrorMsg;
	public static String SbomCommandDialog_ProjectDescDoesntExistsErrorMsg;
	public static String SbomCommandDialog_ProjectDescriptionPathLbl;
	public static String SbomCommandDialog_RedirectOutputCheckBoxLbl;
	public static String SbomCommandDialog_SbomInfoMsg;
	public static String SbomCommandDialog_SbomTitle;
	public static String SbomCommandDialog_StatusCantBeNullErrorMsg;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
