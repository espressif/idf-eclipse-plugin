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
	public static String DirectorySelectionDialog_ChoosePyVersion;
	public static String DirectorySelectionDialog_GitExecutableLocation;
	public static String DirectorySelectionDialog_GitExeLocation;
	public static String DirectorySelectionDialog_IDFDirLabel;
	public static String DirectorySelectionDialog_InstallTools;
	public static String DirectorySelectionDialog_PyExecutableLocation;
	public static String DirectorySelectionDialog_PyExeLocation;
	public static String DirectorySelectionDialog_SelectIDFDirMessage;
	public static String IDFToolsHandler_ToolsManagerConsole;
	public static String InstallToolsHandler_AutoConfigureToolchain;
	public static String InstallToolsHandler_ConfiguredBuildEnvVarMsg;
	public static String InstallToolsHandler_ConfiguredCMakeMsg;
	public static String InstallToolsHandler_ExportingPathsMsg;
	public static String InstallToolsHandler_InstallingPythonMsg;
	public static String InstallToolsHandler_InstallingToolsMsg;
	public static String InstallToolsHandler_ItWilltakeTimeMsg;
	public static String InstallToolsHandler_ToolsCompleted;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
