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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.espressif.idf.core.build.ESPToolChainManager;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.EspToolCommands;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.launch.serial.SerialFlashLaunchTargetProvider;

public class NewSerialFlashTargetWizardPage extends WizardPage {

	private static final String OS = "esp32"; //$NON-NLS-1$
	private static final String ARCH = "xtensa"; //$NON-NLS-1$

	private ILaunchTarget launchTarget;

	private Text nameText;
	private Combo serialPortCombo;
	private Combo idfTargetCombo;
	private Text infoArea;
	private Map<String, List<String>> targetPortMap;
	private TargetPortMapUpdateThread targetPortMapUpdateThread;
	private SerialPortUpdateThread serialPortUpdateThread;
	private Display display;

	public NewSerialFlashTargetWizardPage(ILaunchTarget launchTarget) {
		super(NewSerialFlashTargetWizardPage.class.getName());
		this.launchTarget = launchTarget;
		targetPortMap = new HashMap<>();
		setTitle(Messages.NewSerialFlashTargetWizardPage_Title);
		setDescription(Messages.NewSerialFlashTargetWizardPage_Description);
		targetPortMapUpdateThread = new TargetPortMapUpdateThread();
		serialPortUpdateThread = new SerialPortUpdateThread();
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		setControl(comp);
		display = comp.getDisplay();
		targetPortMapUpdateThread.start();

		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.NewSerialFlashTargetWizardPage_Name);

