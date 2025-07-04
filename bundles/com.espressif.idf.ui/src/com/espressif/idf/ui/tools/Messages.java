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

	public static String EspIdfEditorTitle;
	public static String EspIdfManagerVersionCol;
	public static String EspIdfManagerLocationCol;
	public static String EspIdfManagerActivateCol;
	public static String EspIdfManagerNameCol;
	public static String EspIdfManagerReloadBtnToolTip;
	public static String IDFGuideLinkLabel_Text;
	public static String EIMButtonDownloadText;
	public static String EIMButtonLaunchText;
	
	public static String IDFToolsHandler_ToolsManagerConsole;
	
	public static String EimJsonChangedMsgTitle;
	public static String EimJsonChangedMsgDetail;
	public static String EimJsonStateChangedMsgDetail;
	
	public static String MsgYes;
	public static String MsgNo;
	
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
