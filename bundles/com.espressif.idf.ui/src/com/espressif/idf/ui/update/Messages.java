/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.update;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.ui.update.messages"; //$NON-NLS-1$
	public static String DirectorySelectionDialog_Browse;
	public static String DirectorySelectionDialog_IDFDirLabel;
	public static String DirectorySelectionDialog_SelectIDFDirMessage;
	public static String IDFToolsHandler_ToolsManagerConsole;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
