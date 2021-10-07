package com.espressif.idf.ui.installcomponents;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.ui.installcomponents.messages"; //$NON-NLS-1$
	
	public static String InstallIDFComponentsDialog_InformationMessage;
	public static String InstallIDFComponentsDialog_Title;
	
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
