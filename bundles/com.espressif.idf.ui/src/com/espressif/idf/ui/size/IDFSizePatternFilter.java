/*******************************************************************************
 * Copyright 2018-2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.PatternFilter;

import com.espressif.idf.ui.size.vo.LibraryMemoryComponent;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>, Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class IDFSizePatternFilter extends PatternFilter {

	@Override
	public boolean isElementSelectable(Object element) {
		return element instanceof LibraryMemoryComponent;
	}

	@Override
	protected boolean isLeafMatch(Viewer viewer, Object element) {

		if (element instanceof LibraryMemoryComponent) {
			LibraryMemoryComponent desc = (LibraryMemoryComponent) element;
			return wordMatches(desc.getAbbrevName()) || wordMatches(desc.getName());
		}

		return false;
	}
}
