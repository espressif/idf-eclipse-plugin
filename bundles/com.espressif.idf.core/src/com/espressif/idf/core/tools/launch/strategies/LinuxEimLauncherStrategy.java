/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.launch.strategies;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.MessageConsoleStream;
import com.espressif.idf.core.tools.launch.LaunchResult;
import com.espressif.idf.core.tools.launch.ProcessUtils;
import com.espressif.idf.core.tools.launch.ProcessWaiter;

import com.espressif.idf.core.logging.Logger;

/**
 * Linux EIM launcher strategy
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 */
public final class LinuxEimLauncherStrategy extends AbstractLoggingLauncherStrategy
{
	public LinuxEimLauncherStrategy(Display display, MessageConsoleStream standardConsoleStream,
			MessageConsoleStream errorConsoleStream)
	{
		super(display, standardConsoleStream, errorConsoleStream);
	}

	@Override
	public LaunchResult launch(String eimPath) throws IOException
	{
		if (!Files.exists(Paths.get(eimPath)))
			throw new IOException("EIM path not found: " + eimPath); //$NON-NLS-1$

		String quotedPath = ProcessUtils.bashSingleQuote(eimPath);
		String bashCmd = "nohup " + quotedPath + " > /dev/null 2>&1 & echo $!"; //$NON-NLS-1$ //$NON-NLS-2$

		List<String> command = List.of("bash", "-lc", bashCmd); //$NON-NLS-1$ //$NON-NLS-2$
		Process launcher = new ProcessBuilder(command).redirectErrorStream(true).start();

		String out = ProcessUtils.readAll(launcher.getInputStream());
		Long pid = ProcessUtils.parseFirstLongLine(out);

		if (pid == null)
		{
			Logger.log("Linux launcher output was:\n" + out); //$NON-NLS-1$
			throw new IOException("No PID found in launcher output. Output was:\n" + out); //$NON-NLS-1$
		}

		return LaunchResult.ofPid(pid.longValue(), eimPath, out);
	}

	@Override
	public IStatus waitForExit(LaunchResult launchResult, IProgressMonitor monitor)
	{
		return ProcessWaiter.waitForExitByPid(launchResult.pid(), monitor);
	}
}
