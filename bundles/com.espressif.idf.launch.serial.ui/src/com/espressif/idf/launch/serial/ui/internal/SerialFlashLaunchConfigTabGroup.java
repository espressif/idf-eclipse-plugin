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
package com.espressif.idf.launch.serial.ui.internal;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;

import com.espressif.idf.core.logging.Logger;

public class SerialFlashLaunchConfigTabGroup extends AbstractLaunchConfigurationTabGroup
{

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode)
	{
		setTabs();
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration)
	{
		try
		{
			IResource[] resources = configuration.getMappedResources();
			if (resources != null)
			{
				super.initializeFrom(configuration);
			}
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
	}

}
