/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.tracing;

import org.eclipse.osgi.util.NLS;

/**
 * Messages class to fetch language based properties in tracing components
 * 
 * @author Ali Azam Rana
 *
 */
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
	public static String TracingAnalysisEditor_OverviewDetailSectionAllContexts;

	public static String TracingAnalysisEditor_DetailsColAddress;
	public static String TracingAnalysisEditor_DetailsColCoreId;
	public static String TracingAnalysisEditor_DetailsColContextName;
	public static String TracingAnalysisEditor_DetailsColEventId;
	public static String TracingAnalysisEditor_DetailsColIsIrq;
	public static String TracingAnalysisEditor_DetailsColCallers;
	public static String TracingAnalysisEditor_DetailsColSizeOfAllocatedBlock;
	public static String TracingAnalysisEditor_DetailsColTimestamp;
	public static String TracingAnalysisEditor_DetailsColDetail;
	public static String TracingAnalysisEditor_DetailsEventName;
	public static String TracingAnalysisEditor_DetailsStreamName;
	public static String TracingAnalysisEditor_DetailsContextMenuShowCallers;
	public static String TracingAnalysisEditor_DetailsContextMenuShowCallersTooltip;

	public static String TracingCallerView_ColFileName;
	public static String TracingCallerView_ColFunctionName;
	public static String TracingCallerView_ColLineNumber;
	public static String TracingCallerView_ColAddress;
	
	public static String TracingCallersConsolodiatedView_Tab;
	public static String TracingCallersConsolodiatedView_HitsCount;
	public static String TracingCallersConsolodiatedView_BytesUsed;
	
	public static String AppLvlTracingDialog_Title;
	public static String AppLvlTracingDialog_Description;
	public static String AppLvlTracingDialog_OutputFileDirLbl;
	public static String AppLvlTracingDialog_Browse;
	public static String AppLvlTracing_PollPeriod;
	public static String AppLvlTracing_WaitForHalt;
	public static String AppLvlTracing_MaxTraceSize;
	public static String AppLvlTracing_StopTmo;
	public static String AppLvlTracing_BytesToSKip;
	public static String AppLvlTracing_OutFile;
	public static String AppLvlTracing_TraceScript;
	public static String AppLvlTracing_StartParse;
	public static String AppLvlTracing_ConsoleName;
	public static String AppLvlTracing_ScriptBrowseLbl;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
