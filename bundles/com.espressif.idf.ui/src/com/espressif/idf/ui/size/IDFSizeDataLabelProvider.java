/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size;

import java.util.LinkedHashSet;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.espressif.idf.ui.size.vo.Library;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class IDFSizeDataLabelProvider extends LabelProvider implements ITableLabelProvider, ILabelProvider
{
	private String[] columns;
	
	public IDFSizeDataLabelProvider(LinkedHashSet<String> columns)
	{
		this.columns = new String[columns.size()];
		int i = 0;
		for (String columnName : columns)
		{
			this.columns[i] = columnName;
			i++;
		}
	}
	
	public Image getColumnImage(Object element, int columnIndex)
	{
		return null;
	}

	public String getColumnText(Object element, int columnIndex)
	{
		Library data = (Library) element;
		int totalColumns = columns.length;
		if (columnIndex == 0)
		{
			return data.getName();
		}
		
		if (columnIndex == (totalColumns - 1))
		{
			return String.valueOf(data.getSize());
		}
		
		String columnName = columns[columnIndex];
		
		if (columnName.contains("->")) //$NON-NLS-1$
		{
			// memoryType + " -> " + memorySection
			String [] split = columnName.split(" -> ");
			String memoryType = split[0];
			String memorySection = split[1];
			
			long value = data.getMemoryTypes().get(memoryType).getSections().get(memorySection).getSize();
			return String.valueOf(value);
		}
		else if (columnName.contains(" Total")) //$NON-NLS-1$
		{
			// memoryType + " Total"
			String memoryType = columnName.substring(0, columnName.length() - " Total".length()); //$NON-NLS-1$
			long value = data.getMemoryTypes().get(memoryType).getSize();
			return String.valueOf(value);
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
