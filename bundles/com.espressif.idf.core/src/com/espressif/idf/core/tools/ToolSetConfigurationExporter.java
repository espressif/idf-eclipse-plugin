package com.espressif.idf.core.tools;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.vo.IDFToolSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class ToolSetConfigurationExporter
{
	private IDFToolSet idfToolSet;
	private Gson gson;

	public ToolSetConfigurationExporter(IDFToolSet idfToolSet)
	{
		this.idfToolSet = idfToolSet;
		gson = new GsonBuilder().setPrettyPrinting()
				.enableComplexMapKeySerialization()
				.excludeFieldsWithoutExposeAnnotation()
				.create();
	}

	public void export()
	{
		File toolSetConfigFile = new File(toolSetConfigFilePath());
		List<IDFToolSet> idfToolSets = null;
		Type listType = new TypeToken<ArrayList<IDFToolSet>>()
		{
		}.getType();

		if (toolSetConfigFile.exists())
		{
			try (FileReader fileReader = new FileReader(toolSetConfigFile))
			{
				idfToolSets = gson.fromJson(fileReader, listType);
			}
			catch (IOException e)
			{
				Logger.log(e);
				return;
			}
		}
		
		if (idfToolSets == null)
		{
			idfToolSets = new ArrayList<>();
		}
		idfToolSets.add(idfToolSet);
		try (FileWriter fileWriter = new FileWriter(toolSetConfigFile))
		{
			gson.toJson(idfToolSets, fileWriter);
		}
		catch (IOException e)
		{
			Logger.log(e);
		}

	}

	private String toolSetConfigFilePath()
	{
		IPath path = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(path.toOSString());
		stringBuilder.append(File.separatorChar);
		stringBuilder.append(IToolsInstallationWizardConstants.TOOL_SET_CONFIG_FILE);
		return stringBuilder.toString();
	}
}
