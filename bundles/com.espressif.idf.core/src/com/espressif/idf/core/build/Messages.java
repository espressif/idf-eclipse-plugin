/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.core.build.messages"; //$NON-NLS-1$
	public static String ESP32CMakeToolChainProvider_PathDoesnNotExist;
	public static String CMakeBuildConfiguration_Building;
	public static String CMakeBuildConfiguration_BuildingIn;
	public static String CMakeBuildConfiguration_BuildingComplete;

	public static String CMakeBuildConfiguration_BuildComplete;
	public static String CMakeBuildConfiguration_Configuring;
	public static String CMakeBuildConfiguration_Cleaning;
	public static String CMakeBuildConfiguration_NotFound;
	public static String CMakeBuildConfiguration_ProcCompCmds;
	public static String CMakeBuildConfiguration_ProcCompJson;
	public static String CMakeBuildConfiguration_Failure;
	public static String CMakeErrorParser_NotAWorkspaceResource;
	public static String IDFBuildConfiguration_CMakeBuildConfiguration_NoToolchainFile;
	public static String IDFBuildConfiguration_ParseCommand;
	public static String IDFBuildConfiguration_PreCheck_DifferentIdfPath;
	public static String IncreasePartitionSizeTitle;
	public static String IncreasePartitionSizeMessage;

	public static String ToolsInitializationDifferentPathMessageBoxMessage;
	public static String ToolsInitializationDifferentPathMessageBoxTitle;
	public static String ToolsInitializationDifferentPathMessageBoxOptionYes;
	public static String ToolsInitializationDifferentPathMessageBoxOptionNo;

	public static String RefreshingProjects_JobName;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
