/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.eim.terminal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.runtime.Platform;
import org.eclipse.terminal.connector.ISettingsStore;
import org.eclipse.terminal.connector.ITerminalControl;
import org.eclipse.terminal.connector.process.ProcessConnector;

import com.espressif.idf.core.logging.Logger;

/**
 * Opens a login shell, sends the EIM CLI executable as the first command, and notifies
 * {@link EimCliTerminalLaunchSupport} when the process exits.
 *
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 */
public final class EimCliTerminalConnector extends ProcessConnector
{
	/**
	 * Key used to round-trip the EIM executable path through the settings store.
	 * Must match the key used in {@link EimCliTerminalLauncherDelegate#createTerminalConnector}.
	 */
	static final String SETTINGS_KEY_EIM_PATH = "eim.cli.executable"; //$NON-NLS-1$

	private String eimExecutablePath;

	@Override
	public void load(ISettingsStore store)
	{
		super.load(store);
		eimExecutablePath = store.get(SETTINGS_KEY_EIM_PATH, null);
	}

	@Override
	public void connect(ITerminalControl control)
	{
		super.connect(control);
		var process = getProcess();
		if (process == null)
		{
			Logger.log("EIM CLI terminal: no process after connect"); //$NON-NLS-1$
			EimCliTerminalLaunchSupport.invokeCompletionIfArmed();
			return;
		}

		if (eimExecutablePath != null && !eimExecutablePath.isBlank())
		{
			sendEimCommand(process.getOutputStream());
		}

		Thread waiter = new Thread(() -> {
			try
			{
				int code = process.waitFor();
				Logger.log("EIM CLI terminal process exited with code " + code); //$NON-NLS-1$
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
			finally
			{
				EimCliTerminalLaunchSupport.invokeCompletionIfArmed();
			}
		}, "EimCliTerminalWait"); //$NON-NLS-1$
		waiter.setDaemon(true);
		waiter.start();
	}

	private void sendEimCommand(OutputStream out)
	{
		String command;
		if (Platform.OS_WIN32.equals(Platform.getOS()))
		{
			String escaped = eimExecutablePath.replace("'", "''"); //$NON-NLS-1$ //$NON-NLS-2$
			command = "& '" + escaped + "' wizard; exit\r\n"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
		{
			String quoted = "'" + eimExecutablePath.replace("'", "'\\''") + "'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			command = quoted + " wizard; exit\r\n"; //$NON-NLS-1$
		}
		try
		{
			out.write(command.getBytes(StandardCharsets.UTF_8));
			out.flush();
			Logger.log("EIM CLI terminal sent command: " + command.trim()); //$NON-NLS-1$
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
	}
}
