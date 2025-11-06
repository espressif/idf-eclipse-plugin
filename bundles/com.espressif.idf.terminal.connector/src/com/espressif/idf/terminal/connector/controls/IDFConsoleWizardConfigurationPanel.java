/*******************************************************************************
 * Copyright (c) 2012, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * Kondal Kolipaka <kkolipaka@espressif.com> - ESP-IDF Console implementation
 *******************************************************************************/
package com.espressif.idf.terminal.connector.controls;

import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanelContainer;
import org.eclipse.tm.terminal.view.ui.panels.AbstractExtendedConfigurationPanel;
import org.eclipse.ui.WorkbenchEncoding;

import com.espressif.idf.core.IDFProjectNature;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.EclipseUtil;

/**
 * IDF console wizard configuration panel implementation.
 */
public class IDFConsoleWizardConfigurationPanel extends AbstractExtendedConfigurationPanel {

	private Combo projectCombo;

	/**
	 * Constructor.
	 *
	 * @param container The configuration panel container or <code>null</code>.
	 */
	public IDFConsoleWizardConfigurationPanel(IConfigurationPanelContainer container) {
		super(container);
	}

	@Override
	public void setupPanel(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout());
		panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createProjectCombo(panel);
		// Create the encoding selection combo
		createEncodingUI(panel, false);

		// Set the default encoding:
		//     Default UTF-8 on Mac or Windows for Local, Preferences:Platform encoding otherwise
		if (Platform.OS_MACOSX.equals(Platform.getOS()) || Platform.OS_WIN32.equals(Platform.getOS())) {
			setEncoding("UTF-8"); //$NON-NLS-1$
		} else {
			String encoding = WorkbenchEncoding.getWorkbenchDefaultEncoding();
			if (encoding != null && !"".equals(encoding)) //$NON-NLS-1$
				setEncoding(encoding);
		}

		// Fill the rest of the panel with a label to be able to
		// set a height and width hint for the dialog
		Label label = new Label(panel, SWT.HORIZONTAL);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.widthHint = 300;
		layoutData.heightHint = 80;
		label.setLayoutData(layoutData);

		setControl(panel);
	}

	private void createProjectCombo(Composite parent) {

		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label projectLabel = new Label(panel, SWT.NONE);
		projectLabel.setText("Project name:");

		projectCombo = new Combo(panel, SWT.READ_ONLY);
		projectCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			try {
				if (project.hasNature(IDFProjectNature.ID)) {
					projectCombo.add(project.getName());
				}
			} catch (CoreException e) {
				Logger.log(e);
			}
		}

		Optional<IProject> optProject = Optional.ofNullable(EclipseUtil.getSelectedIDFProjectInExplorer());
		optProject.ifPresentOrElse(project -> projectCombo.setText(project.getName()), () -> projectCombo.select(0));
	}

	@Override
	public void setupData(Map<String, Object> data) {
		if (data == null)
			return;

		String value = (String) data.get(ITerminalsConnectorConstants.PROP_ENCODING);
		if (value != null)
			setEncoding(value);
	}

	@Override
	public void extractData(Map<String, Object> data) {

		data.put(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID,
				"com.espressif.idf.terminal.connector.espidfConnector"); //$NON-NLS-1$

		data.put(ITerminalsConnectorConstants.PROP_ENCODING, getEncoding());

		// <-- this is the important part
		if (projectCombo != null && !projectCombo.isDisposed()) {
			IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(projectCombo.getText());
			if (p != null && p.exists() && p.getLocation() != null) {
				data.put(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR, p.getLocation().toOSString());
				data.put(ITerminalsConnectorConstants.PROP_TITLE, p.getName());
			}
		}
	}

	@Override
	protected void fillSettingsForHost(String host) {
	}

	@Override
	protected void saveSettingsForHost(boolean add) {
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
		// Save the encodings widget values
		doSaveEncodingsWidgetValues(settings, idPrefix);
	}

	@Override
	public void doRestoreWidgetValues(IDialogSettings settings, String idPrefix) {
		// Restore the encodings widget values
		doRestoreEncodingsWidgetValues(settings, idPrefix);
	}

	@Override
	protected String getHostFromSettings() {
		return null;
	}

	@Override
	public boolean isWithHostList() {
		return false;
	}
}
