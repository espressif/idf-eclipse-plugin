/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class Messages extends NLS
{
	public static String KconfMenuJsonNotFound;

	public static String SDKConfigUtil_CouldNotFindBuildDir;

	public static String SDKConfigUtil_ProjectNull;
	private static final String BUNDLE_NAME = "com.espressif.idf.sdk.config.core.messages"; //$NON-NLS-1$
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
