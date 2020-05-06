package com.espressif.idf.ui.handlers;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.ui.handlers.messages"; //$NON-NLS-1$
	public static String NewProjectHandler_CouldntFindPaths;
	public static String NewProjectHandler_MandatoryMsg;
	public static String NewProjectHandler_NavigateToHelpMenu;
	public static String NewProjectHandler_PathErrorTitle;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