		nameText = new Text(comp, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		if (launchTarget != null) {
			nameText.setText(launchTarget.getId());
		}

		label = new Label(comp, SWT.NONE);
		label.setText(Messages.NewSerialFlashTargetWizardPage_IDFTarget);

		idfTargetCombo = new Combo(comp, SWT.NONE);

		List<String> idfTargetList = getIDFTargetList();

		idfTargetCombo.setItems(idfTargetList.toArray(new String[idfTargetList.size()]));
		idfTargetCombo.setToolTipText(Messages.NewSerialFlashTargetWizardPage_IDFTargetToolTipMsg);
		idfTargetCombo.addSelectionListener(new TargetComboSelectionAdapter());

		if (idfTargetCombo.getItemCount() > 0 && idfTargetCombo.getSelectionIndex() < 0) {
			idfTargetCombo.select(0);
		}

		if (launchTarget != null) {
			String idfTarget = launchTarget.getAttribute(SerialFlashLaunchTargetProvider.ATTR_IDF_TARGET, null);
			if (idfTarget != null) {
				int index = idfTargetList.indexOf(idfTarget);
				if (index != -1) {
					idfTargetCombo.select(index);
				} else {
					idfTargetCombo.setText(idfTarget);
				}
			}
		}

		label = new Label(comp, SWT.NONE);
		label.setText(Messages.NewSerialFlashTargetWizardPage_SerialPort);

		serialPortCombo = new Combo(comp, SWT.NONE);
		serialPortCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (targetPortMapUpdateThread.isAlive()) {
					targetPortMapUpdateThread.running = false;
				}
				com.fazecast.jSerialComm.SerialPort serialPort = com.fazecast.jSerialComm.SerialPort
						.getCommPort(serialPortCombo.getText());
				if (serialPort != null) {
					infoArea.setText(serialPort.getDescriptivePortName());
				}

			}
		});
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
			}
		} catch (IOException e) {
			Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					Messages.NewSerialFlashTargetWizardPage_Fetching, e));
		}

		infoArea = new Text(comp, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		infoArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		serialPortUpdateThread.start();
	}

	@Override
	public void dispose() {
		serialPortUpdateThread.running = targetPortMapUpdateThread.running = false;
		super.dispose();
	}

	public String getTargetName() {
		return nameText.getText();
	}

	public String getOS() {
		return getModel();
	}

	private String getModel() {
		String idfTarget = getIDFTarget();
		List<String> idfTargetList = getIDFTargetList();
		int index = idfTargetList.indexOf(idfTarget);
		if (index != -1) {
			return idfTarget;
		}
		return OS;
	}

	public String getArch() {
		for (IToolChain map : getToolchains()) {
			if (map.getProperty(IToolChain.ATTR_OS).equals(getIDFTarget())) {
				return map.getProperty(IToolChain.ATTR_ARCH);
			}
		}
		return ARCH;
	}

	public String getIDFTarget() {
		return idfTargetCombo.getText();
	}

	public String getSerialPortName() {
		return serialPortCombo.getText();
	}

	private List<String> getIDFTargetList() {
		return new ESPToolChainManager().getAvailableEspTargetList();

	}

	private Collection<IToolChain> getToolchains() {
		return new ESPToolChainManager().getAllEspToolchains();
	}

	private void putPortInMapForTarget(String target, String port) {
		List<String> portsForTargetMatched = targetPortMap.get(target);
		if (portsForTargetMatched != null) {
			portsForTargetMatched.add(port);
		} else {
			portsForTargetMatched = new ArrayList<>();
			portsForTargetMatched.add(port);
			targetPortMap.put(target, portsForTargetMatched);
		}
	}

	private String extractChipFromChipInfoOutput(String chipInfoOutput) {
		Pattern pattern = Pattern.compile("Chip is (ESP32[^\\s]*)"); //$NON-NLS-1$
		Matcher matcher = pattern.matcher(chipInfoOutput);
		if (matcher.find()) {
			String chipType = matcher.group(1);
			chipType = chipType.replace("-", StringUtil.EMPTY).toLowerCase(); //$NON-NLS-1$
			return chipType;
		}

		return StringUtil.EMPTY;
	}

	private class TargetComboSelectionAdapter extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			String selectedTarget = idfTargetCombo.getText();

			List<String> comPortList = targetPortMap.get(selectedTarget);
			if (comPortList != null && comPortList.size() > 0) {
				serialPortCombo.select(serialPortCombo.indexOf(comPortList.get(0)));
				infoArea.setText(
						String.format(Messages.TargetPortInformationMessage, selectedTarget, comPortList.toString()));
				com.fazecast.jSerialComm.SerialPort serialPort = com.fazecast.jSerialComm.SerialPort
						.getCommPort(comPortList.get(0));
				if (serialPort != null) {
					infoArea.setText(serialPort.getDescriptivePortName() + System.lineSeparator() + infoArea.getText());
				}
			}

		}
	}

	private class SerialPortUpdateThread extends Thread {
		private boolean running = true;

		@Override
		public void run() {
			try {
				while (running) {
					Thread.sleep(1000);
					String[] ports = getSerialPorts();
					display.asyncExec(() -> {
						if (serialPortCombo.isDisposed())
							return;
						String[] loadedPorts = serialPortCombo.getItems();
						if (loadedPorts.length != ports.length) {

							if (ports.length != 0) {
								serialPortCombo.setItems(ports);
								serialPortCombo.select(0);
								infoArea.setText(Messages.SerialPortUpdateThreadInfoMessage);
							}

							String targetPort = launchTarget
									.getAttribute(SerialFlashLaunchTargetProvider.ATTR_SERIAL_PORT, null);
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
					});

				}
			} catch (Exception e) {
				Logger.log(e);
			}
		}

		private String[] getSerialPorts() throws IOException {
			return SerialPort.list();
		}
	}

	private class TargetPortMapUpdateThread extends Thread {
		private boolean running = true;

		@Override
		public void run() {
			try {
				updateTargetHashMap();
			} catch (Exception e) {
				Logger.log(e);
			}
		}

		private void updateTargetHashMap() throws Exception {
			EspToolCommands espToolCommands = new EspToolCommands();
			List<String> idfTargetList = getIDFTargetList();
			for (String idfTarget : idfTargetList) {
				targetPortMap.put(idfTarget, null);
			}

			String[] ports = SerialPort.list();
			if (ports == null || ports.length == 0)
				return;

			for (String port : ports) {
				String message = String.format(Messages.TargetPortUpdatingMessage, port);

				display.asyncExec(() -> {
					if (infoArea != null && !infoArea.isDisposed())
						infoArea.setText(infoArea.getText() + System.lineSeparator() + message);
				});

				Process chipInfoProcess = espToolCommands.chipInformation(port);
				InputStream targetIn = chipInfoProcess.getInputStream();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(targetIn));
				StringBuilder chipInfo = new StringBuilder();
				String readLine;
				while ((readLine = bufferedReader.readLine()) != null && running) {
					chipInfo.append(readLine);
					chipInfo.append(System.lineSeparator());
				}
				if (!running) {
					bufferedReader.close();
					return;
				}

				String chipType = extractChipFromChipInfoOutput(chipInfo.toString());
				if (StringUtil.isEmpty(chipType)) {
					display.asyncExec(() -> {
						if (infoArea != null && !infoArea.isDisposed())
							infoArea.setText(infoArea.getText() + System.lineSeparator()
									+ String.format(Messages.TargetPortNotFoundMessage, port));
					});
					continue;
				}

				Optional<String> optTarget = idfTargetList.stream().filter(t -> t.equals(chipType)).findFirst();
				if (optTarget.isPresent()) {
					String targetMatched = optTarget.get();
					putPortInMapForTarget(targetMatched, port);
					display.asyncExec(() -> {
						infoArea.setText(infoArea.getText() + System.lineSeparator()
								+ String.format(Messages.TargetPortFoundMessage, port, targetMatched));
					});
				} else {
					// if we dont find the board from chip type
					// assuming esp32 as the chipinfo for that gives additional info
					putPortInMapForTarget("esp32", port); //$NON-NLS-1$
					display.asyncExec(() -> {
						infoArea.setText(infoArea.getText() + System.lineSeparator()
								+ String.format(Messages.TargetPortFoundMessage, port, "esp32")); //$NON-NLS-1$
					});
				}
			}

		}
	}
}
