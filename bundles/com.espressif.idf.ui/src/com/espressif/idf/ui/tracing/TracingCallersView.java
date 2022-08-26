/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.tracing;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;

/**
 * Tracing callers view to show the information for the callers
 * 
 * @author Ali Azam Rana
 *
 */
public class TracingCallersView extends ViewPart
{
	public static final String ID = "com.espressif.idf.ui.views.tracingcallersview";
	private List<AddressInfoVO> addressInfoVOs;
	private Tree tree;

	public TracingCallersView()
	{
	}

	@Override
	public void createPartControl(Composite parent)
	{
		parent.setLayout(new FillLayout());
		tree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		tree.setHeaderVisible(true);
		tree.addSelectionListener(new ItemSelectionListener());
		tree.setLinesVisible(true);

		createTreeColumns();

		refreshTable();
	}

	private void createTreeColumns()
	{
		TreeColumn colFileName = new TreeColumn(tree, SWT.LEFT);
		colFileName.setWidth(200);
		colFileName.setText(Messages.TracingCallerView_ColFileName);
		TreeColumn colFunctionName = new TreeColumn(tree, SWT.LEFT);
		colFunctionName.setWidth(200);
		colFunctionName.setText(Messages.TracingCallerView_ColFunctionName);
		TreeColumn colLineNumber = new TreeColumn(tree, SWT.LEFT);
		colLineNumber.setWidth(100);
		colLineNumber.setText(Messages.TracingCallerView_ColLineNumber);
		TreeColumn colAddress = new TreeColumn(tree, SWT.LEFT);
		colAddress.setText(Messages.TracingCallerView_ColAddress);
		colAddress.setWidth(100);
	}

	public void refreshTable()
	{
		if (addressInfoVOs == null || addressInfoVOs.isEmpty())
		{
			return;
		}

		tree.removeAll();
		tree.setRedraw(false);
		TreeItem mainTreeItem = new TreeItem(tree, SWT.NONE);

		AddressInfoVO mainAddressItem = addressInfoVOs.get(0);
		mainTreeItem.setData(mainAddressItem);
		mainTreeItem.setText(0, mainAddressItem.getFile().getName());
		mainTreeItem.setText(1, mainAddressItem.getFunctionName());
		mainTreeItem.setText(2, String.valueOf(mainAddressItem.getLineNumber()));
		mainTreeItem.setText(3, mainAddressItem.getAddress());
		mainTreeItem.setExpanded(true);

		for (int i = 1; i < addressInfoVOs.size(); i++)
		{
			TreeItem subTreeItem = new TreeItem(mainTreeItem, SWT.NONE);
			AddressInfoVO subAddressItem = addressInfoVOs.get(i);
			subTreeItem.setData(subAddressItem);
			subTreeItem.setText(0, subAddressItem.getFile().getName());
			subTreeItem.setText(1, subAddressItem.getFunctionName());
			subTreeItem.setText(2, String.valueOf(subAddressItem.getLineNumber()));
			subTreeItem.setText(3, subAddressItem.getAddress());
			subTreeItem.setExpanded(true);
			mainTreeItem = subTreeItem;
		}

		tree.setRedraw(true);
	}

	public void expandAll()
	{
		tree.setRedraw(false);
		for (TreeItem item : tree.getItems())
		{
			item.setExpanded(true);
			expandAllTreeItems(item);
		}
		tree.setRedraw(true);
	}

	private void expandAllTreeItems(TreeItem item)
	{
		if (item.getItems() == null)
		{
			item.setExpanded(true);
		}
		else
		{
			for (TreeItem subItem : item.getItems())
			{
				expandAllTreeItems(subItem);
				subItem.setExpanded(true);
			}
		}
	}

	@Override
	public void setFocus()
	{
		if (tree != null)
		{
			tree.setFocus();
		}
	}

	public List<AddressInfoVO> getAddressInfoVOs()
	{
		return addressInfoVOs;
	}

	public void setAddressInfoVOs(List<AddressInfoVO> addressInfoVOs)
	{
		this.addressInfoVOs = addressInfoVOs;
	}

	private class ItemSelectionListener extends SelectionAdapter
	{
		@Override
		public void widgetSelected(SelectionEvent e)
		{
			TreeItem[] selection = tree.getSelection();
			AddressInfoVO addressInfoVO = (AddressInfoVO) selection[0].getData();
			TracingUtil.launchEditor(addressInfoVO.getFullFilePath());
			TracingUtil.goToLineNumber(addressInfoVO.getLineNumber());
		}

	}
}
