/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.dialogs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;

/**
 * Erase Flash Dialog class to erase flash on selected com port device
 * 
 * @author Ali Azam Rana
 *
 */
public class EraseFlashDialog extends TitleAreaDialog
{
	private Combo comPortsCombo;
	private Text deviceInformationText;
	private String[] ports;
	private EspToolCommands espToolCommands;

	public EraseFlashDialog(Shell parent)
	{
		super(parent);
		try
		{
			ports = SerialPort.list();
			espToolCommands = new EspToolCommands();
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(3, false);
		container.setLayout(layout);
		GridData comboLayoutData = new GridData();
		comboLayoutData.grabExcessHorizontalSpace = true;
		comboLayoutData.horizontalAlignment = GridData.FILL;
		comboLayoutData.horizontalSpan = 2;

		Label comPortsLabel = new Label(container, SWT.NONE);
		comPortsLabel.setText(Messages.EraseFlashDialog_ComPortLabel);

		comPortsCombo = new Combo(container, SWT.READ_ONLY);

		for (String port : ports)
		{
			comPortsCombo.add(port);
		}

		comPortsCombo.setLayoutData(comboLayoutData);
		deviceInformationText = new Text(container, SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData deviceInformationTextLayout = new GridData();
		deviceInformationTextLayout.grabExcessHorizontalSpace = true;
		deviceInformationTextLayout.grabExcessVerticalSpace = true;
		deviceInformationTextLayout.horizontalAlignment = GridData.FILL;
		deviceInformationTextLayout.verticalAlignment = GridData.FILL;
		deviceInformationTextLayout.horizontalSpan = 3;

		deviceInformationText.setText(Messages.EraseFlashDialog_DeviceInformationAreaInitialText);
		deviceInformationText.setLayoutData(deviceInformationTextLayout);

		comPortsCombo.addSelectionListener(new ComPortSelectionListener());

		return container;
	}

	@Override
	public void create()
	{
		super.create();
		setTitle(Messages.EraseFlashDialog_Title);
		setMessage(Messages.EraseFlashDialog_InformationMessage, IMessageProvider.INFORMATION);
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText(Messages.EraseFlashDialog_Title);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, Messages.EraseFlashDialog_OkButton, true);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	public boolean close()
	{
		if (espToolCommands.checkActiveFlashEraseProcess())
		{
			MessageBox messageBox = new MessageBox(getParentShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
			messageBox.setMessage(Messages.EraseFlashDialog_EraseFlashInProcessMessageQuestion);
			messageBox.setText(Messages.EraseFlashDialog_EraseFlashInProcessMessageTitle);
			switch (messageBox.open())
			{
			case SWT.YES:
				espToolCommands.killEraseFlashProcess();
				break;
			case SWT.CANCEL:
				return false;
			default:
				break;
			}
		}
		return super.close();
	}

	@Override
	protected void okPressed()
	{
		setReturnCode(OK);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		deviceInformationText.setText("Erasing Flash...."); // $NON-NLS
		String selectedPort = comPortsCombo.getText();
		Thread eraseFlashThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Display.getDefault().syncExec(() -> comPortsCombo.setEnabled(false));
					Display.getDefault().syncExec(() -> deviceInformationText.setText("")); // $NON-NLS
					Process eraseFlashProcess = espToolCommands.eraseFlash(selectedPort);
					InputStream targetIn = eraseFlashProcess.getInputStream();
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(targetIn));
					String line = ""; // $NON-NLS
					while ((line = bufferedReader.readLine()) != null)
					{
						final String toWrite = line;
						Display.getDefault().asyncExec(() -> deviceInformationText.append(toWrite + "\n")); // $NON-NLS
					}
				}
				catch (Exception e)
				{
					Logger.log(e);
				}

				Display.getDefault().syncExec(() -> comPortsCombo.setEnabled(true));
			}
		});
		if (espToolCommands.checkActiveFlashEraseProcess())
		{
			MessageBox messageBox = new MessageBox(getParentShell(), SWT.ICON_INFORMATION);
			messageBox.setMessage(Messages.EraseFlashDialog_EraseFlashInProcessMessage);
			messageBox.setText(Messages.EraseFlashDialog_EraseFlashInProcessMessageTitle);
			messageBox.open();
		}
		else
		{
			eraseFlashThread.start();
		}
	}

	@Override
	protected Point getInitialSize()
	{
		return new Point(450, 300);
	}

	private class ComPortSelectionListener extends SelectionAdapter implements Runnable
	{
		private String selectedPort;
		private Thread infoThread;

		@Override
		public void widgetSelected(SelectionEvent event)
		{
			getButton(IDialogConstants.OK_ID).setEnabled(true);
			selectedPort = ((Combo) event.getSource()).getText();
			try
			{
				deviceInformationText.setText(Messages.EraseFlashDialog_LoadingMessage);
				infoThread = new Thread(this);
				infoThread.start();
			}
			catch (Exception exception)
			{
				Logger.log(exception);
			}
		}

		@Override
		public void run()
		{
			try
			{
				Process chipInfoProcess = espToolCommands.chipInformation(selectedPort);
				InputStream targetIn = chipInfoProcess.getInputStream();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(targetIn));
				int readCharInt = 0;
				while ((readCharInt = bufferedReader.read()) != -1)
				{
					final char charToWrite = (char) readCharInt;
					Display.getDefault().syncExec(() -> deviceInformationText.append(Character.toString(charToWrite)));
				}
			}
			catch (Exception e)
			{
				Logger.log(e);
			}
		}
	}

	private class EspToolCommands
	{
		private Process chipInfoProcess;
		private Process flashEraseProcess;

		private Process chipInformation(String port) throws Exception
		{
			destroyAnyChipInfoProcess();
			chipInfoProcess = new ProcessBuilder(getChipInfoCommand(port)).start();
			return chipInfoProcess;
		}

		private Process eraseFlash(String port) throws Exception
		{
			destroyAnyChipInfoProcess();
			flashEraseProcess = new ProcessBuilder(getFlashEraseCommand(port)).start();
			return flashEraseProcess;
		}

		private List<String> getChipInfoCommand(String port)
		{
			List<String> command = new ArrayList<String>();
			command.add(IDFUtil.getIDFPythonEnvPath());
			command.add(IDFUtil.getEspToolScriptFile().getAbsolutePath());
			command.add("-p");
			command.add(port);
			command.add(IDFConstants.ESP_TOOL_CHIP_ID_CMD);
			return command;
		}

		private List<String> getFlashEraseCommand(String port)
		{
			List<String> command = new ArrayList<String>();
			command.add(IDFUtil.getIDFPythonEnvPath());
			command.add(IDFUtil.getEspToolScriptFile().getAbsolutePath());
			command.add("-p");
			command.add(port);
			command.add(IDFConstants.ESP_TOOL_ERASE_FLASH_CMD);
			return command;
		}

		private void destroyAnyChipInfoProcess()
		{
			if (chipInfoProcess != null && chipInfoProcess.isAlive())
			{
				chipInfoProcess.destroy();
			}
		}

		private boolean checkActiveFlashEraseProcess()
		{
			if (flashEraseProcess != null)
			{
				return flashEraseProcess.isAlive();
			}

			return false;
		}

		private void killEraseFlashProcess()
		{
			if (flashEraseProcess != null)
			{
				flashEraseProcess.destroy();
			}
		}
	}
}
