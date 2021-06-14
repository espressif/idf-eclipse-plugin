/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.launch.serial.ui.internal;

import java.io.File;
import java.util.Map;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.ui.corebuild.GenericMainTab;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.json.simple.JSONArray;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.EspConfigParser;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.launch.serial.internal.SerialFlashLaunch;
import com.espressif.idf.launch.serial.internal.SerialFlashLaunchConfigDelegate;
import com.espressif.idf.launch.serial.util.EspFlashCommandGenerator;

@SuppressWarnings("restriction")
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

	@Override
	public void createControl(Composite parent) {
		isJtagFlashAvailable = checkIfJtagIsAvailable();
		super.createControl(parent);
		createJtagFlashButton(parent);
	}

	private void createJtagFlashButton(Composite parent) {
		if (!isJtagFlashAvailable) {
			return;
		}
		flashOverJtagButton = new Button(parent, SWT.CHECK);
		flashOverJtagButton.setText(Messages.CMakeMainTab2_JtagComboLbl);
		Preferences scopedPreferenceStore = InstanceScope.INSTANCE
				.getNode(com.espressif.idf.launch.serial.internal.Activator.PLUGIN_ID);
		isFlashOverJtag = scopedPreferenceStore.getBoolean(SerialFlashLaunchConfigDelegate.getFlashOverJtag(), false);

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
		flashOverJtagButton.setSelection(isFlashOverJtag);
	}

	private void switchUI() {
		if (!isJtagFlashAvailable) {
			return;
		}
		locationField.setEnabled(!isFlashOverJtag);
		workDirectoryField.setEnabled(!isFlashOverJtag);
		fileLocationButton.setEnabled(!isFlashOverJtag);
		workspaceLocationButton.setEnabled(!isFlashOverJtag);
		variablesLocationButton.setEnabled(!isFlashOverJtag);
		fileWorkingDirectoryButton.setEnabled(!isFlashOverJtag);
		workspaceWorkingDirectoryButton.setEnabled(!isFlashOverJtag);
		variablesWorkingDirectoryButton.setEnabled(!isFlashOverJtag);

		fFlashVoltage.setEnabled(isFlashOverJtag);
		fTargetName.setEnabled(isFlashOverJtag);
		fTarget.setEnabled(isFlashOverJtag);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);
		if (!isJtagFlashAvailable) {
			return;
		}
		Preferences scopedPreferenceStore = InstanceScope.INSTANCE
				.getNode(com.espressif.idf.launch.serial.internal.Activator.PLUGIN_ID);
		scopedPreferenceStore.putBoolean(SerialFlashLaunchConfigDelegate.getFlashOverJtag(),
				flashOverJtagButton.getSelection());
		try {
			scopedPreferenceStore.flush();
		} catch (BackingStoreException e1) {
			Logger.log(e1);
		}
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		updateArgumentsWithDefaultFlashCommand(configuration);
		switchUI();
	}

	private void updateArgumentsWithDefaultFlashCommand(ILaunchConfiguration configuration) {
		ILaunchTargetManager manager = Activator.getService(ILaunchTargetManager.class);
		ILaunchTarget target = manager.getDefaultLaunchTarget(configuration);
		ILaunch launch = new SerialFlashLaunch(configuration, "run", null, target); //$NON-NLS-1$
		String espFlashCommand = EspFlashCommandGenerator.getEspFlashCommand(launch);
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

	private boolean checkIfJtagIsAvailable() {
		EspConfigParser parser = new EspConfigParser();
		String openOCDPath = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.OPENOCD_SCRIPTS);
		if (!openOCDPath.isEmpty() && parser.hasBoardConfigJson()) {
			return true;
		}
		return false;
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
			selectedTarget = launchBarManager.getActiveLaunchTarget().getId();
		} catch (CoreException e) {
			Logger.log(e);
		}
		return selectedTarget;
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
				workingDir = new File(configuration.getMappedResources()[0].getProject().getLocationURI());
				workDirectoryField.setText(newVariableExpression("workspace_loc", workingDir.getName())); //$NON-NLS-1$
			} catch (CoreException e) {
				Logger.log(e);
			}
		}
	}
}
