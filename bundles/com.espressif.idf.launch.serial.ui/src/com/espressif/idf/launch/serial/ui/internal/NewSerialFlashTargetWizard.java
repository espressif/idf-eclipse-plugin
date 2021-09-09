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

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.eclipse.launchbar.ui.target.LaunchTargetWizard;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.build.IDFLaunchConstants;
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
		String typeId = IDFLaunchConstants.ESP_LAUNCH_TARGET_TYPE;
		String id = page.getTargetName();
		ILaunchTarget target = getLaunchTarget();
		target = manager.addLaunchTarget(typeId, id);
		ILaunchTargetWorkingCopy wc = target.getWorkingCopy();
		wc.setId(id);
		wc.setAttribute(ILaunchTarget.ATTR_OS, page.getOS());
		wc.setAttribute(ILaunchTarget.ATTR_ARCH, page.getArch());
		wc.setAttribute(SerialFlashLaunchTargetProvider.ATTR_SERIAL_PORT, page.getSerialPortName());
		wc.setAttribute(SerialFlashLaunchTargetProvider.ATTR_IDF_TARGET, page.getIDFTarget());
		wc.save();

		storeLastUsedSerialPort();
		return true;
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

	@Override
	public boolean canDelete() {
		return true;
	}

	@Override
	public void performDelete() {
		ILaunchTargetManager targetMgr = Activator.getService(ILaunchTargetManager.class);
		ILaunchTarget target = getLaunchTarget();
		if (target != null) {
			targetMgr.removeLaunchTarget(target);
		}
	}

}
