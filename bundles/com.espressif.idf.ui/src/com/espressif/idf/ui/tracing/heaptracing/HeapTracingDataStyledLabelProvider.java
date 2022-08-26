/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.tracing.heaptracing;

import java.text.DecimalFormat;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.nebula.widgets.xviewer.XViewerStyledTextLabelProvider;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import com.espressif.idf.ui.tracing.DetailsVO;
import com.espressif.idf.ui.tracing.ITracingConstants;
import com.espressif.idf.ui.tracing.TracingJsonParser;
import com.espressif.idf.ui.tracing.images.TracingImagesCache;

/**
 * Styled label provider for tracing data details table
 * 
 * @author Ali Azam Rana
 *
 */
public class HeapTracingDataStyledLabelProvider extends XViewerStyledTextLabelProvider
{
	private HeapTracingTreeViewer tracingTreeViewer;
	private TracingJsonParser tracingJsonParser;

	public HeapTracingDataStyledLabelProvider(HeapTracingTreeViewer viewer, TracingJsonParser tracingJsonParser)
	{
		super(viewer);
		this.tracingTreeViewer = tracingTreeViewer;
		this.tracingJsonParser = tracingJsonParser;
	}

	@Override
	public Image getColumnImage(Object element, XViewerColumn xCol, int column) throws Exception
	{
		DetailsVO data = (DetailsVO) element;
		switch (column)
		{
		case 5:
			if (data.getEventName().equals(ITracingConstants.HEAP_ALLOC_EVENT_KEY))
			{
				return TracingImagesCache.getImage(ITracingConstants.HEAP_ALLOC_EVENT_KEY.concat(".png"));
			}
			else if (data.getEventName().equals(ITracingConstants.HEAP_FREE_EVENT_KEY))
			{
				return TracingImagesCache.getImage(ITracingConstants.HEAP_FREE_EVENT_KEY.concat(".png"));
			}

		default:
			break;
		}
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
			return new StyledString(String.valueOf(decimalFormat.format(data.getEventsVO().getTimestampOfEvent())));
		case 1:
			return new StyledString(String.valueOf(data.getEventsVO().getEventId()));
		case 2:
			return new StyledString(String.valueOf(data.getEventsVO().getCoreId()));
		case 3:
			return new StyledString(String.valueOf(data.getEventsVO().isIRQ()));
		case 4:
			return new StyledString(String.valueOf(data.getEventsVO().getContextName()));
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
			return new Color(255, 235, 230);
		}
		else if (data.getEventName().equals(ITracingConstants.HEAP_FREE_EVENT_KEY))
		{
			return new Color(153, 255, 153);
		}

		return null;
	}

	@Override
	public Color getForeground(Object element, XViewerColumn viewerColumn, int columnIndex) throws Exception
	{
		switch (columnIndex)
		{
		case 0:
			return new Color(51, 102, 204);
		case 4:
			return new Color(102, 102, 255);
		default:
			break;
		}
		return null;
	}

	@Override
	public Font getFont(Object element, XViewerColumn viewerColumn, int columnIndex) throws Exception
	{
		return null;
	}

}
