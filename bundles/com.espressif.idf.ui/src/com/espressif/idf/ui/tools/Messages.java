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
	
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
