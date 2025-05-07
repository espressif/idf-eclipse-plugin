package com.espressif.idf.core.tools;

import java.io.FileReader;
import java.io.IOException;

import org.eclipse.core.runtime.Platform;

import com.espressif.idf.core.tools.vo.EimJson;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EimIdfConfiguratinParser
{
	private EimJson eimJson;
	private Gson gson;
	
	public EimIdfConfiguratinParser()
	{
		gson = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization()
				.excludeFieldsWithoutExposeAnnotation().create();
	}
	
	private void load() throws IOException
	{
		try (FileReader fileReader = new FileReader(
				Platform.getOS().equals(Platform.OS_WIN32) ? EimConstants.EIM_WIN_PATH : EimConstants.EIM_POSIX_PATH))
		{
			eimJson = gson.fromJson(fileReader, EimJson.class);
		}
	}

	
	public EimJson getEimJson(boolean reload) throws IOException
	{
		if (reload)
		{
			load();
		}
		
		if (eimJson == null)
		{
			load();
		}
		
		return eimJson;
	}
}
