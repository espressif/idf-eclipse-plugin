package com.espressif.idf.launch.serial.ui.internal;

import java.io.File;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.ui.corebuild.GenericMainTab;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.launch.serial.internal.SerialFlashLaunch;
import com.espressif.idf.launch.serial.util.EspFlashCommandGenerator;

@SuppressWarnings("restriction")
public class CMakeMainTab2 extends GenericMainTab {
	private static final String SYSTEM_PATH_PYTHON = "${system_path:python}"; //$NON-NLS-1$
	public static final String FLASH_OVER_JTAG = "FLASH_OVER_JTAG"; //$NON-NLS-1$
	protected Button flashOverJtagButton;

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		createJtagFlashButton(parent);

	}

	private void createJtagFlashButton(Composite parent) {
		flashOverJtagButton = new Button(parent, SWT.CHECK);
		flashOverJtagButton.setText(Messages.CMakeMainTab2_JtagComboLbl);
		Preferences scopedPreferenceStore = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		boolean isFlashOverJtag = scopedPreferenceStore.getBoolean(FLASH_OVER_JTAG, false);
		flashOverJtagButton.setSelection(isFlashOverJtag);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);
		Preferences scopedPreferenceStore = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		scopedPreferenceStore.getBoolean(FLASH_OVER_JTAG, false);
		scopedPreferenceStore.putBoolean(FLASH_OVER_JTAG, flashOverJtagButton.getSelection());
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
	}

	private void updateArgumentsWithDefaultFlashCommand(ILaunchConfiguration configuration) {
		ILaunchTargetManager manager = Activator.getService(ILaunchTargetManager.class);
		ILaunchTarget target = manager.getDefaultLaunchTarget(configuration);
		ILaunch launch = new SerialFlashLaunch(configuration, "run", null, target); //$NON-NLS-1$
		try {
			String arguments = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_TOOL_ARGUMENTS,
					EspFlashCommandGenerator.getEspFlashCommand(launch));
			argumentField.setText(arguments);
		} catch (CoreException e) {
			Logger.log(e);
		}

	}

	@Override
	protected void updateArgument(ILaunchConfiguration configuration) {
		// TODO Auto-generated method stub
		super.updateArgument(configuration);
	}

	@Override
	protected void updateLocation(ILaunchConfiguration configuration) {
		super.updateLocation(configuration);
		locationField.removeModifyListener(fListener);
		String location = IDFUtil.getIDFPythonEnvPath();
		if (StringUtil.isEmpty(location)) {
			try {
				location = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_LOCATION,
						SYSTEM_PATH_PYTHON);
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
