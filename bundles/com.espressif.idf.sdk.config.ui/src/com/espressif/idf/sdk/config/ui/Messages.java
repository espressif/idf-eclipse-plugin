/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.sdk.config.ui.messages"; //$NON-NLS-1$
	public static String LoadSdkConfigHandler_ErrorLoadingJsonConfigServer;
	public static String SDKConfigurationEditor_ChangesWontbeSaved;
	public static String SDKConfigurationEditor_Design;
	public static String SDKConfigurationEditor_Error;
	public static String SDKConfigurationEditor_ErrorRetrievingOutput;
	public static String SDKConfigurationEditor_Help;
	public static String SDKConfigurationEditor_InvalidInput;
	public static String SDKConfigurationEditor_LaunchSDKConfigEditor;
	public static String SDKConfigurationEditor_NoHelpAvailable;
	public static String SDKConfigurationEditor_Preview;
	public static String SDKConfigurationEditor_SaveChanges;
	public static String SDKConfigurationEditor_SDKConfiguration;
	public static String SDKConfigurationEditor_StartingJSONConfigServer;
	public static String SDKConfigurationEditor_UnableFindKConfigFile;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
