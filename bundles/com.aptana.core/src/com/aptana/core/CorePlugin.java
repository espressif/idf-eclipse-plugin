/**
 * Aptana Studio
 * Copyright (c) 2005-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.BundleContext;

import com.aptana.core.diagnostic.IDiagnosticManager;
import com.aptana.core.internal.UserAgentManager;
import com.aptana.core.internal.diagnostic.DiagnosticManager;
import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.EclipseUtil;

/**
 * The activator class controls the plug-in life cycle
 */
public class CorePlugin extends Plugin implements IPreferenceChangeListener
{

	// The plug-in ID
	public static final String PLUGIN_ID = "com.aptana.core"; //$NON-NLS-1$


	// The shared instance
	private static CorePlugin plugin;

	private BundleContext context;

	private UserAgentManager fUserAgentManager;
	private IDiagnosticManager diagnosticManager;

	/**
	 * The constructor
	 */
	public CorePlugin()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception
	{
		this.context = context;
		super.start(context);

		plugin = this;

		Job job = new Job("Enable debugging and flush log cache") //$NON-NLS-1$
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				// Perhaps don't enable this if platform is already in -debug mode?
				//
				// Place after context & plugin assignments, as this relies on both existing already
				enableDebugging();
				IdeLog.flushCache();
				return Status.OK_STATUS;
			}
		};
		// DO NOT CALL EclipseUtil.setSystemForJob!!! It breaks startup by causing plugin loading issues in
		// resources.core plugin
		job.setSystem(true);
		job.schedule();
	}

	/**
	 * Enable the debugging options
	 */
	private void enableDebugging()
	{
		InstanceScope.INSTANCE.getNode(CorePlugin.PLUGIN_ID).addPreferenceChangeListener(this);

		// Returns the current severity preference
		IdeLog.StatusLevel currentSeverity = IdeLog.getSeverityPreference();
		IdeLog.setCurrentSeverity(currentSeverity);

		// If we are currently in debug mode, don't change the default settings
		if (!Platform.inDebugMode())
		{
			Boolean checked = Platform.getPreferencesService().getBoolean(CorePlugin.PLUGIN_ID,
					ICorePreferenceConstants.PREF_ENABLE_COMPONENT_DEBUGGING, false, null);
			EclipseUtil.setPlatformDebugging(checked);
			if (checked)
			{
				String[] components = EclipseUtil.getCurrentDebuggableComponents();
				EclipseUtil.setBundleDebugOptions(components, true);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception
	{
		diagnosticManager = null;
		try
		{
			// Don't listen to debug changes anymore
			InstanceScope.INSTANCE.getNode(CorePlugin.PLUGIN_ID).removePreferenceChangeListener(this);

			fUserAgentManager = null;
		}
		finally
		{
			plugin = null;
			super.stop(context);
		}
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CorePlugin getDefault()
	{
		return plugin;
	}

	/**
	 * Returns the {@link IDiagnosticManager}.
	 *
	 * @return {@link IDiagnosticManager}.
	 */
	public synchronized IDiagnosticManager getDiagnosticManager()
	{
		if (diagnosticManager == null)
		{
			diagnosticManager = new DiagnosticManager();
		}
		return diagnosticManager;
	}

	/**
	 * @return
	 * @deprecated uses {@link EclipseUtil#getStudioVersion()} instead
	 */
	@Deprecated
	public static String getAptanaStudioVersion()
	{
		return EclipseUtil.getStudioVersion();
	}

	/**
	 * Returns the current bundle context
	 *
	 * @return
	 */
	public BundleContext getContext()
	{
		// FIXME Can't we just call getBundle().getBundleContext()?
		return context;
	}

	/**
	 * Respond to a preference change event
	 */
	public void preferenceChange(PreferenceChangeEvent event)
	{
		if (ICorePreferenceConstants.PREF_DEBUG_LEVEL.equals(event.getKey()))
		{
			IdeLog.setCurrentSeverity(IdeLog.getSeverityPreference());
		}
	}

	public synchronized IUserAgentManager getUserAgentManager()
	{
		if (fUserAgentManager == null)
		{
			fUserAgentManager = new UserAgentManager();
		}
		return fUserAgentManager;
	}
}
