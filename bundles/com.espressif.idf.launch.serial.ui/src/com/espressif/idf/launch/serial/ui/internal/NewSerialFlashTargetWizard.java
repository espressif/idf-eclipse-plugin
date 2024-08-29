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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetManager2;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.eclipse.launchbar.ui.target.LaunchTargetWizard;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.LaunchBarTargetConstants;
import com.espressif.idf.core.build.IDFLaunchConstants;

public class NewSerialFlashTargetWizard extends LaunchTargetWizard
{

	private NewSerialFlashTargetWizardPage page;

	public NewSerialFlashTargetWizard()
	{
		setWindowTitle(Messages.NewSerialFlashTargetWizard_Title);
	}

	@Override
	public void addPages()
	{
		super.addPages();
		page = new NewSerialFlashTargetWizardPage(getLaunchTarget());
		addPage(page);
	}

	@Override
	public boolean performFinish()
	{
		ILaunchTargetManager targetManager = Activator.getService(ILaunchTargetManager.class);

		String typeId = IDFLaunchConstants.ESP_LAUNCH_TARGET_TYPE;
		String id = page.getTargetName();
		ILaunchTarget target = ((ILaunchTargetManager2) targetManager).addLaunchTargetNoNotify(typeId, id);
		ILaunchTargetWorkingCopy wc = target.getWorkingCopy();
		wc.setId(id);
		wc.setAttribute(ILaunchTarget.ATTR_OS, page.getOS());
		wc.setAttribute(ILaunchTarget.ATTR_ARCH, page.getArch());
		wc.setAttribute(LaunchBarTargetConstants.SERIAL_PORT, page.getSerialPortName());
		wc.setAttribute(LaunchBarTargetConstants.TARGET, page.getIDFTarget());
		wc.setAttribute(LaunchBarTargetConstants.BOARD, page.getBoard());
		wc.setAttribute(LaunchBarTargetConstants.FLASH_VOLTAGE, page.getVoltage());
		wc.save();
		storeLastUsedSerialPort();

		// adding the target later to trigger LaunchBarListener with proper wc attributes
		Job job = new Job(Messages.AddingTargetJobName)
		{

			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				targetManager.addLaunchTarget(typeId, id);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return true;
	}

	private void storeLastUsedSerialPort()
	{
		Preferences preferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		preferences.put(LaunchBarTargetConstants.SERIAL_PORT, page.getSerialPortName());

		try
		{
			// forces the application to save the preferences
			preferences.flush();
		}
		catch (BackingStoreException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean canDelete()
	{
		return true;
	}

	@Override
	public void performDelete()
	{
		ILaunchTargetManager targetMgr = Activator.getService(ILaunchTargetManager.class);
		ILaunchTarget target = getLaunchTarget();
		if (target != null)
		{
			targetMgr.removeLaunchTarget(target);
		}
	}

}
