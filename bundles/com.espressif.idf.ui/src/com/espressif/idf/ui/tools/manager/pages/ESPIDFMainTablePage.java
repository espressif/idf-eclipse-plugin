/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.manager.pages;

import java.text.MessageFormat;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.espressif.idf.core.tools.ToolSetConfigurationManager;
import com.espressif.idf.core.tools.vo.IDFToolSet;
import com.espressif.idf.ui.install.IDFNewToolsWizard;
import com.espressif.idf.ui.tools.Messages;
import com.espressif.idf.ui.tools.ToolsActivationJob;
import com.espressif.idf.ui.tools.ToolsActivationJobListener;

/**
 * Main UI class for all listing and interacting with the tools
 * @author Ali Azam Rana
 *
 */
public class ESPIDFMainTablePage
{
	private Composite container;
	private TableViewer tableViewer;
	private ToolSetConfigurationManager toolSetConfigurationManager;
	private ColumnViewerComparator comparator;
	private TableViewerColumn versionColumn;
	private TableViewerColumn locationColumn;
	private TableViewerColumn stateColumn;
	private TableViewerColumn actionsColumn;
	private TableColumnLayout tableColumnLayout;
	private Composite tableComposite;

	public Composite createPage(Composite composite)
	{
		toolSetConfigurationManager = new ToolSetConfigurationManager();
		container = new Composite(composite, SWT.NONE);
		final int numColumns = 3;
		GridLayout gridLayout = new GridLayout(numColumns, false);
		container.setLayout(gridLayout);

		Composite toolsComposite = createIdfTable(container);
		createButtons(toolsComposite);
		return container;
	}

	public void refreshTable()
	{
		for (TableItem item : tableViewer.getTable().getItems())
		{
			String EDITOR_KEY = "action_editor";
			TableEditor editor = (TableEditor) item.getData(EDITOR_KEY);
			if (editor != null)
			{
				if (editor.getEditor() != null && !editor.getEditor().isDisposed())
				{
					editor.getEditor().dispose(); // Dispose the button composite
				}
				editor.dispose(); // Dispose the editor itself
				item.setData(EDITOR_KEY, null); // Clear the stored editor reference
			}
		}
		toolSetConfigurationManager.setReload(true);
		actionsColumn.getColumn().dispose();
		actionsColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		actionsColumn.getColumn().setText("Actions");
		actionsColumn.setLabelProvider(new IdfManagerTableColumnLabelProvider());
		tableColumnLayout.setColumnData(actionsColumn.getColumn(), new ColumnWeightData(1, 50, true));
		tableViewer.setInput(toolSetConfigurationManager.getIdfToolSets(true));
		tableViewer.getControl().requestLayout();
		tableViewer.refresh();
		container.redraw();
		toolSetConfigurationManager.setReload(false);
	}

