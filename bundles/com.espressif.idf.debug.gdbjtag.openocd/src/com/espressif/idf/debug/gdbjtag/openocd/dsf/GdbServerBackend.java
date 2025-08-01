/*******************************************************************************
 * Copyright (c) 2014 Liviu Ionescu.
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

package com.espressif.idf.debug.gdbjtag.openocd.dsf;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.embedcdt.core.StringUtils;
import org.eclipse.embedcdt.debug.gdbjtag.core.DebugUtils;
import org.eclipse.embedcdt.debug.gdbjtag.core.dsf.GnuMcuGdbServerBackend;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.osgi.framework.BundleContext;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.debug.gdbjtag.openocd.Activator;
import com.espressif.idf.debug.gdbjtag.openocd.Configuration;

public class GdbServerBackend extends GnuMcuGdbServerBackend {

	// ------------------------------------------------------------------------
	protected int fGdbServerLaunchDefaultTimeout = 25;
	protected boolean fDoStartGdbClient;

	// ------------------------------------------------------------------------

	public GdbServerBackend(DsfSession session, ILaunchConfiguration lc) {
		super(session, lc);

		if (Activator.getInstance().isDebugging()) {
			System.out.println("openocd.GdbServerBackend(" + session + "," + lc.getName() + ")");
		}
	}

	// ------------------------------------------------------------------------

	@Override
	public void initialize(final RequestMonitor rm) {

		if (Activator.getInstance().isDebugging()) {
			System.out.println("openocd.GdbServerBackend.initialize()");
		}

		try {
			// Update parent data member before calling initialise.
			fDoStartGdbServer = Configuration.getDoStartGdbServer(fLaunchConfiguration);
		} catch (CoreException e) {
			rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Cannot get configuration", e)); //$NON-NLS-1$
			rm.done();
			return;
		}

		try {
			// Update parent data member before calling initialise.
			fDoStartGdbClient = Configuration.getDoStartGdbClient(fLaunchConfiguration);
		} catch (CoreException e) {
			rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "Cannot get configuration", e)); //$NON-NLS-1$
			rm.done();
			return;
		}

		// Initialise the super class, and, when ready, perform the local
		// initialisations.
		super.initialize(new RequestMonitor(getExecutor(), rm) {

			protected void handleSuccess() {
				doInitialize(rm);
			}
		});
	}

	private void doInitialize(RequestMonitor rm) {

		if (Activator.getInstance().isDebugging()) {
			System.out.println("openocd.GdbServerBackend.doInitialize()");
		}
		rm.done();
	}

	@Override
	public void shutdown(final RequestMonitor rm) {

		if (Activator.getInstance().isDebugging()) {
			System.out.println("openocd.GdbServerBackend.shutdown()");
		}

		super.shutdown(rm);
	}

	@Override
	public void destroy() {

		if (Activator.getInstance().isDebugging()) {
			System.out.println("openocd.GdbServerBackend.destroy() " + Thread.currentThread());
		}

		// Destroy the parent (the GDB server; the client is also destroyed
		// there).
		super.destroy();
	}

	// ------------------------------------------------------------------------

	@Override
	protected BundleContext getBundleContext() {
		return Activator.getInstance().getBundle().getBundleContext();
	}

	@Override
	public String[] getServerCommandLineArray() {
		String[] commandLineArray = Configuration.getGdbServerCommandLineArray(fLaunchConfiguration);

		return commandLineArray;
	}

	public String getServerCommandName() {

		String[] commandLineArray = getServerCommandLineArray();
		if (commandLineArray == null) {
			return null;
		}

		String fullCommand = commandLineArray[0];
		return StringUtils.extractNameFromPath(fullCommand);
	}

	@Override
	public int getServerLaunchTimeoutSeconds() {
		return Platform.getPreferencesService().getInt(IDFCorePlugin.PLUGIN_ID, Activator.GDB_SERVER_LAUNCH_TIMEOUT,
				fGdbServerLaunchDefaultTimeout, null);
	}

	public String getServerName() {
		return "OpenOCD";
	}

	public boolean canMatchStdOut() {
		return false;
	}

	public boolean canMatchStdErr() {
		// Do not match stderr if we do not start the client.
		return fDoStartGdbClient ? true : false;
	}

	public boolean matchStdErrExpectedPattern(String line) {
		if (line.indexOf("Started by GNU ARM Eclipse") >= 0 || line.indexOf("Started by GNU MCU Eclipse") >= 0) {
			return true;
		}

		return false;
	}

	/**
	 * Since the J-Link stderr messages are not final, this function makes the
	 * best use of the available information (the exit code and the captured
	 * string) to compose the text displayed in case of error.
	 * 
	 * @param exitCode
	 *            an integer with the process exit code.
	 * @param message
	 *            a string with the captured stderr text.
	 * @return a string with the text to be displayed.
	 */
	@Override
	public String prepareMessageBoxText(int exitCode) {

		String body = "";

		String name = getServerCommandName();
		if (name == null) {
			name = "GDB Server";
		}
		String tail = "\n\nFor more details, see the " + name + " console.";

		if (body.isEmpty()) {
			return getServerName() + " failed with code (" + exitCode + ")." + tail;
		} else {
			return getServerName() + " failed: \n" + body + tail;
		}
	}

	// ------------------------------------------------------------------------

	@Override
	protected Process launchGdbServerProcess(String[] commandLineArray) throws CoreException {
		File dir = null;
		IPath path = DebugUtils.getGdbWorkingDirectory(fLaunchConfiguration);
		if (path != null) {
			dir = new File(path.toOSString());
		}

		// Get the default environment
		String[] envp = DebugUtils.getLaunchEnvironment(fLaunchConfiguration);
		Map<String, String> envMap = new HashMap<>();
		for (String env : envp) {
			int idx = env.indexOf('=');
			if (idx > 0) envMap.put(env.substring(0, idx), env.substring(idx + 1));
		}

		// Add custom env var from launch config
		ILaunchTarget activeLaunchTarget = Activator.getService(ILaunchBarManager.class).getActiveLaunchTarget();
		if (activeLaunchTarget != null)
		{
			String openocdLoc = activeLaunchTarget.getAttribute(IDFLaunchConstants.OPENOCD_USB_LOCATION, (String) null);
			if (openocdLoc != null) {
				envMap.put(IDFLaunchConstants.OPENOCD_USB_LOCATION, openocdLoc);
			}
		}

		// Convert back to envp
		java.util.List<String> envList = new java.util.ArrayList<>();
		for (Entry<String, String> entry : envMap.entrySet()) {
			envList.add(entry.getKey() + "=" + entry.getValue());
		}

		return DebugUtils.exec(commandLineArray, envList.toArray(new String[0]), dir);
	}
}
