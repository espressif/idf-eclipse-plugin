package com.espressif.idf.core;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

import com.espressif.idf.core.logging.Logger;

import java.io.File;

public class TelemetryAgentLoader
{
	@SuppressWarnings("nls")
	public static void loadAgent(String agentPath)
	{
		try
		{
			// Get the JVM's Instrumentation API
			Class<?> vmClass = Class.forName("com.sun.tools.attach.VirtualMachine");
			Method attachMethod = vmClass.getDeclaredMethod("attach", String.class);
			Method loadAgentMethod = vmClass.getDeclaredMethod("loadAgent", String.class);

			Object vm = attachMethod.invoke(null, getCurrentJvmPid());
			loadAgentMethod.invoke(vm, agentPath);

			Logger.log("Agent loaded successfully!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("nls")
	private static String getCurrentJvmPid()
	{
		return java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
	}
	
	//test 
	public static void main(String[] args)
	{
		loadAgent("/Users/kondalkolipaka/esp/idf-eclipse-plugin/bundles/com.espressif.idf.core/lib/applicationinsights-agent-3.6.2.jar");
	}
}
