/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.util.Rotation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.espressif.idf.core.logging.Logger;

/**
 * Charts composite to display all the charts related to memory consumption
 * 
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class IDFSizeChartsComposite
{

	private enum ChartMode
	{
		STACKED_BAR, PIE
	}

	private enum MemoryUnit
	{
		BYTES(1, "B"), //$NON-NLS-1$
		KILOBYTES(1024, "KB"), //$NON-NLS-1$
		MEGABYTES(1024 * 1024, "MB"); //$NON-NLS-1$

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

	private Composite chartComp;
	private JSONObject overviewJson;
	private ScrolledComposite scrollable;

	private final Map<String, Color> labelColorMap = new HashMap<>();
	private final List<Color> availableColors = new ArrayList<>(List.of(Color.BLUE, Color.RED, Color.ORANGE,
			Color.MAGENTA, Color.CYAN, Color.PINK, Color.YELLOW, Color.LIGHT_GRAY, Color.DARK_GRAY));

	public void createPartControl(Composite parent, IFile file, String targetName)
	{
		parent.setLayout(new GridLayout(1, false));

		Composite toolbar = new Composite(parent, SWT.NONE);
		toolbar.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		toolbar.setLayout(new GridLayout(4, false));

		Label toggleLabel = new Label(toolbar, SWT.NONE);
		toggleLabel.setText(Messages.IDFSizeOverviewComposite_ChartView);

		Button toggleButton = new Button(toolbar, SWT.TOGGLE);
		toggleButton.setText(Messages.IDFSizeOverviewComposite_SwitchToPie);

		Label unitLabel = new Label(toolbar, SWT.NONE);
		unitLabel.setText(Messages.IDFSizeChartsComposite_SizeUnit);

		Combo unitCombo = new Combo(toolbar, SWT.DROP_DOWN | SWT.READ_ONLY);
		unitCombo.setItems(new String[] { "Bytes", "KB", "MB" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		unitCombo.select(1);

		scrollable = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		scrollable.setExpandHorizontal(true);
		scrollable.setExpandVertical(true);
		scrollable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		chartComp = new Composite(scrollable, SWT.NONE);
		chartComp.setLayout(new GridLayout(2, false));
		scrollable.setContent(chartComp);

		unitCombo.addListener(SWT.Selection, e -> {
			selectedUnit = switch (unitCombo.getSelectionIndex())
			{
			case 0 -> MemoryUnit.BYTES;
			case 2 -> MemoryUnit.MEGABYTES;
			default -> MemoryUnit.KILOBYTES;
			};
			plotCharts();
		});

		toggleButton.addListener(SWT.Selection, e -> {
			chartMode = (chartMode == ChartMode.STACKED_BAR) ? ChartMode.PIE : ChartMode.STACKED_BAR;
			toggleButton.setText(chartMode == ChartMode.STACKED_BAR ? Messages.IDFSizeOverviewComposite_SwitchToPie
					: Messages.IDFSizeOverviewComposite_SwitchToBar);
			plotCharts();
		});

		overviewJson = getIDFSizeOverviewData(file, targetName);
		plotCharts();
	}

	private void plotCharts()
	{
		for (var child : chartComp.getChildren())
			child.dispose();

		// Defensive check: Handle unexpected empty data gracefully to prevent UI crash.
		if (overviewJson == null || overviewJson.isEmpty() || !overviewJson.containsKey(IDFSizeConstants.LAYOUT))
		{
			Label errorLabel = new Label(chartComp, SWT.WRAP | SWT.CENTER);
			errorLabel.setText(
					Messages.IDFSizeChartsComposite_NoMemoryDataAvailableLblMsg);
			errorLabel.setForeground(chartComp.getDisplay().getSystemColor(SWT.COLOR_RED));

			// Center the error message across the 2-column grid
			GridData gd = new GridData(SWT.CENTER, SWT.CENTER, true, true);
			gd.horizontalSpan = 2;
			errorLabel.setLayoutData(gd);

			// Refresh layout and return to avoid crash
			chartComp.layout(true, true);
			scrollable.setMinSize(chartComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			return;
		}

		JSONArray layoutArray = (JSONArray) overviewJson.get(IDFSizeConstants.LAYOUT);
		for (Object obj : layoutArray)
		{
			JSONObject section = (JSONObject) obj;
			String sectionName = (String) section.get(IDFSizeConstants.NAME);
			long free = (long) section.get(IDFSizeConstants.FREE);
			JSONObject parts = (JSONObject) section.get("parts"); //$NON-NLS-1$

			List<String> labels = new ArrayList<>();
			List<Long> values = new ArrayList<>();
			for (Object key : parts.keySet())
			{
				String label = (String) key;
				JSONObject partInfo = (JSONObject) parts.get(label);
				long size = (long) partInfo.get("size"); //$NON-NLS-1$
				labels.add(label);
				values.add(size);
			}
			if (free > 0)
			{
				labels.add("Free"); //$NON-NLS-1$
				values.add(free);
			}

			String title = Messages.IDFSizeOverviewComposite_MemoryUsagePrefix + sectionName;
			if (chartMode == ChartMode.PIE)
			{
				createPieChart(chartComp, title, labels, values);
			}
			else
			{
				createBarChart(chartComp, title, labels, values);
			}
		}

		chartComp.layout(true, true);
		scrollable.setMinSize(chartComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void createBarChart(Composite parent, String title, List<String> labels, List<Long> values)
	{
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (int i = 0; i < labels.size(); i++)
		{
			double value = (double) values.get(i) / selectedUnit.getDivider();
			dataset.addValue(value, labels.get(i), ""); //$NON-NLS-1$
		}
		JFreeChart chart = ChartFactory.createStackedBarChart(title, Messages.IDFSizeChartsComposite_MemoryParts,
				MessageFormat.format(Messages.IDFSizeChartsComposite_SizeTag, selectedUnit.getLabel()),
				dataset, PlotOrientation.HORIZONTAL, true, true, false);

		CategoryPlot categoryPlot = chart.getCategoryPlot();
		BarRenderer barRenderer = (BarRenderer) categoryPlot.getRenderer();
		
		for (int i = 0; i < dataset.getRowCount(); i++)
		{
			String rowKey = dataset.getRowKey(i).toString();
			if (rowKey.startsWith("Free")) //$NON-NLS-1$
			{
				labelColorMap.putIfAbsent(rowKey, Color.GREEN);
			}
			else
			{
				labelColorMap.putIfAbsent(rowKey, getNextAvailableColor());
			}
			barRenderer.setSeriesPaint(i, labelColorMap.get(rowKey));
		}
		
		barRenderer.setBarPainter(new StandardBarPainter());	
		
		displayChartAsImage(parent, chart, 400, 300);
	}

	private void createPieChart(Composite parent, String title, List<String> labels, List<Long> values)
	{
		DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
		for (int i = 0; i < labels.size(); i++)
		{
			String label = labels.get(i) + " (" + formatMemory(values.get(i)) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			double value = (double) values.get(i) / selectedUnit.getDivider();
			dataset.setValue(label, value);
		}
		JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, true, false);
		@SuppressWarnings("rawtypes")
		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setDirection(Rotation.CLOCKWISE);
		plot.setForegroundAlpha(0.8f); // semi-transparency
		plot.setBackgroundPaint(Color.WHITE); // white background
		plot.setOutlineVisible(true);

		plot.setLabelGenerator(null);
		plot.setSimpleLabels(false);
		plot.setLabelGap(0.05);

		for (String key : dataset.getKeys())
		{
			if (key.startsWith("Free")) //$NON-NLS-1$
			{
				labelColorMap.putIfAbsent(key, Color.GREEN);
			}
			else
			{
				labelColorMap.putIfAbsent(key, getNextAvailableColor());
			}

			plot.setSectionPaint(key, labelColorMap.get(key));
		}

		displayChartAsImage(parent, chart, 400, 400);
	}

	private int colorIndex = 0;

	private Color getNextAvailableColor()
	{
		if (colorIndex >= availableColors.size())
		{
			// If we run out, generate a new random color (not green)
			Color random = new Color((int) (Math.random() * 200), (int) (Math.random() * 200),
					(int) (Math.random() * 200));

			// Don't allow green
			if (random.equals(Color.GREEN))
				return getNextAvailableColor();

			return random;
		}
		return availableColors.get(colorIndex++);
	}

	private void displayChartAsImage(Composite parent, JFreeChart chart, int width, int height)
	{
		try
		{
			BufferedImage image = chart.createBufferedImage(width, height);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(image, "png", os); //$NON-NLS-1$
			ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
			ImageData imgData = new ImageData(is);
			Image swtImg = new Image(parent.getDisplay(), imgData);
			Label imgLabel = new Label(parent, SWT.NONE);
			imgLabel.setImage(swtImg);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}

	private JSONObject getIDFSizeOverviewData(IFile file, String targetName)
	{
		try
		{
			return new IDFSizeDataManager().getIDFSizeOverview(file);
		}
		catch (Exception e)
		{
			Logger.log(e);
			return null;
		}
	}

	private String formatMemory(long bytes)
	{
		double value = (double) bytes / selectedUnit.getDivider();
		return String.format("%.1f %s", value, selectedUnit.getLabel()); //$NON-NLS-1$
	}
}
