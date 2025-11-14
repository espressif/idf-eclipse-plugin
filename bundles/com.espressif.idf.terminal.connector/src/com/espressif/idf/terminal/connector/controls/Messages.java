package com.espressif.idf.terminal.connector.controls;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = Messages.class.getPackageName() + ".messages"; //$NON-NLS-1$
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

	public static String IDFConsoleWizardConfigurationPanel_MissingProjectErrorMsg;
	public static String IDFConsoleWizardConfigurationPanel_IDFConsoleWizardConfigurationPanel_ProjectLabel;
}
