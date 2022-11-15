/*******************************************************************************
 * Copyright 2018-2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size;

import java.io.File;
import java.io.FileReader;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.espressif.idf.ui.UIPlugin;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFSizeOverviewComposite2
{

	public void createPartControl(Composite parent, IFile file, String targetName)
	{
		try
		{
			JSONObject loadJson = loadJson();
			JSONArray layout = (JSONArray) loadJson.get("layout");
			Object[] array = layout.toArray();
			int x = 150;
			int y = 20;
			for (Object memory : array)
			{
				if (memory instanceof JSONObject)
				{
					JSONObject obj = (JSONObject) memory;
					String name = (String) obj.get("name");
					String total = (String) obj.get("total");

					// step 1: Draw total
					int convertToKB = convertToKB(Integer.valueOf(total));
					final int localy = y;
					parent.addListener(SWT.Paint, e -> {

						e.gc.drawText(name, x - 100, localy + 35);
						e.gc.setLineWidth(2);
						e.gc.drawRectangle(x, localy, convertToKB, 70);

					});

					int totalDrawArea = 0;
					// step 2: Draw parts
					JSONObject parts = (JSONObject) obj.get("parts");
					for (Object key : parts.keySet())
					{
						Object value = parts.get(key);
						System.out.print(key.toString() + ":" + value.toString());

						int partArea = convertToKB(Integer.valueOf(value.toString()));
						final int localTotal = totalDrawArea;
						parent.addListener(SWT.Paint, e -> {

							e.gc.setLineWidth(1);
							e.gc.drawRectangle(x, localy, partArea + localTotal, 70);
						});

						totalDrawArea = totalDrawArea + partArea;

					}

					y = y + 100;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	protected int convertToKB(long value)
	{
		return Math.round(value / 1024);
	}

	private JSONObject loadJson() throws Exception
	{
		URL url = FileLocator.find(UIPlugin.getDefault().getBundle(),
				new Path("/src/com/espressif/idf/ui/size/size.json"), null);
		url = FileLocator.toFileURL(url);
		File file = URIUtil.toFile(URIUtil.toURI(url));
		JSONParser parser = new JSONParser();
		FileReader reader = new FileReader(file);
		try
		{
			return (JSONObject) parser.parse(reader);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			reader.close();
		}
		return null;
	}

}
