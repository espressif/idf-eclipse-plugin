/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.installcomponents.handler;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.handlers.EclipseHandler;
import com.espressif.idf.ui.installcomponents.InstallComponentsEditor;
import com.espressif.idf.ui.installcomponents.desrializer.ComponentsDesrializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;

/**
 * Install IDF Components menu command handler
 * 
 * @author Ali Azam Rana
 *
 */
public class InstallIDFComponentsHandler extends AbstractHandler
{
	private static final String API_URL = "https://api.components.espressif.com/components";
	private static int TOTAL_RECORDS_TO_FETCH = 100;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		try
		{
			URL url = new URL(API_URL.concat("?per_page=".concat(String.valueOf(TOTAL_RECORDS_TO_FETCH)))); //$NON-NLS-1$
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
			InputStream responseStream = connection.getInputStream();
			JsonReader jsonReader = new JsonReader(new InputStreamReader(responseStream));
			Gson gson = new GsonBuilder().registerTypeAdapter(ArrayList.class, new ComponentsDesrializer())
					.setPrettyPrinting().disableHtmlEscaping().create();
			JsonArray jsonArray = gson.fromJson(jsonReader, JsonArray.class);
			IProject selectedProject = EclipseHandler
					.getSelectedResource((IEvaluationContext) event.getApplicationContext()).getProject();
			IFile file = selectedProject.getFolder("build").getFile("components.json"); //$NON-NLS-1$ //$NON-NLS-2$
			FileWriter fileWriter = new FileWriter(file.getLocation().toFile());
			fileWriter.write(jsonArray.toString());
			fileWriter.close();
			launchEditor(file);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		return null;
	}

	private void launchEditor(IFile file)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				IWorkbenchWindow activeww = EclipseHandler.getActiveWorkbenchWindow();
				try
				{
					IDE.openEditor(activeww.getActivePage(), file, InstallComponentsEditor.EDITOR_ID);
				}
				catch (PartInitException e)
				{
					Logger.log(e);
				}
			}
		});
	}
}
