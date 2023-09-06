/*******************************************************************************
 * Copyright 2022-2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.dialogs;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.espressif.idf.core.build.ReHintPair;
import com.espressif.idf.core.util.HintsUtil;
import com.espressif.idf.core.util.StringUtil;

public class HintsView extends ViewPart
{
	private TableViewer hintsTableViewer;
	private Table hintsTable;
	private final String[] titles = { "Error Type", "Hint" }; //$NON-NLS-1$ //$NON-NLS-2$
	private Text searchField;
	private List<ReHintPair> reHintsList;

	public HintsView()
	{
		super();
	}

	@Override
	public void createPartControl(Composite parent)
	{
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(1, true);
		container.setLayout(layout);
		reHintsList = HintsUtil.getReHintsList(new File(HintsUtil.getHintsYmlPath()));
		if (reHintsList.isEmpty())
		{
			CLabel errorField = new CLabel(container, SWT.H_SCROLL);
			errorField.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
			errorField.setText(MessageFormat.format(Messages.HintsYmlNotFoundErrMsg, HintsUtil.getHintsYmlPath()));
			return;
		}
		createSearchField(container);
		createHintsViewer(container);
	}

	private void createHintsViewer(Composite container)
	{
		hintsTableViewer = new TableViewer(container,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns(container);
		hintsTable = hintsTableViewer.getTable();
		hintsTable.setHeaderVisible(true);
		hintsTable.setLinesVisible(true);
		hintsTableViewer.setContentProvider(new ArrayContentProvider());
		hintsTableViewer.setInput(reHintsList);
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
			public String getText(Object element)
			{
				Optional<Pattern> reOptionalPattern = ((ReHintPair) element).getRe();
				return reOptionalPattern.isPresent() ? reOptionalPattern.get().pattern()
						: StringUtil.EMPTY;
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
		col.getViewer().addDoubleClickListener(event -> {
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

	private void createSearchField(Composite container)
	{
		GridData dataName = new GridData();
		dataName.grabExcessHorizontalSpace = true;
		dataName.horizontalAlignment = SWT.FILL;
		searchField = new Text(container, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
		searchField.setMessage(Messages.FilterMessage);
		searchField.setLayoutData(dataName);
		searchField.addModifyListener(e -> {
			final List<ReHintPair> allMatchesList = new ArrayList<>();
			reHintsList.stream()
					.filter(reHintEntry -> reHintEntry.getRe()
							.map(pattern -> pattern.matcher(searchField.getText()).find()).orElse(false))
					.forEach(allMatchesList::add);

			if (allMatchesList.isEmpty())
			{
				reHintsList.stream()
						.filter(reHintEntry -> reHintEntry.getRe()
								.map(pattern -> pattern.pattern().contains(searchField.getText())).orElse(false))
						.forEach(allMatchesList::add);
			}

			hintsTableViewer.setInput(allMatchesList.isEmpty() ? reHintsList : allMatchesList);
			hintsTableViewer.refresh();
		});

	}

	@Override
	public void setFocus()
	{
		if (searchField != null)
		{
			searchField.setFocus();
		}
	}

}
