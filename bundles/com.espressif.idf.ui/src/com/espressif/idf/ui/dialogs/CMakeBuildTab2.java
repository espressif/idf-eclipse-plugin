/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package com.espressif.idf.ui.dialogs;

import java.util.Map;

import org.eclipse.cdt.cmake.core.CMakeBuildConfiguration;
import org.eclipse.cdt.cmake.core.ICMakeBuildConfiguration;
import org.eclipse.cdt.debug.core.launch.CoreBuildLaunchConfigDelegate;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.ui.corebuild.CommonBuildTab;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.build.IDFBuildConfigurationProvider;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.LaunchTargetHelper;
import com.espressif.idf.core.util.RecheckConfigsHelper;
import com.espressif.idf.core.util.StringUtil;

@SuppressWarnings("restriction")
public class CMakeBuildTab2 extends CommonBuildTab
{
	private static final String LOCAL_CMAKE_ARGUMENTS = "local_cmake_arguments"; //$NON-NLS-1$
	private static final String UNIX_MAKEFILES = "Unix Makefiles"; //$NON-NLS-1$
	private static final String NINJA = "Ninja"; //$NON-NLS-1$
	private static final String DEFAULT_CMAKE_MSG = ""; //$NON-NLS-1$
	private static final String DEFAULT_BUILD_MSG = "cmake --build ."; //$NON-NLS-1$
	private static final String DEFAULT_CLEAN_MSG = "ninja clean"; //$NON-NLS-1$
	private Button unixGenButton;
	private Button ninjaGenButton;
	private Text cmakeArgsText;
	private Text buildCommandText;
	private Text cleanCommandText;
	private Text buildFolderText;

	@Override
	protected String getBuildConfigProviderId()
	{
		return IDFBuildConfigurationProvider.ID;
	}

