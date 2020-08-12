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

package ilg.gnumcueclipse.debug.gdbjtag.openocd.dsf;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConstants;
import org.eclipse.cdt.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISourceLocator;

import ilg.gnumcueclipse.debug.gdbjtag.dsf.GnuMcuLaunch;
import ilg.gnumcueclipse.debug.gdbjtag.openocd.Activator;
import ilg.gnumcueclipse.debug.gdbjtag.openocd.Configuration;
import ilg.gnumcueclipse.debug.gdbjtag.openocd.ConfigurationAttributes;
import ilg.gnumcueclipse.debug.gdbjtag.openocd.preferences.DefaultPreferences;

@SuppressWarnings("restriction")
public class Launch extends GnuMcuLaunch {

	// ------------------------------------------------------------------------

	ILaunchConfiguration fConfig = null;
	private DsfSession fSession;
	private DsfServicesTracker fTracker;
	private DefaultDsfExecutor fExecutor;

	// ------------------------------------------------------------------------

	public Launch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator) {

		super(launchConfiguration, mode, locator);

		if (Activator.getInstance().isDebugging()) {
			System.out.println("openocd.Launch.launch(" + launchConfiguration.getName() + "," + mode + ") " + this);
		}

		fConfig = launchConfiguration;
		fExecutor = (DefaultDsfExecutor) getDsfExecutor();
		fSession = getSession();
	}

	// ------------------------------------------------------------------------

	@Override
	public void initialize() {

		if (Activator.getInstance().isDebugging()) {
			System.out.println("openocd.Launch.initialize() " + this);
		}

		super.initialize();

		Runnable initRunnable = new DsfRunnable() {
			@Override
			public void run() {
				fTracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fSession.getId());
				// fSession.addServiceEventListener(GdbLaunch.this, null);

				// fInitialized = true;
				// fireChanged();
			}
		};

		// Invoke the execution code and block waiting for the result.
		try {
			fExecutor.submit(initRunnable).get();
		} catch (InterruptedException e) {
			new Status(IStatus.ERROR, Activator.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
					"Error initializing launch", e); //$NON-NLS-1$
		} catch (ExecutionException e) {
			new Status(IStatus.ERROR, Activator.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
					"Error initializing launch", e); //$NON-NLS-1$
		}
	}

	@Override
	protected void provideDefaults(ILaunchConfigurationWorkingCopy config) throws CoreException {

		super.provideDefaults(config);

		if (!config.hasAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS)) {
			config.setAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS, "localhost");
		}

		if (!config.hasAttribute(IGDBJtagConstants.ATTR_JTAG_DEVICE)) {
			config.setAttribute(IGDBJtagConstants.ATTR_JTAG_DEVICE, ConfigurationAttributes.JTAG_DEVICE);
		}

		if (!config.hasAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER)) {
			config.setAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER,
					DefaultPreferences.GDB_SERVER_GDB_PORT_NUMBER_DEFAULT);
		}

		if (!config.hasAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME)) {
			DefaultPreferences fDefaultPreferences = Activator.getInstance().getDefaultPreferences();
			config.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME,
					fDefaultPreferences.getGdbClientExecutable());
		}
	}

	// ------------------------------------------------------------------------

	public void initializeServerConsole(IProgressMonitor monitor) throws CoreException {

		if (Activator.getInstance().isDebugging()) {
			System.out.println("openocd.Launch.initializeServerConsole()");
		}

		IProcess newProcess;
		boolean doAddServerConsole = Configuration.getDoAddServerConsole(fConfig);

		if (doAddServerConsole) {

			// Add the GDB server process to the launch tree
			newProcess = addServerProcess(Configuration.getGdbServerCommandName(fConfig));
			newProcess.setAttribute(IProcess.ATTR_CMDLINE, Configuration.getGdbServerCommandLine(fConfig));

			monitor.worked(1);
		}
	}

	public void initializeConsoles(IProgressMonitor monitor) throws CoreException {

		if (Activator.getInstance().isDebugging()) {
			System.out.println("openocd.Launch.initializeConsoles()");
		}

		IProcess newProcess;
		{
			// Add the GDB client process to the launch tree.
			newProcess = addClientProcess(Configuration.getGdbClientCommandName(fConfig));
			newProcess.setAttribute(IProcess.ATTR_CMDLINE, Configuration.getGdbClientCommandLine(fConfig));

			monitor.worked(1);
		}
	}

	public IProcess addServerProcess(String label) throws CoreException {
		IProcess newProcess = null;
		try {
			// Add the server process object to the launch.
			Process serverProc = getDsfExecutor().submit(new Callable<Process>() {
				@Override
				public Process call() throws CoreException {
					GdbServerBackend backend = (GdbServerBackend) fTracker.getService(GdbServerBackend.class);
					if (backend != null) {
						return backend.getServerProcess();
					}
					return null;
				}
			}).get();

			// Need to go through DebugPlugin.newProcess so that we can use
			// the overrideable process factory to allow others to override.
			// First set attribute to specify we want to create the gdb process.
			// Bug 210366
			Map<String, String> attributes = new HashMap<String, String>();
			if (serverProc != null) {
				newProcess = DebugPlugin.newProcess(this, serverProc, label, attributes);
			}
		} catch (InterruptedException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0,
					"Interrupted while waiting for get process callable.", e)); //$NON-NLS-1$
		} catch (ExecutionException e) {
			throw (CoreException) e.getCause();
		} catch (RejectedExecutionException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0,
					"Debugger shut down before launch was completed.", e)); //$NON-NLS-1$
		}

		return newProcess;
	}

	// ------------------------------------------------------------------------
}
