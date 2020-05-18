/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFSizeDataLabelProvider extends LabelProvider implements ITableLabelProvider, ILabelProvider
{

	public Image getColumnImage(Object element, int columnIndex)
	{
		return null;
	}

	public String getColumnText(Object element, int columnIndex)
	{
		IDFSizeData data = (IDFSizeData) element;
		switch (columnIndex)
		{
		case 0:
			return data.getName();
		case 1:
			return String.valueOf(data.getData());
		case 2:
			return String.valueOf(data.getBss());
		case 3:
			return String.valueOf(data.getDiram());
		case 4:
			return String.valueOf(data.getIram());
		case 5:
			return String.valueOf(data.getFlash_text());
		case 6:
			return String.valueOf(data.getFlash_rodata());
		case 7:
			return String.valueOf(data.getOther());
		case 8:
			return String.valueOf(data.getTotal());
		}
		return null;
	}

	public void addListener(ILabelProviderListener listener)
	{
	}

	public void dispose()
	{
	}

	public boolean isLabelProperty(Object element, String property)
	{
		return false;
	}

	public void removeListener(ILabelProviderListener listener)
	{
	}
}
