/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class SDKConfigCorePlugin extends Plugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "com.espressif.idf.sdk.config.core"; //$NON-NLS-1$

	private static Plugin plugin;

	public static BundleContext getContext()
	{
		return plugin.getBundle().getBundleContext();
	}

	public static String getId()
	{
		return PLUGIN_ID;
	}

	public static Plugin getPlugin()
	{
		return plugin;
	}

	public static <T> T getService(Class<T> service)
	{
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
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

	
}
