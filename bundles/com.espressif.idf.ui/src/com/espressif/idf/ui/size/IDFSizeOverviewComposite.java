/*******************************************************************************
 * Copyright 2018-2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.IBarSeries;
import org.eclipse.swtchart.ICircularSeries;
import org.eclipse.swtchart.ISeries.SeriesType;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.espressif.idf.core.logging.Logger;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFSizeOverviewComposite
{

	private FormToolkit toolkit;
	private Composite overviewComp;
	private JSONObject overviewJson;
	private Font boldFont;
	private Composite chartComp;

	private enum ChartMode
	{
		STACKED_BAR, PIE
	}

	private enum MemoryUnit
	{
		BYTES(1, "B"), KILOBYTES(1024, "KB"), MEGABYTES(1024 * 1024, "MB");

		private final long divider;
		private final String label;

		MemoryUnit(long divider, String label)
		{
			this.divider = divider;
			this.label = label;
		}

		public long getDivider()
		{
			return divider;
		}

		public String getLabel()
		{
			return label;
		}
	}

	private ChartMode chartMode = ChartMode.STACKED_BAR;
	private MemoryUnit selectedUnit = MemoryUnit.KILOBYTES;

	public void createPartControl(Composite parent, IFile file, String targetName)
	{
		toolkit = new FormToolkit(parent.getDisplay());
		Form form = toolkit.createForm(parent);
		toolkit.decorateFormHeading(form);
		form.setText(Messages.IDFSizeOverviewComposite_ApplicatoinMemoryUsage);
		form.getBody().setLayout(new GridLayout());

		Section ec2 = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
		ec2.setText(Messages.IDFSizeOverviewComposite_Overview);
		ec2.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		overviewComp = new Composite(ec2, SWT.NONE);
		overviewComp.setLayout(new GridLayout(2, false));
		overviewComp.setBackground(form.getBody().getBackground());
		overviewComp.setForeground(form.getBody().getForeground());
		ec2.setClient(overviewComp);

		overviewJson = getIDFSizeOverviewData(file, targetName);

		renderOverviewSection();

		// Toggle Button Section
		Composite toggleComposite = new Composite(form.getBody(), SWT.NONE);
		toggleComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		toggleComposite.setLayout(new GridLayout(2, false));

		Section ec = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
		ec.setText(Messages.IDFSizeOverviewComposite_MemoryAllocation);
		ec.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		chartComp = new Composite(ec, SWT.NONE);
		chartComp.setLayout(new GridLayout(2, false));
		chartComp.setBackground(form.getBody().getBackground());
		chartComp.setForeground(form.getBody().getForeground());

		Label toggleLabel = new Label(toggleComposite, SWT.NONE);
		toggleLabel.setText(Messages.IDFSizeOverviewComposite_ChartView);

		Button toggleButton = new Button(toggleComposite, SWT.TOGGLE);
		toggleButton.setText(Messages.IDFSizeOverviewComposite_SwitchToPie);

		Label unitLabel = new Label(toggleComposite, SWT.NONE);
		unitLabel.setText("Size Unit:");

		Combo unitCombo = new Combo(toggleComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		unitCombo.setItems(new String[] { "Bytes", "KB", "MB" });
		unitCombo.select(1); // Default to KB

		unitCombo.addListener(SWT.Selection, e -> {
			int index = unitCombo.getSelectionIndex();
			selectedUnit = switch (index)
			{
			case 0 -> MemoryUnit.BYTES;
			case 2 -> MemoryUnit.MEGABYTES;
			default -> MemoryUnit.KILOBYTES;
			};

			// Refresh Overview
			for (org.eclipse.swt.widgets.Control child : overviewComp.getChildren())
			{
				child.dispose();
			}
			renderOverviewSection();

			// Refresh Charts
			for (org.eclipse.swt.widgets.Control child : chartComp.getChildren())
			{
				child.dispose();
			}
			plotDynamicCharts(chartComp, overviewJson);
			chartComp.layout(true, true);
		});

		toggleButton.addListener(SWT.Selection, e -> {
			if (chartMode == ChartMode.STACKED_BAR)
			{
				chartMode = ChartMode.PIE;
				toggleLabel.setText(Messages.IDFSizeOverviewComposite_ChartView);
			}
			else
			{
				chartMode = ChartMode.STACKED_BAR;
				toggleButton.setText(Messages.IDFSizeOverviewComposite_SwitchToPie);
			}

			// Clear existing chartComp children
			for (org.eclipse.swt.widgets.Control child : chartComp.getChildren())
			{
				child.dispose();
			}
			// Redraw charts with new mode
			plotDynamicCharts(chartComp, overviewJson);
			chartComp.layout(true, true);
		});

		ec.setClient(chartComp);
		plotDynamicCharts(chartComp, overviewJson);
	}

	private void renderOverviewSection()
	{
		Label header1 = toolkit.createLabel(overviewComp, Messages.IDFSizeOverviewComposite_MemoryRegion);
		// Setup boldFont
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(header1.getFont()).setStyle(SWT.BOLD);
		boldFont = boldDescriptor.createFont(header1.getDisplay());
		
		
		Label header2 = toolkit.createLabel(overviewComp, Messages.IDFSizeOverviewComposite_UsedSize);
		header1.setFont(boldFont);
		header2.setFont(boldFont);

		long totalUsed = 0;
		long totalFree = 0;

		JSONArray layoutArray = (JSONArray) overviewJson.get(IDFSizeConstants.LAYOUT);
		for (Object obj : layoutArray)
		{
			JSONObject section = (JSONObject) obj;
			String sectionName = (String) section.get(IDFSizeConstants.NAME);
			long used = (long) section.get(IDFSizeConstants.USED);
			long free = (long) section.get(IDFSizeConstants.FREE);

			totalUsed += used;
			totalFree += free;

			toolkit.createLabel(overviewComp, sectionName + ":");
			Label value = toolkit.createLabel(overviewComp, formatMemory(used));
			value.setFont(boldFont);
		}

		toolkit.createLabel(overviewComp, Messages.IDFSizeOverviewComposite_TotalSize);
		Label totalLbl = toolkit.createLabel(overviewComp, formatMemory(totalUsed + totalFree));
		totalLbl.setFont(boldFont);

		overviewComp.layout(true, true);
	}

	public void plotDynamicCharts(Composite parent, JSONObject overviewJson)
	{
		JSONArray layoutArray = (JSONArray) overviewJson.get(IDFSizeConstants.LAYOUT);

		for (Object obj : layoutArray)
		{
			JSONObject section = (JSONObject) obj;
			String sectionName = (String) section.get(IDFSizeConstants.NAME);
			long free = (long) section.get(IDFSizeConstants.FREE);

			JSONObject parts = (JSONObject) section.get("parts");

			// Extract parts
			List<String> labels = new ArrayList<>();
			List<Long> values = new ArrayList<>();

			for (Object key : parts.keySet())
			{
				String label = (String) key;
				JSONObject partInfo = (JSONObject) parts.get(label);
				long size = (long) partInfo.get("size");
				labels.add(label);
				values.add(size);
			}

			// Add "Free" as remaining memory
			if (free > 0)
			{
				labels.add("Free");
				values.add(free);
			}

			if (chartMode == ChartMode.STACKED_BAR)
			{
				createStackedChart(parent, sectionName, labels, values);
			}
			else
			{
				createPieChart(parent, sectionName, labels, values);
			}
		}
	}

	public Chart createPieChart(Composite parent, String memoryLabel, List<String> labels, List<Long> values)
	{
		Chart chart = new Chart(parent, SWT.NONE);
		chart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		chart.getTitle().setText(Messages.IDFSizeOverviewComposite_MemoryUsagePrefix + memoryLabel);

		double[] pieValues = new double[values.size()];
		String[] pieLabels = new String[labels.size()];

		for (int i = 0; i < values.size(); i++)
		{
			pieValues[i] = values.get(i) / 1024.0; // KB
			pieLabels[i] = labels.get(i) + " - " + formatMemory(values.get(i));
		}

		ICircularSeries<?> circularSeries = (ICircularSeries<?>) chart.getSeriesSet().createSeries(SeriesType.PIE,
				memoryLabel);
		circularSeries.setSeries(pieLabels, pieValues);

		return chart;
	}

	public Chart createStackedChart(Composite parent, String memoryLabel, List<String> labels, List<Long> values)
	{
		Chart chart = new Chart(parent, SWT.NONE);
		chart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		chart.getTitle().setText(Messages.IDFSizeOverviewComposite_MemoryUsagePrefix + memoryLabel);

		chart.getAxisSet().getXAxis(0).enableCategory(true);
		chart.getAxisSet().getXAxis(0).setCategorySeries(new String[] { memoryLabel });
		chart.getAxisSet().getXAxis(0).getTitle().setText("");
		chart.getAxisSet().getYAxis(0).getTitle().setText("");

		int[] colorPool = new int[] { SWT.COLOR_BLUE, SWT.COLOR_RED, SWT.COLOR_DARK_GREEN, SWT.COLOR_DARK_MAGENTA,
				SWT.COLOR_DARK_YELLOW, SWT.COLOR_CYAN, SWT.COLOR_DARK_GRAY, SWT.COLOR_GRAY, SWT.COLOR_BLACK };

		for (int i = 0; i < labels.size(); i++)
		{
			String label = labels.get(i);
			long value = values.get(i);
			double[] ySeries = new double[] { values.get(i) / 1024.0 };

			String labelWithSize = label + " - " + formatMemory(value);

			IBarSeries<?> barSeries = (IBarSeries<?>) chart.getSeriesSet().createSeries(SeriesType.BAR, labelWithSize);
			barSeries.setYSeries(ySeries);
			barSeries.setBarColor(Display.getDefault().getSystemColor(colorPool[i % colorPool.length]));
			barSeries.enableStack(true);
		}

		chart.getAxisSet().adjustRange();

		return chart;
	}

	protected JSONObject getIDFSizeOverviewData(IFile file, String targetName)
	{
		// Get data
		JSONObject idfSizeOverview = null;
		try
		{
			idfSizeOverview = new IDFSizeDataManager().getIDFSizeOverview(file, targetName);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		return idfSizeOverview;
	}

	private String formatMemory(long bytes)
	{
		double value = (double) bytes / selectedUnit.getDivider();
		return String.format("%.1f %s", value, selectedUnit.getLabel()); //$NON-NLS-1$
	}

}
