package com.espressif.idf.debug.gdbjtag.openocd.dsf;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IProcess;

public class LaunchProcessDictionary
{
	private static final LaunchProcessDictionary instance = new LaunchProcessDictionary();

	private final Map<String, Map<String, IProcess>> processDictionary = new ConcurrentHashMap<>();

	private LaunchProcessDictionary()
	{
	}

	public static LaunchProcessDictionary getInstance()
	{
		return instance;
	}

	public void addProcessToDictionary(String launchName, String procName, IProcess process)
	{
		processDictionary.computeIfAbsent(launchName, k -> new ConcurrentHashMap<>()).put(procName, process);
	}

	public IProcess getProcessFromDictionary(String launchName, String procName)
	{
		var processMap = processDictionary.get(launchName);
		return processMap != null ? processMap.get(procName) : null;
	}

	public void killAllProcessesInLaunch(String launchName)
	{
		var processMap = processDictionary.remove(launchName);

		if (processMap == null)
			return;

		for (var process : processMap.values())
		{
			if (process != null && !process.isTerminated())
			{

				Optional.ofNullable(process.getAdapter(Process.class)).map(Process::toHandle)
						.ifPresent(ProcessHandle::destroyForcibly);

				Thread.ofVirtual().name("AsyncTerminator-" + process.getLabel()).start(() -> {
					try
					{
						process.terminate();
					}
					catch (DebugException ignore)
					{
					}
				});
			}
		}
	}
}
