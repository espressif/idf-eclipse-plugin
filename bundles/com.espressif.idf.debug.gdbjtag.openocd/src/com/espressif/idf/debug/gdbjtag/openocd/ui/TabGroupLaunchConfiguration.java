/*******************************************************************************
 * Copyright (c) 2013 Liviu Ionescu.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Liviu Ionescu - initial version
 *******************************************************************************/

package com.espressif.idf.debug.gdbjtag.openocd.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.LaunchUtil;

public class TabGroupLaunchConfiguration extends AbstractLaunchConfigurationTabGroup
{

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode)
	{

		// Normally the tabs should be defined in the plugin.xml, and
		// here just return an empty array:
		// setTabs(new ILaunchConfigurationTab[0]);
		// But the first attempt to make this work failed, it seems
		// there is something missing in the definitions and
		// the tab extensions are filtered out.

		// To avoid these problems and for a better control,
		// we manually define the tabs here.

		TabStartup tabStartup = new TabStartup();

		ILaunchConfigurationTab tabs[] = new ILaunchConfigurationTab[] { new TabMain(), new TabDebugger(tabStartup),
				tabStartup, new SourceLookupTab(), new CommonTab(), new TabSvdTarget() };

		setTabs(tabs);

	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration)
	{
		super.performApply(configuration);
		try
		{
			IDFUtil.updateProjectBuildFolder(new LaunchUtil(DebugPlugin.getDefault().getLaunchManager())
					.getBoundConfiguration(configuration).getWorkingCopy());
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}

	}

}
