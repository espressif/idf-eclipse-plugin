/*******************************************************************************
 * Copyright 2018-2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size;

import java.util.ArrayList;
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

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFSizeDetailsComposite
{
	private TreeViewer treeViewer;
	private String columnProperties[] = new String[] { "File Name", "DRAM .data", "DRAM .bss", "DIRAM", "IRAM", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"Flash Code", "Flash rodata", "Other", "Total" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	
	public void createPartControl(Composite parent, IFile iFile)
	{
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
		if (IDFSizeConstants.OTHER != "other") { //$NON-NLS-1$ 
			columnProperties[7] = "Ram Total"; //$NON-NLS-1$ 
			columnProperties[8] = "Flash Total"; //$NON-NLS-1$ 
		}
		for (int i = 0; i < columnProperties.length; i++)
		{
			TreeColumn tc = new TreeColumn(tree, SWT.NONE, i);
			tc.setText(columnProperties[i]);
			tc.setWidth(90);
			tc.addSelectionListener(new ResortColumn(comparator, tc, treeViewer, i));
		}

		final TreeColumn[] columns = treeViewer.getTree().getColumns();
		columns[0].setWidth(200);
		tree.setSortColumn(columns[0]);
		tree.setSortDirection(comparator.isAscending() ? SWT.UP : SWT.DOWN);

		List<IDFSizeData> dataList = new ArrayList<>();
		try
		{
			dataList = new IDFSizeDataManager().getDataList(iFile);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}

		treeViewer.setContentProvider(new IDFSizeDataContentProvider());
		treeViewer.setLabelProvider(new IDFSizeDataLabelProvider());
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
