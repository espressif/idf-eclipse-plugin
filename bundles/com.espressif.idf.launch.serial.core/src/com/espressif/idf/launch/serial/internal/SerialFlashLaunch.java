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
package com.espressif.idf.launch.serial.internal;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.launch.TargetedLaunch;

import com.espressif.idf.launch.serial.SerialFlashLaunchTargetProvider;
import com.espressif.idf.terminal.connector.serial.connector.SerialPortHandler;

public class SerialFlashLaunch extends TargetedLaunch
{

	private SerialPortHandler serialPort;
	private boolean wasOpen;

	public SerialFlashLaunch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator,
			ILaunchTarget target)
	{
		super(launchConfiguration, mode, target, locator);
		if (target != null)
		{
			String serialPortName = target.getAttribute(SerialFlashLaunchTargetProvider.ATTR_SERIAL_PORT, ""); //$NON-NLS-1$
			serialPort = !serialPortName.isEmpty() ? SerialPortHandler.get(serialPortName) : null;
		}
		DebugPlugin.getDefault().addDebugEventListener(this);

	}

	public void start()
	{
		if (serialPort != null)
		{
			wasOpen = serialPort.isOpen();
			if (wasOpen)
			{
				try
				{
					serialPort.pause();
				}
				catch (IOException e)
				{
					Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.SerialFlashLaunch_Pause, e));
				}
			}
		}
		else
		{
			wasOpen = false;
		}
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events)
	{
		super.handleDebugEvents(events);
		if (isTerminated() && wasOpen)
		{
			try
			{
				serialPort.resume();
			}
			catch (IOException e)
			{
				Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.SerialFlashLaunch_Resume, e));
			}
			wasOpen = false;
		}
	}

}
