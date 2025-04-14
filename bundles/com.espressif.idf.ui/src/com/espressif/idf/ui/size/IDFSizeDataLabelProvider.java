/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size;

import java.util.LinkedHashSet;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.espressif.idf.ui.size.vo.Library;

/**
 * Dynamically provides labels for each visible column.
 * Handles memory sections, memory totals, file name, and total size.
 */
public class IDFSizeDataLabelProvider extends LabelProvider implements ITableLabelProvider {

	private final String[] columns;

	public IDFSizeDataLabelProvider(LinkedHashSet<String> columns) {
		this.columns = columns.toArray(new String[0]);
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		Library data = (Library) element;

		if (columnIndex >= columns.length)
			return null;

		String columnName = columns[columnIndex];

		if (columnName.equals("File Name")) { //$NON-NLS-1$
			return data.getName();
		}

		if (columnName.equals("Total")) { //$NON-NLS-1$
			return String.valueOf(data.getSize());
		}

		if (columnName.contains("->")) { //$NON-NLS-1$
			// memoryType -> section
			String[] split = columnName.split(" -> ");
			if (split.length == 2) {
				String memoryType = split[0];
				String memorySection = split[1];

				try {
					return String.valueOf(
						data.getMemoryTypes().get(memoryType).getSections().get(memorySection).getSize()
					);
				} catch (Exception e) {
					// section not found
					return "0";
				}
			}
		} else if (columnName.endsWith(" Total")) { //$NON-NLS-1$
			String memoryType = columnName.replace(" Total", "");
			try {
				return String.valueOf(data.getMemoryTypes().get(memoryType).getSize());
			} catch (Exception e) {
				// memoryType not found
				return "0";
			}
		}

		return ""; // default fallback
	}
}
