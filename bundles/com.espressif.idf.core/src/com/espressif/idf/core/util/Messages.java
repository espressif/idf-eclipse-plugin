/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.espressif.idf.core.util.messages"; //$NON-NLS-1$
	public static String FileUtil_CopyingMsg;
	public static String FileUtil_DesDirNotavailable;
	public static String FileUtil_DestinationNotaDir;
	public static String FileUtil_SourceDirNotavailable;
	public static String FileUtil_UnableToCopy;
	public static String FileUtil_WritableProblemMsg;
	public static String IDFUtil_CouldNotFindDir;
	public static String MsgLinkDialog_DoNotShowMsg;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	private Messages() {
	}
}
