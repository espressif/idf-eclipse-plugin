/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tracing.heaptracing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.ILineSeries.PlotSymbolType;
import org.eclipse.swtchart.ISeries;
import org.eclipse.swtchart.ISeries.SeriesType;
import org.eclipse.swtchart.LineStyle;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.espressif.idf.ui.tracing.EventsVO;
import com.espressif.idf.ui.tracing.ITracingConstants;
import com.espressif.idf.ui.tracing.Messages;
import com.espressif.idf.ui.tracing.TracingJsonParser;

/**
 * Tracing overview tab composite for tracing editor
 * 
 * @author Ali Azam Rana
 *
 */
public class HeapTracingOverviewComposite
{
	private TracingJsonParser tracingJsonParser;
	private FormToolkit toolkit;
	private Composite detailsComp;
	private Composite chartComp;
	private Button refreshGraphBtn;
	private Form form;
	private Section chartSection;
	private Section detailsSection;
	private Chart chart;
	private Set<ContextEventData> contextDatasList;
	private List<Button> contextChkButtons;
	private Group contextNamesGroup;

	public HeapTracingOverviewComposite(TracingJsonParser tracingJsonParser)
	{
		this.tracingJsonParser = tracingJsonParser;
	}

	public void createPartControl(Composite parent)
	{
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createForm(parent);
		toolkit.decorateFormHeading(form);
		List<EventsVO> filteredEvents = tracingJsonParser.getEventsVOs().stream()
				.filter(event -> event.getEventId() == tracingJsonParser.getAllocEventId()
						|| event.getEventId() == tracingJsonParser.getFreeEventId())
				.collect(Collectors.toList());
		Double currentSum = 0d;
		List<Double> eventMemoryUsage = new ArrayList<Double>();
		for (EventsVO filteredEvent : filteredEvents)
		{
			if (filteredEvent.getEventId() == tracingJsonParser.getFreeEventId())
			{
				Optional<EventsVO> foundEvent = filteredEvents.stream()
						.filter(eventVo -> eventVo.getEventId() == tracingJsonParser.getAllocEventId()
								&& eventVo.getAddressOfAllocatedMemoryBlock()
										.equals(filteredEvent.getAddressOfAllocatedMemoryBlock()))
						.findAny();
				if (foundEvent.isPresent())
				{
					currentSum -= foundEvent.get().getSizeOfAllocatedMemoryBlock();
				}
			}
			else
			{
				currentSum += filteredEvent.getSizeOfAllocatedMemoryBlock();
			}

			eventMemoryUsage.add(currentSum);
		}

		double[] allocEventsMemoryUsage = new double[eventMemoryUsage.size()];
		for (int i = 0; i < eventMemoryUsage.size(); i++)
		{
			allocEventsMemoryUsage[i] = eventMemoryUsage.get(i).doubleValue();
		}

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
		Set<String> contextNames = tracingJsonParser.getAllocEvents().stream().map(eventVo -> eventVo.getContextName())
				.collect(Collectors.toSet());

		contextNamesGroup = new Group(detailsComp, SWT.SHADOW_ETCHED_IN | SWT.V_SCROLL);
		contextNamesGroup.setText("Available Context Names");
		contextNamesGroup.setSize(200, 100);
		contextNamesGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 5, 1));
		contextNamesGroup.setLayout(new GridLayout(1, false));

		contextChkButtons = new ArrayList<Button>();
		Button allContextsChkButton = new Button(contextNamesGroup, SWT.CHECK);
		allContextsChkButton.setText(Messages.TracingAnalysisEditor_OverviewDetailSectionAllContexts);
		contextChkButtons.add(allContextsChkButton);
		for (String contextName : contextNames)
		{
			Button contextChkButton = new Button(contextNamesGroup, SWT.CHECK);
			contextChkButton.setText(contextName);
			contextChkButtons.add(contextChkButton);
		}

		refreshGraphBtn = new Button(detailsComp, SWT.PUSH);
		refreshGraphBtn.setText("Refresh Graph");
		refreshGraphBtn.addSelectionListener(new RefreshGraphSelectionAdapter());

		detailsSection.setClient(detailsComp);
	}

	@SuppressWarnings({ "deprecation", "rawtypes" })
	private void createChartWithMultipleEvents(Set<ContextEventData> contextEventsData)
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

		for (ContextEventData contextData : contextEventsData)
		{
			ILineSeries lineSeriesEvents = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE,
					contextData.getContextName());
			lineSeriesEvents.setLineStyle(LineStyle.SOLID);
			lineSeriesEvents.setYSeries(contextData.getContextMemoryValsArray());
			lineSeriesEvents.setLineColor(getRandomColorForChart());
			lineSeriesEvents.setLineWidth(2);
			lineSeriesEvents.setSymbolType(PlotSymbolType.NONE);
			lineSeriesEvents.setAntialias(SWT.ON);
			lineSeriesEvents.enableArea(true);
		}

		// adjust the axis range
		chart.getAxisSet().getYAxis(0).enableLogScale(false);
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
		chart.getAxisSet().getYAxis(0).enableLogScale(false);
		chart.getAxisSet().adjustRange();
	}

	private class ContextEventData
	{
		private String contextName;
		private List<Double> contextMemoryVals;
		private int eventId;
		private String eventName;
		private boolean isAllContexts;

		public List<Double> getContextMemoryVals()
		{
			return contextMemoryVals;
		}

		public double[] getContextMemoryValsArray()
		{
			double[] memoryVals = new double[contextMemoryVals.size()];
			for (int i = 0; i < contextMemoryVals.size(); i++)
			{
				memoryVals[i] = contextMemoryVals.get(i).doubleValue();
			}
			return memoryVals;
		}

		public void setContextMemoryVals(List<Double> eventMemoryVals)
		{
			this.contextMemoryVals = eventMemoryVals;
		}

		public String getContextName()
		{
			return contextName;
		}

		public void setContextName(String eventName)
		{
			this.contextName = eventName;
		}

		@Override
		public boolean equals(Object object)
		{
			if (!(object instanceof ContextEventData) || !(object instanceof String))
				return false;
			if (object instanceof ContextEventData)
			{
				ContextEventData eData = (ContextEventData) object;
				return eData.contextName.equals(this.contextName);
			}
			else
			{
				return this.contextName.equals(object);
			}
		}

		@Override
		public int hashCode()
		{
			return this.contextName.hashCode();
		}

		public int getEventId()
		{
			return eventId;
		}

		public void setEventId(int eventId)
		{
			this.eventId = eventId;
		}

		public String getEventName()
		{
			return eventName;
		}

		public void setEventName(String eventName)
		{
			this.eventName = eventName;
		}

		public boolean isAllContexts()
		{
			return isAllContexts;
		}

		public void setAllContexts(boolean isAllContexts)
		{
			this.isAllContexts = isAllContexts;
		}
	}

	private class RefreshGraphSelectionAdapter extends SelectionAdapter
	{
		@Override
		public void widgetSelected(SelectionEvent e)
		{
			List<String> selectedContextNamesFromAvailableList = contextChkButtons.stream()
					.filter(btn -> btn.getSelection()).map(btn -> btn.getText()).collect(Collectors.toList());
			contextDatasList = new HashSet<ContextEventData>();

			int eventId = tracingJsonParser.getAllEventsNameIdMap().get(ITracingConstants.HEAP_ALLOC_EVENT_KEY);

			for (String contextName : selectedContextNamesFromAvailableList)
			{
				List<Double> eventMemoryUsage = new LinkedList<Double>();
				boolean setAllContext = false;
				double memorySum = 0d;
				List<EventsVO> filteredEvents = tracingJsonParser.getEventsVOs().stream()
						.filter(eventVo -> eventVo.getEventId() == eventId
								|| eventVo.getEventId() == tracingJsonParser.getFreeEventId())
						.collect(Collectors.toList());
				if (contextName.equals(Messages.TracingAnalysisEditor_OverviewDetailSectionAllContexts))
				{
					for (EventsVO filteredEvent : filteredEvents)
					{
						memorySum = getNextSeriesValue(memorySum, filteredEvent, filteredEvents);
						eventMemoryUsage.add(memorySum);
					}

					setAllContext = true;
				}
				else
				{
					filteredEvents = filteredEvents.stream()
							.filter(fEvent -> fEvent.getContextName().equals(contextName)
									|| fEvent.getEventId() == tracingJsonParser.getFreeEventId())
							.collect(Collectors.toList());
					for (EventsVO filteredEvent : filteredEvents)
					{
						memorySum = getNextSeriesValue(memorySum, filteredEvent, filteredEvents);
						eventMemoryUsage.add(memorySum);
					}
				}
				contextDatasList.add(getContextEventDataObjectForChart(contextName, eventMemoryUsage, eventId,
						ITracingConstants.HEAP_ALLOC_EVENT_KEY, setAllContext));
			}

			createChartWithMultipleEvents(contextDatasList);
		}

		private ContextEventData getContextEventDataObjectForChart(String contextName, List<Double> contextMemorySeiies,
				int eventId, String eventName, boolean setAllContexts)
		{
			ContextEventData contextEventData = new ContextEventData();
			contextEventData.setContextName(contextName);
			contextEventData.setContextMemoryVals(contextMemorySeiies);
			contextEventData.setEventId(eventId);
			contextEventData.setEventName(eventName);
			contextEventData.setAllContexts(setAllContexts);
			return contextEventData;
		}

		private double getNextSeriesValue(double currentSum, EventsVO filteredEvent, List<EventsVO> filteredEvents)
		{
			if (filteredEvent.getEventId() == tracingJsonParser.getFreeEventId())
			{
				Optional<EventsVO> foundEvent = filteredEvents.stream()
						.filter(eventVo -> eventVo.getEventId() == tracingJsonParser.getAllocEventId()
								&& eventVo.getAddressOfAllocatedMemoryBlock()
										.equals(filteredEvent.getAddressOfAllocatedMemoryBlock()))
						.findAny();
				if (foundEvent.isPresent())
				{
					currentSum -= foundEvent.get().getSizeOfAllocatedMemoryBlock();
				}
			}
			else
			{
				currentSum += filteredEvent.getSizeOfAllocatedMemoryBlock();
			}

			return currentSum;
		}
	}
}
