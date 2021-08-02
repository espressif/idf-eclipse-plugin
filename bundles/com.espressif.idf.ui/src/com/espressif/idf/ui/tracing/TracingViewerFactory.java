/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.tracing;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.xviewer.XViewerFactory;
import org.eclipse.nebula.widgets.xviewer.core.model.SortDataType;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerAlign;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.customize.XViewerCustomMenu;

/**
 * Nebual XViewerFactory for the custom table
 * 
 * @author Ali Azam Rana
 *
 */
public class TracingViewerFactory extends XViewerFactory
{
	public final static String NAMESPACE = "tracingviewer"; //$NON-NLS-1$
	public final static String COLUMN_NAMESPACE = "tracingviewer.cols."; //$NON-NLS-1$
	private List<XViewerColumn> columns;
	private TracingJsonParser tracingJsonParser;

	public TracingViewerFactory(TracingJsonParser tracingJsonParser)
	{
		super(NAMESPACE);
		this.tracingJsonParser = tracingJsonParser;
		createColumnsList();
		registerColumns(columns);
	}

	private void createColumnsList()
	{
		columns = new ArrayList<>();
		columns.add(new XViewerColumn(COLUMN_NAMESPACE + Messages.TracingAnalysisEditor_DetailsColTimestamp,
				Messages.TracingAnalysisEditor_DetailsColTimestamp, 100, XViewerAlign.Left, true, SortDataType.Float,
				false, null));
		columns.add(new XViewerColumn(COLUMN_NAMESPACE + Messages.TracingAnalysisEditor_DetailsColEventId,
				Messages.TracingAnalysisEditor_DetailsColEventId, 80, XViewerAlign.Left, true, SortDataType.Integer,
				false, null));
		columns.add(new XViewerColumn(COLUMN_NAMESPACE + Messages.TracingAnalysisEditor_DetailsColCoreId,
				Messages.TracingAnalysisEditor_DetailsColCoreId, 80, XViewerAlign.Left, true, SortDataType.Integer,
				false, null));
		columns.add(new XViewerColumn(COLUMN_NAMESPACE + Messages.TracingAnalysisEditor_DetailsColIsIrq,
				Messages.TracingAnalysisEditor_DetailsColIsIrq, 80, XViewerAlign.Left, true, SortDataType.Boolean,
				false, null));
		columns.add(new XViewerColumn(COLUMN_NAMESPACE + Messages.TracingAnalysisEditor_DetailsColContextName,
				Messages.TracingAnalysisEditor_DetailsColContextName, 150, XViewerAlign.Left, true, SortDataType.String,
				false, null));
		columns.add(new XViewerColumn(COLUMN_NAMESPACE + Messages.TracingAnalysisEditor_DetailsEventName,
				Messages.TracingAnalysisEditor_DetailsEventName, 160, XViewerAlign.Left, true, SortDataType.String,
				false, null));
		columns.add(new XViewerColumn(COLUMN_NAMESPACE + Messages.TracingAnalysisEditor_DetailsColSizeOfAllocatedBlock,
				Messages.TracingAnalysisEditor_DetailsColSizeOfAllocatedBlock, 80, XViewerAlign.Left, true,
				SortDataType.Integer, false, null));
		columns.add(new XViewerColumn(COLUMN_NAMESPACE + Messages.TracingAnalysisEditor_DetailsColAddress,
				Messages.TracingAnalysisEditor_DetailsColAddress, 150, XViewerAlign.Left, true, SortDataType.String,
				false, null));
	}

	@Override
	public XViewerCustomMenu getXViewerCustomMenu()
	{
		return new TracingViewerCustomMenu(tracingJsonParser);
	}

	@Override
	public boolean isCellGradientOn()
	{
		return true;
	}

	@Override
	public boolean isAdmin()
	{
		return true;
	}
}
