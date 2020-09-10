package com.espressif.idf.serial.monitor;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class SerialMonitorBundle implements BundleActivator
{

	private static BundleContext context;

	static BundleContext getContext()
	{
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception
	{
		SerialMonitorBundle.context = bundleContext;
	}

	public void stop(BundleContext bundleContext) throws Exception
	{
		SerialMonitorBundle.context = null;
	}

}
