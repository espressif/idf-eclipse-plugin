/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial version
 *     Espressif Systems Ltd — Kondal Kolipaka <kondal.kolipaka@espressif.com>

 *******************************************************************************/
package com.espressif.idf.launch.serial.ui.internal;

import java.io.IOException;

import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.espressif.idf.launch.serial.SerialFlashLaunchTargetProvider;

public class NewSerialFlashTargetWizardPage extends WizardPage {

	private static final String OS = "esp32"; //$NON-NLS-1$
	private static final String ARCH = "xtensa"; //$NON-NLS-1$

	private ILaunchTarget launchTarget;

	private Text nameText;
	private Combo serialPortCombo;
	private Combo idfTargetCombo;

	public NewSerialFlashTargetWizardPage(ILaunchTarget launchTarget) {
		super(NewSerialFlashTargetWizardPage.class.getName());
		this.launchTarget = launchTarget;
		setTitle(Messages.NewSerialFlashTargetWizardPage_Title);
		setDescription(Messages.NewSerialFlashTargetWizardPage_Description);
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		setControl(comp);

		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.NewSerialFlashTargetWizardPage_Name);

		nameText = new Text(comp, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		if (launchTarget != null) {
			nameText.setText(launchTarget.getId());
		}

		label = new Label(comp, SWT.NONE);
		label.setText(Messages.NewSerialFlashTargetWizardPage_IDFTarget);

		idfTargetCombo = new Combo(comp, SWT.READ_ONLY);
		idfTargetCombo.setItems(getIDFTargetList());
		idfTargetCombo.setToolTipText(Messages.NewSerialFlashTargetWizardPage_IDFTargetToolTipMsg);
		if (idfTargetCombo.getItemCount() > 0 && idfTargetCombo.getSelectionIndex() < 0) {
			idfTargetCombo.select(0);
		}

		label = new Label(comp, SWT.NONE);
		label.setText(Messages.NewSerialFlashTargetWizardPage_SerialPort);

		serialPortCombo = new Combo(comp, SWT.NONE);
		try {
			String[] ports = SerialPort.list();
			for (String port : ports) {
				serialPortCombo.add(port);
			}
			if (serialPortCombo.getItemCount() > 0) {
				if (launchTarget != null) {
					String targetPort = launchTarget.getAttribute(SerialFlashLaunchTargetProvider.ATTR_SERIAL_PORT,
							null);
					if (targetPort != null) {
						int i = 0;
						for (String port : ports) {
							if (port.equals(targetPort)) {
								serialPortCombo.select(i);
								break;
							}
							i++;
						}
					}
				}

				if (serialPortCombo.getSelectionIndex() < 0) {
					serialPortCombo.select(0);
				}
			}
		} catch (IOException e) {
			Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					Messages.NewSerialFlashTargetWizardPage_Fetching, e));
		}
	}

	public String getTargetName() {
		return nameText.getText();
	}

	public String getOS() {
		return OS;
	}

	public String getArch() {
		return ARCH;
	}

	public String getIDFTarget() {
		return idfTargetCombo.getText();
	}

	public String getSerialPortName() {
		return serialPortCombo.getText();
	}

	private String[] getIDFTargetList() {
		//Going forward we can support esp32s2 in the list
		return new String[] { "esp32" }; //$NON-NLS-1$
	}

}
