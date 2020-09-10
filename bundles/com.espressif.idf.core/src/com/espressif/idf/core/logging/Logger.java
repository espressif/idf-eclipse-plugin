/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.logging;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

import com.espressif.idf.core.IDFCorePlugin;

/**
 * IDF Eclipse plug-in logger
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class Logger
{

	public static void log(String message)
	{
		log(IDFCorePlugin.getPlugin(), message);
	}

	public static void log(Exception e)
	{
		log(IDFCorePlugin.getPlugin(), e);
	}
	
	public static void log(Exception e, boolean onlyDebugMode)
	{
		if (onlyDebugMode && Platform.inDebugMode())
		{
			log(IDFCorePlugin.getPlugin(), e);
		}
	}

	public static void logError(String message)
	{
		logError(IDFCorePlugin.getPlugin(), message);
	}

	public static void log(Plugin plugin, Exception e)
	{
		if (e instanceof CoreException)
		{
			plugin.getLog().log(((CoreException) e).getStatus());
		}
		else
		{
			String symbolicName = IDFCorePlugin.PLUGIN_ID;
			if (plugin != null && plugin.getBundle() != null)
			{
				symbolicName = plugin.getBundle().getSymbolicName();
			}
			plugin.getLog().log(new Status(IStatus.ERROR, symbolicName, e.getLocalizedMessage(), e));
		}
	}

	public static void log(Plugin plugin, String message, Exception e)
	{
		plugin.getLog().log(createStatus(message, e));
	}

	public static void log(Plugin plugin, Exception e, boolean onlyDebugMode)
	{
		if (onlyDebugMode && !Platform.inDebugMode())
		{
			return;
		}
		log(plugin, e);
	}

	public static void log(Plugin plugin, String message)
	{
		String symbolicName = IDFCorePlugin.PLUGIN_ID;
		if (plugin != null && plugin.getBundle() != null)
		{
			symbolicName = plugin.getBundle().getSymbolicName();
		}
		plugin.getLog().log(new Status(IStatus.INFO, symbolicName, message));
	}

	public static void log(Plugin plugin, String message, boolean onlyDebugMode)
	{
		if (onlyDebugMode && !Platform.inDebugMode())
		{
			return;
		}
		log(plugin, message);
	}

	public static void log(Plugin plugin, IStatus status)
	{
		plugin.getLog().log(status);
	}

	public static void logError(Plugin plugin, String message)
	{
		plugin.getLog().log(createStatus(message));
	}

	public static void logTrace(Plugin plugin, String message)
	{
		log(plugin, message, true);
	}
	
	public static void logTrace(String message)
	{
		log(IDFCorePlugin.getPlugin(), message, true);
	}

	public static IStatus createStatus(String msg)
	{
		return createStatus(msg, null);
	}

	public static IStatus createStatus(String msg, Throwable e)
	{
		return new Status(IStatus.ERROR, IDFCorePlugin.getId(), msg, e);
	}

}
