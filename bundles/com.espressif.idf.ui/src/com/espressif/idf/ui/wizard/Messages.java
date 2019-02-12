/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.wizard;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.espressif.idf.ui.wizard.messages"; //$NON-NLS-1$
	public static String NewIDFProjectWizard_Project_Title;
	public static String NewIDFProjectWizard_ProjectDesc;
	public static String NewIDFProjectWizard_TaskName;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
