/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.gcov;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.ui.gcov.messages"; //$NON-NLS-1$

	public static String TableCol_FileName;
	public static String TableCol_Path;
	public static String TableCol_LastModifiedGCNO;
	public static String TableCol_LastModifiedGCDA;
	public static String TableCol_SizeGCNO;
	public static String TableCol_SizeGCDA;
	public static String Dialog_SelectProject_Title;
	public static String Table_Unknown;
	
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
