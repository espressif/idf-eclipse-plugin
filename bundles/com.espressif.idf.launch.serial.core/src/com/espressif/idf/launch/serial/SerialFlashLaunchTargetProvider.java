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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetProvider;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.eclipse.launchbar.core.target.TargetStatus;

import com.espressif.idf.core.build.AbstractESPToolchain;
import com.espressif.idf.core.build.ESP32C3ToolChain;
import com.espressif.idf.core.build.ESP32S2ToolChain;
import com.espressif.idf.core.build.ESP32S3ToolChain;
import com.espressif.idf.core.build.ESP32ToolChain;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;

/**
 * Launch Target used to flash images to a device over a serial port, usually
 * USB serial.
 */
public class SerialFlashLaunchTargetProvider implements ILaunchTargetProvider {

	public static final String ATTR_SERIAL_PORT = "com.espressif.idf.launch.serial.core.serialPort"; //$NON-NLS-1$

	public static final String ATTR_IDF_TARGET = "com.espressif.idf.launch.serial.core.idfTarget"; //$NON-NLS-1$

	@Override
	public void init(ILaunchTargetManager targetManager) {

		List<Class<? extends AbstractESPToolchain>> toolchainlist = new ArrayList<>();
		toolchainlist.add(ESP32ToolChain.class);
		toolchainlist.add(ESP32S2ToolChain.class);
		toolchainlist.add(ESP32S3ToolChain.class);
		toolchainlist.add(ESP32C3ToolChain.class);

		try {
			addLaunchTarget(targetManager, toolchainlist);
		} catch (Exception e) {
			Logger.log(e);
		}

	}

	private void addLaunchTarget(ILaunchTargetManager targetManager,
			List<Class<? extends AbstractESPToolchain>> toolchainlist)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		for (Class<?> toolchain : toolchainlist) {
			String os = (String) toolchain.getField("OS").get(null); //$NON-NLS-1$
			String arch = (String) toolchain.getField("ARCH").get(null); //$NON-NLS-1$

			if (targetManager.getLaunchTarget(IDFLaunchConstants.ESP_LAUNCH_TARGET_TYPE, os) == null) {
				ILaunchTarget target = targetManager.addLaunchTarget(IDFLaunchConstants.ESP_LAUNCH_TARGET_TYPE, os);
				ILaunchTargetWorkingCopy wc = target.getWorkingCopy();
				wc.setAttribute(ILaunchTarget.ATTR_OS, os);
				wc.setAttribute(ILaunchTarget.ATTR_ARCH, arch);
				wc.setAttribute(IDFLaunchConstants.ATTR_IDF_TARGET, os);
				wc.save();
			}
		}
	}

	@Override
	public TargetStatus getStatus(ILaunchTarget target) {
		// Always OK
		return TargetStatus.OK_STATUS;
	}

}
