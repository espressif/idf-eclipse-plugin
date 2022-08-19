/*******************************************************************************
 * Copyright 2022-2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

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
	public static enum ReHintsArrayEntries
	{
		RE, HINT
	};

	public static List<String[]> getReHintsList()
	{
		Yaml yaml = new Yaml();
		InputStream inputStream = null;
		try
		{
			inputStream = new FileInputStream(IDFUtil.getIDFPath() + "\\tools\\idf_py_actions\\hints.yml"); //$NON-NLS-1$
		}
		catch (FileNotFoundException e)
		{
			Logger.log(e);
		}
		ArrayList<LinkedHashMap<String, String>> reHintsArray = yaml.load(inputStream);
		List<String[]> reHintsPairArray = new ArrayList<>();
		for (LinkedHashMap<String, String> entry : reHintsArray)
		{
			reHintsPairArray.add(new String[] { entry.get("re"), entry.get("hint") }); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return reHintsPairArray;
	}
}
