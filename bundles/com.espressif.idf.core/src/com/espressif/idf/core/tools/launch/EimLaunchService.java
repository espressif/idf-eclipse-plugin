/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.launch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.tools.launch.strategies.EimLauncherStrategy;

/**
 * Service for launching EIM using appropriate strategy
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public final class EimLaunchService
{
	private final EimLauncherStrategy strategy;

	public EimLaunchService(Display display, MessageConsoleStream standardConsoleStream,
			MessageConsoleStream errorConsoleStream)
	{
		this.strategy = EimLauncherFactory.create(display, standardConsoleStream, errorConsoleStream);
	}

	public LaunchResult launch(String eimPath) throws IOException
	{
		if (eimPath == null || eimPath.isBlank())
			throw new IOException("EIM path is null/blank"); //$NON-NLS-1$

		if (!Files.exists(Paths.get(eimPath)))
			throw new IOException("EIM path not found: " + eimPath); //$NON-NLS-1$

		return strategy.launch(eimPath);
	}

	public IStatus waitForExit(LaunchResult launchResult, IProgressMonitor monitor)
	{
		return strategy.waitForExit(launchResult, monitor);
	}
}
