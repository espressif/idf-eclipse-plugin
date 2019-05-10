/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFCorePlugin extends Plugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "com.espressif.idf.core"; //$NON-NLS-1$

	private static Plugin plugin;

	public static BundleContext getContext()
	{
		return plugin.getBundle().getBundleContext();
	}

	public static Plugin getPlugin()
	{
		return plugin;
	}

	public static String getId()
	{
		return PLUGIN_ID;
	}

	public static void log(Exception e)
	{
		if (e instanceof CoreException)
		{
			plugin.getLog().log(((CoreException) e).getStatus());
		} else
		{
			plugin.getLog().log(new Status(IStatus.ERROR, IDFCorePlugin.getId(), e.getLocalizedMessage(), e));
		}
	}
	
	public static void log(Exception e, boolean onlyDebugMode)
	{
		if (!Platform.inDebugMode())
		{
			return;
		}
		log(e);
	}

	public static void logInfo(String message)
	{
		plugin.getLog().log(new Status(IStatus.INFO, IDFCorePlugin.getId(), message));
	}
	
	public static void logInfo(String message,  boolean onlyDebugMode)
	{
		plugin.getLog().log(new Status(IStatus.INFO, IDFCorePlugin.getId(), message));
	}
	
	@Override
	public void start(BundleContext bundleContext) throws Exception
	{
		plugin = this;
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception
	{
		plugin = null;
	}

	public static <T> T getService(Class<T> service)
	{
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

	public static CoreException coreException(Throwable e)
	{
		if (e instanceof RuntimeException && e.getCause() instanceof CoreException)
		{
			return (CoreException) e.getCause();
		} else
			if (e instanceof CoreException)
			{
				return (CoreException) e;
			}
		return new CoreException(new Status(IStatus.ERROR, getId(), e.getLocalizedMessage(), e));
	}

	public static CoreException coreException(String message, Throwable e)
	{
		return new CoreException(new Status(IStatus.ERROR, getId(), message, e));
	}
}
