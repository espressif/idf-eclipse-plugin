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

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetProvider;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.eclipse.launchbar.core.target.TargetStatus;

import com.espressif.idf.core.build.ESPToolChainManager;
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

		Collection<IToolChain> toolchainsWithoutDuplicateTargets = new ESPToolChainManager().getAllEspToolchains()
				.stream().filter(distinctByOs(tc -> tc.getProperty(IToolChain.ATTR_OS))).collect(Collectors.toList());

		try {
			addLaunchTarget(targetManager, toolchainsWithoutDuplicateTargets);
		} catch (Exception e) {
			Logger.log(e);
		}

	}

	private <T> Predicate<T> distinctByOs(Function<? super T, Object> extractor) {
		HashSet<Object> osSet = new HashSet<>();
		return t -> osSet.add(extractor.apply(t));
	}

	private void addLaunchTarget(ILaunchTargetManager targetManager,
			Collection<IToolChain> toolchainsWithoutDuplicateTargets)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		for (IToolChain toolchain : toolchainsWithoutDuplicateTargets) {
			String os = toolchain.getProperty(IToolChain.ATTR_OS);
			String arch = toolchain.getProperty(IToolChain.ATTR_ARCH);

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
