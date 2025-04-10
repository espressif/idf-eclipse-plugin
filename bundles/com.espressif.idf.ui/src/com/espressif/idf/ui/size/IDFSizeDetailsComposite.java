/*******************************************************************************
 * Copyright 2018-2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.size.vo.Library;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>, Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class IDFSizeDetailsComposite
{
	private TreeViewer treeViewer;
	
	public void createPartControl(Composite parent, IFile iFile)
	{
		List<Library> dataList = new ArrayList<>();
		try
		{
			dataList = new IDFSizeDataManager().getDataList(iFile);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		
		LinkedHashSet<String> columnList = new LinkedHashSet<String>();
		columnList.add("File Name"); //$NON-NLS-1$
		for (Library library : dataList)
		{
			for (String memoryType : library.getMemoryTypes().keySet())
			{
				for (String memorySection : library.getMemoryTypes().get(memoryType).getSections().keySet())
				{
					columnList.add(memoryType + " -> " + memorySection); //$NON-NLS-1$
				}
				columnList.add(memoryType + " Total"); //$NON-NLS-1$
			}
		}
		columnList.add("Total"); //$NON-NLS-1$
		
		
		PatternFilter patternFilter = new IDFSizePatternFilter();
		FilteredTree filteredTree = new FilteredTree(parent, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER,
				patternFilter, true, true);

		final GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		filteredTree.setLayout(layout);

		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		filteredTree.setLayoutData(gridData);

		final TreeViewer treeViewer = filteredTree.getViewer();
		Tree tree = treeViewer.getTree();

		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		treeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

		IDFSizeComparator comparator = new IDFSizeComparator();

		int i = 0;
		for (String column : columnList)
		{
			TreeColumn tc = new TreeColumn(tree, SWT.NONE, i);
			tc.setText(column);
			tc.setWidth(column.length() * 8);
			tc.addSelectionListener(new ResortColumn(comparator, tc, treeViewer, i++));
		}

		final TreeColumn[] columns = treeViewer.getTree().getColumns();
		columns[0].setWidth(200);
		tree.setSortColumn(columns[0]);
		tree.setSortDirection(comparator.isAscending() ? SWT.UP : SWT.DOWN);

		

		treeViewer.setContentProvider(new IDFSizeDataContentProvider());
		treeViewer.setLabelProvider(new IDFSizeDataLabelProvider(columnList));
		treeViewer.setInput(dataList);
		treeViewer.setComparator(comparator);
	}

	public void setFocus()
	{
		treeViewer.getControl().setFocus();
	}

	private final class ResortColumn extends SelectionAdapter
	{
		private final IDFSizeComparator comparator;
		private final TreeColumn treeColumn;
		private final TreeViewer viewer;
		private final int column;

		private ResortColumn(IDFSizeComparator comparator, TreeColumn treeColumn, TreeViewer viewer, int column)
		{
			this.comparator = comparator;
			this.treeColumn = treeColumn;
			this.viewer = viewer;
			this.column = column;
		}

		@Override
		public void widgetSelected(SelectionEvent e)
		{
			if (comparator.getSortColumn() == column)
			{
				comparator.setAscending(!comparator.isAscending());
				viewer.getTree().setSortDirection(comparator.isAscending() ? SWT.UP : SWT.DOWN);
			}
			else
			{
				viewer.getTree().setSortColumn(treeColumn);
				comparator.setSortColumn(column);
			}
			try
			{
				viewer.refresh();
			} finally
			{
				viewer.getTree().setRedraw(true);
			}
		}
	}
}
