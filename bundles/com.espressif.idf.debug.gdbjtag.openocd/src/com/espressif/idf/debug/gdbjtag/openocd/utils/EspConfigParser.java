/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;

public class EspConfigParser
{
	private final String espConfigPath;

	public EspConfigParser()
	{
		espConfigPath = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.OPENOCD_SCRIPTS)
				+ "/tcl_esp-config.json";
	}

	@SuppressWarnings("unchecked")
	public List<String> getTargets()
	{

		List<String> targets = new ArrayList<String>();
		JSONParser jsonParser = new JSONParser();
		try (FileReader reader = new FileReader(espConfigPath))
		{
			JSONObject json = (JSONObject) jsonParser.parse(reader);
			JSONArray targetsArray = (JSONArray) json.get("targets"); // $NON-NLS-1$
			targetsArray.forEach(target -> targets.add((String) ((JSONObject) target).get("id"))); // $NON-NLS-1$
			reader.close();
		}
		catch (FileNotFoundException e)
		{
			Logger.log(e);
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
		catch (ParseException e)
		{
			Logger.log(e);
		}

		return targets;
	}

	@SuppressWarnings("unchecked")
	public List<String> getEspFlashVoltages()
	{
		List<String> voltages = new ArrayList<String>();
		JSONObject voltageOption;
		JSONParser jsonParser = new JSONParser();
		try (FileReader reader = new FileReader(espConfigPath))
		{
			JSONObject json = (JSONObject) jsonParser.parse(reader);
			JSONArray optionsArray = (JSONArray) json.get("options"); // $NON-NLS-1$
			voltageOption = (JSONObject) optionsArray.stream()
					.filter(option -> ((JSONObject) option).get("name").equals("ESP_FLASH_VOLTAGE")).findFirst() //$NON-NLS-1$
					.orElse(null);
			((JSONArray) voltageOption.get("values")).forEach(value -> voltages.add(value.toString())); //$NON-NLS-1$
			reader.close();
		}
		catch (FileNotFoundException e)
		{
			Logger.log(e);
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
		catch (ParseException e)
		{
			Logger.log(e);
		}

		return voltages;

	}

	@SuppressWarnings("unchecked")
	public Map<String, JSONArray> getBoardsConfigs(String target)
	{
		Map<String, JSONArray> boardsConfigs = new HashMap<>();
		List<JSONObject> objects = new ArrayList<>();
		JSONParser jsonParser = new JSONParser();
		try (FileReader reader = new FileReader(espConfigPath))
		{
			JSONObject json = (JSONObject) jsonParser.parse(reader);
			JSONArray boardsArray = (JSONArray) json.get("boards"); // $NON-NLS-1$
			boardsArray.forEach(board -> objects.add((JSONObject) board));
			for (JSONObject object : objects)
			{
				if (object.get("target").equals(target)) // $NON-NLS-1$
				{
					boardsConfigs.put((String) object.get("name"), // $NON-NLS-1$
							((JSONArray) object.get("config_files"))); // $NON-NLS-1$
				}
			}
			reader.close();
		}
		catch (FileNotFoundException e)
		{
			Logger.log(e);
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
		catch (ParseException e)
		{
			Logger.log(e);
		}

		return boardsConfigs;
	}

	public boolean hasBoardConfigJson()
	{
		return new File(espConfigPath).exists();
	}
}
