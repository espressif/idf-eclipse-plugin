package com.espressif.idf.wokwi.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = Messages.class.getPackageName() + ".messages"; //$NON-NLS-1$
	public static String WokwiConfigTab_Browse;
	public static String WokwiConfigTab_ChooseProject;
	public static String WokwiConfigTab_ProjDoesNotExist;
	public static String WokwiConfigTab_Project;
	public static String WokwiConfigTab_ProjectID;
	public static String WokwiConfigTab_ProjectNotSpecified;
	public static String WokwiConfigTab_ProjectSelection;
	public static String WokwiConfigTab_ProjMustOpened;
	public static String WokwiConfigTab_WokwiServer;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
