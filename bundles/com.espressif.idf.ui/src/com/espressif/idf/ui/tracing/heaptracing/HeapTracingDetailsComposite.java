/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.tracing.heaptracing;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.espressif.idf.ui.tracing.Messages;
import com.espressif.idf.ui.tracing.TracingJsonParser;
import com.espressif.idf.ui.tracing.TracingSizeDataContentProvider;
import com.espressif.idf.ui.tracing.TracingViewerFactory;

/**
 * Heap tracing details tab view composite class
 * 
 * @author Ali Azam Rana
 *
 */
public class HeapTracingDetailsComposite
{
	private TracingJsonParser tracingJsonParser;
	private List<String> columnNames;
	private HeapTracingTreeViewer tracingTreeViewer;

	public HeapTracingDetailsComposite(TracingJsonParser tracingJsonParser)
	{
		this.tracingJsonParser = tracingJsonParser;
		createColumnsList();
	}

	private void createColumnsList()
	{
		columnNames = new ArrayList<>();
		columnNames.add(Messages.TracingAnalysisEditor_DetailsColTimestamp);
		columnNames.add(Messages.TracingAnalysisEditor_DetailsColEventId);
		columnNames.add(Messages.TracingAnalysisEditor_DetailsColCoreId);
		columnNames.add(Messages.TracingAnalysisEditor_DetailsColIsIrq);
		columnNames.add(Messages.TracingAnalysisEditor_DetailsColContextName);
		columnNames.add(Messages.TracingAnalysisEditor_DetailsEventName);
		columnNames.add(Messages.TracingAnalysisEditor_DetailsColSizeOfAllocatedBlock);
		columnNames.add(Messages.TracingAnalysisEditor_DetailsColAddress);
		columnNames.add(Messages.TracingAnalysisEditor_DetailsColCallers);
	}

	public void createPartControl(Composite parent)
	{
		Integer[] tracingEventIds = new Integer[] { tracingJsonParser.getAllocEventId(),
				tracingJsonParser.getFreeEventId() };
		TracingViewerFactory tracingViewerFactory = new TracingViewerFactory(tracingJsonParser);
		tracingTreeViewer = new HeapTracingTreeViewer(parent, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER,
				tracingJsonParser, tracingEventIds, tracingViewerFactory);
		tracingTreeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		tracingTreeViewer.setContentProvider(new TracingSizeDataContentProvider());
		tracingTreeViewer.setLabelProvider(new HeapTracingDataStyledLabelProvider(tracingTreeViewer, tracingJsonParser));
		tracingTreeViewer.setUseHashlookup(true);
		tracingTreeViewer.loadInputForXViewer();
	}
}
