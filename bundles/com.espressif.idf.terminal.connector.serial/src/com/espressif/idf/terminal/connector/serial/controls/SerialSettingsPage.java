/*******************************************************************************
 * Copyright (c) 2017, 2018 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package com.espressif.idf.terminal.connector.serial.controls;

import java.io.IOException;

import org.eclipse.cdt.serial.BaudRate;
import org.eclipse.cdt.serial.ByteSize;
import org.eclipse.cdt.serial.Parity;
import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.cdt.serial.StopBits;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tm.internal.terminal.provisional.api.AbstractSettingsPage;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanel;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanelContainer;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.util.SDKConfigJsonReader;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.terminal.connector.serial.activator.Activator;
import com.espressif.idf.terminal.connector.serial.connector.SerialConnector;
import com.espressif.idf.terminal.connector.serial.connector.SerialSettings;
import com.espressif.idf.terminal.connector.serial.nls.Messages;
import com.espressif.idf.ui.EclipseUtil;

public class SerialSettingsPage extends AbstractSettingsPage {

	private final SerialSettings settings;
	private final IConfigurationPanel panel;
	private final IDialogSettings dialogSettings;

	private Combo portCombo;
	private Combo baudRateCombo;
	private Combo byteSizeCombo;
	private Combo parityCombo;
	private Combo stopBitsCombo;

	private String portName;
	private BaudRate baudRate;
	private ByteSize byteSize;
	private Parity parity;
	private StopBits stopBits;
	private String lastUsedSerialPort;

	public SerialSettingsPage(SerialSettings settings, IConfigurationPanel panel) {
		this.settings = settings;
		this.panel = panel;
		setHasControlDecoration(true);

		dialogSettings = DialogSettings.getOrCreateSection(Activator.getDefault().getDialogSettings(),
				this.getClass().getSimpleName());
		portName = dialogSettings.get(SerialSettings.PORT_NAME_ATTR);

		lastUsedSerialPort = getLastUsedSerialPort();

		String sdkconfigBaudRate = getsdkconfigBaudRate();
		if (!StringUtil.isEmpty(sdkconfigBaudRate)) {
			baudRate = getBaudRate(sdkconfigBaudRate);
		}

		if (baudRate == null) {
			String baudRateStr = dialogSettings.get(SerialSettings.BAUD_RATE_ATTR);
			if (baudRateStr == null || baudRateStr.isEmpty()) {
				baudRate = BaudRate.getDefault();
			} else {
				baudRate = getBaudRate(baudRateStr);
			}
		}

		String byteSizeStr = dialogSettings.get(SerialSettings.BYTE_SIZE_ATTR);
		if (byteSizeStr == null || byteSizeStr.isEmpty()) {
			byteSize = ByteSize.getDefault();
		} else {
			String[] sizes = ByteSize.getStrings();
			for (int i = 0; i < sizes.length; ++i) {
				if (byteSizeStr.equals(sizes[i])) {
					byteSize = ByteSize.fromStringIndex(i);
					break;
				}
			}
		}

		String parityStr = dialogSettings.get(SerialSettings.PARITY_ATTR);
		if (parityStr == null || parityStr.isEmpty()) {
			parity = Parity.getDefault();
		} else {
			String[] parities = Parity.getStrings();
			for (int i = 0; i < parities.length; ++i) {
				if (parityStr.equals(parities[i])) {
					parity = Parity.fromStringIndex(i);
					break;
				}
			}
		}

		String stopBitsStr = dialogSettings.get(SerialSettings.STOP_BITS_ATTR);
		if (stopBitsStr == null || stopBitsStr.isEmpty()) {
			stopBits = StopBits.getDefault();
		} else {
			String[] bits = StopBits.getStrings();
			for (int i = 0; i < bits.length; ++i) {
				if (stopBitsStr.equals(bits[i])) {
					stopBits = StopBits.fromStringIndex(i);
					break;
				}
			}
		}
	}

	protected String getsdkconfigBaudRate() {
		IResource resource = EclipseUtil.getSelectionResource();
		if (resource != null) {
			IProject project = resource.getProject();
			return new SDKConfigJsonReader(project).getValue("ESPTOOLPY_MONITOR_BAUD"); //$NON-NLS-1$
		}
		return null;
	}

	protected BaudRate getBaudRate(String baudRateStr) {
		String[] rates = BaudRate.getStrings();
		for (int i = 0; i < rates.length; ++i) {
			if (baudRateStr.equals(rates[i])) {
				return BaudRate.fromStringIndex(i);
			}
		}
		return baudRate;
	}

	protected String getLastUsedSerialPort() {
		Preferences preferences = InstanceScope.INSTANCE.getNode("com.espressif.idf.launch.serial.ui"); //$NON-NLS-1$
		return preferences.get("com.espressif.idf.launch.serial.core.serialPort", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayout(gridLayout);
		comp.setLayoutData(gridData);

		Label portLabel = new Label(comp, SWT.NONE);
		portLabel.setText(Messages.SerialTerminalSettingsPage_SerialPort);

		portCombo = new Combo(comp, SWT.NONE);
		portCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		String[] portNames = new String[0];
		try {
			portNames = SerialPort.list();
		} catch (IOException e) {
			Activator.log(e);
		}
		for (String portName : portNames) {
			if (!SerialConnector.isOpen(portName)) {
				portCombo.add(portName);
			}
		}
		portCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validate();
			}
		});

		Label baudRateLabel = new Label(comp, SWT.NONE);
		baudRateLabel.setText(Messages.SerialTerminalSettingsPage_BaudRate);

		baudRateCombo = new Combo(comp, SWT.READ_ONLY);
		baudRateCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (String baudRateStr : BaudRate.getStrings()) {
			baudRateCombo.add(baudRateStr);
		}

		Label byteSizeLabel = new Label(comp, SWT.NONE);
		byteSizeLabel.setText(Messages.SerialTerminalSettingsPage_DataSize);

		byteSizeCombo = new Combo(comp, SWT.READ_ONLY);
		byteSizeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (String byteSizeStr : ByteSize.getStrings()) {
			byteSizeCombo.add(byteSizeStr);
		}

		Label parityLabel = new Label(comp, SWT.NONE);
		parityLabel.setText(Messages.SerialTerminalSettingsPage_Parity);

		parityCombo = new Combo(comp, SWT.READ_ONLY);
		parityCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (String parityStr : Parity.getStrings()) {
			parityCombo.add(parityStr);
		}

		Label stopBitsLabel = new Label(comp, SWT.NONE);
		stopBitsLabel.setText(Messages.SerialTerminalSettingsPage_StopBits);

		stopBitsCombo = new Combo(comp, SWT.READ_ONLY);
		stopBitsCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (String stopBitsStr : StopBits.getStrings()) {
			stopBitsCombo.add(stopBitsStr);
		}

		loadSettings();
	}

	void validate() {
		IConfigurationPanelContainer container = panel.getContainer();
		container.validate();
	}

	@Override
	public void loadSettings() {
		String portName = settings.getPortName();
		if (portName == null || portName.isEmpty()) {
			portName = this.lastUsedSerialPort;
		}
		if (portName != null && !portName.isEmpty() && !SerialConnector.isOpen(portName)) {
			int i = 0;
			for (String name : portCombo.getItems()) {
				if (portName.equals(name)) {
					portCombo.select(i);
					break;
				}
				i++;
			}
		} else if (portCombo.getItemCount() > 0) {
			portCombo.select(0);
		}

		BaudRate baudRate = settings.getBaudRate();
		if (baudRate == null) {
			baudRate = this.baudRate;
		}
		baudRateCombo.select(BaudRate.getStringIndex(baudRate));

		ByteSize byteSize = settings.getByteSize();
		if (byteSize == null) {
			byteSize = this.byteSize;
		}
		byteSizeCombo.select(ByteSize.getStringIndex(byteSize));

		Parity parity = settings.getParity();
		if (parity == null) {
			parity = this.parity;
		}
		parityCombo.select(Parity.getStringIndex(parity));

		StopBits stopBits = settings.getStopBits();
		if (stopBits == null) {
			stopBits = this.stopBits;
		}
		stopBitsCombo.select(StopBits.getStringIndex(stopBits));
	}

	@Override
	public void saveSettings() {
		settings.setPortName(portCombo.getText());
		settings.setBaudRate(BaudRate.fromStringIndex(baudRateCombo.getSelectionIndex()));
		settings.setByteSize(ByteSize.fromStringIndex(byteSizeCombo.getSelectionIndex()));
		settings.setParity(Parity.fromStringIndex(parityCombo.getSelectionIndex()));
		settings.setStopBits(StopBits.fromStringIndex(stopBitsCombo.getSelectionIndex()));

		dialogSettings.put(SerialSettings.PORT_NAME_ATTR, portCombo.getText());
		dialogSettings.put(SerialSettings.BAUD_RATE_ATTR, BaudRate.getStrings()[baudRateCombo.getSelectionIndex()]);
		dialogSettings.put(SerialSettings.BYTE_SIZE_ATTR, ByteSize.getStrings()[byteSizeCombo.getSelectionIndex()]);
		dialogSettings.put(SerialSettings.PARITY_ATTR, Parity.getStrings()[parityCombo.getSelectionIndex()]);
		dialogSettings.put(SerialSettings.STOP_BITS_ATTR, StopBits.getStrings()[stopBitsCombo.getSelectionIndex()]);
	}

	@Override
	public boolean validateSettings() {
		if (portCombo.getSelectionIndex() < 0 && portCombo.getText().isEmpty()) {
			return false;
		}
		return true;
	}

}
