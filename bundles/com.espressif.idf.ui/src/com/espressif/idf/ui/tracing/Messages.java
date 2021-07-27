package com.espressif.idf.ui.tracing;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.ui.tracing.messages"; //$NON-NLS-1$
	
	public static String TracingAnalysisEditor_OverviewTab;
	public static String TracingAnalysisEditor_OverviewFromHeading;
	public static String TracingAnalysisEditor_OverviewChartSectionHeading;
	public static String TracingAnalysisEditor_OverviewDetailSectionHeading;
	public static String TracingAnalysisEditor_OverviewDetailSectionEnableLogScaleButtonText;
	public static String TracingAnalysisEditor_OverviewDetailSectionListAvailableEvents;
	public static String TracingAnalysisEditor_OverviewDetailSectionListSelectedEvents;
	public static String TracingAnalysisEditor_OverviewChartSectionMultiChartHeading;
	public static String TracingAnalysisEditor_OverviewChartSectionYAxisTitle;
	public static String TracingAnalysisEditor_OverviewChartSectionHeapChartHeading;
	
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
