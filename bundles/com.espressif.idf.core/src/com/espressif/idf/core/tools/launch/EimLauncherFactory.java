/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.launch;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.tools.launch.strategies.EimLauncherStrategy;
import com.espressif.idf.core.tools.launch.strategies.LinuxEimLauncherStrategy;
import com.espressif.idf.core.tools.launch.strategies.MacOsEimLauncherStrategy;
import com.espressif.idf.core.tools.launch.strategies.WindowsEimLauncherStrategy;

/**
 * Factory for creating EIM launcher strategies based on OS
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 */
public final class EimLauncherFactory
{
	private EimLauncherFactory()
	{
		// utility
	}

	public static EimLauncherStrategy create(Display display, MessageConsoleStream standardConsoleStream,
			MessageConsoleStream errorConsoleStream)
	{
		String os = Platform.getOS();

		if (Platform.OS_MACOSX.equals(os))
		{
			return new MacOsEimLauncherStrategy(display, standardConsoleStream, errorConsoleStream);
		}
		if (Platform.OS_WIN32.equals(os))
		{
			return new WindowsEimLauncherStrategy(display, standardConsoleStream, errorConsoleStream);
		}
		if (Platform.OS_LINUX.equals(os))
		{
			return new LinuxEimLauncherStrategy(display, standardConsoleStream, errorConsoleStream);
		}

		throw new UnsupportedOperationException("Unsupported OS: " + os); //$NON-NLS-1$
	}
}
