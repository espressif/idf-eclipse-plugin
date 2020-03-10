/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core;

import org.eclipse.core.runtime.Plugin;
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

	
}
