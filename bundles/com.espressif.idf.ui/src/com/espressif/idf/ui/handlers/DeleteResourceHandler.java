/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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
 ******************************************************************************/
package com.espressif.idf.ui.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.actions.AbstractResourcesHandler;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.dialogs.DeleteResourceWizard;

@SuppressWarnings("restriction")
public class DeleteResourceHandler extends AbstractResourcesHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell activeShell= HandlerUtil.getActiveShell(event);
		ISelection sel= HandlerUtil.getCurrentSelection(event);
		if (sel instanceof IStructuredSelection) {
			IResource[] resources= getSelectedResources((IStructuredSelection) sel);
			if (resources.length > 0) {
				DeleteResourceWizard refactoringWizard= new DeleteResourceWizard(resources);
				RefactoringWizardOpenOperation op= new RefactoringWizardOpenOperation(refactoringWizard);
				try {
					op.run(activeShell, RefactoringUIMessages.DeleteResourcesHandler_title);
				} catch (InterruptedException e) {
					Logger.log(e);
				}
			}
		}
		return null;
	}
}
