/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.nvs.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.build.NvsTableBean;
import com.espressif.idf.core.util.NvsBeanValidator;

public class NvsTableEditorLabelProvider extends CellLabelProvider implements ITableLabelProvider, ITableColorProvider
{

	@Override
	public Color getForeground(Object element, int columnIndex)
	{
		String status = new NvsBeanValidator().validateBean((NvsTableBean) element, columnIndex);
		if (!status.isBlank())
		{
			return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
		}

		return null;
	}

	@Override
	public Color getBackground(Object element, int columnIndex)
	{
		return null;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex)
	{
		String status = new NvsBeanValidator().validateBean((NvsTableBean) element, columnIndex);
		if (!status.isBlank())
		{
			return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		NvsTableBean bean = (NvsTableBean) element;
		switch (columnIndex)
		{
		case 0:
			return bean.getKey();
		case 1:
			return bean.getType();
		case 2:
			return bean.getEncoding();
		case 3:
			return bean.getValue();
		default:
			break;
		}
		return null;
	}

	@Override
	public void update(ViewerCell cell)
	{
	}

}
