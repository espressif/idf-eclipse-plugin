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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanelContainer;
import org.eclipse.tm.terminal.view.ui.panels.AbstractExtendedConfigurationPanel;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchEncoding;
import org.osgi.framework.Bundle;

/**
 * IDF console wizard configuration panel implementation.
 */
public class IDFConsoleWizardConfigurationPanel extends AbstractExtendedConfigurationPanel {

	private Object resource;

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

		Bundle bundle = Platform.getBundle("org.eclipse.core.resources"); //$NON-NLS-1$
		if (bundle != null && bundle.getState() != Bundle.UNINSTALLED && bundle.getState() != Bundle.STOPPING) {
			resource = getSelectionResource();
		}

		setControl(panel);
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
		// set the terminal connector id for local terminal
		data.put(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID,
				"com.espressif.idf.terminal.connector.espidfConnector"); //$NON-NLS-1$

		// Store the encoding
		data.put(ITerminalsConnectorConstants.PROP_ENCODING, getEncoding());

		Bundle bundle = Platform.getBundle("org.eclipse.core.resources"); //$NON-NLS-1$
		if (bundle != null && bundle.getState() != Bundle.UNINSTALLED && bundle.getState() != Bundle.STOPPING) {
			// if we have a IResource selection use the location for working directory
			if (resource instanceof org.eclipse.core.resources.IResource) {
				String dir = ((org.eclipse.core.resources.IResource) resource).getProject().getLocation().toString();
				data.put(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR, dir);
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

	/**
	 * Returns the IResource from the current selection
	 *
	 * @return the IResource, or <code>null</code>.
	 */
	private org.eclipse.core.resources.IResource getSelectionResource() {
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		ISelection selection = selectionService != null ? selectionService.getSelection() : StructuredSelection.EMPTY;

		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof org.eclipse.core.resources.IResource) {
				return ((org.eclipse.core.resources.IResource) element);
			}
			if (element instanceof IAdaptable) {
				return ((IAdaptable) element).getAdapter(org.eclipse.core.resources.IResource.class);
			}
		}
		return null;
	}
}
