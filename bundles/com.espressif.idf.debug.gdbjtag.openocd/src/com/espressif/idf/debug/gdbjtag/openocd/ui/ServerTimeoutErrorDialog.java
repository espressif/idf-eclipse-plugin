/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class ServerTimeoutErrorDialog extends MessageDialog
{

	private static final String ESPRESSIF_PREFERENCES_MAINPAGE_ID = "com.espressif.idf.ui.preferences.mainpage"; //$NON-NLS-1$

	public ServerTimeoutErrorDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage,
			int dialogImageType, int defaultIndex, String[] dialogButtonLabels)
	{
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, defaultIndex,
				dialogButtonLabels);
	}

	public static boolean open(int kind, Shell parent, String title, String message)
	{
		ServerTimeoutErrorDialog dialog = new ServerTimeoutErrorDialog(parent, title, null, message, kind, 0,
				new String[] { IDialogConstants.OK_LABEL });
		return dialog.open() == 0;
	}

	public static void openError(Shell parent)
	{
		open(ERROR, parent, Messages.ServerTimeoutErrorDialog_title, Messages.ServerTimeoutErrorDialog_message);
	}

	@Override
	protected Control createCustomArea(Composite parent)
	{
		Link link = new Link(parent, SWT.WRAP);
		link.setText(Messages.ServerTimeoutErrorDialog_customAreaMessage);
		link.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				PreferencesUtil
						.createPreferenceDialogOn(parent.getShell(), ESPRESSIF_PREFERENCES_MAINPAGE_ID, null, null)
						.open();
			}
		});
		;
		return link;
	}
}
