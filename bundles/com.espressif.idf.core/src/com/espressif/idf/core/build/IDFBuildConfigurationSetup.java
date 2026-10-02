/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build;

import java.text.MessageFormat;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfiguration2;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.logging.Logger;

/**
 * Creates the Core Build configuration of a freshly created IDF project and makes it active.
 * <p>
 * Normally CDT's {@code CoreBuildLaunchBarTracker} does this in response to Launch Bar changes, but it skips the work
 * whenever the incoming descriptor, mode and target all equal the ones it handled last. Deleting a project leaves those
 * fields pointing at it, so a project created afterwards under the same name matches all three and is silently left
 * without a configuration. The wizard knows the project and the chip it just created, so it performs the assignment
 * itself instead of relying on an event reaching the tracker.
 * </p>
 * <p>
 * The call is idempotent: when the tracker did run, the configuration already exists and is already active, and this
 * becomes a no-op.
 * </p>
 */
public final class IDFBuildConfigurationSetup
{
	private IDFBuildConfigurationSetup()
	{
	}

	/**
	 * Schedules creation and activation of the Core Build configuration for a newly created project.
	 *
	 * @param project newly created IDF project
	 * @param launchTarget launch target of the chip selected in the wizard
	 */
	public static void schedule(IProject project, ILaunchTarget launchTarget)
	{
		if (project == null || launchTarget == null || ILaunchTarget.NULL_TARGET.equals(launchTarget))
		{
			return;
		}

		Job job = new Job(Messages.IDFBuildConfigurationSetup_JobName)
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				if (monitor.isCanceled() || !project.isAccessible())
				{
					return Status.OK_STATUS;
				}

				try
				{
					assignConfiguration(project, launchTarget, monitor);
				}
				catch (CoreException e)
				{
					Logger.log(e);
				}
				return Status.OK_STATUS;
			}
		};
		// Serialises against the tracker job, which takes the same rule, so whichever runs second sees the result of
		// the first instead of racing it over the project description
		job.setRule(project.getWorkspace().getRoot());
		job.schedule();
	}

	private static void assignConfiguration(IProject project, ILaunchTarget launchTarget, IProgressMonitor monitor)
			throws CoreException
	{
		ICBuildConfigurationManager configManager = CCorePlugin.getService(ICBuildConfigurationManager.class);
		IToolChainManager toolChainManager = CCorePlugin.getService(IToolChainManager.class);
		ILaunchBarManager launchBarManager = IDFCorePlugin.getService(ILaunchBarManager.class);
		if (configManager == null || toolChainManager == null || launchBarManager == null
				|| !configManager.supports(project))
		{
			return;
		}

		ILaunchMode launchMode = launchBarManager.getActiveLaunchMode();
		String launchModeId = launchMode != null ? launchMode.getIdentifier() : ILaunchManager.RUN_MODE;
		for (IToolChain toolChain : toolChainManager.getToolChainsMatching(launchTarget.getAttributes()))
		{
			ICBuildConfiguration buildConfig = configManager.getBuildConfiguration(project, toolChain, launchModeId,
					launchTarget, monitor);
			if (buildConfig != null)
			{
				setActive(buildConfig, monitor);
				return;
			}
		}

		Logger.log(MessageFormat.format("No IDF build configuration is available for project {0} and target {1}", //$NON-NLS-1$
				project.getName(), launchTarget.getId()));
	}

	private static void setActive(ICBuildConfiguration buildConfig, IProgressMonitor monitor) throws CoreException
	{
		if (buildConfig instanceof CBuildConfiguration)
		{
			((CBuildConfiguration) buildConfig).setActive(monitor);
		}
		if (buildConfig instanceof ICBuildConfiguration2)
		{
			((ICBuildConfiguration2) buildConfig).setActive();
		}
	}
}
