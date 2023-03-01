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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.cdt.cmake.core.internal.CMakeBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.ICBuildConfigurationProvider;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.debug.core.launch.CoreBuildLaunchConfigDelegate;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.launch.ui.corebuild.CommonBuildTab;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.build.IDFBuildConfigurationProvider;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.RecheckConfigsHelper;
import com.espressif.idf.core.util.StringUtil;

@SuppressWarnings("restriction")
public class CMakeBuildTab2 extends CommonBuildTab
{

	private static final String UNIX_MAKEFILES = "Unix Makefiles"; //$NON-NLS-1$
	private static final String NINJA = "Ninja"; //$NON-NLS-1$
	private Button unixGenButton;
	private Button ninjaGenButton;
	private Text cmakeArgsText;
	private Text buildCommandText;
	private Text cleanCommandText;

	private static IToolChainManager tcManager = LaunchUIPlugin.getService(IToolChainManager.class);
	private static ICBuildConfigurationManager bcManager = LaunchUIPlugin.getService(ICBuildConfigurationManager.class);

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
		label.setText(Messages.CMakeBuildTab2_AdditionalCMakeArgs);

		cmakeArgsText = new Text(cmakeGroup, SWT.BORDER);
		cmakeArgsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		cmakeArgsText.addModifyListener(e -> updateLaunchConfigurationDialog());

		label = new Label(cmakeGroup, SWT.NONE);
		label.setText(Messages.CMakeBuildTab2_BuildCmd);

		buildCommandText = new Text(cmakeGroup, SWT.BORDER);
		buildCommandText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		buildCommandText.addModifyListener(e -> updateLaunchConfigurationDialog());

		label = new Label(cmakeGroup, SWT.NONE);
		label.setText(Messages.CMakeBuildTab2_CleanCmd);

		cleanCommandText = new Text(cmakeGroup, SWT.BORDER);
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
			String generator = configuration.getAttribute(CMakeBuildConfiguration.CMAKE_GENERATOR, StringUtil.EMPTY);
			updateGeneratorButtons(generator);

			String cmakeArgs = configuration.getAttribute(CMakeBuildConfiguration.CMAKE_ARGUMENTS, StringUtil.EMPTY);
			cmakeArgsText.setText(cmakeArgs);

			String buildCommand = configuration.getAttribute(CMakeBuildConfiguration.BUILD_COMMAND, StringUtil.EMPTY);
			buildCommandText.setText(buildCommand);

			String cleanCommand = configuration.getAttribute(CMakeBuildConfiguration.CLEAN_COMMAND, StringUtil.EMPTY);
			cleanCommandText.setText(cleanCommand);

		}
		catch (CoreException e)
		{
			Logger.log(e);
		}


	}

	private void createBuildConfiguration(ILaunchConfiguration configuration)
			throws CoreException
	{
		IProject project = CoreBuildLaunchConfigDelegate.getProject(configuration);
		IDFBuildConfigurationProvider provider = new IDFBuildConfigurationProvider();
		provider.setNameBasedOnLaunchConfiguration(configuration);
		provider.createBuildConfiguration(project,
				getBuildConfiguration() == null ? getDefaultMatchingToolChain()
						: getBuildConfiguration().getToolChain(),
				IDFCorePlugin.getService(ILaunchBarManager.class).getActiveLaunchMode().getIdentifier(), null);
	}

	private IToolChain getDefaultMatchingToolChain()
	{
		ICBuildConfigurationProvider bcProvider = bcManager.getProvider(getBuildConfigProviderId());
		Collection<IToolChain> toolchainsCollection = Collections.emptyList();
		try
		{
			toolchainsCollection = bcProvider
					.getSupportedToolchains(tcManager.getToolChainsMatching(getLaunchTarget().getAttributes()));
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}

		return toolchainsCollection.stream().findFirst().orElseThrow();
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

		try
		{
			IProject project = CoreBuildLaunchConfigDelegate.getProject(configuration);
			RecheckConfigsHelper.revalidateToolchain(project);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}

		configuration.setAttribute(CMakeBuildConfiguration.CMAKE_GENERATOR,
				ninjaGenButton.getSelection() ? NINJA : UNIX_MAKEFILES);

		String cmakeArgs = cmakeArgsText.getText().trim();
		if (!cmakeArgs.isEmpty())
		{
			configuration.setAttribute(CMakeBuildConfiguration.CMAKE_ARGUMENTS, cmakeArgs);
		}
		else
		{
			configuration.removeAttribute(CMakeBuildConfiguration.CMAKE_ARGUMENTS);
		}

		String buildCommand = buildCommandText.getText().trim();
		if (!buildCommand.isEmpty())
		{
			configuration.setAttribute(CMakeBuildConfiguration.BUILD_COMMAND, buildCommand);
		}
		else
		{
			configuration.removeAttribute(CMakeBuildConfiguration.BUILD_COMMAND);
		}

		String cleanCommand = cleanCommandText.getText().trim();
		if (!cleanCommand.isEmpty())
		{
			configuration.setAttribute(CMakeBuildConfiguration.CLEAN_COMMAND, cleanCommand);
		}
		else
		{
			configuration.removeAttribute(CMakeBuildConfiguration.CLEAN_COMMAND);
		}

		try
		{
			createBuildConfiguration(configuration);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
	}

	@Override
	protected void saveProperties(Map<String, String> properties)
	{
		super.saveProperties(properties);
		properties.put(CMakeBuildConfiguration.CMAKE_GENERATOR, ninjaGenButton.getSelection() ? NINJA : UNIX_MAKEFILES);

		properties.put(CMakeBuildConfiguration.CMAKE_ARGUMENTS, cmakeArgsText.getText().trim());
		properties.put(CMakeBuildConfiguration.BUILD_COMMAND, buildCommandText.getText().trim());
		properties.put(CMakeBuildConfiguration.CLEAN_COMMAND, cleanCommandText.getText().trim());
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

		String cmakeArgs = properties.get(CMakeBuildConfiguration.CMAKE_ARGUMENTS);
		if (cmakeArgs != null)
		{
			cmakeArgsText.setText(cmakeArgs);
		}
		else
		{
			cmakeArgsText.setText(StringUtil.EMPTY);
		}

		String buildCmd = properties.get(CMakeBuildConfiguration.BUILD_COMMAND);
		if (buildCmd != null)
		{
			buildCommandText.setText(buildCmd);
		}
		else
		{
			buildCommandText.setText(StringUtil.EMPTY);
		}

		String cleanCmd = properties.get(CMakeBuildConfiguration.CLEAN_COMMAND);
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
		ILaunchBarManager barManager = IDFCorePlugin.getService(ILaunchBarManager.class);
		try
		{
			return barManager.getActiveLaunchTarget();
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return super.getLaunchTarget();
	}

	@Override
	public String getName()
	{
		return "CMake"; //$NON-NLS-1$
	}

}
