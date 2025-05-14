/*******************************************************************************
 * Copyright 2022-2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.yaml.snakeyaml.Yaml;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.build.ReHintPair;
import com.espressif.idf.core.logging.Logger;

public class HintsUtil
{
	private HintsUtil()
	{
	}

	public static List<ReHintPair> getReHintsList(File hintsYmFile)
	{
		List<ReHintPair> reHintsPairArray = new ArrayList<>();
		InputStream inputStream = null;
		try
		{
			inputStream = hintsYmFile.isFile() ? new FileInputStream(hintsYmFile) : null;
			reHintsPairArray = inputStream != null ? loadHintsYamlFis(inputStream) : reHintsPairArray;
		}
		catch (FileNotFoundException e)
		{
			Logger.log(e);
		}

		Optional.ofNullable(inputStream).ifPresent(is -> {
			try
			{
				is.close();
			}
			catch (IOException e)
			{
				Logger.log(e);
			}
		});

		return reHintsPairArray;
	}

	public static String getHintsYmlPath()
	{
		return IDFUtil.getIDFPath() + File.separator + "tools" + File.separator + "idf_py_actions" //$NON-NLS-1$ //$NON-NLS-2$
				+ File.separator + "hints.yml"; //$NON-NLS-1$
	}

	public static String getOpenocdHintsYmlPath()
	{

		String openOCDScriptPath = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.OPENOCD_SCRIPTS);
		if (!StringUtil.isEmpty(openOCDScriptPath))
		{
			return openOCDScriptPath.replace("scripts",
					"espressif" + File.separator + "tools" + File.separator + "esp_problems_hints.yml");
		}

		return StringUtil.EMPTY;
	}

	private static List<ReHintPair> loadHintsYamlFis(InputStream inputStream)
	{
		Yaml yaml = new Yaml();
		List<ReHintPair> reHintsPairArray = new ArrayList<>();
		List<Map<String, Object>> hintEntries = yaml.load(inputStream);

		for (Map<String, Object> entry : hintEntries)
		{
			String re = (String) entry.get("re"); //$NON-NLS-1$
			String hint = (String) entry.get("hint"); //$NON-NLS-1$
			String ref = (String) entry.get("ref"); // optional //$NON-NLS-1$

			@SuppressWarnings("unchecked")
			List<Map<String, List<String>>> variablesList = (List<Map<String, List<String>>>) entry.get("variables"); //$NON-NLS-1$

			if (variablesList != null && !variablesList.isEmpty())
			{
				for (Map<String, List<String>> variableMap : variablesList)
				{
					List<String> reVars = variableMap.get("re_variables"); //$NON-NLS-1$
					List<String> hintVars = variableMap.get("hint_variables"); //$NON-NLS-1$

					String formattedRe = formatEntry(reVars, re);
					String formattedHint = formatEntry(hintVars, hint);

					reHintsPairArray.add(new ReHintPair(formattedRe, formattedHint, ref));
				}
			}
			else
			{
				reHintsPairArray.add(new ReHintPair(re, hint, ref));
			}
		}

		return reHintsPairArray;
	}

	private static String formatEntry(List<String> vars, String entry)
	{
		int i = 0;
		entry = entry.replace("'", "''"); //$NON-NLS-1$ //$NON-NLS-2$
		while (entry.contains("{}")) //$NON-NLS-1$
		{
			entry = entry.replaceFirst(Pattern.quote("{}"), "{" + i++ + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
																				// /$NON-NLS-3$
		}
		return MessageFormat.format(entry, vars.toArray());
	}
}
