/*******************************************************************************
 * Copyright 2018-2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.IBarSeries;
import org.eclipse.swtchart.ISeries.SeriesType;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.json.simple.JSONObject;

import com.espressif.idf.core.logging.Logger;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFSizeOverviewComposite
{

	public void createPartControl(Composite parent, IFile file)
	{

		JSONObject idfSizeOverview = getIDFSizeOverviewData(file);

		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		Form form = toolkit.createForm(parent);
		toolkit.decorateFormHeading(form);
		form.setText("ESP-IDF Application Memory Usage");
		form.getBody().setLayout(new GridLayout());

		Section ec2 = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
		ec2.setText("Overview");
		ec2.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		Composite overviewComp = new Composite(ec2, SWT.NONE);
		overviewComp.setLayout(new GridLayout(2, false));
		overviewComp.setBackground(form.getBody().getBackground());
		overviewComp.setForeground(form.getBody().getForeground());
		ec2.setClient(overviewComp);

		long dram_data = (long) idfSizeOverview.get("dram_data");
		long dram_bss = (long) idfSizeOverview.get("dram_bss");
		long flash_code = (long) idfSizeOverview.get("flash_code");
		long flash_rodata = (long) idfSizeOverview.get("flash_rodata");
		long total_size = (long) idfSizeOverview.get("total_size");

		long used_iram = (long) idfSizeOverview.get("used_iram");
		long available_iram = (long) idfSizeOverview.get("available_iram");
		double used_iram_ratio = (double) idfSizeOverview.get("used_iram_ratio");

		long used_dram = (long) idfSizeOverview.get("used_dram");
		long available_dram = (long) idfSizeOverview.get("available_dram");
		double used_dram_ratio = (double) idfSizeOverview.get("used_dram_ratio");

		Label sizeLbl = toolkit.createLabel(overviewComp, "Total Size:"); //$NON-NLS-1$
		Label sizeVal = toolkit.createLabel(overviewComp, convertToKB(total_size));

		FontDescriptor boldDescriptor = FontDescriptor.createFrom(sizeLbl.getFont()).setStyle(SWT.BOLD);
		Font boldFont = boldDescriptor.createFont(sizeLbl.getDisplay());
		sizeVal.setFont(boldFont);

		toolkit.createLabel(overviewComp, "DRAM .data Size:"); //$NON-NLS-1$
		Label b1Val = toolkit.createLabel(overviewComp, convertToKB(dram_data));
		b1Val.setFont(boldFont);

		toolkit.createLabel(overviewComp, "DRAM .bss Size:"); //$NON-NLS-1$
		Label b2Val = toolkit.createLabel(overviewComp, convertToKB(dram_bss)); // $NON-NLS-1$
		b2Val.setFont(boldFont);

		// Used static DRAM
		toolkit.createLabel(overviewComp, "Used static DRAM:"); //$NON-NLS-1$
		String dramText = convertToKB(used_dram) + " (" + convertToKB(available_dram) + " available, "
				+ Math.round(used_dram_ratio * 100) + "% used)";
		Label dramUsedVal = toolkit.createLabel(overviewComp, dramText); // $NON-NLS-1$
		dramUsedVal.setFont(boldFont);

		// Used static IRAM
		toolkit.createLabel(overviewComp, "Used static IRAM:"); //$NON-NLS-1$
		String iramText = convertToKB(used_iram) + " (" + convertToKB(available_iram) + " available, "
				+ Math.round(used_iram_ratio * 100) + "% used)";
		Label iramUsedVal = toolkit.createLabel(overviewComp, iramText); // $NON-NLS-1$
		iramUsedVal.setFont(boldFont);

		toolkit.createLabel(overviewComp, "FASH Code Size:"); //$NON-NLS-1$
		Label b3Val = toolkit.createLabel(overviewComp, convertToKB(flash_code)); // $NON-NLS-1$
		b3Val.setFont(boldFont);

		toolkit.createLabel(overviewComp, "FLASH rodata Size:"); //$NON-NLS-1$
		Label b4Val = toolkit.createLabel(overviewComp, convertToKB(flash_rodata)); // $NON-NLS-1$
		b4Val.setFont(boldFont);

		Section ec = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
		ec.setText("Memory Allocation");
		ec.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite client = new Composite(ec, SWT.NONE);
		client.setLayout(new GridLayout(2, false));
		client.setBackground(form.getBody().getBackground());
		client.setForeground(form.getBody().getForeground());
		ec.setClient(client);

		createChart(client, used_dram, available_dram, dramText, "DRAM");
		createChart(client, used_iram, available_iram, iramText, "IRAM");

	}

	protected JSONObject getIDFSizeOverviewData(IFile file)
	{
		// Get data
		JSONObject idfSizeOverview = null;
		try
		{
			idfSizeOverview = new IDFSizeDataManager().getIDFSizeOverview(file);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		return idfSizeOverview;
	}

	/**
	 * create the chart.
	 * 
	 * @param parent         The parent composite
	 * @param available_ram
	 * @param used_ram
	 * @param chartText
	 * @param chartName 
	 * @return The created chart
	 */
	static public Chart createChart(Composite parent, long used_ram, long available_ram, String chartText, String chartName)
	{

		double[] used = { used_ram / 1024 }; // KB
		double[] available = { available_ram / 1024 }; // KB

		// create a chart
		Chart chart = new Chart(parent, SWT.NONE);
		chart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// set titles
		chart.getTitle().setText("Used " + chartText);

		chart.getAxisSet().getXAxis(0).getTitle().setText("");
		chart.getAxisSet().getYAxis(0).getTitle().setText("");

		// set category
		chart.getAxisSet().getXAxis(0).enableCategory(true);
		chart.getAxisSet().getXAxis(0).setCategorySeries(new String[] {chartName});

		// create bar series
		IBarSeries<?> barSeries1 = (IBarSeries<?>) chart.getSeriesSet().createSeries(SeriesType.BAR, "Used");
		barSeries1.setYSeries(used);
		barSeries1.setBarColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));

		IBarSeries<?> barSeries2 = (IBarSeries<?>) chart.getSeriesSet().createSeries(SeriesType.BAR, "Available");
		barSeries2.setYSeries(available);
		barSeries2.setBarColor(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN));

		// enable stack series
		barSeries1.enableStack(true);
		barSeries2.enableStack(true);
		chart.setSize(100, 200);

		// adjust the axis range
		chart.getAxisSet().adjustRange();
		return chart;
	}

	protected String convertToKB(long value)
	{
		return String.valueOf(Math.round(value / 1024)) + " KB";
	}

}
