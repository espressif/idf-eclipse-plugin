/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
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
	private LinkedHashSet<String> allColumns = new LinkedHashSet<>();
	private LinkedHashSet<String> visibleColumns = new LinkedHashSet<>();
	private IFile currentFile;
	private Composite treeContainer;

	public void createPartControl(Composite parent, IFile iFile)
	{
		this.currentFile = iFile;

		if (treeContainer != null && !treeContainer.isDisposed())
		{
			treeContainer.dispose(); // remove old container if rebuilding
		}

		treeContainer = new Composite(parent, SWT.NONE);
		treeContainer.setLayout(new GridLayout(1, false));
		treeContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Button columnSelectBtn = new Button(treeContainer, SWT.PUSH);
		columnSelectBtn.setText("Select Columns...");
		columnSelectBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		columnSelectBtn.addListener(SWT.Selection, e -> openColumnSelectionDialog());

		List<Library> dataList = new ArrayList<>();
		try
		{
			dataList = new IDFSizeDataManager().getDataList(currentFile);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}

		LinkedHashSet<String> columnList = new LinkedHashSet<>();
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

		if (allColumns.isEmpty())
		{
			allColumns.addAll(columnList);
			visibleColumns.addAll(columnList);
		}

		PatternFilter patternFilter = new IDFSizePatternFilter();
		FilteredTree filteredTree = new FilteredTree(treeContainer, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER,
				patternFilter, true, true);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		filteredTree.setLayoutData(gridData);

		treeViewer = filteredTree.getViewer();
		Tree tree = treeViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		IDFSizeComparator comparator = new IDFSizeComparator();
		int i = 0;
		for (String column : visibleColumns)
		{
			TreeColumn tc = new TreeColumn(tree, SWT.NONE, i);
			tc.setText(column);
			tc.addSelectionListener(new ResortColumn(comparator, tc, treeViewer, i++));
			tc.pack();
		}

		if (tree.getColumnCount() > 0)
		{
			tree.setSortColumn(tree.getColumn(0));
			tree.setSortDirection(SWT.UP);
		}

		treeViewer.setContentProvider(new IDFSizeDataContentProvider());
		treeViewer.setLabelProvider(new IDFSizeDataLabelProvider(visibleColumns));
		treeViewer.setInput(dataList);
		treeViewer.setComparator(comparator);

		treeContainer.layout(true, true);
		parent.layout(true, true);
	}

	private void openColumnSelectionDialog()
	{
		Shell shell = new Shell(treeContainer.getShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText("Choose Columns");
		shell.setLayout(new GridLayout(1, false));

		List<Button> checkboxes = new ArrayList<>();
		for (String col : allColumns)
		{
			Button checkbox = new Button(shell, SWT.CHECK);
			checkbox.setText(col);
			checkbox.setSelection(visibleColumns.contains(col));
			checkboxes.add(checkbox);
		}

		Button applyBtn = new Button(shell, SWT.PUSH);
		applyBtn.setText("Apply");
		applyBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		applyBtn.addListener(SWT.Selection, e -> {
			visibleColumns.clear();
			for (Button cb : checkboxes)
			{
				if (cb.getSelection())
				{
					visibleColumns.add(cb.getText());
				}
			}
			shell.close();
			rebuildTree(); // ✅ safely rebuild
		});

		shell.pack();
		shell.open();
	}

	private void rebuildTree()
	{
		if (treeContainer != null && !treeContainer.isDisposed())
		{
			Composite parent = treeContainer.getParent(); // Store before disposal
			treeContainer.dispose();
			createPartControl(parent, currentFile); // Recreate safely
			parent.layout(true, true);
		}
	}

	public void setFocus()
	{
		if (treeViewer != null)
		{
			treeViewer.getControl().setFocus();
		}
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