package com.espressif.idf.ui;

import java.util.ArrayList;
import java.util.List;

import com.espressif.idf.ui.dialogs.TelemetryGraph;

public class TelemetryViewerObserver
{
	public static List<TelemetryGraph> graphs = new ArrayList<>();

	public static void updateGraphs(byte[] data)
	{
		for (TelemetryGraph graph : graphs)
		{
			graph.update(data);
		}
	}

	public static void subcribe(TelemetryGraph subscriber)
	{
		graphs.add(subscriber);
	}

}
