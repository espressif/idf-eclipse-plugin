/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * copied from:
 * org.eclipse.ui.externaltools.internal.launchConfigurations.GenericMainTab
 *******************************************************************************/

package com.espressif.idf.launch.serial.ui.internal;

import java.io.File;
import java.util.Map;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.ui.corebuild.GenericMainTab;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.json.simple.JSONArray;

import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.EspConfigParser;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.launch.serial.SerialFlashLaunchTargetProvider;
import com.espressif.idf.launch.serial.internal.SerialFlashLaunchConfigDelegate;
import com.espressif.idf.launch.serial.util.ESPFlashUtil;

public class CMakeMainTab2 extends GenericMainTab {
	private static final String EMPTY_CONFIG_OPTIONS = "-s ${openocd_path}/share/openocd/scripts"; //$NON-NLS-1$
	private Button flashOverJtagButton;
	private Combo fFlashVoltage;
	private Combo fTarget;
	private Map<String, JSONArray> boardConfigsMap;
	private Combo fTargetName;
	private boolean isFlashOverJtag;
	private String defaultArguments;
	private String argumentsForJtagFlash;
	private boolean isJtagFlashAvailable;
	private GridData openOcdGroupData;
	private GridData locationAndWorkDirGroupData;

	@Override
	public void createControl(Composite parent) {
		isJtagFlashAvailable = ESPFlashUtil.checkIfJtagIsAvailable();
		createJtagFlashButton(parent);
		super.createControl(parent);
		locationAndWorkDirGroupData = new GridData(SWT.FILL, SWT.NONE, true, false);
		locationField.getParent().setLayoutData(locationAndWorkDirGroupData);
		workDirectoryField.getParent().setLayoutData(locationAndWorkDirGroupData);
	}

	private void createJtagFlashButton(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		c.setLayout(layout);

		flashOverJtagButton = new Button(c, SWT.CHECK);
		flashOverJtagButton.setText(Messages.CMakeMainTab2_JtagComboLbl);
		flashOverJtagButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				isFlashOverJtag = !isFlashOverJtag;
				if (!isFlashOverJtag) {
					argumentField.setText(defaultArguments);
				} else {
					argumentField.setText(argumentsForJtagFlash);
				}
				switchUI();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		if (!isJtagFlashAvailable) {
			Label lbl = new Label(c, SWT.NONE);
			lbl.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
			lbl.setText(Messages.CMakeMainTab2_JtagFlashingNotSupportedMsg);
			flashOverJtagButton.setEnabled(false);
		}
	}

