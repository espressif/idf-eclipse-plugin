package com.espressif.idf.core.tools;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.core.tools.messages"; //$NON-NLS-1$
	
	
	public static String EimVersionMismatchExceptionMessage;
	public static String EimVersionMismatchExceptionMessageTitle;
	
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
