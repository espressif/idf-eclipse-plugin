package com.espressif.idf.ui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.espressif.idf.ui.dialogs.TelemetryGraph;
import com.espressif.idf.ui.telemetry.util.DataParser;

public class TelemetryViewer extends ViewPart
{

	Composite parentComposite;

	public TelemetryViewer()
	{
		super();
	}

	@Override
	public void createPartControl(Composite parent)
	{

		parentComposite = parent;

	}

	public void createGraph(int cacheLimit, String graphName, String yAxisName, DataParser dataParser)
	{
		TelemetryGraph graph = new TelemetryGraph(parentComposite, cacheLimit, graphName, yAxisName, dataParser);
		Thread grahThread = new Thread(graph);
		grahThread.start();
	}

	@Override
	public void setFocus()
	{
		// TODO Auto-generated method stub

	}

}
