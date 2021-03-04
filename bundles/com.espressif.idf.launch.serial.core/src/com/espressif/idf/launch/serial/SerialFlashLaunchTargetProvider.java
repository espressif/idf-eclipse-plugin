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
package com.espressif.idf.launch.serial;

import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetProvider;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.eclipse.launchbar.core.target.TargetStatus;

import com.espressif.idf.core.build.ESP32S2ToolChain;
import com.espressif.idf.core.build.ESP32ToolChain;
import com.espressif.idf.core.build.IDFLaunchConstants;

/**
 * Launch Target used to flash images to a device over a serial port, usually
 * USB serial.
 */
public class SerialFlashLaunchTargetProvider implements ILaunchTargetProvider {

	public static final String ATTR_SERIAL_PORT = "com.espressif.idf.launch.serial.core.serialPort"; //$NON-NLS-1$

	public static final String ATTR_IDF_TARGET = "com.espressif.idf.launch.serial.core.idfTarget"; //$NON-NLS-1$

	@Override
	public void init(ILaunchTargetManager targetManager) {

		//Create default esp32 target if that doesn't exist
		if (targetManager.getLaunchTarget(IDFLaunchConstants.ESP_LAUNCH_TARGET_TYPE, ESP32ToolChain.OS) == null) {
			ILaunchTarget target = targetManager.addLaunchTarget(IDFLaunchConstants.ESP_LAUNCH_TARGET_TYPE,
					ESP32ToolChain.OS);
			ILaunchTargetWorkingCopy wc = target.getWorkingCopy();
			wc.setAttribute(ILaunchTarget.ATTR_OS, ESP32ToolChain.OS);
			wc.setAttribute(ILaunchTarget.ATTR_ARCH, ESP32ToolChain.ARCH);
			wc.setAttribute(IDFLaunchConstants.ATTR_IDF_TARGET, ESP32ToolChain.OS);
			wc.save();
		}

		//Create default esp32s2 target if that doesn't exist
		if (targetManager.getLaunchTarget(IDFLaunchConstants.ESP_LAUNCH_TARGET_TYPE, ESP32S2ToolChain.OS) == null) {
			ILaunchTarget target = targetManager.addLaunchTarget(IDFLaunchConstants.ESP_LAUNCH_TARGET_TYPE,
					ESP32S2ToolChain.OS);
			ILaunchTargetWorkingCopy wc = target.getWorkingCopy();
			wc.setAttribute(ILaunchTarget.ATTR_OS, ESP32S2ToolChain.OS);
			wc.setAttribute(ILaunchTarget.ATTR_ARCH, ESP32S2ToolChain.ARCH);
			wc.setAttribute(IDFLaunchConstants.ATTR_IDF_TARGET, ESP32S2ToolChain.OS);
			wc.save();
		}

	}

	@Override
	public TargetStatus getStatus(ILaunchTarget target) {
		// Always OK
		return TargetStatus.OK_STATUS;
	}

}
