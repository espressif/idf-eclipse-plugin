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

	private FormToolkit toolkit;
	private Composite overviewComp;
	private JSONObject overviewJson;
	private Font boldFont;
	private Composite chartComp;

	public void createPartControl(Composite parent, IFile file, String targetName)
	{
		toolkit = new FormToolkit(parent.getDisplay());
		Form form = toolkit.createForm(parent);
		toolkit.decorateFormHeading(form);
		form.setText("ESP-IDF Application Memory Usage"); //$NON-NLS-1$
		form.getBody().setLayout(new GridLayout());

		Section ec2 = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
		ec2.setText("Overview"); //$NON-NLS-1$
		ec2.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		overviewComp = new Composite(ec2, SWT.NONE);
		overviewComp.setLayout(new GridLayout(2, false));
		overviewComp.setBackground(form.getBody().getBackground());
		overviewComp.setForeground(form.getBody().getForeground());
		ec2.setClient(overviewComp);

		overviewJson = getIDFSizeOverviewData(file, targetName);
		long dram_data = (long) overviewJson.get(IDFSizeConstants.DRAM_DATA);
		long dram_bss = (long) overviewJson.get(IDFSizeConstants.DRAM_BSS);
		long flash_code = (long) overviewJson.get(IDFSizeConstants.FLASH_CODE);
		long flash_rodata = (long) overviewJson.get(IDFSizeConstants.FLASH_RODATA);
		long total_size = (long) overviewJson.get(IDFSizeConstants.TOTAL_SIZE);

		Label sizeLbl = toolkit.createLabel(overviewComp, "Total Size:"); //$NON-NLS-1$
		Label sizeVal = toolkit.createLabel(overviewComp, convertToKB(total_size));

		FontDescriptor boldDescriptor = FontDescriptor.createFrom(sizeLbl.getFont()).setStyle(SWT.BOLD);
		boldFont = boldDescriptor.createFont(sizeLbl.getDisplay());
		sizeVal.setFont(boldFont);

		toolkit.createLabel(overviewComp, "DRAM .data Size:"); //$NON-NLS-1$
		Label b1Val = toolkit.createLabel(overviewComp, convertToKB(dram_data));
		b1Val.setFont(boldFont);

		toolkit.createLabel(overviewComp, "DRAM .bss Size:"); //$NON-NLS-1$
		Label b2Val = toolkit.createLabel(overviewComp, convertToKB(dram_bss)); // $NON-NLS-1$
		b2Val.setFont(boldFont);

		toolkit.createLabel(overviewComp, "FLASH Code Size:"); //$NON-NLS-1$
		Label b3Val = toolkit.createLabel(overviewComp, convertToKB(flash_code)); // $NON-NLS-1$
		b3Val.setFont(boldFont);

		toolkit.createLabel(overviewComp, "FLASH rodata Size:"); //$NON-NLS-1$
		Label b4Val = toolkit.createLabel(overviewComp, convertToKB(flash_rodata)); // $NON-NLS-1$
		b4Val.setFont(boldFont);

		Section ec = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
		ec.setText("Memory Allocation"); //$NON-NLS-1$
		ec.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		chartComp = new Composite(ec, SWT.NONE);
		chartComp.setLayout(new GridLayout(2, false));
		chartComp.setBackground(form.getBody().getBackground());
		chartComp.setForeground(form.getBody().getForeground());
		ec.setClient(chartComp);

		//available_diram is non-zero for esp32
		long available_diram = (long) overviewJson.get(IDFSizeConstants.AVAILABLE_DIRAM);
		if (available_diram == 0)
		{
			plotDoubleBar();
		}
		else
		{
			plotSingleBar();
		}

	}

	private void plotSingleBar()
	{
		// esps2-s2 specific
		long used_diram = (long) overviewJson.get(IDFSizeConstants.USED_DIRAM);
		long available_diram = (long) overviewJson.get(IDFSizeConstants.AVAILABLE_DIRAM);
		double used_diram_ratio = (double) overviewJson.get(IDFSizeConstants.USED_DIRAM_RATIO);

		toolkit.createLabel(overviewComp, "Used DIRAM:"); //$NON-NLS-1$
		String chartText = convertToKB(used_diram) + " (" + convertToKB(available_diram) + " available, "
				+ Math.round(used_diram_ratio * 100) + "% used)";
		Label dramUsedVal = toolkit.createLabel(overviewComp, chartText); // $NON-NLS-1$
		dramUsedVal.setFont(boldFont);

		createChart(chartComp, used_diram, available_diram, chartText, "DIRAM"); //$NON-NLS-1$

	}

	protected void plotDoubleBar()
	{
		long used_iram = (long) overviewJson.get(IDFSizeConstants.USED_IRAM);
		long available_iram = (long) overviewJson.get(IDFSizeConstants.AVAILABLE_IRAM);
		double used_iram_ratio = (double) overviewJson.get(IDFSizeConstants.USED_IRAM_RATIO);

		long used_dram = (long) overviewJson.get(IDFSizeConstants.USED_DRAM);
		long available_dram = (long) overviewJson.get(IDFSizeConstants.AVAILABLE_DRAM);
		double used_dram_ratio = (double) overviewJson.get(IDFSizeConstants.USED_DRAM_RATIO);

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

		createChart(chartComp, used_dram, available_dram, dramText, "DRAM"); //$NON-NLS-1$
		createChart(chartComp, used_iram, available_iram, iramText, "IRAM"); //$NON-NLS-1$

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

	/**
	 * create the chart.
	 * 
	 * @param parent        The parent composite
	 * @param available_ram
	 * @param used_ram
	 * @param chartText
	 * @param chartName
	 * @return The created chart
	 */
	static public Chart createChart(Composite parent, long used_ram, long available_ram, String chartText,
			String chartName)
	{

		double[] used = { used_ram / 1024 }; // KB
		double[] available = { available_ram / 1024 }; // KB

		// create a chart
		Chart chart = new Chart(parent, SWT.NONE);
		chart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// set titles
		chart.getTitle().setText("Used " + chartText); //$NON-NLS-1$

		chart.getAxisSet().getXAxis(0).getTitle().setText(""); //$NON-NLS-1$
		chart.getAxisSet().getYAxis(0).getTitle().setText(""); //$NON-NLS-1$ S

		// set category
		chart.getAxisSet().getXAxis(0).enableCategory(true);
		chart.getAxisSet().getXAxis(0).setCategorySeries(new String[] { chartName });

		// create bar series
		IBarSeries<?> barSeries1 = (IBarSeries<?>) chart.getSeriesSet().createSeries(SeriesType.BAR, "Used"); //$NON-NLS-1$
		barSeries1.setYSeries(used);
		barSeries1.setBarColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));

		IBarSeries<?> barSeries2 = (IBarSeries<?>) chart.getSeriesSet().createSeries(SeriesType.BAR, "Available"); //$NON-NLS-1$
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
		return String.valueOf(Math.round(value / 1024)) + " KB"; //$NON-NLS-1$
	}

}
