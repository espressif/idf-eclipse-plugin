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
	public static String IDFSizeOverviewComposite_DramDataSize;
	public static String IDFSizeOverviewComposite_DramBssSize;
	public static String IDFSizeOverviewComposite_FlashCodeSize;
	public static String IDFSizeOverviewComposite_FlashRoDataSize;
	public static String IDFSizeOverviewComposite_UsedStaticDram;
	public static String IDFSizeOverviewComposite_UsedStaticIram;
	public static String IDFSizeOverviewComposite_UsedSizeText;
	public static String IDFSizeOverviewComposite_ChartUsedText;
	public static String IDFSizeOverviewComposite_ChartAvailableText;
	public static String IDFSizeOverviewComposite_SinglePlot_UsedDiram;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
