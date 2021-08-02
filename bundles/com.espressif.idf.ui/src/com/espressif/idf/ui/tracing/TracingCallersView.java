/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.tracing;

import java.io.File;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.espressif.idf.core.logging.Logger;

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
	private TableViewer viewer;

	public TracingCallersView()
	{
	}

	@Override
	public void createPartControl(Composite parent)
	{
		parent.setLayout(new FillLayout());
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns(parent, viewer);
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(getAddressInfoVOs());
		table.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				TableItem[] items = table.getSelection();
				AddressInfoVO addressInfoVO = (AddressInfoVO) items[0].getData();
				launchEditor(addressInfoVO.getFullFilePath());
				goToLineNumber(addressInfoVO.getLineNumber());
			}
		});
		getSite().setSelectionProvider(viewer);
	}

	private void launchEditor(String fullFilePath)
	{
		File fileToOpen = new File(fullFilePath);

		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				if (fileToOpen.exists() && fileToOpen.isFile())
				{
					IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

					try
					{
						IDE.openEditorOnFileStore(page, fileStore);
					}
					catch (PartInitException e)
					{
						Logger.log(e);
					}
				}
			}
		});
	}

	private void goToLineNumber(int lineNumber)
	{
		ITextEditor editor = (ITextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActiveEditor();
		IDocumentProvider provider = editor.getDocumentProvider();
		IDocument document = provider.getDocument(editor.getEditorInput());
		try
		{

			int start = document.getLineOffset(lineNumber);
			editor.selectAndReveal(start, 0);

			IWorkbenchPage page = editor.getSite().getPage();
			page.activate(editor);

		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}

	public TableViewer getViewer()
	{
		return viewer;
	}

	private void createColumns(Composite parent, TableViewer viewer2)
	{
		String[] titles = { Messages.TracingCallerView_ColFileName, Messages.TracingCallerView_ColFunctionName,
				Messages.TracingCallerView_ColLineNumber };
		int[] bounds = { 200, 200, 100 };

		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				AddressInfoVO addressInfoVO = (AddressInfoVO) element;
				return addressInfoVO.getFile().getName();
			}
		});

		col = createTableViewerColumn(titles[1], bounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				AddressInfoVO addressInfoVO = (AddressInfoVO) element;
				return addressInfoVO.getFunctionName();
			}
		});

		col = createTableViewerColumn(titles[2], bounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				AddressInfoVO addressInfoVO = (AddressInfoVO) element;
				return String.valueOf(addressInfoVO.getLineNumber());
			}
		});
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber)
	{
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;

	}

	@Override
	public void setFocus()
	{
		viewer.getControl().setFocus();
	}

	public List<AddressInfoVO> getAddressInfoVOs()
	{
		return addressInfoVOs;
	}

	public void setAddressInfoVOs(List<AddressInfoVO> addressInfoVOs)
	{
		this.addressInfoVOs = addressInfoVOs;
		viewer.setInput(this.addressInfoVOs);
	}

}
