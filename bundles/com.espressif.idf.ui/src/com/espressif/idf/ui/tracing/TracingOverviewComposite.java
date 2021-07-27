/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tracing;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.ILineSeries.PlotSymbolType;
import org.eclipse.swtchart.ISeries;
import org.eclipse.swtchart.ISeries.SeriesType;
import org.eclipse.swtchart.LineStyle;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.espressif.idf.core.logging.Logger;

/**
 * Tracing overview tab composite for tracing editor
 * 
 * @author Ali Azam Rana
 *
 */
public class TracingOverviewComposite
{
	private TracingJsonParser heapTracingJsonParser;
	private FormToolkit toolkit;
	private Composite detailsComp;
	private Composite chartComp;
	private Button enableLogScaleChkBtn;
	private Button refreshGraph;
	private org.eclipse.swt.widgets.List availableEventStreamList;
	private org.eclipse.swt.widgets.List selectedEventStreamList;
	private org.eclipse.swt.widgets.List eventsContextNameList;
	private Button addBtn;
	private Button removeBtn;
	private Button refreshGraphBtn;
	private Form form;
	private Section chartSection;
	private Section detailsSection;
	private Chart chart;
	private List<EventData> eventDatasList;

	public TracingOverviewComposite(IFile jsonDumpFile)
	{
		try
		{
			heapTracingJsonParser = new TracingJsonParser(jsonDumpFile.getRawLocation().toOSString());
			eventDatasList = new LinkedList<EventData>();
		}
		catch (Exception execption)
		{
			Logger.log(execption);
		}
	}

	public void createPartControl(Composite parent)
	{
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createForm(parent);
		toolkit.decorateFormHeading(form);
		double[] allocEventsMemoryUsage = heapTracingJsonParser.getAllocEvents().stream()
				.mapToDouble(fEvent -> fEvent.getSizeOfAllocatedMemoryBlock()).toArray();

		form.setText(Messages.TracingAnalysisEditor_OverviewFromHeading);
		form.getBody().setLayout(new GridLayout());
		createDetailsCompositeSection();

		chartSection = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
		chartSection.setText(Messages.TracingAnalysisEditor_OverviewChartSectionHeading);
		chartSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		chartComp = new Composite(chartSection, SWT.NONE);
		chartComp.setLayout(new GridLayout(2, false));
		chartComp.setBackground(form.getBody().getBackground());
		chartComp.setForeground(form.getBody().getForeground());
		chartSection.setClient(chartComp);
		createChart(chartComp, allocEventsMemoryUsage);

	}

	private void createDetailsCompositeSection()
	{
		detailsSection = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
		detailsSection.setText(Messages.TracingAnalysisEditor_OverviewDetailSectionHeading);
		detailsSection.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		detailsComp = new Composite(detailsSection, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 6;

		detailsComp.setLayout(gridLayout);
		detailsComp.setBackground(form.getBody().getBackground());
		detailsComp.setForeground(form.getBody().getForeground());

		enableLogScaleChkBtn = new Button(detailsComp, SWT.CHECK);
		enableLogScaleChkBtn.setText(Messages.TracingAnalysisEditor_OverviewDetailSectionEnableLogScaleButtonText);
		enableLogScaleChkBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 6, 1));

