/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.launch.strategies;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import com.espressif.idf.core.tools.launch.LaunchResult;

/**
 * Strategy interface for launching EIM
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 * *
 */
public interface EimLauncherStrategy
{
	LaunchResult launch(String eimPath) throws IOException;

	IStatus waitForExit(LaunchResult launchResult, IProgressMonitor monitor);
}