	private Composite createIdfTable(Composite parent)
	{
		Group idfToolsGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		idfToolsGroup.setText("IDF Tools");
		idfToolsGroup.setLayout(new GridLayout());
		idfToolsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));

		// Composite for the TableViewer, with TableColumnLayout
		tableComposite = new Composite(idfToolsGroup, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);

		tableViewer = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		comparator = new ColumnViewerComparator();
		tableViewer.setComparator(comparator);
		setupColumns();

		tableViewer.setInput(toolSetConfigurationManager.getIdfToolSets(true));
		table.layout();
		return idfToolsGroup;
	}

	private void setupColumns()
	{
		int colIndex = 0;
		versionColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		versionColumn.getColumn().setText(Messages.EspIdfManagerVersionCol);
		versionColumn.setLabelProvider(new IdfManagerTableColumnLabelProvider());
		setComparatorForCols(versionColumn, colIndex++);
		tableColumnLayout.setColumnData(versionColumn.getColumn(), new ColumnWeightData(1, 50, true));

		locationColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		locationColumn.getColumn().setText(Messages.EspIdfManagerLocationCol);
		locationColumn.setLabelProvider(new IdfManagerTableColumnLabelProvider());
		setComparatorForCols(locationColumn, colIndex++);
		tableColumnLayout.setColumnData(locationColumn.getColumn(), new ColumnWeightData(2, 100, true));

		stateColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		stateColumn.getColumn().setText(Messages.EspIdfManagerStateCol);
		stateColumn.setLabelProvider(new IdfManagerTableColumnLabelProvider());
		setComparatorForCols(stateColumn, colIndex++);
		tableColumnLayout.setColumnData(stateColumn.getColumn(), new ColumnWeightData(1, 50, true));

		actionsColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		actionsColumn.getColumn().setText(Messages.EspIdfManagerActionsCol);
		actionsColumn.setLabelProvider(new IdfManagerTableColumnLabelProvider());
		tableColumnLayout.setColumnData(actionsColumn.getColumn(), new ColumnWeightData(1, 50, true));
	}

	private void setComparatorForCols(TableViewerColumn column, int colIndex)
	{
		column.getColumn().addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				comparator.setColumn(colIndex);
				int direction = tableViewer.getTable().getSortDirection();
				if (tableViewer.getTable().getSortColumn() == column.getColumn())
				{
					direction = direction == SWT.UP ? SWT.DOWN : SWT.UP;
				}
				else
				{
					direction = SWT.DOWN;
				}

				tableViewer.getTable().setSortDirection(direction);
				tableViewer.getTable().setSortColumn(column.getColumn());
				tableViewer.refresh();
			}
		});

	}

	private void createButtons(Composite composite)
	{
		Composite buttonComposite = new Composite(composite, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 3, 1));
		
		GridLayout gridLayout = new GridLayout(2, true);
		gridLayout.horizontalSpacing = 10;
		
		buttonComposite.setLayout(gridLayout);

		// Add button at bottom of the table
		Button addButton = new Button(buttonComposite, SWT.PUSH);
		addButton.setText(Messages.EspIdfManagerAddToolsBtn);
		addButton.addSelectionListener(new SelectionAdapter()
		{

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				IDFNewToolsWizard wizard = new IDFNewToolsWizard(ESPIDFMainTablePage.this);
				wizard.setWindowTitle(Messages.IDFDownloadHandler_ESPIDFConfiguration);
				WizardDialog wizDialog = new WizardDialog(container.getShell(), wizard);
				wizDialog.create();

				wizDialog.setTitle(Messages.IDFDownloadHandler_DownloadPage_Title);
				wizDialog.setMessage(Messages.IDFDownloadHandler_DownloadPageMsg);
				wizDialog.getShell().setSize(Math.max(850, wizDialog.getShell().getSize().x), 550);

				wizDialog.open();
			}
		});
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Button deleteButton = new Button(buttonComposite, SWT.PUSH);
		deleteButton.setText(Messages.EspIdfManagerDeleteBtn);
		deleteButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				// Get the selected item
				IStructuredSelection selection = tableViewer.getStructuredSelection();
				if (!selection.isEmpty())
				{
					IDFToolSet selectedToolSet = (IDFToolSet) selection.getFirstElement();
					performDeleteOperation(selectedToolSet);
					refreshTable();
				}
			}
		});
		deleteButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}
	
	private void performDeleteOperation(IDFToolSet idfToolSet)
	{
		if (idfToolSet.isActive())
		{
			MessageBox messageBox = new MessageBox(Display.getDefault().getActiveShell(),
					SWT.ICON_INFORMATION | SWT.OK);
			messageBox.setMessage(Messages.EspIdfManagerMessageBoxActiveToolchainDelete);
			messageBox.setText(Messages.EspIdfManagerMessageBoxActiveToolchainDeleteTitle);
			messageBox.open();
		}
		else 
		{
			MessageBox messageBox = new MessageBox(Display.getDefault().getActiveShell(),
					SWT.ICON_WARNING | SWT.YES | SWT.NO);
			messageBox.setMessage(MessageFormat.format(Messages.EspIdfManagerMessageBoxDeleteConfirmMessage, idfToolSet.getIdfVersion()));
			messageBox.setText(Messages.EspIdfManagerMessageBoxDeleteConfirmMessageTitle);
			int response = messageBox.open();
			if (response == SWT.YES)
			{
				toolSetConfigurationManager.delete(idfToolSet);
			}
		}
	}

	private class IdfManagerTableColumnLabelProvider extends ColumnLabelProvider
	{
		private Color activeBackgroundColor;
		
		private IdfManagerTableColumnLabelProvider()
		{
			super();
			this.activeBackgroundColor = new Color(Display.getCurrent(), 144, 238, 144);
		}
		
		@Override
		public Color getBackground(Object element)
		{
			if (element instanceof IDFToolSet)
			{
				IDFToolSet idfToolSet = (IDFToolSet) element;
				if (idfToolSet.isActive())
				{
					// Return the green color for active rows
					return activeBackgroundColor;
				}
			}
			return null;
		}

		@Override
		public void update(ViewerCell cell)
		{
			super.update(cell);
			int totalCols = tableViewer.getTable().getColumnCount();
			boolean isLastCol = cell.getColumnIndex() == (totalCols - 1);
			if (isLastCol)
			{
				createButtonsForLastCol(cell);
			}
			else
			{
				updateDataIntoCells(cell);
			}
		}

		private void updateDataIntoCells(ViewerCell cell)
		{
			if ((!(cell.getElement() instanceof IDFToolSet)) && cell.getElement() == null)
				return;

			IDFToolSet idfToolSet = (IDFToolSet) cell.getElement();
			switch (cell.getColumnIndex())
			{
			case 0:
				cell.setText(idfToolSet.getIdfVersion());
				break;
			case 1:
				cell.setText(idfToolSet.getIdfLocation());
				break;
			case 2:
				cell.setText(idfToolSet.isActive() ? "Active" : "Inactive");
				break;
			}
		}

		private void createButtonsForLastCol(ViewerCell cell)
		{
			TableItem item = (TableItem) cell.getItem();
			// using a unique key to store the editor to avoid creating multiple editors for the same cell
			String EDITOR_KEY = "action_editor";
			if (item.getData(EDITOR_KEY) != null)
			{
				return; // This cell already has an editor
			}
			TableEditor editor = new TableEditor(tableViewer.getTable());
			Composite buttonComposite = new Composite(tableViewer.getTable(), SWT.NONE);
			buttonComposite.setLayout(new FillLayout());
			item.setData(EDITOR_KEY, editor);
			IDFToolSet idfToolSet = (IDFToolSet) cell.getElement();
			Button setActiveButton = new Button(buttonComposite, SWT.RADIO);
			setActiveButton.setSelection(idfToolSet.isActive());
			setActiveButton.setData("IDFToolSet", idfToolSet);
			setActiveButton.setText("Set Active");
			setActiveButton.addListener(SWT.Selection, e -> {
				Button btn = (Button) e.widget;
				ToolsActivationJob toolsActivationJob = new ToolsActivationJob((IDFToolSet) btn.getData("IDFToolSet"),
						null, null);
				ToolsActivationJobListener toolsActivationJobListener = new ToolsActivationJobListener(
						ESPIDFMainTablePage.this);
				toolsActivationJob.addJobChangeListener(toolsActivationJobListener);
				toolsActivationJob.schedule();
				btn.setEnabled(false);
			});

			editor.grabHorizontal = true;
			editor.setEditor(buttonComposite, item, cell.getColumnIndex());
		}
		
		@Override
		public void dispose()
		{
			if (this.activeBackgroundColor != null && !this.activeBackgroundColor.isDisposed())
			{
				this.activeBackgroundColor.dispose();
			}
			super.dispose();
		}
	}

	private class ColumnViewerComparator extends ViewerComparator
	{
		private int propertyIndex;
		private static final int DESCENDING = 1;
		private int direction = 0;

		public ColumnViewerComparator()
		{
			this.propertyIndex = 0;
			direction = DESCENDING;
		}

		public void setColumn(int column)
		{
			if (column == this.propertyIndex)
			{
				// If the same column is clicked again, toggle the direction
				direction = 1 - direction;
			}
			else
			{
				// Else, sort the new column in ascending order
				this.propertyIndex = column;
				direction = DESCENDING;
			}
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2)
		{
			IDFToolSet p1 = (IDFToolSet) e1;
			IDFToolSet p2 = (IDFToolSet) e2;
			int rc = 0;
			switch (propertyIndex)
			{
			case 0:
				rc = p1.getIdfVersion().compareTo(p2.getIdfVersion());
				break;
			case 1:
				rc = p1.getIdfLocation().compareTo(p2.getIdfLocation());
				break;
			case 2:
				Boolean p1State = p1.isActive();
				Boolean p2State = p2.isActive();
				rc = p1State.compareTo(p2State);
				break;
			default:
				break;
			}
			if (direction == DESCENDING)
			{
				rc = -rc;
			}
			return rc;
		}

	}
}