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
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
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
	private String argumentsForSerialFlash;
	private String argumentsForJtagFlash;
	private boolean isJtagFlashAvailable;
	private GridData openOcdGroupData;
	private GridData locationAndWorkDirGroupData;
	private ILaunchBarManager launchBarManager;

	@Override
	public void createControl(Composite parent) {
		launchBarManager = Activator.getService(ILaunchBarManager.class);
		isJtagFlashAvailable = ESPFlashUtil.checkIfJtagIsAvailable();

		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setFont(parent.getFont());
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);

		createJtagFlashButton(mainComposite);
		createLocationComponent(mainComposite);
		createWorkDirectoryComponent(mainComposite);
		createArgumentComponent(mainComposite);
		createVerticalSpacer(mainComposite, 1);

		Dialog.applyDialogFont(parent);
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
					argumentField.setText(argumentsForSerialFlash);
				} else {
					argumentField.setText(argumentsForJtagFlash);
					fTarget.notifyListeners(SWT.Selection, null);
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
			wc.setAttribute(IDFLaunchConstants.JTAG_FLASH_VOLTAGE, fFlashVoltage.getText());
			wc.setAttribute(IDFLaunchConstants.TARGET_FOR_JTAG, fTarget.getText());
			wc.setAttribute(IDFLaunchConstants.JTAG_BOARD, fTargetName.getText());
			wc.setAttribute(IDFLaunchConstants.FLASH_OVER_JTAG, flashOverJtagButton.getSelection());
			//For the case, when user wants to edit arguments line somehow and save changes
			if (isFlashOverJtag) {
				argumentsForJtagFlash = argumentField.getText();
				wc.setAttribute(IDFLaunchConstants.ATTR_JTAG_FLASH_ARGUMENTS, argumentsForJtagFlash);
				wc.setAttribute(IDFLaunchConstants.ATTR_SERIAL_FLASH_ARGUMENTS, argumentsForSerialFlash);
			} else {
				wc.setAttribute(IDFLaunchConstants.ATTR_SERIAL_FLASH_ARGUMENTS, argumentField.getText());
				wc.setAttribute(IDFLaunchConstants.ATTR_JTAG_FLASH_ARGUMENTS, argumentsForJtagFlash);
			}
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
			initializeJtagComboFields(configuration);
		} catch (CoreException e) {
			Logger.log(e);
		}
		flashOverJtagButton.setSelection(isFlashOverJtag);
	}

	private void updateArgumentsWithDefaultFlashCommand(ILaunchConfiguration configuration) {
		String espFlashCommand = ESPFlashUtil.getEspFlashCommand(getSerialPort());
		try {
			argumentsForSerialFlash = configuration.getAttribute(IDFLaunchConstants.ATTR_SERIAL_FLASH_ARGUMENTS,
					espFlashCommand);
			argumentsForSerialFlash = argumentsForSerialFlash.isEmpty() ? espFlashCommand : argumentsForSerialFlash;
			argumentsForSerialFlash = argumentsForSerialFlash.contains(EMPTY_CONFIG_OPTIONS) ? espFlashCommand
					: argumentsForSerialFlash;
			argumentField.setText(argumentsForSerialFlash);
			if (!isJtagFlashAvailable) {
				return;
			}
			String savedArgumentsForJtagFlash = configuration.getAttribute(IDFLaunchConstants.ATTR_JTAG_FLASH_ARGUMENTS,
					argumentsForJtagFlash);
			argumentsForJtagFlash = savedArgumentsForJtagFlash;
			if (isFlashOverJtag) {
				argumentField.setText(argumentsForJtagFlash);
			}

		} catch (CoreException e) {
			Logger.log(e);
		}

	}

	private void initializeJtagComboFields(ILaunchConfiguration configuration) throws CoreException {
		fFlashVoltage
				.setText(configuration.getAttribute(IDFLaunchConstants.JTAG_FLASH_VOLTAGE, fFlashVoltage.getText()));
		fTarget.setText(configuration.getAttribute(IDFLaunchConstants.TARGET_FOR_JTAG, fTarget.getText()));
		fTarget.notifyListeners(SWT.Selection, null);
		fTargetName.setText(configuration.getAttribute(IDFLaunchConstants.JTAG_BOARD, fTargetName.getText()));
		fTargetName.notifyListeners(SWT.Selection, null);
	}

	private static void showNoTargetMessage(String selectedTarget) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				boolean isYes = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
						Messages.IDFLaunchTargetNotFoundIDFLaunchTargetNotFoundTitle,
						Messages.IDFLaunchTargetNotFoundMsg1 + selectedTarget + Messages.IDFLaunchTargetNotFoundMsg2
								+ Messages.IDFLaunchTargetNotFoundMsg3);
				if (isYes) {
					NewSerialFlashTargetWizard wizard = new NewSerialFlashTargetWizard();
					WizardDialog dialog = new WizardDialog(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
					dialog.open();
				}
			}
		});
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
			fFlashVoltage = new Combo(group, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
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
			fTarget = new Combo(group, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
			fTarget.setItems(parser.getTargets().toArray(new String[0]));
			fTarget.setText(selectedTarget);
			fTarget.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String updatedSelectedTarget = getLaunchTarget();
					String selectedItem = fTarget.getItem(fTarget.getSelectionIndex());
					if (!selectedItem.contentEquals(updatedSelectedTarget) && isFlashOverJtag) {
						try {
							ILaunchConfigurationWorkingCopy wc = launchBarManager.getActiveLaunchConfiguration()
									.getWorkingCopy();
							wc.setAttribute(IDFLaunchConstants.TARGET_FOR_JTAG, selectedItem);
							wc.doSave();
							System.out.print(wc.getAttribute(IDFLaunchConstants.TARGET_FOR_JTAG, ""));

						} catch (CoreException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
						updateLaunchBar(selectedItem);
					}
					boardConfigsMap = parser.getBoardsConfigs(selectedItem);
					fTargetName.setItems(parser.getBoardsConfigs(selectedItem).keySet().toArray(new String[0]));
					fTargetName.select(0);
					updateArgumentsField();
				}

				private void updateLaunchBar(String selectedItem) {
					ILaunchTarget target = findSuitableTargetForSelectedItem(selectedItem);
					try {
						if (target != null) {
							launchBarManager.setActiveLaunchTarget(target);
						} else {
							showNoTargetMessage(selectedItem);
						}

					} catch (CoreException e1) {
						Logger.log(e1);
					}
				}

				private ILaunchTarget findSuitableTargetForSelectedItem(String selectedItem) {
					ILaunchTargetManager launchTargetManager = Activator.getService(ILaunchTargetManager.class);
					ILaunchTarget[] targets = launchTargetManager
							.getLaunchTargetsOfType("com.espressif.idf.launch.serial.core.serialFlashTarget"); //$NON-NLS-1$
					ILaunchTarget suitableTarget = null;

					for (ILaunchTarget target : targets) {
						String idfTarget = target.getAttribute("com.espressif.idf.launch.serial.core.idfTarget", null); //$NON-NLS-1$
						String targetSerialPort = target.getAttribute(SerialFlashLaunchTargetProvider.ATTR_SERIAL_PORT,
								""); //$NON-NLS-1$
						if (idfTarget.contentEquals(selectedItem)) {
							if (targetSerialPort.contentEquals(getSerialPort())) {
								return target;
							}
							suitableTarget = target;
						}
					}
					return suitableTarget;
				}
			});
		}
		{
			Label label = new Label(group, SWT.NONE);
			label.setText(Messages.configBoardLabel);
			label.setToolTipText(Messages.configBoardTooTip);
			fTargetName = new Combo(group, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
			fTargetName.setItems(parser.getBoardsConfigs(selectedTarget).keySet().toArray(new String[0]));
			boardConfigsMap = parser.getBoardsConfigs(selectedTarget);
			fTargetName.select(0);
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
