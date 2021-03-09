package com.espressif.idf.debug.gdbjtag.openocd.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.espressif.idf.core.logging.Logger;

public class EspConfigParser
{
	@SuppressWarnings("unchecked")
	public ArrayList<String> getTargets()
	{

		ArrayList<String> targets = new ArrayList<String>();
		InputStream is = EspConfigParser.class.getResourceAsStream("tcl_esp-config.json"); // $NON-NLS-1$
		JSONObject json = (JSONObject) JSONValue.parse(new InputStreamReader(is));
		JSONArray targetsArray = (JSONArray) json.get("targets"); // $NON-NLS-1$
		targetsArray.forEach(target -> targets.add((String) ((JSONObject) target).get("id"))); // $NON-NLS-1$
		try
		{
			is.close();
		}
		catch (IOException e)
		{
			Logger.log(e);
		}

		return targets;
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, JSONArray> getBoardsConfigs(String target)
	{
		HashMap<String, JSONArray> boardsConfigs = new HashMap<>();
		ArrayList<JSONObject> objects = new ArrayList<>();
		InputStream is = EspConfigParser.class.getResourceAsStream("tcl_esp-config.json"); // $NON-NLS-1$
		JSONObject json = (JSONObject) JSONValue.parse(new InputStreamReader(is));
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
		try
		{
			is.close();
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
		return boardsConfigs;
	}
}
