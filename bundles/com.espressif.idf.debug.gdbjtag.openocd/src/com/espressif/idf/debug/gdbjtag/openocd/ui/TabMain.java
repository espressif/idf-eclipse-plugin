/*******************************************************************************
 * Copyright (c) 2007 - 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial implementation
 *     Liviu Ionescu - ARM version
 *******************************************************************************/

package com.espressif.idf.debug.gdbjtag.openocd.ui;

import java.io.File;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.ui.CMainTab2;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.GenericJsonReader;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.debug.gdbjtag.openocd.Activator;

public class TabMain extends CMainTab2
{

	/**
	 * If the preference is set to true, check program and disable Debug button if not found. The default GDB Hardware
	 * Debug plug-in behaviour is to do not check program, to allow project-less debug sessions.
	 */
	public TabMain()
	{
		super((Activator.getInstance().getDefaultPreferences().getTabMainCheckProgram() ? 0
				: CMainTab2.DONT_CHECK_PROGRAM) | CMainTab2.INCLUDE_BUILD_SETTINGS);
	}

	/**
	 * Set the program name attributes on the working copy based on the ICElement
	 */
	@Override
	protected void initializeProgramName(ICElement cElement, ILaunchConfigurationWorkingCopy config)
	{
		boolean renamed = false;

		if (!(cElement instanceof IBinary))
		{
			cElement = cElement.getCProject();
		}

		if (cElement instanceof ICProject)
		{
			IProject project = cElement.getCProject().getProject();
			String name = project.getName();
			ICProjectDescription projDes = CCorePlugin.getDefault().getProjectDescription(project);
			if (projDes != null)
			{
				String buildConfigName = projDes.getActiveConfiguration().getName();
				name = name + " " + buildConfigName; //$NON-NLS-1$
			}
			name = getLaunchConfigurationDialog().generateName(name);
			config.rename(name);
			renamed = true;
		}

		IBinary binary = null;
		if (cElement instanceof ICProject)
		{

			// project description file
			GenericJsonReader jsonReader = new GenericJsonReader(((ICProject) cElement).getProject(),
					"/build/project_description.json"); //$NON-NLS-1$
			String value = jsonReader.getValue("app_elf"); //$NON-NLS-1$

			IBinary[] bins = getBinaryFiles((ICProject) cElement);
			if (bins != null)
			{
				for (IBinary iBinary : bins)
				{
					if (iBinary.getResource().getName().equals(value))
					{
						binary = iBinary;
						break;
					}
				}
			}
		}
		else if (cElement instanceof IBinary)
		{
			binary = (IBinary) cElement;
		}

		if (binary != null)
		{
			String path;
			path = binary.getResource().getProjectRelativePath().toOSString();
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, path);
			if (!renamed)
			{
				String name = binary.getElementName();
				int index = name.lastIndexOf('.');
				if (index > 0)
				{
					name = name.substring(0, index);
				}
				name = getLaunchConfigurationDialog().generateName(name);
				config.rename(name);
				renamed = true;
			}
		}

		if (!renamed)
		{
			String name = getLaunchConfigurationDialog().generateName(cElement.getCProject().getElementName());
			config.rename(name);
		}
	}

	protected void updateProgramFromConfig(ILaunchConfiguration config)
	{
		if (fProgText != null)
		{
			IProject project = getCProject().getProject();
			String programName = EMPTY_STRING;
			try
			{
				programName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EMPTY_STRING);
			}
			catch (CoreException ce)
			{
				Logger.log(ce);
			}

			if (StringUtil.isEmpty(programName))
			{
				// project description file
				GenericJsonReader jsonReader = new GenericJsonReader(project,
						File.separator + "build" + File.separator + "project_description.json");
				String value = jsonReader.getValue("app_elf"); //$NON-NLS-1$
				if (!StringUtil.isEmpty(value))
				{
					programName = "build" + File.separator + value; //$NON-NLS-1$
				}

			}
			fProgText.setText(programName);
		}
	}

}
