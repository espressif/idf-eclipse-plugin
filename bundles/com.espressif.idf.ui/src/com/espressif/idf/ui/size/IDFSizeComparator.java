/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.size;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFSizeComparator extends ViewerComparator
{

	private LinkedList<Integer> sortColumns = new LinkedList<>();
	private boolean ascending = true;

	public IDFSizeComparator()
	{
		for (int i = 0; i < 9; i++)
		{ // no.of columns
			sortColumns.add(Integer.valueOf(i));
		}
	}

	public int getSortColumn()
	{
		return sortColumns.getFirst().intValue();
	}

	public void setSortColumn(int column)
	{
		if (column == getSortColumn())
		{
			return;
		}
		Integer sortColumn = Integer.valueOf(column);
		sortColumns.remove(sortColumn);
		sortColumns.addFirst(sortColumn);
	}

	/**
	 * @return Returns the ascending.
	 */
	public boolean isAscending()
	{
		return ascending;
	}

	/**
	 * @param ascending The ascending to set.
	 */
	public void setAscending(boolean ascending)
	{
		this.ascending = ascending;
	}

	@Override
	public int compare(final Viewer viewer, final Object a, final Object b)
	{
		int result = 0;
		Iterator<Integer> i = sortColumns.iterator();
		while (i.hasNext() && result == 0)
		{
			int column = i.next().intValue();
			result = compareColumn(viewer, a, b, column);
		}
		return ascending ? result : (-1) * result;
	}

	private int compareColumn(final Viewer viewer, final Object a, final Object b, final int columnNumber)
	{

		IBaseLabelProvider baseLabel = ((TreeViewer) viewer).getLabelProvider();
		if (baseLabel instanceof ITableLabelProvider)
		{
			ITableLabelProvider tableProvider = (ITableLabelProvider) baseLabel;
			String e1p = tableProvider.getColumnText(a, columnNumber);
			String e2p = tableProvider.getColumnText(b, columnNumber);
			if (e1p != null && e2p != null)
			{
				if (columnNumber == 0)
				{
					return getComparator().compare(e1p, e2p);
				}
				else
				{
					return (int) (Long.valueOf(e1p) - Long.valueOf(e2p));
				}

			}
		}
		return 0;
	}

}