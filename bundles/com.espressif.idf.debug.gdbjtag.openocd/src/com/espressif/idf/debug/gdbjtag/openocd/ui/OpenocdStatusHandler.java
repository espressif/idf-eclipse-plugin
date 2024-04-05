/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class OpenocdStatusHandler implements IStatusHandler
{
	private static final String ESPRESSIF_PREFERENCES_MAINPAGE_ID = "com.espressif.idf.ui.preferences.mainpage"; //$NON-NLS-1$

	public Object handleStatus(IStatus status, Object source) throws CoreException
	{
		Display.getDefault().asyncExec(() -> {
			MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(),
					Messages.ServerTimeoutErrorDialog_title, null, Messages.ServerTimeoutErrorDialog_message,
					MessageDialog.ERROR, 0, IDialogConstants.OK_LABEL)
			{
				@Override
				public Control createCustomArea(Composite parent)
				{
					Link link = new Link(parent, SWT.WRAP);
					link.setText(Messages.ServerTimeoutErrorDialog_customAreaMessage);
					link.addSelectionListener(new SelectionAdapter()
					{
						@Override
						public void widgetSelected(SelectionEvent e)
						{
							PreferencesUtil.createPreferenceDialogOn(parent.getShell(),
									ESPRESSIF_PREFERENCES_MAINPAGE_ID, null, null).open();
						}
					});
					return link;
				}

			};
			dialog.open();
		});
		return null;
	}

}
