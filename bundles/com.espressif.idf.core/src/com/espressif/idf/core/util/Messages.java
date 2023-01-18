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
	public static String DfuWarningDialog_Title;
	public static String DfuWarningDialog_WrongTargterMsg;
	public static String NameValidationError_1;
	public static String NameValidationError_2;
	public static String OffSetValidationError_1;
	public static String SizeValidationError_1;
	public static String SizeValidationError_2;
	public static String SubTypeValidationError_1;
	public static String SubTypeValidationError_2;
	public static String SubTypeValidationError_3;
	public static String SubTypeValidationError_4;
	public static String TypeValidationError_1;
	public static String TypeValidationError_2;
	public static String NvsValidation_FirstBeanValidationErr;
	public static String NvsValidation_ValueValidationErr_1;
	public static String NvsValidation_ValueValidationErr_2;
	public static String NvsValidation_ValueValidationErr_3;
	public static String NvsValidation_NumberValueValidationErr_1;
	public static String NvsValidation_NumberValueValidationErr_2;
	public static String NvsValidation_EncodingValidationErr_1;
	public static String NvsValidation_KeyValidationErr_1;
	public static String NvsValidation_KeyValidationErr_2;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	private Messages() {
	}
}
