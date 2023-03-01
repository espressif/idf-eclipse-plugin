package com.espressif.idf.ui.install;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.SDKConfigJsonReader;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.EclipseUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

/**
 * @author Kondal Kolipaka
 *
 */
public class WokWiSimulatorHandler extends AbstractHandler
{

	private static final String WOKWI_CONFIG_JSON = "wokwi-config.json";//$NON-NLS-1$
	private static final String WOKWI_SERVER_PATH = "WOKWI_SERVER_PATH";//$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{

		IProject project = getProject();
		if (project == null)
		{
			Logger.log("Project can't be null. Make sure project is selected in the project explorer.");
			return null;

		}
		SDKConfigJsonReader sdkconfig = new SDKConfigJsonReader(project);
		String target = sdkconfig.getValue("IDF_TARGET"); //$NON-NLS-1$

		String elfFilePath = IDFUtil.getELFFilePath(project).toOSString();

		String wokwiExecutablePath = getWokwiServerPath();
		if (StringUtil.isEmpty(wokwiExecutablePath))
		{
			String msg = "WOKWI_SERVER_PATH is not found. Please make sure WOKWI_SERVER_PATH is configured in the Preferences > C/C+ > Build > Environment";
			MessageDialog.openError(Display.getDefault().getActiveShell(), "wokwi-server not found", msg);
			throw new ExecutionException(msg);
		}

		String diagram_id = null;
		IFile wokwiConfig = project.getFile(WOKWI_CONFIG_JSON);
		if (wokwiConfig.exists())
		{
			diagram_id = getDiagramId(wokwiConfig);
		}

		List<String> arguments = new ArrayList<String>();
		arguments.add(wokwiExecutablePath);
		arguments.add("--chip"); //$NON-NLS-1$
		arguments.add(target);

		if (diagram_id != null)
		{
			arguments.add("--id"); //$NON-NLS-1$
			arguments.add(diagram_id);
		}
		arguments.add(elfFilePath);

		try
		{
			startWokwiServer(arguments);
		}
		catch (IOException e)
		{
			Logger.log(e);
		}

		return null;
	}

	private void startWokwiServer(List<String> arguments) throws IOException
	{
		ProcessBuilder processBuilder = new ProcessBuilder(arguments);
		Process process = processBuilder.start();

		Thread errReader = new Thread("Error Reader")
		{
			@Override
			public void run()
			{
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream())))
				{
					for (String line = reader.readLine(); line != null; line = reader.readLine())
					{
						System.out.println("es::" + line);
					}
				}
				catch (IOException e)
				{
					CCorePlugin.log(e);
				}
			}
		};
		errReader.start();

		Thread inputReader = new Thread("Input reader")
		{
			@Override
			public void run()
			{
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
				{
					for (String line = reader.readLine(); line != null; line = reader.readLine())
					{
						System.out.println("is::" + line);
					}
				}
				catch (IOException e)
				{
					CCorePlugin.log(e);
				}
			}
		};
		inputReader.start();

	}

	private String getWokwiServerPath()
	{
		IEnvironmentVariable env = new IDFEnvironmentVariables().getEnv(WOKWI_SERVER_PATH);
		if (env != null)
		{
			return env.getValue();
		}
		return null;
	}

	private String getDiagramId(IFile wokwiConfig)
	{
		try
		{
			Gson gson = new GsonBuilder().create();
			JsonReader jsonReader = new JsonReader(new FileReader(wokwiConfig.getFullPath().toFile()));
			JsonObject jsonObject = gson.fromJson(jsonReader, JsonObject.class);
			return jsonObject.get("id").toString(); //$NON-NLS-1$
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		return null;
	}

	private IProject getProject()
	{
		return EclipseUtil.getSelectedProjectInExplorer();
	}

}