	private void switchUI() {
		if (!isJtagFlashAvailable) {
			return;
		}
		locationField.getParent().setVisible(!isFlashOverJtag);
		workDirectoryField.getParent().setVisible(!isFlashOverJtag);
		fFlashVoltage.getParent().setVisible(isFlashOverJtag);
		openOcdGroupData.exclude = !isFlashOverJtag;
		locationAndWorkDirGroupData.exclude = isFlashOverJtag;
		this.getShell().layout(true, true);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);
		if (!isJtagFlashAvailable) {
			return;
		}
		try {
			ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
			wc.setAttribute(IDFLaunchConstants.FLASH_OVER_JTAG, flashOverJtagButton.getSelection());
			wc.doSave();
		} catch (CoreException e) {
			Logger.log(e);
		}
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		updateFlashOverJtagStatus(configuration);
		updateArgumentsWithDefaultFlashCommand(configuration);
		switchUI();
	}

	private void updateFlashOverJtagStatus(ILaunchConfiguration configuration) {
		if (!isJtagFlashAvailable) {
			return;
		}
		try {
			isFlashOverJtag = configuration.getAttribute(IDFLaunchConstants.FLASH_OVER_JTAG, isFlashOverJtag);
		} catch (CoreException e) {
			Logger.log(e);
		}
		flashOverJtagButton.setSelection(isFlashOverJtag);
	}

	private void updateArgumentsWithDefaultFlashCommand(ILaunchConfiguration configuration) {
		String espFlashCommand = ESPFlashUtil.getEspFlashCommand(getSerialPort());
		try {
			String undefinedArguments = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_TOOL_ARGUMENTS,
					espFlashCommand);
			if (undefinedArguments.contains(EMPTY_CONFIG_OPTIONS)) {
				defaultArguments = espFlashCommand;
				argumentsForJtagFlash = undefinedArguments;
			} else {
				defaultArguments = undefinedArguments;
				argumentsForJtagFlash = EMPTY_CONFIG_OPTIONS;
			}
			argumentField.setText(undefinedArguments);
		} catch (CoreException e) {
			Logger.log(e);
		}

	}

	@Override
	protected void createArgumentComponent(Composite parent) {
		if (isJtagFlashAvailable) {
			String selectedTarget = getLaunchTarget();
			EspConfigParser parser = new EspConfigParser();
			createOpenOcdSetupComponent(parent, selectedTarget, parser);
		}
		super.createArgumentComponent(parent);
	}

	private void createOpenOcdSetupComponent(Composite parent, String selectedTarget, EspConfigParser parser) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 6;
		group.setText(Messages.CMakeMainTab2_OpeonOcdSetupGroupTitle);
		group.setLayout(gridLayout);
		{
			Label label = new Label(group, SWT.NONE);
			label.setText(Messages.flashVoltageLabel);
			label.setToolTipText(Messages.flashVoltageToolTip);
			fFlashVoltage = new Combo(group, SWT.SINGLE | SWT.BORDER);
			fFlashVoltage.setItems(parser.getEspFlashVoltages().toArray(new String[0]));
			fFlashVoltage.setText("default"); //$NON-NLS-1$
			fFlashVoltage.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					fTargetName.notifyListeners(SWT.Selection, null);
				}
			});
		}
		{
			Label label = new Label(group, SWT.NONE);
			label.setText(Messages.configTargetLabel);
			label.setToolTipText(Messages.configTargetToolTip);
			fTarget = new Combo(group, SWT.SINGLE | SWT.BORDER);
			fTarget.setItems(parser.getTargets().toArray(new String[0]));
			fTarget.setText(selectedTarget);
			fTarget.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String selectedItem = fTarget.getItem(fTarget.getSelectionIndex());
					boardConfigsMap = parser.getBoardsConfigs(selectedItem);
					fTargetName.setItems(parser.getBoardsConfigs(selectedItem).keySet().toArray(new String[0]));
				}
			});
		}
		{
			Label label = new Label(group, SWT.NONE);
			label.setText(Messages.configBoardLabel);
			label.setToolTipText(Messages.configBoardTooTip);
			fTargetName = new Combo(group, SWT.SINGLE | SWT.BORDER);
			fTargetName.setItems(parser.getBoardsConfigs(selectedTarget).keySet().toArray(new String[0]));
			boardConfigsMap = parser.getBoardsConfigs(selectedTarget);

			fTargetName.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateArgumentsField();
				}
			});
		}
		openOcdGroupData = new GridData(SWT.FILL, SWT.NONE, true, true);
		group.setLayoutData(openOcdGroupData);
	}

	@SuppressWarnings("unchecked")
	private void updateArgumentsField() {
		String selectedVoltage = fFlashVoltage.getText();
		String selectedItem = fTargetName.getText();
		String configOptiopns = EMPTY_CONFIG_OPTIONS;
		if (!selectedVoltage.equals("default"))//$NON-NLS-1$
		{
			configOptiopns = configOptiopns + " -c 'set ESP32_FLASH_VOLTAGE " + selectedVoltage + "'"; //$NON-NLS-1$//$NON-NLS-2$
		}
		if (!selectedItem.isEmpty()) {
			for (String config : (String[]) boardConfigsMap.get(selectedItem).toArray(new String[0])) {
				configOptiopns = configOptiopns + " -f " + config; //$NON-NLS-1$
			}
		}
		argumentsForJtagFlash = configOptiopns;
		argumentField.setText(argumentsForJtagFlash);
	}

	private String getLaunchTarget() {
		ILaunchBarManager launchBarManager = Activator.getService(ILaunchBarManager.class);
		String selectedTarget = ""; //$NON-NLS-1$
		try {
			selectedTarget = launchBarManager.getActiveLaunchTarget().getAttribute(IDFLaunchConstants.ATTR_IDF_TARGET,
					""); //$NON-NLS-1$
		} catch (CoreException e) {
			Logger.log(e);
		}
		return selectedTarget;
	}

	private String getSerialPort() {
		ILaunchBarManager launchBarManager = Activator.getService(ILaunchBarManager.class);
		String serialPort = ""; //$NON-NLS-1$
		try {
			serialPort = launchBarManager.getActiveLaunchTarget()
					.getAttribute(SerialFlashLaunchTargetProvider.ATTR_SERIAL_PORT, ""); //$NON-NLS-1$

		} catch (CoreException e) {
			Logger.log(e);
		}
		return serialPort;
	}

	@Override
	protected void updateLocation(ILaunchConfiguration configuration) {
		super.updateLocation(configuration);
		locationField.removeModifyListener(fListener);
		String location = IDFUtil.getIDFPythonEnvPath();
		if (StringUtil.isEmpty(location)) {
			try {
				location = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_LOCATION,
						SerialFlashLaunchConfigDelegate.getSystemPythonPath());
			} catch (CoreException e) {
				Logger.log(e);
			}
		}
		locationField.setText(location);
	}

	@Override
	protected void updateWorkingDirectory(ILaunchConfiguration configuration) {
		super.updateWorkingDirectory(configuration);
		File workingDir;
		if (workDirectoryField.getText().isEmpty()) {
			try {
				if (configuration.getMappedResources() == null) {
					return;
				}
				workingDir = new File(configuration.getMappedResources()[0].getProject().getLocationURI());
				workDirectoryField.setText(newVariableExpression("workspace_loc", workingDir.getName())); //$NON-NLS-1$
			} catch (CoreException e) {
				Logger.log(e);
			}
		}
	}
}
