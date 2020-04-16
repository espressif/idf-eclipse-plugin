/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class EclipseUtil
{

	/**
	 * Returns the IResource from the current selection
	 *
	 * @return the IResource, or <code>null</code>.
	 */
	public static IResource getSelectionResource()
	{
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		ISelection selection = selectionService != null ? selectionService.getSelection() : StructuredSelection.EMPTY;

		if (selection instanceof IStructuredSelection && !selection.isEmpty())
		{
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof org.eclipse.core.resources.IResource)
			{
				return ((IResource) element);
			}
			if (element instanceof IAdaptable)
			{
				return ((IAdaptable) element).getAdapter(IResource.class);
			}
		}
		return null;
	}

}
