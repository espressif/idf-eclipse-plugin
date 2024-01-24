package com.espressif.idf.ui.tools.manager.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.espressif.idf.ui.install.IDFDownloadWizard;
import com.espressif.idf.ui.install.Messages;

public class ESPIDFMainTablePage
{
	private Composite container;
	private TableViewer tableViewer;
	private String[] columnDataKeys = { "Version", "Location", "State" };

	public Composite createPage(Composite composite)
	{
		container = new Composite(composite, SWT.NONE);
		final int numColumns = 3;
		GridLayout gridLayout = new GridLayout(numColumns, false);
		container.setLayout(gridLayout);

		createIdfTable(container);
		return container;
	}

	private void createIdfTable(Composite parent)
	{
		Group idfToolsGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		idfToolsGroup.setText("IDF Tools");
		idfToolsGroup.setLayout(new GridLayout());
		idfToolsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));

		// Composite for the TableViewer, with TableColumnLayout
		Composite tableComposite = new Composite(idfToolsGroup, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);

		tableViewer = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableViewerColumn versionColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		versionColumn.getColumn().setText("ESP-IDF Version");
		versionColumn.setLabelProvider(new IdfManagerTableColumnLabelProvider());

		tableColumnLayout.setColumnData(versionColumn.getColumn(), new ColumnWeightData(1, 50, true));

		TableViewerColumn locationColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		locationColumn.getColumn().setText("Location");
		locationColumn.setLabelProvider(new IdfManagerTableColumnLabelProvider());
		tableColumnLayout.setColumnData(locationColumn.getColumn(), new ColumnWeightData(2, 100, true));

		TableViewerColumn stateColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		stateColumn.getColumn().setText("State");
		stateColumn.setLabelProvider(new IdfManagerTableColumnLabelProvider());
		tableColumnLayout.setColumnData(stateColumn.getColumn(), new ColumnWeightData(1, 50, true));

		TableViewerColumn actionsColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		actionsColumn.getColumn().setText("Actions");
		actionsColumn.setLabelProvider(new IdfManagerTableColumnLabelProvider());
		tableColumnLayout.setColumnData(actionsColumn.getColumn(), new ColumnWeightData(1, 50, true));

		// example data
		List<Map<String, Object>> data = new ArrayList<>();
		// Populate your example data here
		Map<String, Object> rowData = new HashMap<>();
		rowData.put("Version", "v1.0");
		rowData.put("Location", "/path/to/v1.0");
		rowData.put("State", true);
		data.add(rowData);

		// Input sample data into the table viewer
		tableViewer.setInput(data);
		table.layout();

		Composite buttonComposite = new Composite(idfToolsGroup, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 3, 1));
		buttonComposite.setLayout(new FillLayout());

		// Add button on top of the table
		Button newButton = new Button(buttonComposite, SWT.PUSH);
		newButton.setText("New Tools");
		newButton.addSelectionListener(new SelectionListener()
		{
			
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				IDFDownloadWizard wizard = new IDFDownloadWizard();
				wizard.setWindowTitle(Messages.IDFDownloadHandler_ESPIDFConfiguration);

				WizardDialog wizDialog = new WizardDialog(container.getShell(), wizard);
				wizDialog.create();

				wizDialog.setTitle(Messages.IDFDownloadHandler_DownloadPage_Title);
				wizDialog.setMessage(Messages.IDFDownloadHandler_DownloadPageMsg);
				wizDialog.getShell().setSize(Math.max(850,wizDialog.getShell().getSize().x), 500);
				
				wizDialog.open();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
		});
		newButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Button deleteButton = new Button(buttonComposite, SWT.PUSH);
		deleteButton.setText("Delete");
		deleteButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	private class IdfManagerTableColumnLabelProvider extends ColumnLabelProvider
	{

		@Override
		public void update(ViewerCell cell)
		{
			int totalCols = tableViewer.getTable().getColumnCount();
			boolean isLastCol = cell.getColumnIndex() == (totalCols - 1);
			if (isLastCol)
			{
				createButtonsForLastCol(cell);
			}
			else 
			{
				String key = columnDataKeys[cell.getColumnIndex()];
				@SuppressWarnings("unchecked")
				Map<String, Object> row = (Map<String, Object>) cell.getElement();
				if (row.get(key) instanceof String)
				{
					cell.setText(row.get(key).toString());
				}
				else if (row.get(key) instanceof Boolean)
				{
					boolean value = Boolean.valueOf(row.get(key).toString());
					cell.setText(value ? "Yes" : "No");
				}
			}
		}

		private void createButtonsForLastCol(ViewerCell cell)
		{
			TableItem item = (TableItem) cell.getItem();
			// We use a unique key to store the editor to avoid creating multiple editors for the same cell
			String EDITOR_KEY = "action_editor";
			if (item.getData(EDITOR_KEY) != null)
			{
				return; // This cell already has an editor
			}
			TableEditor editor = new TableEditor(tableViewer.getTable());
			Composite buttonComposite = new Composite(tableViewer.getTable(), SWT.NONE);
			buttonComposite.setLayout(new FillLayout());
			item.setData(EDITOR_KEY, editor);

			Button editButton = new Button(buttonComposite, SWT.NONE);
			editButton.setText("Edit");
			editButton.addListener(SWT.Selection, e -> {
				// handle edit
			});

			Button setActiveButton = new Button(buttonComposite, SWT.NONE);
			setActiveButton.setText("Set Active");
			setActiveButton.addListener(SWT.Selection, e -> {
				// handle set active
			});

			editor.grabHorizontal = true;
			editor.setEditor(buttonComposite, item, cell.getColumnIndex());
		}
	}
}
