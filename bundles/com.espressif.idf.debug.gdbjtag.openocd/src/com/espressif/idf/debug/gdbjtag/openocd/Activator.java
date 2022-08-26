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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.espressif.idf.debug.gdbjtag.openocd.preferences.DefaultPreferences;
import com.espressif.idf.debug.gdbjtag.openocd.preferences.PersistentPreferences;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// ------------------------------------------------------------------------

	// The plug-in ID
	public static final String PLUGIN_ID = "com.espressif.idf.debug.gdbjtag.openocd"; //$NON-NLS-1$
	public static final String GDB_SERVER_LAUNCH_TIMEOUT = "fGdbServerLaunchTimeout"; //$NON-NLS-1$
	
	@Override
	protected void initializeDefaultPreferences(IPreferenceStore preferenceStore)
	{
		preferenceStore.setDefault(GDB_SERVER_LAUNCH_TIMEOUT, 25);
	}
	
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
	protected boolean fIsDebugging;
	
	public Activator() {

		super();
		fgInstance = this;
		fIsDebugging = "true".equalsIgnoreCase(Platform.getDebugOption(getBundleId() + "/debug"));
	}

	// ------------------------------------------------------------------------

	@Override
	public void start(BundleContext context) throws Exception {
		if (isDebugging()) {
			System.out.println(getBundleId() + ".start()");
		}
		super.start(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		if (isDebugging()) {
			System.out.println(getBundleId() + ".stop()");
		}
	}
	
	// ------------------------------------------------------------------------

	@Override
	public boolean isDebugging() {

		return fIsDebugging;
	}

	// ------------------------------------------------------------------------
	
	/**
	 * For a given name, get the 'icons/name.png' description from the registry.
	 * If not there, register it.
	 *
	 * @param name
	 *            a String with the file name (default .png).
	 * @return an ImageDescriptor or null.
	 */
	public ImageDescriptor getImageDescriptor(String name) {

		String str = name.toLowerCase();
		ImageDescriptor imageDescriptor = getImageRegistry().getDescriptor(str);
		if (imageDescriptor == null) {
			imageDescriptor = declareImage(str);
		}
		return imageDescriptor;
	}
	
	/**
	 * For a given name, get the 'icons/name.png' image from the registry. If
	 * not there, register it.
	 *
	 * @param name
	 *            a String with the file name (default .png).
	 * @return an Image, possibly a default one.
	 */
	public Image getImage(String name) {

		String str = name.toLowerCase();

		Image image = getImageRegistry().get(str);
		if (image != null) {
			// The common case, when the image exist; return it.
			return image;
		}

		// Image not known, register it.
		ImageDescriptor imageDescriptor = declareImage(str);
		if (imageDescriptor == null) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
		}
		image = getImageRegistry().get(str);
		return image;
	}

	/**
	 * For a given 'name', register 'icons/name.png' in the local image
	 * registry.
	 *
	 * @param name
	 *            a String with the file name (default .png).
	 * @return an ImageDescriptor or null.
	 */
	protected ImageDescriptor declareImage(String name) {

		Object path = new Path("icons/", name);
		Object pathx = path;
		String extension = ((IPath) path).getFileExtension();
		if ((extension == null) || (extension.isEmpty())) {
			// If missing, try default extension '.png'.
			pathx = ((IPath) path).addFileExtension("png");
		}

		// Check path in the plug-in name space.
		ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(getBundleId(),
				((IPath) pathx).toString());
		if (imageDescriptor == null) {
			if ((extension == null) || (extension.isEmpty())) {
				// If missing, second default extension is '.gif'.
				pathx = ((IPath) path).addFileExtension("gif");

				// Check gif path in the plug-in name space.
				imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(getBundleId(), ((IPath) pathx).toString());
			}
			if (imageDescriptor == null) {
				return null;
			}
		}
		try {
			String key = getKey(name);
			if (getImageRegistry().getDescriptor(key) == null) {
				getImageRegistry().put(key, imageDescriptor);
			}
		} catch (Exception e) {
			log(e);
		}
		return imageDescriptor;
	}

	/**
	 * The key is based on the image name.
	 *
	 * @param name
	 * @return
	 */
	private String getKey(String name) {
		return name.toLowerCase();
	}

	// ------------------------------------------------------------------------

	public static void log(IStatus status) {
		getInstance().getLog().log(status);
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getInstance().getBundleId(), 1, "Internal Error", e)); //$NON-NLS-1$
	}

	public static void log(String message) {
		log(new Status(IStatus.ERROR, getInstance().getBundleId(), 1, message, null)); // $NON-NLS-1$
	}

	// ------------------------------------------------------------------------

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
