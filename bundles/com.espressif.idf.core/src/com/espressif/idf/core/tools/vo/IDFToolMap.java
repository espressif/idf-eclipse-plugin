package com.espressif.idf.core.tools.vo;

import java.util.Map;

public class IDFToolMap
{
	private Map<String, IDFToolSet> toolsMap;
	
	private static IDFToolMap INSTANCE;
	
	private IDFToolMap()
	{
	}
	
	public static IDFToolMap getInstance()
	{
		synchronized (INSTANCE)
		{
			if (INSTANCE == null)
				INSTANCE = new IDFToolMap();
			return INSTANCE;
		}
	}
	
	private IDFToolSet getIdfToolSet(String id)
	{
		return toolsMap.get(id);
	}
	
	private void addIdfToolSet(String id, IDFToolSet idfToolSet)
	{
		toolsMap.put(id, idfToolSet);
	}
}
