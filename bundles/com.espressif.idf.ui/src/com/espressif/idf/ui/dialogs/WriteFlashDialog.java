package com.espressif.idf.ui.dialogs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.LaunchBarTargetConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.BigIntDecoder;
import com.espressif.idf.core.util.EspToolCommands;
import com.espressif.idf.core.util.StringUtil;

public class WriteFlashDialog extends TitleAreaDialog
{
	private static final String DEFAULT_BIN_NAME = "nvs.bin"; //$NON-NLS-1$
	private static final String DEFAULT_OFFSET = "0x8000"; //$NON-NLS-1$
	private static final String[] EXTENSIONS = new String[] { "*.bin" }; //$NON-NLS-1$
	private Combo comPortsCombo;
	private Text deviceInformationText;
	private String[] ports;
	private EspToolCommands espToolCommands;
	private Text offsetText;
	private Text binPathText;
	private IProject project;
	private boolean doChangeErrorMessage = false;

	public WriteFlashDialog(Shell parentShell)
	{
		super(parentShell);
		ports = new String[] {};
		try
		{
			ports = SerialPort.list();
			espToolCommands = new EspToolCommands();
		}
		catch (IOException e)
		{
			Logger.log(e);
		}

		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
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
		Label binPathLabel = new Label(container, SWT.NONE);
		binPathLabel.setText(Messages.WriteFlashDialog_Bin_Path_Lbl);
		binPathText = new Text(container, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL);
		binPathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button browseButton = new Button(container, SWT.PUSH);
		browseButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1));
		browseButton.setText(Messages.WriteFlashDialog_Browse_Btn);
		browseButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent evt)
			{
				FileDialog fileSelectionDialog = new FileDialog(getParentShell());
				fileSelectionDialog.setFilterExtensions(EXTENSIONS);
				fileSelectionDialog.setFilterPath(project.getLocationURI().getPath());
				String selectedFilePath = fileSelectionDialog.open();

				if (selectedFilePath != null && !selectedFilePath.isEmpty())
				{
					binPathText.setText(selectedFilePath);
				}
			}
		});

		Label offsetLabel = new Label(container, SWT.NONE);
		offsetLabel.setText(Messages.WriteFlashDialog_Offset_Lbl);
		offsetText = new Text(container, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL);
		offsetText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

		deviceInformationText = new Text(container, SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		deviceInformationText.setText(Messages.EraseFlashDialog_DeviceInformationAreaInitialText);
		deviceInformationText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));

		comPortsCombo.addSelectionListener(new ComPortSelectionListener());
		binPathText.addListener(SWT.Modify, e -> getButton(IDialogConstants.OK_ID).setEnabled(validateInputs()));
		offsetText.addListener(SWT.Modify, e -> getButton(IDialogConstants.OK_ID).setEnabled(validateInputs()));

		return container;
	}

	@Override
	public void create()
	{
		super.create();
		setTitle(Messages.WriteFlashDialog_Title);
		setMessage(Messages.WriteFlashDialog_Information_Msg, IMessageProvider.INFORMATION);
		setDefaults();
	}

	private void setDefaults()
	{
		offsetText.setText(DEFAULT_OFFSET);
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService()
				.getSelection();
		if (selection instanceof IStructuredSelection sel)
		{
			Object element = sel.getFirstElement();

			if (element instanceof IResource elem)
			{
				project = elem.getProject();
			}
		}
		String defaultPathToBin = project.getFile(DEFAULT_BIN_NAME).getLocation().toOSString();
		if (new File(defaultPathToBin).exists())
		{

			binPathText.setText(defaultPathToBin);
		}
		else
		{
			binPathText.setMessage(defaultPathToBin);
		}

		ILaunchBarManager barManager = IDFCorePlugin.getService(ILaunchBarManager.class);
		try
		{
			String serialPortFromTarget = barManager.getActiveLaunchTarget()
					.getAttribute(LaunchBarTargetConstants.SERIAL_PORT, StringUtil.EMPTY);
			comPortsCombo.setText(serialPortFromTarget);
			if (!serialPortFromTarget.isEmpty())
			{
				comPortsCombo.notifyListeners(SWT.Selection, null);
			}
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		doChangeErrorMessage = true;
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText(Messages.WriteFlashDialog_Title);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, Messages.WriteFlashDialog_Flash_Btn_Lbl, true);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
	}

	@Override
	public boolean close()
	{
		if (espToolCommands.checkActiveWriteFlashProcess())
		{
			boolean canClose = MessageDialog.openQuestion(getParentShell(),
					Messages.EraseFlashDialog_EraseFlashInProcessMessageTitle,
					Messages.EraseFlashDialog_EraseFlashInProcessMessageQuestion);
			if (!canClose)
			{
				return false;
			}
			espToolCommands.killWriteFlashProcess();
		}

		return super.close();
	}

	@Override
	protected void okPressed()
	{
		setReturnCode(OK);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		deviceInformationText.setText(Messages.WriteFlashDialog_WritingBinsToFlashMsg0);
		String selectedPort = comPortsCombo.getText();
		comPortsCombo.setEnabled(false);
		deviceInformationText.setText(StringUtil.EMPTY);
		String binsPathString = binPathText.getText();
		String offString = offsetText.getText();
		Thread writeFlashThread = new Thread(() -> {
			try
			{
				Process writeFlashProcess = espToolCommands.writeFlash(selectedPort, binsPathString, offString);
				InputStream targetIn = writeFlashProcess.getInputStream();
				InputStream targetErr = writeFlashProcess.getErrorStream();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(targetIn));
				BufferedReader errorReader = new BufferedReader(new InputStreamReader(targetErr));

				String line;
				StringBuilder errorMessage = new StringBuilder();
				while ((line = bufferedReader.readLine()) != null)
				{
					final String toWrite = line;
					Display.getDefault().asyncExec(() -> {
						if (!deviceInformationText.isDisposed())
						{
							deviceInformationText.append(toWrite + StringUtil.LINE_SEPARATOR);
						}
					});
				}
				while ((line = errorReader.readLine()) != null)
				{
					errorMessage.append(line).append(System.lineSeparator());
				}
				int exitCode = writeFlashProcess.waitFor();
				if (exitCode != 0 || errorMessage.length() > 0)
				{
					final String errorMsg = errorMessage.toString();
					Display.getDefault().asyncExec(() -> {
						if (!deviceInformationText.isDisposed())
						{
							deviceInformationText.append(
									Messages.WriteFlashDialog_ErrorExitCodeMsg + exitCode + StringUtil.LINE_SEPARATOR);
							if (!errorMsg.isEmpty())
							{
								deviceInformationText.append(Messages.WriteFlashDialog_ErrorOutputMsg + errorMsg
										+ StringUtil.LINE_SEPARATOR);
							}
						}
					});
				}
			}
			catch (IOException e)
			{
				Logger.log(e);
			}
			catch (InterruptedException e)
			{
				Logger.log(e);
			}
			catch (Exception e)
			{
				Logger.log(e);
			}

			Display.getDefault().syncExec(() -> {
				if (!comPortsCombo.isDisposed())
				{
					comPortsCombo.setEnabled(true);
				}
			});
		});

		writeFlashThread.start();

	}

	@Override
	protected Point getInitialSize()
	{
		return new Point(450, 500);
	}

	private boolean validateInputs()
	{
		boolean validateStatus = true;
		if (comPortsCombo.getText().isEmpty())
		{
			validateStatus = false;
			setErrorMessage(Messages.WriteFlashDialog_SerialPortErrMsg);
		}
		if (!new File(binPathText.getText()).exists())
		{
			validateStatus = false;
			setErrorMessage(String.format(Messages.WriteFlashDialog_BinFileErrFormatErrMsg, binPathText.getText()));
		}
		try
		{
			BigIntDecoder.decode(offsetText.getText());
		}
		catch (NumberFormatException e)
		{
			validateStatus = false;
			setErrorMessage(Messages.WriteFlashDialog_OffsetErrMsg);
		}
		if (validateStatus)
		{
			setErrorMessage(null);
		}
		return validateStatus;
	}

	@Override
	public void setErrorMessage(String newErrorMessage)
	{
		if (doChangeErrorMessage)
		{
			super.setErrorMessage(newErrorMessage);
		}
	}

	private class ComPortSelectionListener extends SelectionAdapter implements Runnable
	{
		private String selectedPort;

		@Override
		public void widgetSelected(SelectionEvent event)
		{
			Thread infoThread;
			getButton(IDialogConstants.OK_ID).setEnabled(validateInputs());
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
					Display.getDefault().asyncExec(() -> {
						if (!deviceInformationText.isDisposed())
						{
							deviceInformationText.append(Character.toString(charToWrite));
						}
					});
				}
			}
			catch (Exception e)
			{
				Logger.log(e);
			}
		}
	}
}
