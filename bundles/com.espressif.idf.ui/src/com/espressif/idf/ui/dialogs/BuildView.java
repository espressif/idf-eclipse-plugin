package com.espressif.idf.ui.dialogs;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.espressif.idf.core.build.ReHintPair;

public class BuildView extends ViewPart
{

	private TableViewer hintsTableViewer;
	private final String[] titles = { "Error Message", "Hint" }; //$NON-NLS-1$ //$NON-NLS-2$
	private List<ReHintPair> reHintsPairs;
	private Composite parent;
	private Composite container;
	public BuildView()
	{
		reHintsPairs = Collections.emptyList();
	}

	public void setReHintsPairs(List<ReHintPair> reHintPairs)
	{
		this.reHintsPairs = reHintPairs;
		container.dispose();
		createPartControl(parent);
		parent.pack();
		parent.layout(true);
	}

	public void createPartControl(Composite parent)
	{
		this.parent = parent;
		container = new Composite(this.parent, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(1, true);
		container.setLayout(layout);
		createHintsViewer(container);
	}

	private void createHintsViewer(Composite container)
	{
		hintsTableViewer = new TableViewer(container,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns(container);
		Table hintsTable = hintsTableViewer.getTable();
		hintsTable.setHeaderVisible(true);
		hintsTable.setLinesVisible(true);
		hintsTableViewer.setContentProvider(new ArrayContentProvider());
		hintsTableViewer.setInput(reHintsPairs);
		resizeAllColumns();
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 1;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		hintsTableViewer.getControl().setLayoutData(gridData);

	}

	private void resizeAllColumns()
	{
		for (TableColumn tc : hintsTableViewer.getTable().getColumns())
		{
			tc.pack();
		}
	}

	private void createColumns(Composite container)
	{
		TableViewerColumn col = createTableViewerColumn(titles[0]);
		col.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public Image getImage(Object element)
			{
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
			}

			@Override
			public String getText(Object element)
			{
				return ((ReHintPair) element).getRe();
			}
		});
		col = createTableViewerColumn(titles[1]);
		col.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public Image getImage(Object element)
			{
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
			}

			@Override
			public String getText(Object element)
			{
				return ((ReHintPair) element).getHint();
			}
		});
		col.getViewer().addDoubleClickListener(event ->
		{
				StructuredSelection selection = (StructuredSelection) event.getViewer().getSelection();
				MessageDialog.openInformation(container.getShell(), Messages.HintDetailsTitle,
						((ReHintPair) selection.getFirstElement()).getHint());

		});
	}

	private TableViewerColumn createTableViewerColumn(String title)
	{
		TableViewerColumn viewerColumn = new TableViewerColumn(hintsTableViewer, SWT.NONE);
		TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	public void setFocus()
	{
	}

}
