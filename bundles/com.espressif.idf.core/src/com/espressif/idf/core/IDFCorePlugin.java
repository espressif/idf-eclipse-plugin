/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.espressif.idf.core.build.ErrorMarkerListener;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFCorePlugin extends Plugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "com.espressif.idf.core"; //$NON-NLS-1$

	private static Plugin plugin;

	public static ErrorMarkerListener ERROR_MARKER_LISTENER;

	public static Plugin getPlugin()
	{
		return plugin;
	}

	public static String getId()
	{
		return PLUGIN_ID;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception
	{
		plugin = this;
		if (ERROR_MARKER_LISTENER == null)
		{
			ERROR_MARKER_LISTENER = new ErrorMarkerListener();
		}
		ResourcesPlugin.getWorkspace().addResourceChangeListener(ERROR_MARKER_LISTENER);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception
	{
		plugin = null;
		if (ERROR_MARKER_LISTENER != null)
		{
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(ERROR_MARKER_LISTENER);
		}
	}

	public static <T> T getService(Class<T> service)
	{
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

	public static IStatus errorStatus(String msg, Exception e)
	{
		return new Status(IStatus.ERROR, PLUGIN_ID, msg, e);
	}

	public static IStatus okStatus(String msg, Exception e)
	{
		return new Status(IStatus.OK, PLUGIN_ID, msg, e);
	}
}
