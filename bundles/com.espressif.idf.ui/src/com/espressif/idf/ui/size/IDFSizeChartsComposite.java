package com.espressif.idf.ui.size;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

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
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.util.Rotation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.espressif.idf.core.logging.Logger;

public class IDFSizeChartsComposite {

	private enum ChartMode { STACKED_BAR, PIE }

	private enum MemoryUnit {
		BYTES(1, "B"), KILOBYTES(1024, "KB"), MEGABYTES(1024 * 1024, "MB");
		private final long divider;
		private final String label;
		MemoryUnit(long divider, String label) {
			this.divider = divider;
			this.label = label;
		}
		public long getDivider() { return divider; }
		public String getLabel() { return label; }
	}

	private ChartMode chartMode = ChartMode.STACKED_BAR;
	private MemoryUnit selectedUnit = MemoryUnit.KILOBYTES;

	private Composite chartComp;
	private JSONObject overviewJson;
	private ScrolledComposite scrollable;

	public void createPartControl(Composite parent, IFile file, String targetName) {
		parent.setLayout(new GridLayout(1, false));

		Composite toolbar = new Composite(parent, SWT.NONE);
		toolbar.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		toolbar.setLayout(new GridLayout(4, false));

		Label toggleLabel = new Label(toolbar, SWT.NONE);
		toggleLabel.setText(Messages.IDFSizeOverviewComposite_ChartView);

		Button toggleButton = new Button(toolbar, SWT.TOGGLE);
		toggleButton.setText(Messages.IDFSizeOverviewComposite_SwitchToPie);

		Label unitLabel = new Label(toolbar, SWT.NONE);
		unitLabel.setText("Size Unit:"); //$NON-NLS-1$

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
			selectedUnit = switch (unitCombo.getSelectionIndex()) {
				case 0 -> MemoryUnit.BYTES;
				case 2 -> MemoryUnit.MEGABYTES;
				default -> MemoryUnit.KILOBYTES;
			};
			plotCharts();
		});

		toggleButton.addListener(SWT.Selection, e -> {
			chartMode = (chartMode == ChartMode.STACKED_BAR) ? ChartMode.PIE : ChartMode.STACKED_BAR;
			toggleButton.setText(chartMode == ChartMode.STACKED_BAR
				? Messages.IDFSizeOverviewComposite_SwitchToPie
				: Messages.IDFSizeOverviewComposite_SwitchToBar);
			plotCharts();
		});

		overviewJson = getIDFSizeOverviewData(file, targetName);
		plotCharts();
	}

	private void plotCharts() {
		for (var child : chartComp.getChildren()) child.dispose();

		JSONArray layoutArray = (JSONArray) overviewJson.get(IDFSizeConstants.LAYOUT);
		for (Object obj : layoutArray) {
			JSONObject section = (JSONObject) obj;
			String sectionName = (String) section.get(IDFSizeConstants.NAME);
			long free = (long) section.get(IDFSizeConstants.FREE);
			JSONObject parts = (JSONObject) section.get("parts"); //$NON-NLS-1$

			List<String> labels = new ArrayList<>();
			List<Long> values = new ArrayList<>();
			for (Object key : parts.keySet()) {
				String label = (String) key;
				JSONObject partInfo = (JSONObject) parts.get(label);
				long size = (long) partInfo.get("size"); //$NON-NLS-1$
				labels.add(label);
				values.add(size);
			}
			if (free > 0) {
				labels.add("Free"); //$NON-NLS-1$
				values.add(free);
			}

			String title = Messages.IDFSizeOverviewComposite_MemoryUsagePrefix + sectionName;
			if (chartMode == ChartMode.PIE) {
				createPieChart(chartComp, title, labels, values);
			} else {
				createBarChart(chartComp, title, labels, values);
			}
		}

		chartComp.layout(true, true);
		scrollable.setMinSize(chartComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void createBarChart(Composite parent, String title, List<String> labels, List<Long> values) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (int i = 0; i < labels.size(); i++) {
			double value = (double) values.get(i) / selectedUnit.getDivider();
			dataset.addValue(value, labels.get(i), ""); //$NON-NLS-1$
		}
		JFreeChart chart = ChartFactory.createStackedBarChart(
			title,
			"Memory Parts", //$NON-NLS-1$
			"Size (" + selectedUnit.getLabel() + ")", //$NON-NLS-1$ //$NON-NLS-2$
			dataset,
			PlotOrientation.HORIZONTAL,
			true, true, false
		);
		displayChartAsImage(parent, chart, 400, 300);
	}

	private void createPieChart(Composite parent, String title, List<String> labels, List<Long> values) {
		DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
		for (int i = 0; i < labels.size(); i++) {
			String label = labels.get(i) + " (" + formatMemory(values.get(i)) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			double value = (double) values.get(i) / selectedUnit.getDivider();
			dataset.setValue(label, value);
		}
		JFreeChart chart = ChartFactory.createPieChart3D(title, dataset, true, true, false);
		PiePlot3D plot = (PiePlot3D) chart.getPlot();
		plot.setStartAngle(290);
		plot.setDirection(Rotation.CLOCKWISE);
		plot.setForegroundAlpha(0.8f); // semi-transparency
		plot.setBackgroundPaint(Color.WHITE); // white background
		plot.setOutlineVisible(false);
		
		plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({1} KB) ({2})"));
		plot.setSimpleLabels(false);
		plot.setLabelGap(0.02);

		StandardChartTheme theme = (StandardChartTheme) StandardChartTheme.createJFreeTheme();
		theme.apply(chart);

		displayChartAsImage(parent, chart, 400, 400);
	}

	private void displayChartAsImage(Composite parent, JFreeChart chart, int width, int height) {
		try {
			BufferedImage image = chart.createBufferedImage(width, height);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(image, "png", os); //$NON-NLS-1$
			ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
			ImageData imgData = new ImageData(is);
			Image swtImg = new Image(parent.getDisplay(), imgData);
			Label imgLabel = new Label(parent, SWT.NONE);
			imgLabel.setImage(swtImg);
		} catch (Exception e) {
			Logger.log(e);
		}
	}

	private JSONObject getIDFSizeOverviewData(IFile file, String targetName) {
		try {
			return new IDFSizeDataManager().getIDFSizeOverview(file, targetName);
		} catch (Exception e) {
			Logger.log(e);
			return null;
		}
	}

	private String formatMemory(long bytes) {
		double value = (double) bytes / selectedUnit.getDivider();
		return String.format("%.1f %s", value, selectedUnit.getLabel()); //$NON-NLS-1$
	}
}