	@Override
	public void createControl(Composite parent)
	{
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		setControl(comp);

		Control tcControl = createToolchainSelector(comp);
		tcControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Group cmakeGroup = new Group(comp, SWT.NONE);
		cmakeGroup.setText(Messages.CMakeBuildTab2_CMakeSettings);
		cmakeGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		cmakeGroup.setLayout(new GridLayout());

		Label label = new Label(cmakeGroup, SWT.NONE);
		label.setText(Messages.CMakeBuildTab2_Generator);

		Composite genComp = new Composite(cmakeGroup, SWT.BORDER);
		genComp.setLayout(new GridLayout(2, true));

		unixGenButton = new Button(genComp, SWT.RADIO);
		unixGenButton.setText(Messages.CMakeBuildTab2_UnixMakeFiles);
		unixGenButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				updateLaunchConfigurationDialog();
			}
		});

		ninjaGenButton = new Button(genComp, SWT.RADIO);
		ninjaGenButton.setText(Messages.CMakeBuildTab2_Ninja);
		ninjaGenButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				updateLaunchConfigurationDialog();
			}
		});

		label = new Label(cmakeGroup, SWT.NONE);
		label.setText(Messages.CMakeBuildTab2_BuildFolderTextLbl);

		// Create a composite to hold the text field and button
		Composite buildFolderComp = new Composite(cmakeGroup, SWT.NONE);
		buildFolderComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		buildFolderComp.setLayout(new GridLayout(2, false)); // Two columns: Text field & Button

		// Text field for displaying the build folder path
		buildFolderText = new Text(buildFolderComp, SWT.BORDER);
		buildFolderText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		buildFolderText.setMessage(Messages.CMakeBuildTab2_BuildFolderTextMsg);
		buildFolderText.setToolTipText(Messages.CMakeBuildTab2_BuildFolderTextToolTip);

		// Browse button to select a folder
		Button browseButton = createPushButton(buildFolderComp, LaunchMessages.Launch_common_Browse_1, null); // $NON-NLS-1$
		browseButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				DirectoryDialog dialog = new DirectoryDialog(parent.getShell());
				dialog.setMessage(Messages.CMakeBuildTab2_SelectBuildFolderMsg);
				String selectedDir = dialog.open();
				if (selectedDir != null)
				{
					buildFolderText.setText(selectedDir);
				}
			}
		});

		label = new Label(cmakeGroup, SWT.NONE);
		label.setText(Messages.CMakeBuildTab2_AdditionalCMakeArgs);

		cmakeArgsText = new Text(cmakeGroup, SWT.BORDER);
		cmakeArgsText.setMessage(DEFAULT_CMAKE_MSG);
		cmakeArgsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		cmakeArgsText.addModifyListener(e -> updateLaunchConfigurationDialog());

		label = new Label(cmakeGroup, SWT.NONE);
		label.setText(Messages.CMakeBuildTab2_BuildCmd);

		buildCommandText = new Text(cmakeGroup, SWT.BORDER);
		buildCommandText.setMessage(DEFAULT_BUILD_MSG);
		buildCommandText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		buildCommandText.addModifyListener(e -> updateLaunchConfigurationDialog());

		label = new Label(cmakeGroup, SWT.NONE);
		label.setText(Messages.CMakeBuildTab2_CleanCmd);

		cleanCommandText = new Text(cmakeGroup, SWT.BORDER);
		cleanCommandText.setMessage(DEFAULT_CLEAN_MSG);
		cleanCommandText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		cleanCommandText.addModifyListener(e -> updateLaunchConfigurationDialog());
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
	{
		try
		{
			IProject project = CoreBuildLaunchConfigDelegate.getProject(configuration);
			RecheckConfigsHelper.revalidateToolchain(project);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}

		configuration.setAttribute(CMakeBuildConfiguration.CMAKE_GENERATOR, NINJA);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration)
	{

		super.initializeFrom(configuration);
		try
		{
			IProject project = CoreBuildLaunchConfigDelegate.getProject(configuration);
			RecheckConfigsHelper.revalidateToolchain(project);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		try
		{
			String buildFolderPath = configuration.getAttribute(IDFLaunchConstants.BUILD_FOLDER_PATH, StringUtil.EMPTY);
			buildFolderText.setText(buildFolderPath);

			String generator = configuration.getAttribute(CMakeBuildConfiguration.CMAKE_GENERATOR, StringUtil.EMPTY);
			updateGeneratorButtons(generator);

			String cmakeArgs = configuration.getAttribute(LOCAL_CMAKE_ARGUMENTS,
					configuration.getAttribute(CMakeBuildConfiguration.CMAKE_ARGUMENTS, StringUtil.EMPTY));
			cmakeArgsText.setText(cmakeArgs);

			String buildCommand = configuration.getAttribute(CMakeBuildConfiguration.CMAKE_BUILD_COMMAND,
					StringUtil.EMPTY);
			buildCommandText.setText(buildCommand);

			String cleanCommand = configuration.getAttribute(CMakeBuildConfiguration.CMAKE_CLEAN_TARGET,
					StringUtil.EMPTY);
			cleanCommandText.setText(cleanCommand);

		}
		catch (CoreException e)
		{
			Logger.log(e);
		}

	}

	private void updateGeneratorButtons(String generator)
	{
		if (generator == null || generator.equals(NINJA) || generator.isBlank())
		{
			ninjaGenButton.setSelection(true);
		}
		else
		{
			unixGenButton.setSelection(true);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration)
	{
		super.performApply(configuration);
		IProject project = null;
		try
		{
			project = CoreBuildLaunchConfigDelegate.getProject(configuration);
			RecheckConfigsHelper.revalidateToolchain(project);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}

		configuration.setAttribute(IDFLaunchConstants.BUILD_FOLDER_PATH, buildFolderText.getText());

		configuration.setAttribute(ICMakeBuildConfiguration.CMAKE_GENERATOR,
				ninjaGenButton.getSelection() ? NINJA : UNIX_MAKEFILES);

		String cmakeArgs = cmakeArgsText.getText().trim();
		if (!cmakeArgs.isEmpty())
		{
			configuration.setAttribute(LOCAL_CMAKE_ARGUMENTS, cmakeArgs);
			if (project != null)
				configuration.setAttribute(CMakeBuildConfiguration.CMAKE_ARGUMENTS, cmakeArgs);
		}
		else
		{
			configuration.removeAttribute(LOCAL_CMAKE_ARGUMENTS);
			configuration.removeAttribute(ICMakeBuildConfiguration.CMAKE_ARGUMENTS);
		}

		String buildCommand = buildCommandText.getText().trim();
		if (!buildCommand.isEmpty())
		{
			configuration.setAttribute(CMakeBuildConfiguration.CMAKE_BUILD_COMMAND, buildCommand);
		}
		else
		{
			configuration.removeAttribute(CMakeBuildConfiguration.CMAKE_BUILD_COMMAND);
		}

		String cleanCommand = cleanCommandText.getText().trim();
		if (!cleanCommand.isEmpty())
		{
			configuration.setAttribute(CMakeBuildConfiguration.CMAKE_CLEAN_TARGET, cleanCommand);
		}
		else
		{
			configuration.removeAttribute(CMakeBuildConfiguration.CMAKE_CLEAN_TARGET);
		}

	}

	@Override
	protected void saveProperties(Map<String, String> properties)
	{
		super.saveProperties(properties);
		properties.put(CMakeBuildConfiguration.CMAKE_GENERATOR, ninjaGenButton.getSelection() ? NINJA : UNIX_MAKEFILES);

		properties.put(IDFLaunchConstants.BUILD_FOLDER_PATH, buildFolderText.getText().trim());
		properties.put(LOCAL_CMAKE_ARGUMENTS, cmakeArgsText.getText().trim());
		properties.put(CMakeBuildConfiguration.CMAKE_BUILD_COMMAND, buildCommandText.getText().trim());
		properties.put(CMakeBuildConfiguration.CMAKE_CLEAN_TARGET, cleanCommandText.getText().trim());
	}

	@Override
	protected void restoreProperties(Map<String, String> properties)
	{
		super.restoreProperties(properties);

		String gen = properties.get(CMakeBuildConfiguration.CMAKE_GENERATOR);
		if (gen != null)
		{
			switch (gen)
			{
			case UNIX_MAKEFILES:
				ninjaGenButton.setSelection(false);
				unixGenButton.setSelection(true);
				break;
			default:
				ninjaGenButton.setSelection(true);
				unixGenButton.setSelection(false);
				break;
			}
		}

		String buildFolderPath = properties.get(IDFLaunchConstants.BUILD_FOLDER_PATH);
		if (buildFolderPath != null)
		{
			buildFolderText.setText(buildFolderPath);
		}
		String cmakeArgs = properties.get(LOCAL_CMAKE_ARGUMENTS);
		if (cmakeArgs != null)
		{
			cmakeArgsText.setText(cmakeArgs);
		}
		else
		{
			cmakeArgsText.setText(StringUtil.EMPTY);
		}

		String buildCmd = properties.get(CMakeBuildConfiguration.CMAKE_BUILD_COMMAND);
		if (buildCmd != null)
		{
			buildCommandText.setText(buildCmd);
		}
		else
		{
			buildCommandText.setText(StringUtil.EMPTY);
		}

		String cleanCmd = properties.get(CMakeBuildConfiguration.CMAKE_CLEAN_TARGET);
		if (cleanCmd != null)
		{
			cleanCommandText.setText(cleanCmd);
		}
		else
		{
			cleanCommandText.setText(StringUtil.EMPTY);
		}
	}

	@Override
	public ILaunchTarget getLaunchTarget()
	{
		ILaunchTarget defaultTarget = super.getLaunchTarget();
		ILaunchTargetManager launchTargetManager = IDFCorePlugin.getService(ILaunchTargetManager.class);

		String targetName = LaunchTargetHelper.getLastTargetName()
				.orElseGet(() -> defaultTarget != null ? defaultTarget.getId() : StringUtil.EMPTY);

		if (!targetName.isEmpty())
		{
			ILaunchTarget selectedTarget = LaunchTargetHelper.findLaunchTargetByName(launchTargetManager, targetName);
			if (selectedTarget != null)
			{
				return selectedTarget;
			}
		}

		try
		{
			ILaunchTarget activeILaunchTarget = IDFCorePlugin.getService(ILaunchBarManager.class)
					.getActiveLaunchTarget();
			return (activeILaunchTarget != null) ? activeILaunchTarget : defaultTarget;
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}

		return defaultTarget;
	}

	@Override
	public String getName()
	{
		return "CMake"; //$NON-NLS-1$
	}

}
