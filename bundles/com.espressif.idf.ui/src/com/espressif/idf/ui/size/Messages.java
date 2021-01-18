package com.espressif.idf.ui.size;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.ui.size.messages"; //$NON-NLS-1$
	public static String IDFSizeOverviewComposite_0;
	public static String IDFSizeOverviewComposite_ApplicatoinMemoryUsage;
	public static String IDFSizeOverviewComposite_MemoryAllocation;
	public static String IDFSizeOverviewComposite_Overview;
	public static String IDFSizeOverviewComposite_TotalSize;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
