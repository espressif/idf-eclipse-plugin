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

public class SerialSettings
{

	public static final String PORT_NAME_ATTR = "cdtserial.portName"; //$NON-NLS-1$
	public static final String MONITOR_FILTER = "idf.monitor.filter"; //$NON-NLS-1$
	public static final String SELECTED_PROJECT_ATTR = "idf.monitor.project"; //$NON-NLS-1$
	public static final String ENCRYPTION_ATTR = "idf.monitor.extraOptions"; //$NON-NLS-1$

	private String portName;
	private String filterText;
	private String selectedProject;
	private boolean encryptionOption;

	/**
	 * Load information into the RemoteSettings object.
	 */
	public void load(ISettingsStore store)
	{
		portName = store.get(PORT_NAME_ATTR, ""); //$NON-NLS-1$
		filterText = store.get(MONITOR_FILTER, ""); //$NON-NLS-1$
		selectedProject = store.get(SELECTED_PROJECT_ATTR, ""); //$NON-NLS-1$
		encryptionOption = Boolean.parseBoolean(store.get(ENCRYPTION_ATTR, "false")); //$NON-NLS-1$
	}

	/**
	 * Extract information from the RemoteSettings object.
	 */
	public void save(ISettingsStore store)
	{
		store.put(PORT_NAME_ATTR, portName);
		store.put(MONITOR_FILTER, filterText);
		store.put(SELECTED_PROJECT_ATTR, selectedProject);
		store.put(ENCRYPTION_ATTR, Boolean.toString(encryptionOption));
	}

	public String getPortName()
	{
		return portName;
	}

	public String getFilterText()
	{
		return filterText;
	}

	public String getProjectName()
	{
		return selectedProject;
	}

	public boolean getEncryptionOption()
	{
		return encryptionOption;
	}

	public IProject getProject()
	{

		return !StringUtil.isEmpty(selectedProject)
				? ResourcesPlugin.getWorkspace().getRoot().getProject(selectedProject)
				: null;
	}

	public void setPortName(String portName)
	{
		this.portName = portName;
	}

	public void setFilterText(String filterText)
	{
		this.filterText = filterText;
	}

	public String getSummary()
	{
		return portName;
	}

	public void setProject(String projectName)
	{
		this.selectedProject = projectName;
	}

	public void setEncryptionOption(boolean encryptionOption)
	{
		this.encryptionOption = encryptionOption;
	}

}
