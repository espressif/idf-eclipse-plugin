package com.espressif.idf.core.tools;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.core.runtime.Platform;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.exceptions.EimVersionMismatchException;
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

	private void load() throws IOException, EimVersionMismatchException
	{
		String path = Platform.getOS().equals(Platform.OS_WIN32) ? EimConstants.EIM_WIN_PATH
				: EimConstants.EIM_POSIX_PATH;

		File file = new File(path);
		if (!file.exists())
		{
			Logger.log("EIM config file not found: " + path); //$NON-NLS-1$
			return;
		}

		try (FileReader fileReader = new FileReader(file))
		{
			eimJson = gson.fromJson(fileReader, EimJson.class);
		}
		
		if (!eimJson.getVersion().equals(EimConstants.EIM_JSON_VALID_VERSION))
		{
			throw new EimVersionMismatchException(EimConstants.EIM_JSON_VALID_VERSION,eimJson.getVersion());
		}
	}

	public EimJson getEimJson(boolean reload) throws IOException, EimVersionMismatchException
	{
		if (reload || eimJson == null)
		{
			load();
		}

		return eimJson;
	}
}
