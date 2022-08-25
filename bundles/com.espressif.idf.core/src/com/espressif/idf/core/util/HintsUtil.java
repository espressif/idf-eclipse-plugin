/*******************************************************************************
 * Copyright 2022-2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

import com.espressif.idf.core.logging.Logger;

public class HintsUtil
{

	public static List<String[]> getReHintsList()
	{

		File hintsYmFile = new File(IDFUtil.getIDFPath() + File.separator + "tools" + File.separator + "idf_py_actions"
				+ File.separator
				+ "hints.yml"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		List<String[]> reHintsPairArray = new ArrayList<>();
		try
		{
			reHintsPairArray = hintsYmFile.isFile() ? parseHintsYamlFile(hintsYmFile) : reHintsPairArray;
		}
		catch (FileNotFoundException e)
		{
			Logger.log(e);
		}

		return reHintsPairArray;
	}

	private static List<String[]> parseHintsYamlFile(File hintsYmFile) throws FileNotFoundException
	{
		Yaml yaml = new Yaml();
		List<String[]> reHintsPairArray = new ArrayList<>();
		InputStream inputStream;
		inputStream = new FileInputStream(hintsYmFile);
		ArrayList<LinkedHashMap<String, String>> reHintsArray = yaml.load(inputStream);
		for (LinkedHashMap<String, String> entry : reHintsArray)
		{
			reHintsPairArray.add(new String[] { entry.get("re"), entry.get("hint") }); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return reHintsPairArray;
	}
}
