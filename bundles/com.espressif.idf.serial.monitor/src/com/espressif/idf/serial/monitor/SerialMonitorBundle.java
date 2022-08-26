package com.espressif.idf.serial.monitor;

import org.eclipse.embedcdt.internal.ui.AbstractUIActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class SerialMonitorBundle extends AbstractUIActivator
{
	public static final String PLUGIN_ID = "com.espressif.idf.serial.monitor"; //$NON-NLS-1$

	private static BundleContext context;

	private static SerialMonitorBundle fgInstance;

	public SerialMonitorBundle()
	{
		super();
		fgInstance = this;
	}

	public static SerialMonitorBundle getInstance()
	{
		return fgInstance;
	}

	static BundleContext getContext()
	{
		return context;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception
	{
		super.start(bundleContext);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception
	{
		super.stop(bundleContext);
	}

	public static <T> T getService(Class<T> service)
	{
		BundleContext context = fgInstance.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

	@Override
	public String getBundleId()
	{
		return PLUGIN_ID;
	}
}
