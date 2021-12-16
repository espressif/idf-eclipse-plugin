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
	public static String ToolsTreeHeading;
	public static String ToolsManagerShellHeading;
	public static String ToolsTreeNameCol;
	public static String ToolsTreeSizeCol;
	public static String ToolsTreeStatusCol;
	public static String DescriptionText;
	public static String InstallToolsText;
	public static String DeleteToolsText;
	public static String InstallToolsProgressShell_lblNewLabel_text;
	public static String InstallToolsProgressShell_text_text;
	public static String InstallToolsProgressShell_txtTxtinstalledtool_text;
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
	public static String RemovingDirectoryMessage;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
