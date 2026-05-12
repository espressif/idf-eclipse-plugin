/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import java.io.IOException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.EimLoader;
import com.espressif.idf.core.tools.ToolInitializer;
import com.espressif.idf.core.tools.launch.LaunchResult;
import com.espressif.idf.core.tools.watcher.EimJsonWatchService;
import com.espressif.idf.ui.tools.eim.terminal.EimCliTerminalLaunchSupport;

/**
 * Launches EIM as a GUI app when the binary supports it; otherwise runs the CLI wizard in the integrated terminal.
 *
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 */
public final class EimGuiOrCliLauncher
{
	private EimGuiOrCliLauncher()
	{
	}

	/**
	 * @param afterEimClosed invoked after EIM exits (GUI or CLI terminal), on a thread appropriate for the launch mode;
	 *                     for the CLI path the runnable is executed on the SWT UI thread after the terminal process ends
	 */
	public static void launch(ToolInitializer toolInitializer, EimLoader eimLoader, String eimPath,
			MessageConsoleStream standardConsoleStream, Display display, Runnable afterEimClosed) throws IOException
	{
		if (toolInitializer.isEimGuiCapable(eimPath))
		{
			LaunchResult launchResult = eimLoader.launchEimWithResult(eimPath);
			eimLoader.waitForEimClosure(launchResult, afterEimClosed);
			return;
		}

		EimJsonWatchService.getInstance().pauseListeners();
		display.syncExec(() -> {
			try
			{
				standardConsoleStream.write(Messages.EimCliTerminalOpeningWizard + "\n"); //$NON-NLS-1$
				EimCliTerminalLaunchSupport.launch(eimPath, () -> display.asyncExec(() -> {
					try
					{
						standardConsoleStream.write(Messages.EimCliTerminalWizardCompleted + "\n"); //$NON-NLS-1$
					}
					catch (IOException e)
					{
						Logger.log(e);
					}
					try
					{
						afterEimClosed.run();
					}
					finally
					{
						EimJsonWatchService.getInstance().unpauseListeners();
					}
				}));
			}
			catch (IOException e)
			{
				Logger.log(e);
				EimJsonWatchService.getInstance().unpauseListeners();
			}
			catch (RuntimeException e)
			{
				Logger.log(e);
				EimJsonWatchService.getInstance().unpauseListeners();
			}
		});
	}
}
