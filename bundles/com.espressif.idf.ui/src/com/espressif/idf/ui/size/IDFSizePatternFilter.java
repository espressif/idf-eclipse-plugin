/*******************************************************************************
 * Copyright 2018-2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFSizePatternFilter extends PatternFilter {

	@Override
	public boolean isElementSelectable(Object element) {
		return element instanceof IDFSizeData;
	}

	@Override
	protected boolean isLeafMatch(Viewer viewer, Object element) {

		if (element instanceof IDFSizeData) {
			IDFSizeData desc = (IDFSizeData) element;
			return wordMatches(desc.getName());
		}

		return false;
	}
}
