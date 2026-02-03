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
 * Kondal Kolipaka <kkolipaka@espressif.com> - ESP-IDF Console implementationS
 *******************************************************************************/
package com.espressif.idf.terminal.connector.launcher;

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.terminal.view.ui.IMementoHandler;
import org.eclipse.ui.IMemento;

/**
 * Local terminal connection memento handler implementation.
 */
public class IDFConsoleMementoHandler implements IMementoHandler {

	private static final String TERMINALS_VIEW_ID = "org.eclipse.terminal.view.ui.TerminalsView"; //$NON-NLS-1$
	private static final String PROPERTY_ID = "id"; //$NON-NLS-1$

	@Override
	public void saveState(IMemento memento, Map<String, Object> properties) {
		Assert.isNotNull(memento);
		Assert.isNotNull(properties);
	}


	@Override
	public void restoreState(IMemento memento, Map<String, Object> properties) {
		Assert.isNotNull(memento);
		Assert.isNotNull(properties);

		Object terminalId = properties.get(PROPERTY_ID);

		Assert.isTrue(TERMINALS_VIEW_ID.equals(terminalId), Messages.IDFConsoleMementoHandler_TerminalNameOutdatedMsg);
	}
}
