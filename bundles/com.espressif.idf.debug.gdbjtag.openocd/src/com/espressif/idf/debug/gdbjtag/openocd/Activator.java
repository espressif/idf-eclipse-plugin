/*******************************************************************************
 * Copyright (c) 2013 Liviu Ionescu.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Liviu Ionescu - initial version
 *******************************************************************************/

package com.espressif.idf.debug.gdbjtag.openocd;

import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.espressif.idf.debug.gdbjtag.openocd.preferences.DefaultPreferences;
import com.espressif.idf.debug.gdbjtag.openocd.preferences.PersistentPreferences;

import org.eclipse.embedcdt.internal.ui.AbstractUIActivator;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIActivator {

	// ------------------------------------------------------------------------

	// The plug-in ID
	public static final String PLUGIN_ID = "com.espressif.idf.debug.gdbjtag.openocd"; //$NON-NLS-1$
	public static final String GDB_SERVER_LAUNCH_TIMEOUT = "fGdbServerLaunchTimeout"; //$NON-NLS-1$
	
	@Override
	protected void initializeDefaultPreferences(IPreferenceStore preferenceStore)
	{
		preferenceStore.setDefault(GDB_SERVER_LAUNCH_TIMEOUT, 25);
	}
	
	@Override
	public String getBundleId() {
		return PLUGIN_ID;
	}

	// ------------------------------------------------------------------------

	// The shared instance
	private static Activator fgInstance;

	public static Activator getInstance() {
		return fgInstance;
	}

	protected DefaultPreferences fDefaultPreferences = null;
	protected PersistentPreferences fPersistentPreferences = null;

	public Activator() {

		super();
		fgInstance = this;
	}

	// ------------------------------------------------------------------------

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	public DefaultPreferences getDefaultPreferences() {

		if (fDefaultPreferences == null) {
			fDefaultPreferences = new DefaultPreferences(PLUGIN_ID);
		}
		return fDefaultPreferences;
	}

	public PersistentPreferences getPersistentPreferences() {

		if (fPersistentPreferences == null) {
			fPersistentPreferences = new PersistentPreferences(PLUGIN_ID);
		}
		return fPersistentPreferences;
	}

	public static <T> T getService(Class<T> service)
	{
		BundleContext context = fgInstance.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

	// ------------------------------------------------------------------------
}
