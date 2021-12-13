/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import org.eclipse.osgi.util.NLS;

/**
 * Messages class to fetch language based properties in tools manager
 * 
 * @author Ali Azam Rana
 *
 */
public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.ui.tools.messages"; //$NON-NLS-1$

	public static String UrlDescriptionText;
	public static String SizeDescriptionText;
	public static String SupportedTargetsDescriptionText;
	public static String ToolsManagerShellHeading;
	public static String ToolsTreeNameCol;
	public static String ToolsTreeSizeCol;
	public static String ToolsTreeStatusCol;
	public static String DescriptionText;
	public static String InstallToolsText;
	public static String DeleteToolsText;
	public static String Installed;
	public static String DownloadFileText;
	public static String DownloadProgressText;
	public static String ExtractionTextMessage;
	public static String InstallingToolMessage;
	public static String UpdatingPathMessage;
	public static String PreviousToolMessage;
	public static String UpdateToolPathMessage;
	public static String SystemPathMessage;
	public static String ExtractionCompletedMessage;
	public static String RemovedPathMessage;
	public static String RemoveToolMessageBox;
	public static String RemoveToolMessageBoxTitle;
	public static String SelectAllButton;
	public static String DeselectAllButton;
	public static String FilterTargets;
	public static String Warning;
	public static String DirectorySelectionDialog_IDFDirLabel;
	public static String DirectorySelectionDialog_SelectIDFDirMessage;
	public static String WarningMessagebox;
	public static String NotInstalled;
	public static String Available;
	public static String ShowAvailableVersionsOnly;
	public static String ManageToolsInstallationShell_mntmNewItem_text;
	public static String ManageToolsInstallationShell_tltmCheckItem_text;
	public static String ManageToolsInstallationShell_label_text;
	public static String SelectRecommended;
	public static String FilterLabel;
	public static String ManageToolsInstallation;
	public static String ManageToolsInstallationDescription;
	public static String ToolsManagerWizard;
	public static String InstallPreRquisitePage;
	public static String InstallPreRquisitePage_lblLog_text;
	public static String InstallToolsPreReqPageDescription;
	public static String FileSelectionDialogTitle;
	public static String DirectorySelectionDialogMessage;
	public static String GitLabel;
	public static String PythonLabel;
	public static String InstallButton;
	public static String BrowseButton;
	public static String InstallEspIdfPage;
	public static String InstallEspIdfPageDescription;
	public static String InstallEspIdfPage_Existing;
	public static String InstallEspIdfPage_btnNew_text;
	public static String InstallEspIdfPage_lblEspidfPath_text;
	public static String InstallEspIdfPage_lblEspidfVersion_text;
	public static String InstallEspIdfPage_btnDownload_text;
	public static String SelectPythonVersion;
	public static String InstallEspIdfPage_lblDownloadDirectory_text;
	public static String SelectDownloadDir;
	public static String GitCloningJobMsg;
	public static String CloningCompletedMsg;
	public static String IDFDownloadWizard_DownloadingMessage;
	public static String IDFDownloadWizard_DownloadCompleteMsg;
	public static String IDFDownloadWizard_DecompressingMsg;
	public static String IDFDownloadWizard_DecompressingCompleted;
	public static String IDFDownloadWizard_UpdatingIDFPathMessage;
	public static String BtnCancel;
	public static String OperationCancelledByUser;
	public static String InstallToolsHandler_CopyingOpenOCDRules;
	public static String InstallToolsHandler_OpenOCDRulesCopied;
	public static String InstallToolsHandler_OpenOCDRulesCopyError;
	public static String InstallToolsHandler_OpenOCDRulesCopyPaths;
	public static String InstallToolsHandler_OpenOCDRulesCopyWarning;
	public static String InstallToolsHandler_OpenOCDRulesCopyWarningMessage;
	public static String InstallToolsHandler_OpenOCDRulesNotCopied;
	public static String AbstractToolsHandler_ExecutingMsg;
	public static String ToolAreadyPresent;
	public static String ForceDownload_ToolTip;
	public static String ForceDownload;
	public static String CancelMsg;
	public static String InstallToolsProgressShell_lblNewLabel_text;
	public static String InstallToolsProgressShell_text_text;
	public static String InstallToolsProgressShell_txtTxtinstalledtool_text;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
