/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 * 
 *         The activator class controls the plug-in life cycle
 *
 */
public class UIPlugin extends AbstractUIPlugin
{

	// Context where registered the custom stop build/launch command
	private static final String LAUNCH_CONTEXT = "com.espressif.idf.ui.espLaunchScope"; //$NON-NLS-1$

	// The plug-in ID
	public static final String PLUGIN_ID = "com.espressif.idf.ui"; //$NON-NLS-1$

	// The shared instance
	private static UIPlugin plugin;

	/**
	 * The constructor
	 */
	public UIPlugin()
	{
	}

	@Override
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		IContextService contextService = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
		contextService.activateContext(LAUNCH_CONTEXT);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception
	{
		plugin = null;
		super.stop(context);
	}

	public static <T> T getService(Class<T> service)
	{
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static UIPlugin getDefault()
	{
		return plugin;
	}

	/**
	 * @param path
	 * @return
	 */
	public static ImageDescriptor getImageDescriptor(String path)
	{
		ImageDescriptor imageDescriptor = getDefault().getImageRegistry().getDescriptor(path);
		if (imageDescriptor == null)
		{
			imageDescriptor = imageDescriptorFromPlugin(PLUGIN_ID, path);
			if (imageDescriptor != null)
			{
				getDefault().getImageRegistry().put(path, imageDescriptor);
			}
		}
		return imageDescriptor;
	}

	/**
	 * @param string
	 * @return
	 */
	public static Image getImage(String string)
	{
		if (getImageDescriptor(string) != null)
		{
			return getDefault().getImageRegistry().get(string);
		}
		return null;
	}
}