		Label availableEventStreamLabel = new Label(detailsComp, SWT.NONE);
		availableEventStreamLabel.setText(Messages.TracingAnalysisEditor_OverviewDetailSectionListAvailableEvents);
		availableEventStreamLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label selectedEventStreamLabel = new Label(detailsComp, SWT.NONE);
		selectedEventStreamLabel.setText(Messages.TracingAnalysisEditor_OverviewDetailSectionListSelectedEvents);
		selectedEventStreamLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));

		availableEventStreamList = new org.eclipse.swt.widgets.List(detailsComp,
				SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		availableEventStreamList.setLayoutData(new GridData(200, 100));
		Map<String, Integer> eventNameIdMap = heapTracingJsonParser.getAllEventsNameIdMap();
		Vector<String> eventsVector = new Vector<>(eventNameIdMap.keySet());
		Collections.sort(eventsVector, new SortIgnoreCase());
		for (String eventName : eventsVector)
		{
			availableEventStreamList.add(eventName);
		}

		addBtn = new Button(detailsComp, SWT.PUSH);
		addBtn.setText("Add to Selected");
		addBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				String[] selectedEventNames = availableEventStreamList.getSelection();
				int[] selectedIndices = availableEventStreamList.getSelectionIndices();
				availableEventStreamList.remove(selectedIndices);
				for (String eventName : selectedEventNames)
				{
					selectedEventStreamList.add(eventName);
					int eventId = heapTracingJsonParser.getAllEventsNameIdMap().get(eventName);
					List<EventsVO> filteredEvent = heapTracingJsonParser.getEventsVOs().stream()
							.filter(eventVo -> eventVo.getEventId() == eventId).collect(Collectors.toList());
					double[] eventMemoryUsage = filteredEvent.stream()
							.mapToDouble(fEvent -> fEvent.getSizeOfAllocatedMemoryBlock()).toArray();
					EventData eventData = new EventData();
					eventData.setEventName(eventName);
					eventData.setEventMemoryVals(eventMemoryUsage);
					eventData.setEventId(eventId);
					List<String> contextNames = filteredEvent.stream().map(fEvent -> fEvent.getContextName())
							.collect(Collectors.toList());
					eventData.setContextNames(new HashSet<>(contextNames));
					eventDatasList.add(eventData);
				}
			}
		});

		selectedEventStreamList = new org.eclipse.swt.widgets.List(detailsComp,
				SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		selectedEventStreamList.setLayoutData(new GridData(200, 100));

		removeBtn = new Button(detailsComp, SWT.PUSH);
		removeBtn.setText("Remove");
		removeBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Vector<String> selectedEventNames = new Vector<>(Arrays.asList(selectedEventStreamList.getSelection()));
				int[] selectedIndices = selectedEventStreamList.getSelectionIndices();
				selectedEventStreamList.remove(selectedIndices);
				selectedEventNames.addAll(Arrays.asList(availableEventStreamList.getItems()));
				availableEventStreamList.removeAll();
				Collections.sort(selectedEventNames, new SortIgnoreCase());
				for (String eventName : selectedEventNames)
				{
					eventDatasList.removeIf(eData -> eData.eventName.equals(eventName));
					availableEventStreamList.add(eventName);
				}
			}
		});

		refreshGraphBtn = new Button(detailsComp, SWT.PUSH);
		refreshGraphBtn.setText("Refresh Graph");
		refreshGraphBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				createChartWithMultipleEvents(eventDatasList, enableLogScaleChkBtn.getSelection());
			}
		});

		detailsSection.setClient(detailsComp);
	}

	@SuppressWarnings({ "deprecation", "rawtypes" })
	private void createChartWithMultipleEvents(List<EventData> eventsData, boolean enableLogScale)
	{
		ISeries[] seriesInChart = chart.getSeriesSet().getSeries();
		for (ISeries series : seriesInChart)
		{
			chart.getSeriesSet().deleteSeries(series.getId());
		}

		chart.getTitle().setText(Messages.TracingAnalysisEditor_OverviewChartSectionMultiChartHeading);
		chart.getAxisSet().getXAxis(0).getTitle().setText(""); //$NON-NLS-1$
		chart.getAxisSet().getYAxis(0).getTitle()
				.setText(Messages.TracingAnalysisEditor_OverviewChartSectionYAxisTitle);

		for (EventData eventData : eventsData)
		{
			ILineSeries lineSeriesEvents = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE,
					eventData.getEventName());
			lineSeriesEvents.setLineStyle(LineStyle.SOLID);
			lineSeriesEvents.setYSeries(eventData.getEventMemoryVals());
			lineSeriesEvents.setLineColor(getRandomColorForChart());
			lineSeriesEvents.setLineWidth(2);
			lineSeriesEvents.setSymbolType(PlotSymbolType.NONE);
			lineSeriesEvents.setAntialias(SWT.ON);
			lineSeriesEvents.enableArea(true);

			for (String contextName : eventData.getContextNames())
			{
				ILineSeries lineSeriesContextNameEvents = (ILineSeries) chart.getSeriesSet()
						.createSeries(SeriesType.LINE, contextName);
				double[] contextMemConsumption = heapTracingJsonParser.getEventsVOs().stream()
						.filter(eventVo -> eventVo.getEventId() == eventData.getEventId()
								&& eventVo.getContextName().equals(contextName))
						.mapToDouble(eventVo -> eventVo.getSizeOfAllocatedMemoryBlock()).toArray();

				lineSeriesContextNameEvents.setLineStyle(LineStyle.SOLID);
				lineSeriesContextNameEvents.setYSeries(contextMemConsumption);
				lineSeriesContextNameEvents.setLineColor(getRandomColorForChart());
				lineSeriesContextNameEvents.setLineWidth(1);
				lineSeriesContextNameEvents.setSymbolType(PlotSymbolType.NONE);
				lineSeriesContextNameEvents.setAntialias(SWT.ON);
				lineSeriesContextNameEvents.enableArea(true);
			}
		}

		// adjust the axis range
		chart.getAxisSet().getYAxis(0).enableLogScale(enableLogScale);
		chart.getAxisSet().adjustRange();

		chart.redraw();
	}

	private Color getRandomColorForChart()
	{
		Random rand = new Random();
		int r = rand.nextInt(255);
		int g = rand.nextInt(255);
		int b = rand.nextInt(255);
		Color randomColor = new Color(r, g, b);
		return randomColor;
	}

	@SuppressWarnings({ "deprecation", "rawtypes" })
	private void createChart(Composite parent, double[] allocEventsMemoryUsage)
	{
		chart = new Chart(parent, SWT.NONE);
		chart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		chart.getTitle().setText(Messages.TracingAnalysisEditor_OverviewChartSectionHeapChartHeading);
		chart.getAxisSet().getXAxis(0).getTitle().setText(""); //$NON-NLS-1$
		chart.getAxisSet().getYAxis(0).getTitle()
				.setText(Messages.TracingAnalysisEditor_OverviewChartSectionYAxisTitle);

		ILineSeries lineSeriesAllocEvents = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE,
				"ALLOC EVENTS");
		lineSeriesAllocEvents.setLineStyle(LineStyle.SOLID);
		lineSeriesAllocEvents.setYSeries(allocEventsMemoryUsage);
		lineSeriesAllocEvents.setLineColor(new Color(255, 128, 0));
		lineSeriesAllocEvents.setLineWidth(2);
		lineSeriesAllocEvents.setSymbolColor(new Color(255, 255, 0));
		lineSeriesAllocEvents.setSymbolSize(4);
		lineSeriesAllocEvents.setSymbolType(PlotSymbolType.NONE);
		lineSeriesAllocEvents.setAntialias(SWT.ON);
		lineSeriesAllocEvents.enableArea(true);
		lineSeriesAllocEvents.enableStep(false);

		// adjust the axis range
		chart.getAxisSet().getYAxis(0).enableLogScale(true);
		chart.getAxisSet().adjustRange();
	}

	private class EventData
	{
		private String eventName;
		private double[] eventMemoryVals;
		private Set<String> contextNames;
		private int eventId;

		public double[] getEventMemoryVals()
		{
			return eventMemoryVals;
		}

		public void setEventMemoryVals(double[] eventMemoryVals)
		{
			this.eventMemoryVals = eventMemoryVals;
		}

		public String getEventName()
		{
			return eventName;
		}

		public void setEventName(String eventName)
		{
			this.eventName = eventName;
		}

		public Set<String> getContextNames()
		{
			return contextNames;
		}

		public void setContextNames(Set<String> contextNames)
		{
			this.contextNames = contextNames;
		}

		@Override
		public boolean equals(Object object)
		{
			if (!(object instanceof EventData) || !(object instanceof String))
				return false;
			if (object instanceof EventData)
			{
				EventData eData = (EventData) object;
				return eData.eventName.equals(this.eventName);
			}
			else
			{
				return this.eventName.equals(object);
			}
		}

		@Override
		public int hashCode()
		{
			return this.eventName.hashCode();
		}

		public int getEventId()
		{
			return eventId;
		}

		public void setEventId(int eventId)
		{
			this.eventId = eventId;
		}
	}

	private class SortIgnoreCase implements Comparator<Object>
	{
		public int compare(Object o1, Object o2)
		{
			String s1 = (String) o1;
			String s2 = (String) o2;
			return s1.toLowerCase().compareTo(s2.toLowerCase());
		}
	}
}
