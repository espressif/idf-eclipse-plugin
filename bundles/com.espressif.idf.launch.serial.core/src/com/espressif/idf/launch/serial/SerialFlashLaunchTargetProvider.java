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
import org.eclipse.launchbar.core.target.TargetStatus;

/**
 * Launch Target used to flash images to a device over a serial port, usually
 * USB serial.
 */
public class SerialFlashLaunchTargetProvider implements ILaunchTargetProvider {

	//Launch Target type id
	public static final String TYPE_ID = "com.espressif.idf.launch.serial.core.serialFlashTarget"; //$NON-NLS-1$

	public static final String ATTR_SERIAL_PORT = "com.espressif.idf.launch.serial.core.serialPort"; //$NON-NLS-1$

	public static final String ATTR_IDF_TARGET = "com.espressif.idf.launch.serial.core.idfTarget"; //$NON-NLS-1$

	@Override
	public void init(ILaunchTargetManager targetManager) {
		// Nothing to do at init time
	}

	@Override
	public TargetStatus getStatus(ILaunchTarget target) {
		// Always OK
		return TargetStatus.OK_STATUS;
	}

}
