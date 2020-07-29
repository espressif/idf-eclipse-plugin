/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial version
 *     Espressif Systems Ltd — Kondal Kolipaka <kondal.kolipaka@espressif.com>

 *******************************************************************************/
package com.espressif.idf.launch.serial.ui.internal;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.eclipse.launchbar.ui.target.LaunchTargetWizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.SDKConfigJsonReader;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.launch.serial.SerialFlashLaunchTargetProvider;

public class NewSerialFlashTargetWizard extends LaunchTargetWizard {

	private NewSerialFlashTargetWizardPage page;

	public NewSerialFlashTargetWizard() {
		setWindowTitle(Messages.NewSerialFlashTargetWizard_Title);
	}

	@Override
	public void addPages() {
		super.addPages();

		page = new NewSerialFlashTargetWizardPage(getLaunchTarget());
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		ILaunchTargetManager manager = Activator.getService(ILaunchTargetManager.class);
		String typeId = SerialFlashLaunchTargetProvider.TYPE_ID;
		String id = page.getTargetName();

		ILaunchTarget target = getLaunchTarget();
		if (target == null) {
			target = manager.addLaunchTarget(typeId, id);
		}

		ILaunchTargetWorkingCopy wc = target.getWorkingCopy();
		wc.setId(id);
		wc.setAttribute(ILaunchTarget.ATTR_OS, page.getOS());
		wc.setAttribute(ILaunchTarget.ATTR_ARCH, page.getArch());
		wc.setAttribute(SerialFlashLaunchTargetProvider.ATTR_SERIAL_PORT, page.getSerialPortName());
		wc.setAttribute(SerialFlashLaunchTargetProvider.ATTR_IDF_TARGET, page.getIDFTarget());
		wc.save();

		storeLastUsedSerialPort();
		String newIdfTarget = page.getIDFTarget();

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				update(newIdfTarget);
			}

			private void update(String newTarget) {
				//Get old IDF target for the project
				try {
					ILaunchBarManager launchBarManager = Activator.getService(ILaunchBarManager.class);
					ILaunchDescriptor activeLaunchDescriptor = launchBarManager.getActiveLaunchDescriptor();

					String projectName = activeLaunchDescriptor.getName();
					if (!StringUtil.isEmpty(projectName)) {

						//Get IProject
						IWorkspace workspace = ResourcesPlugin.getWorkspace();
						IResource project = workspace.getRoot().findMember(projectName);

						Logger.log("Project Name:: " + projectName); //$NON-NLS-1$
						Logger.log("Project:: " + project); //$NON-NLS-1$

						//build folder exist?
						if (project != null) {
							File buildLocation = new File(project.getLocation() + "/build"); //$NON-NLS-1$
							if (buildLocation.exists()) {
								//get current target
								String currentTarget = new SDKConfigJsonReader((IProject) project)
										.getValue("IDF_TARGET"); //$NON-NLS-1$

								//If both are not same
								if (currentTarget != null && !newTarget.equals(currentTarget)) {
									boolean isDelete = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
											"IDF TARGET Changed", //$NON-NLS-1$
											"IDF_TARGET has changed for the project. Do you want to delete the `build` folder for the project?"); //$NON-NLS-1$
									if (isDelete) {
										IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

											@Override
											public void run(IProgressMonitor monitor) throws CoreException {

												monitor.beginTask("Deleting build folder...", 1); //$NON-NLS-1$
												Logger.log("Deleting build folder " + buildLocation.getAbsolutePath()); //$NON-NLS-1$
												deleteDirectory(buildLocation);
												cleanSdkConfig(project);
												project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
											}

										};

										//run workspace job
										try {
											ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());
										} catch (Exception e1) {
											Logger.log(IDFCorePlugin.getPlugin(), "Unable to delete the build folder", //$NON-NLS-1$
													e1);
										}
									}
								}
							}
						}

					}

				} catch (CoreException e1) {
					Logger.log(e1);
				}
			}
		});

		return true;
	}

	private boolean deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}

	private void cleanSdkConfig(IResource project) {
		File sdkconfig = new File(project.getLocation().toOSString(), "sdkconfig"); //$NON-NLS-1$
		if (sdkconfig.exists()) {
			File sdkconfigOld = new File(project.getLocation().toOSString(), "sdkconfig.old"); //$NON-NLS-1$
			boolean isRenamed = sdkconfig.renameTo(sdkconfigOld);
			Logger.log(NLS.bind("Renaming {0} status...{1}", sdkconfig.getAbsolutePath(), isRenamed)); //$NON-NLS-1$

			if (!isRenamed) //sdkconfig.old might already exist
			{
				//delete sdkconfig.old file!
				Logger.log(
						NLS.bind("Deleting {0} status...{1}", sdkconfigOld.getAbsolutePath(), sdkconfigOld.delete())); //$NON-NLS-1$

				//attempting one more time!
				sdkconfig.renameTo(sdkconfigOld);
			}
		}
	}

	private void storeLastUsedSerialPort() {
		Preferences preferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		preferences.put(SerialFlashLaunchTargetProvider.ATTR_SERIAL_PORT, page.getSerialPortName());

		try {
			// forces the application to save the preferences
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

}
