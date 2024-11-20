package com.espressif.idf.debug.gdbjtag.openocd.dsf;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.core.model.IProcess;

public class LaunchProcessDictionary
{
	private static LaunchProcessDictionary instance;
	
	private Map<String, Map<String, IProcess>> processDictionary;
	
	private LaunchProcessDictionary()
	{
		processDictionary = new HashMap<>();
	}
	
	public static LaunchProcessDictionary getInstance()
	{
		if(instance == null)
			instance = new LaunchProcessDictionary();
		
		return instance;
	}
	
	public void addProcessToDictionary(String launchName, String procName, IProcess process)
	{
		if (!processDictionary.containsKey(launchName))
		{
			Map<String, IProcess> processMap = new HashMap<>();
			processMap.put(procName, process);
			processDictionary.put(launchName, processMap);
			return;
		}
		
		Map<String, IProcess> processMap = processDictionary.get(launchName);
		processMap.put(procName, process);
		processDictionary.put(launchName, processMap);
	}
	
	public IProcess getProcessFromDictionary(String launchName, String procName)
	{
		if (!processDictionary.containsKey(launchName))
		{
			return null;
		}
		
		return processDictionary.get(launchName).get(procName);
	}

}
