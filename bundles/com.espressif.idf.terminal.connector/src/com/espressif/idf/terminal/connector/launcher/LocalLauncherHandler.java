/*******************************************************************************
 * Copyright (c) 2014, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.espressif.idf.terminal.connector.launcher;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate;
import org.eclipse.tm.terminal.view.ui.launcher.LauncherDelegateManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Local terminal launcher handler implementation.
 */
public class LocalLauncherHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the current selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		// If the selection is not a structured selection, check if there is an active
		// editor and get the path from the editor input
		if (!(selection instanceof IStructuredSelection)) {
			IEditorInput input = HandlerUtil.getActiveEditorInput(event);
			if (input instanceof IPathEditorInput) {
				IPath path = ((IPathEditorInput) input).getPath();
				if (path != null) {
					if (path.toFile().isFile())
						path = path.removeLastSegments(1);
					if (path.toFile().isDirectory() && path.toFile().canRead())
						selection = new StructuredSelection(path);
				}
			}
		}

		// Get all applicable launcher delegates for the current selection
		ILauncherDelegate[] delegates = LauncherDelegateManager.getInstance().getApplicableLauncherDelegates(selection);
		// Find the local terminal launcher delegate
		ILauncherDelegate delegate = null;
		for (ILauncherDelegate candidate : delegates) {
			if ("com.espressif.idf.terminal.connector.launcher.local".equals(candidate.getId())) { //$NON-NLS-1$
				delegate = candidate;
				break;
			}
		}

		// Launch the local terminal
		if (delegate != null) {
			Map<String, Object> properties = new HashMap<>();
			properties.put(ITerminalsConnectorConstants.PROP_DELEGATE_ID, delegate.getId());
			properties.put(ITerminalsConnectorConstants.PROP_SELECTION, selection);

			delegate.execute(properties, null);
		}

		return null;
	}

}
