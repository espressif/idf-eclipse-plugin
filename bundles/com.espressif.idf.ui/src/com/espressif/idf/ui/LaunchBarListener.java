/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.launchbar.core.ILaunchBarListener;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.ui.ILaunchBarUIManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.SDKConfigJsonReader;
import com.espressif.idf.core.util.StringUtil;

public class LaunchBarListener implements ILaunchBarListener
{
	private static boolean jtagIgnored = false;
	private static boolean targetChangeIgnored = false;

	public static void setIgnoreJtagTargetChange(boolean status)
	{
		jtagIgnored = status;
	}

	public static void setIgnoreTargetChange(boolean status)
	{
		targetChangeIgnored = status;
	}

	@Override
	public void activeLaunchTargetChanged(ILaunchTarget target)
	{
		Display.getDefault().asyncExec(() -> {
			if (target != null)
			{
				String targetName = target.getAttribute("com.espressif.idf.launch.serial.core.idfTarget", //$NON-NLS-1$
						StringUtil.EMPTY);
				if (!StringUtil.isEmpty(targetName) && (!targetChangeIgnored))
				{
					update(targetName);
				}
			}
		});

	}

	private void update(String newTarget)
	{
		// Get old IDF target for the project
		try
		{
			ILaunchBarManager launchBarManager = IDFCorePlugin.getService(ILaunchBarManager.class);
			ILaunchConfiguration activeConfig = launchBarManager.getActiveLaunchConfiguration();
			if (activeConfig == null)
			{
				return;
			}

			String projectName = activeConfig.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					StringUtil.EMPTY);
			if (projectName.isEmpty())
			{
				ILaunchDescriptor activeLaunchDescriptor = launchBarManager.getActiveLaunchDescriptor();
				projectName = activeLaunchDescriptor.getName();
			}
			if (!StringUtil.isEmpty(projectName))
			{

				// Get IProject
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IResource project = workspace.getRoot().findMember(projectName);

				Logger.log("Project Name:: " + projectName); //$NON-NLS-1$
				Logger.log("Project:: " + project); //$NON-NLS-1$

				// build folder exist?
				if (project != null)
				{
					File buildLocation = new File(IDFUtil.getBuildDir((IProject) project));
					if (buildLocation.exists())
					{
						// get current target
						String currentTarget = new SDKConfigJsonReader((IProject) project).getValue("IDF_TARGET"); //$NON-NLS-1$

						if ((activeConfig.getAttribute(IDFLaunchConstants.FLASH_OVER_JTAG, false) || activeConfig
								.getType().getIdentifier().contentEquals(IDFLaunchConstants.DEBUG_LAUNCH_CONFIG_TYPE))
								&& !jtagIgnored)
						{
							String targetForJtagFlash = activeConfig.getWorkingCopy()
									.getAttribute(IDFLaunchConstants.TARGET_FOR_JTAG, StringUtil.EMPTY);
							if (!newTarget.equals(targetForJtagFlash))
							{
								boolean isYes = MessageDialog.openQuestion(EclipseUtil.getShell(),
										Messages.LaunchBarListener_TargetChanged_Title,
										MessageFormat.format(Messages.LaunchBarListener_TargetDontMatch_Msg, newTarget,
												targetForJtagFlash, activeConfig.getName()));
								if (isYes)
								{
									ILaunchBarUIManager uiManager = UIPlugin.getService(ILaunchBarUIManager.class);
									uiManager.openConfigurationEditor(launchBarManager.getActiveLaunchDescriptor());
									deleteBuildFolder(project, buildLocation);
									return;
								}
							}
						}

						// If both are not same
						if (currentTarget != null && !newTarget.equals(currentTarget))
						{

							boolean isDelete = MessageDialog.openQuestion(EclipseUtil.getShell(),
									Messages.LaunchBarListener_TargetChanged_Title,
									MessageFormat.format(Messages.LaunchBarListener_TargetChanged_Msg,
											project.getName(), currentTarget, newTarget));
							if (isDelete)
							{
								deleteBuildFolder(project, buildLocation);
							}
						}
					}
				}

			}

		}
		catch (CoreException e1)
		{
			Logger.log(e1);
		}
	}

	private void deleteBuildFolder(IResource project, File buildLocation)
	{
		IWorkspaceRunnable runnable = new IWorkspaceRunnable()
		{

			@Override
			public void run(IProgressMonitor monitor) throws CoreException
			{

				monitor.beginTask("Deleting build folder...", 1); //$NON-NLS-1$
				Logger.log("Deleting build folder " + buildLocation.getAbsolutePath()); //$NON-NLS-1$
				deleteDirectory(buildLocation);
				cleanSdkConfig(project);
				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			}

		};

		// run workspace job
		try
		{
			ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());
		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), "Unable to delete the build folder", //$NON-NLS-1$
					e1);
		}
	}

	private boolean deleteDirectory(File directoryToBeDeleted)
	{
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null)
		{
			for (File file : allContents)
			{
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.getName().equals("build") || directoryToBeDeleted.delete(); //$NON-NLS-1$
	}

	private void cleanSdkConfig(IResource project)
	{
		File sdkconfig = new File(project.getLocation().toOSString(), "sdkconfig"); //$NON-NLS-1$
		if (sdkconfig.exists())
		{
			File sdkconfigOld = new File(project.getLocation().toOSString(), "sdkconfig.old"); //$NON-NLS-1$
			boolean isRenamed = sdkconfig.renameTo(sdkconfigOld);
			Logger.log(NLS.bind("Renaming {0} status...{1}", sdkconfig.getAbsolutePath(), isRenamed)); //$NON-NLS-1$

			if (!isRenamed) // sdkconfig.old might already exist
			{
				// delete sdkconfig.old file!
				Logger.log(
						NLS.bind("Deleting {0} status...{1}", sdkconfigOld.getAbsolutePath(), sdkconfigOld.delete())); //$NON-NLS-1$

				// attempting one more time!
				sdkconfig.renameTo(sdkconfigOld);
			}
		}
	}

}