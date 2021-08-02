/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.tracing;

import java.text.DecimalFormat;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.nebula.widgets.xviewer.XViewerStyledTextLabelProvider;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * Styled label provider for tracing data details table
 * 
 * @author Ali Azam Rana
 *
 */
public class TracingDataStyledLabelProvider extends XViewerStyledTextLabelProvider
{
	private TracingTreeViewer tracingTreeViewer;
	private TracingJsonParser tracingJsonParser;

	public TracingDataStyledLabelProvider(TracingTreeViewer viewer, TracingJsonParser tracingJsonParser)
	{
		super(viewer);
		this.tracingTreeViewer = tracingTreeViewer;
		this.tracingJsonParser = tracingJsonParser;
	}

	@Override
	public Image getColumnImage(Object element, XViewerColumn xCol, int column) throws Exception
	{
		return null;
	}

	@Override
	public StyledString getStyledText(Object element, XViewerColumn xCol, int column) throws Exception
	{
		DetailsVO data = (DetailsVO) element;
		switch (column)
		{
		case 0:
			DecimalFormat decimalFormat = new DecimalFormat("#.################"); //$NON-NLS-1$
			decimalFormat.setMaximumFractionDigits(30);
			return new StyledString(String.valueOf(decimalFormat.format(data.getEventsVO().getTimestampOfEvent())),
					StyledString.QUALIFIER_STYLER);
		case 1:
			return new StyledString(String.valueOf(data.getEventsVO().getEventId()));
		case 2:
			return new StyledString(String.valueOf(data.getEventsVO().getCoreId()));
		case 3:
			return new StyledString(String.valueOf(data.getEventsVO().isIRQ()));
		case 4:
			return new StyledString(String.valueOf(data.getEventsVO().getContextName()), StyledString.COUNTER_STYLER);
		case 5:
			return new StyledString(String.valueOf(data.getEventName()));
		case 6:
			return new StyledString(String.valueOf(data.getEventsVO().getSizeOfAllocatedMemoryBlock()));
		case 7:
			return new StyledString(String.valueOf(data.getEventsVO().getAddressOfAllocatedMemoryBlock()));
		}
		return null;
	}

	@Override
	public Color getBackground(Object element, XViewerColumn viewerColumn, int columnIndex) throws Exception
	{
		DetailsVO data = (DetailsVO) element;
		if (data.isMemoryLeak())
		{
			return new Color(245, 102, 102);
		}

		return null;
	}

	@Override
	public Color getForeground(Object element, XViewerColumn viewerColumn, int columnIndex) throws Exception
	{
		return null;
	}

	@Override
	public Font getFont(Object element, XViewerColumn viewerColumn, int columnIndex) throws Exception
	{
		return null;
	}

}
