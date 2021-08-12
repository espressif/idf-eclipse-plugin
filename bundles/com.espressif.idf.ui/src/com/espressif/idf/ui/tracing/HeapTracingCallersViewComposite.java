/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.tracing;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Callers consolidated view composite
 * 
 * @author Ali Azam Rana
 *
 */
public class HeapTracingCallersViewComposite
{
	private TracingJsonParser tracingJsonParser;
	private Tree tree;
	private TreeViewer viewer;

	public HeapTracingCallersViewComposite(TracingJsonParser tracingJsonParser)
	{
		this.tracingJsonParser = tracingJsonParser;
	}

	public void createPartControl(Composite parent)
	{
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);

		tree = viewer.getTree();

		createViewerColumns();

		viewer.setContentProvider(new ConsolidatedCallersViewContentProvider());
		createTree();
		viewer.getTree().redraw();
		viewer.expandAll();
	}

	private void createViewerColumns()
	{
		TreeViewerColumn colSize = new TreeViewerColumn(viewer, SWT.LEFT);
		colSize.getColumn().setWidth(150);
		colSize.getColumn().setText(Messages.TracingCallersConsolodiatedView_BytesUsed);
		colSize.setLabelProvider(new ColumnLabelProvider());

		TreeViewerColumn colHitCount = new TreeViewerColumn(viewer, SWT.LEFT);
		colHitCount.getColumn().setWidth(150);
		colHitCount.getColumn().setText(Messages.TracingCallersConsolodiatedView_HitsCount);
		colHitCount.setLabelProvider(new ColumnLabelProvider());

		TreeViewerColumn colFileName = new TreeViewerColumn(viewer, SWT.LEFT);
		colFileName.getColumn().setWidth(200);
		colFileName.getColumn().setText(Messages.TracingCallerView_ColFileName);
		colFileName.setLabelProvider(new ColumnLabelProvider());

		TreeViewerColumn colFunctionName = new TreeViewerColumn(viewer, SWT.LEFT);
		colFunctionName.getColumn().setWidth(200);
		colFunctionName.getColumn().setText(Messages.TracingCallerView_ColFunctionName);
		colFunctionName.setLabelProvider(new ColumnLabelProvider());

		TreeViewerColumn colLineNumber = new TreeViewerColumn(viewer, SWT.CENTER);
		colLineNumber.getColumn().setWidth(100);
		colLineNumber.getColumn().setText(Messages.TracingCallerView_ColLineNumber);
		colLineNumber.setLabelProvider(new ColumnLabelProvider());

		TreeViewerColumn colAddress = new TreeViewerColumn(viewer, SWT.CENTER);
		colAddress.getColumn().setText(Messages.TracingCallerView_ColAddress);
		colAddress.getColumn().setWidth(100);
		colAddress.setLabelProvider(new ColumnLabelProvider());
	}

	private void createTree()
	{
		Integer[] ids = { tracingJsonParser.getAllocEventId(), tracingJsonParser.getFreeEventId() };
		List<DetailsVO> detailsVOs = tracingJsonParser.getDetailsVOs(Arrays.asList(ids));
		for (DetailsVO detailsVO : detailsVOs)
		{
			List<String> callers = detailsVO.getEventsVO().getCallersAddressList().stream()
					.filter(addr -> !addr.equals("0x0")) //$NON-NLS-1$
					.collect(Collectors.toList());
			Collections.reverse(callers);

			int index = 0;
			String parentForCurrentSet = callers.get(0);
			if (tree.getItemCount() == 0) // check for first element to be inserted
			{
				AddressInfoVO addressInfoVO = tracingJsonParser.getCallersAddressMap().get(parentForCurrentSet);
				ConsolidatedCallersVO consolidatedCallersVO = new ConsolidatedCallersVO();
				consolidatedCallersVO.setAddressInfoVO(addressInfoVO);
				consolidatedCallersVO.setSizeUsed(0);
				TreeItem item = new TreeItem(tree, SWT.NONE);
				item.setData(consolidatedCallersVO);
				setTextOnTreeItem(item);
			}

			for (String caller : callers)
			{
				boolean itemExists = false;
				// check if item exists as a root in tree and is also the parent for the current set then just update
				// the size
				for (TreeItem item : tree.getItems())
				{
					ConsolidatedCallersVO itemCaller = (ConsolidatedCallersVO) item.getData();
					if (itemCaller.getAddressInfoVO().getAddress().equals(caller) && parentForCurrentSet.equals(caller))
					{
						itemCaller.setSizeUsed(
								itemCaller.getSizeUsed() + detailsVO.getEventsVO().getSizeOfAllocatedMemoryBlock());
						itemCaller.setHitCount(itemCaller.getHitCount() + 1);
						item.setData(itemCaller);
						setTextOnTreeItem(item);
						itemExists = true;
						break;
					}
				}

				if (itemExists)
				{
					index++;
					continue;
				}

				// caller doesn't exist in the tree now find the appropriate place for the caller to be inserted
				boolean parentItemExists = false;
				if (!itemExists && ((index - 1) >= 0))
				{
					String prevCaller = callers.get(index - 1);
					TreeItem foundParentItem = findItemForCurrentSetOnly(prevCaller, parentForCurrentSet);

					if (foundParentItem != null)
					{
						TreeItem itemForCaller = findItem(foundParentItem, caller);
						// Item already exists in the tree for the correct parent so we update the size in the item
						if (itemForCaller != null)
						{
							ConsolidatedCallersVO currentItemCaller = (ConsolidatedCallersVO) itemForCaller.getData();
							currentItemCaller.setSizeUsed(currentItemCaller.getSizeUsed()
									+ detailsVO.getEventsVO().getSizeOfAllocatedMemoryBlock());
							currentItemCaller.setHitCount(currentItemCaller.getHitCount() + 1);
							itemForCaller.setData(currentItemCaller);
							setTextOnTreeItem(itemForCaller);
							itemExists = true;
						}
						else
						{
							// item doesn't exist in the parent so add a new item inside the parent we don't need to
							// update size here as the flow will be reached through parent in some previous iteration
							// whose sizes are already updated
							ConsolidatedCallersVO currentItemCaller = new ConsolidatedCallersVO();
							currentItemCaller.setAddressInfoVO(tracingJsonParser.getCallersAddressMap().get(caller));
							currentItemCaller.setSizeUsed(detailsVO.getEventsVO().getSizeOfAllocatedMemoryBlock());
							currentItemCaller.setHitCount(currentItemCaller.getHitCount() + 1);
							TreeItem item = new TreeItem(foundParentItem, SWT.NONE);
							item.setData(currentItemCaller);
							setTextOnTreeItem(item);
							parentItemExists = true;
						}
					}
				}

				if (!parentItemExists && !itemExists)
				{
					// no parent exists so the item is added as root in tree
					ConsolidatedCallersVO itemCaller = new ConsolidatedCallersVO();
					itemCaller.setSizeUsed(detailsVO.getEventsVO().getSizeOfAllocatedMemoryBlock());
					itemCaller.setAddressInfoVO(tracingJsonParser.getCallersAddressMap().get(caller));
					itemCaller.setHitCount(itemCaller.getHitCount() + 1);
					TreeItem item = new TreeItem(tree, SWT.NONE);
					item.setData(itemCaller);
					setTextOnTreeItem(item);
				}

				index++;
			}
		}
	}

	/**
	 * Sets or refreshes the text on the tree item based on the data
	 * 
	 * @param item
	 */
	private void setTextOnTreeItem(TreeItem item)
	{
		ConsolidatedCallersVO itemCaller = (ConsolidatedCallersVO) item.getData();
		int index = 0;
		item.setText(index++, String.valueOf(itemCaller.getSizeUsed()));
		item.setText(index++, String.valueOf(itemCaller.getHitCount()));
		item.setText(index++, itemCaller.getAddressInfoVO().getFile().getName());
		item.setText(index++, itemCaller.getAddressInfoVO().getFunctionName());
		item.setText(index++, String.valueOf(itemCaller.getAddressInfoVO().getLineNumber()));
		item.setText(index++, itemCaller.getAddressInfoVO().getAddress());
	}

	/**
	 * Finds the tree item in the sub tree that has the address specified by parent for current set
	 * 
	 * @param caller
	 * @param parentForCurrentSet
	 * @return
	 */
	private TreeItem findItemForCurrentSetOnly(String caller, String parentForCurrentSet)
	{
		for (TreeItem subItem : tree.getItems())
		{
			ConsolidatedCallersVO mainItem = (ConsolidatedCallersVO) subItem.getData();
			if (mainItem.getAddressInfoVO().getAddress().equals(parentForCurrentSet))
			{
				if (parentForCurrentSet.equals(caller))
				{
					return subItem;
				}
				else
				{
					return findItem(subItem, caller);
				}
			}
		}
		return null;
	}

	private TreeItem findItem(TreeItem item, String caller)
	{
		for (TreeItem subItem : item.getItems())
		{
			ConsolidatedCallersVO subCallerItem = (ConsolidatedCallersVO) subItem.getData();
			if (subCallerItem.getAddressInfoVO().getAddress().equals(caller))
			{
				return subItem;
			}
			else
			{
				TreeItem itemFound = findItem(subItem, caller);
				if (itemFound != null)
				{
					return itemFound;
				}
			}
		}

		return null;
	}
}
