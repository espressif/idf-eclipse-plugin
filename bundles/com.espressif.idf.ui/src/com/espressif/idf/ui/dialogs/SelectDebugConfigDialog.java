/*******************************************************************************
 * Copyright 2024-2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.dialogs;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.UIPlugin;

public class SelectDebugConfigDialog extends TitleAreaDialog
{

	private Combo descriptorsCombo;
	private final List<String> suitableConfiguratios;

	public SelectDebugConfigDialog(Shell parentShell, List<String> suitableConfiguratios)
	{
		super(parentShell);
		this.suitableConfiguratios = suitableConfiguratios;
	}

	@Override
	public void create()
	{
		super.create();
		setTitle(Messages.SelectDebugConfigDialog_Title);
		setMessage(Messages.SelectDebugConfigDialog_Text, IMessageProvider.INFORMATION);
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText(Messages.SelectDebugConfigDialog_Title);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		// create OK and Cancel buttons by default
		createButton(parent, IDialogConstants.OK_ID, "Debug", true); //$NON-NLS-1$
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);

		Label descriptorsLabel = new Label(container, SWT.NONE);
		descriptorsLabel.setText(Messages.SelectDebugConfigDialog_LableText);

		GridData comboLayoutData = new GridData();
		comboLayoutData.grabExcessHorizontalSpace = true;
		comboLayoutData.horizontalAlignment = GridData.FILL;
		comboLayoutData.horizontalSpan = 1;

		descriptorsCombo = new Combo(container, SWT.READ_ONLY);
		descriptorsCombo.setItems(suitableConfiguratios.toArray(new String[0]));
		descriptorsCombo.select(0);
		descriptorsCombo.setLayoutData(comboLayoutData);
		return super.createDialogArea(parent);
	}

	@Override
	protected void okPressed()
	{
		ILaunchBarManager launchBarManager = UIPlugin.getService(ILaunchBarManager.class);
		try
		{
			ILaunchDescriptor[] descriptors = launchBarManager.getLaunchDescriptors();
			Optional<ILaunchDescriptor> optDisc = Stream.of(descriptors)
					.filter(disc -> disc.getName().contentEquals(descriptorsCombo.getText())).findFirst();
			if (optDisc.isPresent())
			{
				launchBarManager.setActiveLaunchDescriptor(optDisc.get());
			}

		}
		catch (CoreException e)
		{
			Logger.log(e);
		}

		super.okPressed();
	}

}
