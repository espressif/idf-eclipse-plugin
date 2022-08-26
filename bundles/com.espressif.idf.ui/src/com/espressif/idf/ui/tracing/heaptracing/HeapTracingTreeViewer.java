/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.tracing.heaptracing;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.espressif.idf.ui.tracing.DetailsVO;
import com.espressif.idf.ui.tracing.TracingJsonParser;
import com.espressif.idf.ui.tracing.TracingViewerFactory;

/**
 * Nebula XViewer extension to show the tree for details
 * 
 * @author Ali Azam Rana
 *
 */
public class HeapTracingTreeViewer extends XViewer
{
	private Button filterMemoryLeaksChkBtn;
	private List<DetailsVO> detailsVOs;
	private List<DetailsVO> memoryLeakVOs;
	private TracingJsonParser tracingJsonParser;
	private Integer[] eventIdsDisplayed;

	public HeapTracingTreeViewer(Composite parent, int style, TracingJsonParser tracingJsonParser,
			Integer[] eventIdsDisplayed, TracingViewerFactory tracingViewerFactory)
	{
		super(parent, style, tracingViewerFactory, true, true);
		this.tracingJsonParser = tracingJsonParser;
		this.eventIdsDisplayed = eventIdsDisplayed;
		detailsVOs = tracingJsonParser.getDetailsVOs(Arrays.asList(eventIdsDisplayed));
		memoryLeakVOs = detailsVOs.stream().filter(details -> details.isMemoryLeak()).collect(Collectors.toList());
	}

	@Override
	protected void createSupportWidgets(Composite parent)
	{
		super.createSupportWidgets(parent);
		setFilterMemoryLeaksChkBtn(new Button(parent, SWT.CHECK));
		getFilterMemoryLeaksChkBtn().setText("View Possible Memory Leaks");
		getFilterMemoryLeaksChkBtn().setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		getFilterMemoryLeaksChkBtn().addSelectionListener(new FilterMemoryLeakSelectionListener());
	}

	public void loadInputForXViewer()
	{
		setInputXViewer(detailsVOs);
	}

	public Button getFilterMemoryLeaksChkBtn()
	{
		return filterMemoryLeaksChkBtn;
	}

	public void setFilterMemoryLeaksChkBtn(Button filterMemoryLeaksChkBtn)
	{
		this.filterMemoryLeaksChkBtn = filterMemoryLeaksChkBtn;
	}

	public int getRowIndex(ViewerCell cell)
	{
		DetailsVO row = (DetailsVO) cell.getElement();
		int result = 0;
		if (getFilterMemoryLeaksChkBtn().getSelection())
		{
			for (DetailsVO details : memoryLeakVOs)
			{
				if (details.getEventsVO().getTimestampOfEvent() == row.getEventsVO().getTimestampOfEvent())
				{
					return result;
				}
				result++;
			}
		}
		else
		{
			for (DetailsVO details : detailsVOs)
			{
				if (details.getEventsVO().getTimestampOfEvent() == row.getEventsVO().getTimestampOfEvent())
				{
					return result;
				}
				result++;
			}
		}
		return result;
	}

	private class FilterMemoryLeakSelectionListener extends SelectionAdapter
	{
		@Override
		public void widgetSelected(SelectionEvent e)
		{
			boolean selected = getFilterMemoryLeaksChkBtn().getSelection();
			if (selected)
			{
				setInputXViewer(memoryLeakVOs);
			}
			else
			{
				setInputXViewer(detailsVOs);
			}
		}

	}
}
