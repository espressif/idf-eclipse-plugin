package com.espressif.idf.ui.dialogs;

import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.ISeries;
import org.eclipse.swtchart.ISeries.SeriesType;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.TelemetryViewerObserver;
import com.espressif.idf.ui.telemetry.util.DataParser;

public class TelemetryGraph implements Runnable
{
	private Queue<Double> dataQueue;
	private Chart chart;
	private boolean dataAvailable;
	private String chartTitle;
	private String yAxisTitle;
	private DataParser dataParser;

	public TelemetryGraph(Composite parent, int cacheLimit, String chartTitle, String yAxistTitle,
			DataParser dataParser)
	{
		TelemetryViewerObserver.subcribe(this);
		dataQueue = new CircularFifoQueue<>(cacheLimit);
		this.chartTitle = chartTitle;
		this.yAxisTitle = yAxistTitle;
		this.dataParser = dataParser;
		createGraph(parent);
	}

	private void createGraph(Composite parent)
	{
		Composite container = new Composite(parent, SWT.NONE);
		container.setSize(400, 400);
		container.setLayout(new FillLayout());
		chart = createChart(container);
	}

	@Override
	public synchronized void run()
	{
		Display display = Display.getDefault();
		while (!chart.isDisposed())
		{
			if (!dataAvailable)
			{
				try
				{
					wait();
				}
				catch (InterruptedException e)
				{
					Logger.log(e);
				}
			}
			display.asyncExec(new Runnable()
			{
				@Override
				public void run()
				{
					ISeries[] seriesInChart = chart.getSeriesSet().getSeries();
					for (ISeries series : seriesInChart)
					{
						chart.getSeriesSet().deleteSeries(series.getId());
					}
					ILineSeries<?> lineSeries = (ILineSeries<?>) chart.getSeriesSet().createSeries(SeriesType.LINE,
							"line series"); //$NON-NLS-1$
					Double[] ySeries = dataQueue.toArray(new Double[] {});
					lineSeries.setYSeries(Stream.of(ySeries).mapToDouble(Double::doubleValue).toArray());
					chart.getAxisSet().adjustRange();
					chart.redraw();
				}
			});
			dataAvailable = false;
			notifyAll();
		}
	}

	public Chart createChart(Composite parent)
	{

		// create a chart
		Chart chart = new Chart(parent, SWT.NONE);
		// set titles
		chart.getTitle().setText(chartTitle);
		chart.getAxisSet().getYAxis(0).getTitle().setText(yAxisTitle);
		chart.getAxisSet().getXAxis(0).getTitle().setText("Data Points"); //$NON-NLS-1$
		return chart;
	}

	public synchronized void update(byte[] buff)
	{
		if (!dataAvailable)
		{
			List<Double> dataDouble = dataParser.parseSerialData(buff);
			if (dataDouble == null)
			{
				return;
			}
			dataQueue.addAll(dataDouble);
			notifyAll();
			dataAvailable = true;
		}

	}

}
