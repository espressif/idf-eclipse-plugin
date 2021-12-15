/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.installcomponents;

import org.eclipse.osgi.util.NLS;

/**
 * Messages keys for translations related to installing of components
 * 
 * @author Ali Azam Rana
 *
 */
public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.ui.installcomponents.messages"; //$NON-NLS-1$

	public static String InstallComponents_OpenReadmeButton;
	public static String InstallComponents_InstallButton;
	public static String InstallComponents_InstallButtonAlreadyAdded;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
