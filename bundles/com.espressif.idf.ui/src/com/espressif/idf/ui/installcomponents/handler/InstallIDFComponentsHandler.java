/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.installcomponents.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.installcomponents.desrializer.ComponentsDesrializer;
import com.espressif.idf.ui.installcomponents.dialog.InstallIDFComponentsDialog;
import com.espressif.idf.ui.installcomponents.vo.ComponentVO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

/**
 * Install IDF Components menu command handler
 * @author Ali Azam Rana
 *
 */
public class InstallIDFComponentsHandler extends AbstractHandler
{
	private static final String API_URL = "https://api.components.espressif.com/components";
	private static int TOTAL_RECORDS_TO_FETCH = 100;
	private List<ComponentVO> componentVOs;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		try
		{
			URL url = new URL(API_URL.concat("?per_page=".concat(String.valueOf(TOTAL_RECORDS_TO_FETCH))));
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("accept", "application/json");
			InputStream responseStream = connection.getInputStream();
			JsonReader jsonReader = new JsonReader(new InputStreamReader(responseStream));
			Gson gson = new GsonBuilder().registerTypeAdapter(ArrayList.class, new ComponentsDesrializer()).create();
			JsonArray jsonArray = gson.fromJson(jsonReader, JsonArray.class);
			componentVOs = gson.fromJson(jsonArray.toString(), ArrayList.class);
			InstallIDFComponentsDialog idfComponentsDialog = new InstallIDFComponentsDialog(activeShell, componentVOs);
			idfComponentsDialog.create();
			idfComponentsDialog.open();
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		return null;
	}

}
