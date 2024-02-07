/*******************************************************************************
 * Copyright 2024-2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.actions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.core.actions.messages"; //$NON-NLS-1$

	public static String IDFLaunchTargetNotFoundMsg1;
	public static String IDFLaunchTargetNotFoundMsg2;
	public static String IDFLaunchTargetNotFoundIDFLaunchTargetNotFoundTitle;
	public static String IDFLaunchTargetNotFoundMsg3;

	public static String SettingTargetJob;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
