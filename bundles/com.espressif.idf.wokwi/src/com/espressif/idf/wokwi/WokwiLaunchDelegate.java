/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.wokwi;

import org.eclipse.cdt.debug.core.launch.CoreBuildLaunchConfigDelegate;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

import com.espressif.idf.core.logging.Logger;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class WokwiLaunchDelegate extends CoreBuildLaunchConfigDelegate
{

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException
	{

		WokwiSimulatorHandler wokWiSimulatorHandler = new WokwiSimulatorHandler();
		try
		{
			IProject project = getProject(configuration);
			wokWiSimulatorHandler.execute(configuration, launch, project);
		}
		catch (ExecutionException e)
		{
			Logger.log(e);
		}

	}

}
