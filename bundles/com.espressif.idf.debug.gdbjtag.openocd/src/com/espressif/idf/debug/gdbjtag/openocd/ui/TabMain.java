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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.GenericJsonReader;
import com.espressif.idf.core.util.IDFUtil;
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
		IBinary binary = null;
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

			IPath elfFilePath = IDFUtil.getELFFilePath(project);
			IBinary[] bins = getBinaryFiles((ICProject) cElement);
			if (bins != null)
			{
				for (IBinary iBinary : bins)
				{
					if (iBinary.getResource().getName().equals(elfFilePath.toFile().getName()))
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

	@Override
	protected void updateProgramFromConfig(ILaunchConfiguration config)
	{
		if (fProgText != null)
		{
			String programName = EMPTY_STRING;
			try
			{
				programName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EMPTY_STRING);
			}
			catch (CoreException ce)
			{
				Logger.log(ce);
			}

			if (StringUtil.isEmpty(programName) && getCProject() != null)
			{
				try
				{
					IProject project = getCProject().getProject();
					// project description file
					GenericJsonReader jsonReader = new GenericJsonReader(
							IDFUtil.getBuildDir(project) + File.separator + IDFConstants.PROECT_DESCRIPTION_JSON);
					String value = jsonReader.getValue("app_elf"); //$NON-NLS-1$
					if (!StringUtil.isEmpty(value))
					{
						programName = IDFConstants.BUILD_FOLDER + File.separator + value;
					}
				}
				catch (CoreException e)
				{
					Logger.log(e);
				}

			}
			fProgText.setText(programName);
		}
	}

	@Override
	protected void createBuildConfigCombo(Composite parent, int colspan)
	{

		Composite comboComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		comboComp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colspan;
		comboComp.setLayoutData(gd);
		Link dlabel = new Link(comboComp, SWT.NONE);
		dlabel.setText(Messages.TabMain_Launch_Config);

		fBuildConfigCombo = new Combo(comboComp, SWT.READ_ONLY | SWT.DROP_DOWN);
		fBuildConfigCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fBuildConfigCombo.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				updateLaunchConfigurationDialog();
			}
		});

	}

	@Override
	protected void updateBuildOptionFromConfig(ILaunchConfiguration config)
	{
		int buildBeforeLaunchValue = ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_USE_WORKSPACE_SETTING;
		try
		{
			buildBeforeLaunchValue = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_BUILD_BEFORE_LAUNCH,
					buildBeforeLaunchValue);
			String configName = EMPTY_STRING;
			configName = config.getAttribute(IDFLaunchConstants.ATTR_LAUNCH_CONFIGURATION_NAME, configName);
			updateBuildConfigCombo(configName);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}

		if (fDisableBuildButton != null)
			fDisableBuildButton.setSelection(
					buildBeforeLaunchValue == ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_DISABLED);
		if (fEnableBuildButton != null)
			fEnableBuildButton.setSelection(
					buildBeforeLaunchValue == ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_ENABLED);
		if (fWorkspaceSettingsButton != null)
			fWorkspaceSettingsButton.setSelection(
					buildBeforeLaunchValue == ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_USE_WORKSPACE_SETTING);
	}

	@Override
	protected void updateBuildConfigCombo(String selectedConfigName)
	{
		fBuildConfigCombo.removeAll();
		int offset = 0;
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		try
		{
			ILaunchConfiguration[] configs = launchManager.getLaunchConfigurations(DebugPlugin.getDefault()
					.getLaunchManager().getLaunchConfigurationType(IDFLaunchConstants.RUN_LAUNCH_CONFIG_TYPE));
			for (ILaunchConfiguration config : configs)
			{
				IResource[] mappedResource = config.getMappedResources();
				if (mappedResource != null
						&& mappedResource[0].getProject().getName().contentEquals(fProjText.getText()))
				{
					fBuildConfigCombo.add(config.getName());
					if (config.getName().contentEquals(selectedConfigName))
					{
						fBuildConfigCombo.select(offset);
					}
					offset += 1;
				}
			}
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		if (fBuildConfigCombo.getText().isBlank())
		{
			fBuildConfigCombo.select(0);
		}

	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config)
	{
		config.setAttribute(IDFLaunchConstants.ATTR_LAUNCH_CONFIGURATION_NAME, fBuildConfigCombo.getText());
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, AUTO_CONFIG);
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_AUTO, true);
		if (fDisableBuildButton != null)
		{
			int buildBeforeLaunchValue = ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_USE_WORKSPACE_SETTING;
			if (fDisableBuildButton.getSelection())
			{
				buildBeforeLaunchValue = ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_DISABLED;
			}
			else if (fEnableBuildButton.getSelection())
			{
				buildBeforeLaunchValue = ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_ENABLED;
			}
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_BUILD_BEFORE_LAUNCH, buildBeforeLaunchValue);
		}

		ICProject cProject = this.getCProject();
		if (cProject != null && cProject.exists())
		{
			config.setMappedResources(new IResource[] { cProject.getProject() });
		}
		else
		{
			config.setMappedResources(null);
		}

		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText());
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, fProgText.getText());
		if (fCoreText != null)
		{
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, fCoreText.getText());
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_POST_MORTEM_TYPE, getSelectedCoreType());
		}
	}

}
