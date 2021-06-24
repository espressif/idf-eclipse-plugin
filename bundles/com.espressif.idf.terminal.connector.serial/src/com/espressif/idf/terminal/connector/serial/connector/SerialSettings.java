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
package com.espressif.idf.terminal.connector.serial.connector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;

import com.espressif.idf.core.util.StringUtil;

public class SerialSettings {

	public static final String PORT_NAME_ATTR = "cdtserial.portName"; //$NON-NLS-1$
	public static final String MONITOR_FILTER = "idf.monitor.filter"; //$NON-NLS-1$
	public static final String SELECTED_PROJECT_ATTR = "idf.monitor.project"; //$NON-NLS-1$
	public static final String NUMBER_OF_COLS = "idf.monitor.cols"; //$NON-NLS-1$
	public static final String NUMBER_OF_ROWS = "idf.monitor.rows"; //$NON-NLS-1$

	private String portName;
	private String filterText;
	private String selectedProject;
	private String numberOfCols;
	private String numberOfRows;

	/**
	 * Load information into the RemoteSettings object.
	 */
	public void load(ISettingsStore store) {
		portName = store.get(PORT_NAME_ATTR, ""); //$NON-NLS-1$
		filterText = store.get(MONITOR_FILTER, ""); //$NON-NLS-1$
		selectedProject = store.get(SELECTED_PROJECT_ATTR, ""); //$NON-NLS-1$
		numberOfCols = store.get(NUMBER_OF_COLS, ""); //$NON-NLS-1$
		numberOfRows = store.get(NUMBER_OF_ROWS, ""); //$NON-NLS-1$
	}

	/**
	 * Extract information from the RemoteSettings object.
	 */
	public void save(ISettingsStore store) {
		store.put(PORT_NAME_ATTR, portName);
		store.put(MONITOR_FILTER, filterText);
		store.put(SELECTED_PROJECT_ATTR, selectedProject);
		store.put(NUMBER_OF_COLS, numberOfCols);
		store.put(NUMBER_OF_ROWS, numberOfRows);
	}

	public String getPortName() {
		return portName;
	}

	public String getFilterText() {
		return filterText;
	}

	public String getProjectName() {
		return selectedProject;
	}

	public IProject getProject() {

		return !StringUtil.isEmpty(selectedProject)
				? ResourcesPlugin.getWorkspace().getRoot().getProject(selectedProject)
				: null;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public void setFilterText(String filterText) {
		this.filterText = filterText;
	}

	public String getSummary() {
		return portName;
	}

	public void setProject(String projectName) {
		this.selectedProject = projectName;
	}

	public String getNumberOfCols() {
		return numberOfCols;
	}

	public void setNumberOfCols(String numberOfCols) {
		this.numberOfCols = numberOfCols;
	}

	public String getNumberOfRows() {
		return numberOfRows;
	}

	public void setNumberOfRows(String numberOfRows) {
		this.numberOfRows = numberOfRows;
	}

}
