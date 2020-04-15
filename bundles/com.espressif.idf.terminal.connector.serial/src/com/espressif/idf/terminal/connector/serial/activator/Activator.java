/*******************************************************************************
 * Copyright (c) 2017, 2018 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package com.espressif.idf.terminal.connector.serial.activator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tm.terminal.view.core.tracing.TraceHandler;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	// The shared instance
	private static Activator plugin;
	// The trace handler instance
	private static volatile TraceHandler traceHandler;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() != null && getDefault().getBundle() != null) {
			return getDefault().getBundle().getSymbolicName();
		}
		return "com.espressif.idf.terminal.connector.serial"; //$NON-NLS-1$
	}

	/**
	 * Returns the bundles trace handler.
	 *
	 * @return The bundles trace handler.
	 */
	public static TraceHandler getTraceHandler() {
		if (traceHandler == null) {
			traceHandler = new TraceHandler(getUniqueIdentifier());
		}
		return traceHandler;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
	}

	/**
	 * Loads the image registered under the specified key from the image
	 * registry and returns the <code>Image</code> object instance.
	 *
	 * @param key The key the image is registered with.
	 * @return The <code>Image</code> object instance or <code>null</code>.
	 */
	public static Image getImage(String key) {
		return getDefault().getImageRegistry().get(key);
	}

	/**
	 * Loads the image registered under the specified key from the image
	 * registry and returns the <code>ImageDescriptor</code> object instance.
	 *
	 * @param key The key the image is registered with.
	 * @return The <code>ImageDescriptor</code> object instance or <code>null</code>.
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		return getDefault().getImageRegistry().getDescriptor(key);
	}

	public static void log(IStatus status) {
		plugin.getLog().log(status);
	}

	public static void log(Exception exception) {
		if (exception instanceof CoreException) {
			log(((CoreException) exception).getStatus());
		} else {
			log(new Status(IStatus.ERROR, plugin.getBundle().getSymbolicName(), exception.getLocalizedMessage(),
					exception));
		}
	}

}
