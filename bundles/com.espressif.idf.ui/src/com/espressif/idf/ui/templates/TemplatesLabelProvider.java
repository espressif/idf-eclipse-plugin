/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.templates;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class TemplatesLabelProvider extends LabelProvider
{

	private static final Image IMG_FOLDER = PlatformUI.getWorkbench().getSharedImages()
			.getImage(ISharedImages.IMG_OBJ_FOLDER);

	private static final Image IMG_OBJ_PROJECT = PlatformUI.getWorkbench().getSharedImages()
			.getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT);

	/*
	 * @see ILabelProvider#getImage(Object)
	 */
	@Override
	public Image getImage(Object element)
	{
		if (element instanceof ITemplateNode)
		{
			int type = ((ITemplateNode) element).getType();
			if (type == IResource.FOLDER)
			{
				return IMG_FOLDER;
			}
			return IMG_OBJ_PROJECT;
		}
		return null;
	}

	/*
	 * @see ILabelProvider#getText(Object)
	 */
	@Override
	public String getText(Object element)
	{
		if (element instanceof ITemplateNode)
		{
			return ((ITemplateNode) element).getName();
		}

		return null;
	}

}
