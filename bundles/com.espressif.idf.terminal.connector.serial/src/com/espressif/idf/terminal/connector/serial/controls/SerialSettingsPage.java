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
 *     Espressif systems - IDF Monitor integration
 *******************************************************************************/
package com.espressif.idf.terminal.connector.serial.controls;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.internal.terminal.provisional.api.AbstractSettingsPage;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanel;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanelContainer;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.IDFProjectNature;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.terminal.connector.serial.activator.Activator;
import com.espressif.idf.terminal.connector.serial.connector.SerialConnector;
import com.espressif.idf.terminal.connector.serial.connector.SerialSettings;
import com.espressif.idf.terminal.connector.serial.nls.Messages;
import com.espressif.idf.ui.EclipseUtil;

public class SerialSettingsPage extends AbstractSettingsPage
{

	private final SerialSettings settings;
	private final IConfigurationPanel panel;
	private final IDialogSettings dialogSettings;

	private Combo portCombo;
	private Combo projectCombo;

	private String portName;
	private String lastUsedSerialPort;
	private Text filterText;
	private String filterConfig;

	public SerialSettingsPage(SerialSettings settings, IConfigurationPanel panel)
	{
		this.settings = settings;
		this.panel = panel;
		setHasControlDecoration(true);

		dialogSettings = DialogSettings.getOrCreateSection(Activator.getDefault().getDialogSettings(),
				this.getClass().getSimpleName());
		portName = dialogSettings.get(SerialSettings.PORT_NAME_ATTR);
		filterConfig = dialogSettings.get(SerialSettings.MONITOR_FILTER);

		lastUsedSerialPort = getLastUsedSerialPort();

	}

	protected String getLastUsedSerialPort()
	{
		Preferences preferences = InstanceScope.INSTANCE.getNode("com.espressif.idf.launch.serial.ui"); //$NON-NLS-1$
		return preferences.get("com.espressif.idf.launch.serial.core.serialPort", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void createControl(Composite parent)
	{
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayout(gridLayout);
		comp.setLayoutData(gridData);

		Label projectLabel = new Label(comp, SWT.NONE);
		projectLabel.setText(Messages.SerialSettingsPage_ProjectName);

		projectCombo = new Combo(comp, SWT.NONE);
		projectCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Optional<IProject> optProject = Optional.ofNullable(EclipseUtil.getSelectedIDFProjectInExplorer());
		optProject.ifPresent(project -> projectCombo.setText(project.getName()));
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject project : projects)
		{
			try
			{
				if (project.hasNature(IDFProjectNature.ID))
				{
					projectCombo.add(project.getName());
				}
			}
			catch (CoreException e1)
			{
				Logger.log(e1);
			}

		}

		Label portLabel = new Label(comp, SWT.NONE);
		portLabel.setText(Messages.SerialTerminalSettingsPage_SerialPort);

		portCombo = new Combo(comp, SWT.NONE);
		portCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		String[] portNames = new String[0];
		try
		{
			portNames = SerialPort.list();
		}
		catch (IOException e)
		{
			Activator.log(e);
		}
		for (String portName : portNames)
		{
			if (!SerialConnector.isOpen(portName))
			{
				portCombo.add(portName);
			}
		}
		portCombo.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				validate();
			}
		});

		Label configOptions = new Label(comp, SWT.NONE);
		configOptions.setText(Messages.SerialSettingsPage_FilterOptions);

		filterText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		loadSettings();
	}

	void validate()
	{
		IConfigurationPanelContainer container = panel.getContainer();
		container.validate();
	}

	@Override
	public void loadSettings()
	{
		String portName = settings.getPortName();
		if (portName == null || portName.isEmpty())
		{
			portName = this.lastUsedSerialPort;
		}
		if (portName != null && !portName.isEmpty() && !SerialConnector.isOpen(portName))
		{
			int i = 0;
			for (String name : portCombo.getItems())
			{
				if (portName.equals(name))
				{
					portCombo.select(i);
					break;
				}
				i++;
			}
		}
		else if (portCombo.getItemCount() > 0)
		{
			portCombo.select(0);
		}

		// IDF monitor filter
		if (!StringUtil.isEmpty(filterConfig))
		{
			this.filterText.setText(filterConfig);
		}
	}

	@Override
	public void saveSettings()
	{
		settings.setPortName(portCombo.getText());
		settings.setFilterText(filterText.getText().trim());
		settings.setProject(projectCombo.getText());

		dialogSettings.put(SerialSettings.SELECTED_PROJECT_ATTR, projectCombo.getText());
		dialogSettings.put(SerialSettings.PORT_NAME_ATTR, portCombo.getText());
		dialogSettings.put(SerialSettings.MONITOR_FILTER, filterText.getText().trim());
	}

	@Override
	public boolean validateSettings()
	{
		if (portCombo.getSelectionIndex() < 0 && portCombo.getText().isEmpty())
		{
			return false;
		}
		return true;
	}

}
