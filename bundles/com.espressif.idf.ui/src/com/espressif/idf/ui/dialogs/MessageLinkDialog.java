/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Lars Vogel <Lars.Vogel@vogella.com> - Bug 472690
 *******************************************************************************/
package com.espressif.idf.ui.dialogs;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.prefs.BackingStoreException;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.Messages;

public class MessageLinkDialog extends MessageDialog
{
	private static final String DO_NOT_SHOW_MSG = "DO_NOT_SHOW_MSG"; //$NON-NLS-1$
	private static IEclipsePreferences preferences;

	public MessageLinkDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage,
			int dialogImageType, int defaultIndex, String[] dialogButtonLabels)
	{
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, defaultIndex,
				dialogButtonLabels);
	}
	
	@Override
	protected Control createMessageArea(Composite composite)
	{
	    Image image = getImage();
		if (image != null)
		{
			imageLabel = new Label(composite, SWT.NULL);
			image.setBackground(imageLabel.getBackground());
			imageLabel.setImage(image);
			GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.BEGINNING).applyTo(imageLabel);
	    }
		if (message != null)
		{
			Link link = new Link(composite, getMessageLabelStyle());
			link.setText(message);
			link.addListener(SWT.Selection, new Listener()
			{
				@Override
				public void handleEvent(Event event)
				{
					Program.launch(event.text);

				}
			});
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false)
					.hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT)
					.applyTo(link);
	    }
	    return composite;
	}

	@Override
	protected Control createCustomArea(Composite parent)
	{
		Button checkBox = new Button(parent, SWT.CHECK);
		checkBox.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				preferences.putBoolean(DO_NOT_SHOW_MSG, checkBox.getSelection());
				try
				{
					preferences.flush();
				}
				catch (BackingStoreException e1)
				{
					Logger.log(e1);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
			}

		});
		checkBox.setText(Messages.MsgLinkDialog_DoNotShowMsg);
		return parent;
	}

	public static void openWarning(Shell parent, String title, String message)
	{
		preferences = InstanceScope.INSTANCE.getNode("com.espressif.idf.core"); //$NON-NLS-1$
		if (!preferences.getBoolean(DO_NOT_SHOW_MSG, false))
		{
			open(WARNING, parent, title, message, SWT.NONE);
		}
	}

	public static boolean open(int kind, Shell parent, String title, String message, int style)
	{
		MessageLinkDialog dialog = new MessageLinkDialog(parent, title, null, message, kind, 0,
				new String[] { IDialogConstants.OK_LABEL });
		style &= SWT.SHEET;
		dialog.setShellStyle(dialog.getShellStyle() | style);
		return dialog.open() == 0;
	}
}
