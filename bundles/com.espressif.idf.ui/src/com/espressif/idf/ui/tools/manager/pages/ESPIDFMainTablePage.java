/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.manager.pages;

import java.io.IOException;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.EimIdfConfiguratinParser;
import com.espressif.idf.core.tools.vo.EimJson;
import com.espressif.idf.core.tools.vo.IdfInstalled;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.install.IDFNewToolsWizard;
import com.espressif.idf.ui.tools.Messages;

/**
 * Main UI class for all listing and interacting with the tools
 * @author Ali Azam Rana
 *
 */
public class ESPIDFMainTablePage
{
	private Composite container;
	private TableViewer tableViewer;
	private ColumnViewerComparator comparator;
	private TableViewerColumn versionColumn;
	private TableViewerColumn locationColumn;
	private TableViewerColumn activateColumn;
	private TableViewerColumn removeColumn;
	private TableColumnLayout tableColumnLayout;
	private Composite tableComposite;
	private List<IdfInstalled> idfInstalledList;
	private static EimJson eimJson;
	private EimIdfConfiguratinParser eimIdfConfiguratinParser;
	
	private static final String REMOVE_ICON = "icons/tools/delete.png"; //$NON-NLS-1$
	private static final String RELOAD_ICON = "icons/tools/reload.png"; //$NON-NLS-1$
	private static final String IDF_TOOL_SET_BTN_KEY = "IDFToolSet"; //$NON-NLS-1$
	
	private static ESPIDFMainTablePage espidfMainTablePage;
	
	private ESPIDFMainTablePage()
	{
		eimIdfConfiguratinParser = new EimIdfConfiguratinParser();
	}
	
	public static ESPIDFMainTablePage getInstance(EimJson eimJson)
	{
		if (espidfMainTablePage == null)
		{
			espidfMainTablePage = new ESPIDFMainTablePage();
		}
		
		ESPIDFMainTablePage.eimJson = eimJson;
		return espidfMainTablePage;
	}
	
	public Composite createPage(Composite composite)
	{
		idfInstalledList = eimJson != null ? eimJson.getIdfInstalled() : null;
		container = new Composite(composite, SWT.NONE);
		final int numColumns = 2;
		GridLayout gridLayout = new GridLayout(numColumns, false);
		container.setLayout(gridLayout);
		createIdfTable(container);
		return container;
	}

	public void refreshEditorUI()
	{
		if (container == null)
			return;
		for (TableItem item : tableViewer.getTable().getItems())
		{
			String EDITOR_KEY = "action_editor";
			String EDITOR_KEY_LAST = "action_editor_last";
			TableEditor editorFirst = (TableEditor) item.getData(EDITOR_KEY);
			TableEditor editorLast = (TableEditor) item.getData(EDITOR_KEY_LAST);
			if (editorFirst != null)
			{
				if (editorFirst.getEditor() != null && !editorFirst.getEditor().isDisposed())
				{
					editorFirst.getEditor().dispose(); // Dispose the button composite
				}
				editorFirst.dispose(); // Dispose the editor itself
				item.setData(EDITOR_KEY, null); // Clear the stored editor reference
			}
			
			if (editorLast != null)
			{
				if (editorLast.getEditor() != null && !editorLast.getEditor().isDisposed())
				{
					editorLast.getEditor().dispose();
				}
				
				editorLast.dispose();
				item.setData(EDITOR_KEY_LAST, null);
			}
		}
		try
		{
			eimJson = eimIdfConfiguratinParser.getEimJson(true);
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
		
		idfInstalledList = eimJson.getIdfInstalled();
		setupColumns();
		tableViewer.setInput(idfInstalledList);
		tableViewer.getControl().requestLayout();
		tableViewer.refresh();
		container.redraw();
	}

	private Composite createIdfTable(Composite parent)
	{
		Group idfToolsGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		idfToolsGroup.setText("IDF Tools");
		idfToolsGroup.setLayout(new GridLayout(2, false));
		idfToolsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Composite for the TableViewer, with TableColumnLayout
		tableComposite = new Composite(idfToolsGroup, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);
		tableViewer = new TableViewer(tableComposite, SWT.BORDER | SWT.H_SCROLL);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		
		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.addListener(SWT.MeasureItem, event -> {
				event.height = 25;
			});
		comparator = new ColumnViewerComparator();
		tableViewer.setComparator(comparator);
		setupColumns();
		table.addListener(SWT.MeasureItem, e -> {
			e.height = 30;
		});

		tableViewer.setInput(idfInstalledList);
		table.layout();
		// Composite for the "Add" button
	    Composite buttonComposite = new Composite(idfToolsGroup, SWT.NONE);
	    GridData buttonCompositeGridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
	    buttonCompositeGridData.verticalAlignment = SWT.TOP; // Aligns the button composite at the top
	    buttonComposite.setLayoutData(buttonCompositeGridData);
	    buttonComposite.setLayout(new GridLayout(1, true));

	    // Creating the "Add" button
	    Button addButton = new Button(buttonComposite, SWT.PUSH);
	    addButton.setText(Messages.EspIdfManagerAddToolsBtn);
	    GridData addButtonGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
	    addButton.setLayoutData(addButtonGridData);
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
	    
//		removeAllButton = new Button(buttonComposite, SWT.PUSH);
//		removeAllButton.setText(Messages.EspIdfManagerRemoveAllBtn);
//		removeAllButton.addSelectionListener(new SelectionAdapter()
//		{
//			@Override
//			public void widgetSelected(SelectionEvent e)
//			{
//				MessageBox messageBox = new MessageBox(Display.getDefault().getActiveShell(),
//						SWT.ICON_WARNING | SWT.YES | SWT.NO);
//				messageBox.setMessage(Messages.EspIdfManagerMessageBoxDeleteAllConfirmMessage);
//				messageBox.setText(Messages.EspIdfManagerMessageBoxDeleteAllConfirmMessageTitle);
//				int response = messageBox.open();
//				if (response == SWT.YES)
//				{
//					for(IDFToolSet idfToolSet : idfToolSets)
//					{
//						toolSetConfigurationManager.delete(idfToolSet);
//					}
//					refreshEditorUI();
//				}
//			}
//		});
//		
//	    GridData removeAllButtonGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
//	    removeAllButton.setLayoutData(removeAllButtonGridData);
//	    if (idfToolSets == null || idfToolSets.isEmpty())
//	    {
//	    	removeAllButton.setEnabled(false);
//	    }
//	    else 
//		{
//			removeAllButton.setEnabled(true);
//		}
	    
		return idfToolsGroup;
	}
	
	private void disposeColumns()
	{
		if (activateColumn != null && activateColumn.getColumn() != null)
		{
			activateColumn.getColumn().dispose();
		}
		
		if (versionColumn != null && versionColumn.getColumn() != null)
		{
			versionColumn.getColumn().dispose();
		}
		
		if (locationColumn != null && locationColumn.getColumn() != null)
		{
			locationColumn.getColumn().dispose();
		}
		
		if (removeColumn != null && removeColumn.getColumn() != null)
		{
			removeColumn.getColumn().dispose();
		}
	}

	private void setupColumns()
	{
		disposeColumns();
		int colIndex = 0;
		
		activateColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		activateColumn.getColumn().setText(Messages.EspIdfManagerActivateCol);
		activateColumn.setLabelProvider(new IdfManagerTableColumnLabelProvider());
		tableColumnLayout.setColumnData(activateColumn.getColumn(), new ColumnWeightData(2, 5, true));
		
		versionColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		versionColumn.getColumn().setText(Messages.EspIdfManagerVersionCol);
		versionColumn.setLabelProvider(new IdfManagerTableColumnLabelProvider());
		setComparatorForCols(versionColumn, colIndex++);
		tableColumnLayout.setColumnData(versionColumn.getColumn(), new ColumnWeightData(3, 50, true));

		locationColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		locationColumn.getColumn().setText(Messages.EspIdfManagerLocationCol);
		locationColumn.setLabelProvider(new IdfManagerTableColumnLabelProvider());
		setComparatorForCols(locationColumn, colIndex++);
		tableColumnLayout.setColumnData(locationColumn.getColumn(), new ColumnWeightData(10, 100, true));
		
		removeColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		removeColumn.setLabelProvider(new IdfManagerTableColumnLabelProvider());
		tableColumnLayout.setColumnData(removeColumn.getColumn(), new ColumnWeightData(3, 100, true));
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

//	private void performDeleteOperation(IDFToolSet idfToolSet)
//	{
//		MessageBox messageBox = new MessageBox(Display.getDefault().getActiveShell(),
//				SWT.ICON_WARNING | SWT.YES | SWT.NO);
//		messageBox.setMessage(MessageFormat.format(Messages.EspIdfManagerMessageBoxDeleteConfirmMessage, idfToolSet.getIdfVersion()));
//		messageBox.setText(Messages.EspIdfManagerMessageBoxDeleteConfirmMessageTitle);
//		int response = messageBox.open();
//		if (response == SWT.YES)
//		{
////			toolSetConfigurationManager.delete(idfToolSet);
//		}
//	}

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
			if (element instanceof IdfInstalled)
			{
				IdfInstalled idfInstalled = (IdfInstalled) element;
				if (isIdfInstalledActive(idfInstalled))
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
			int totalCols = tableViewer.getTable().getColumnCount();
			boolean isLastCol = cell.getColumnIndex() == (totalCols - 1);
			boolean isFirstCol = cell.getColumnIndex() == 0;
			if (isFirstCol)
			{
				createButtonsForFirstCol(cell);
			}
			else if (isLastCol)
			{
				createButtonsForLastCol(cell);
			}
			else
			{
				updateDataIntoCells(cell);
			}
			
			Color color = getBackground(cell.getElement());
			if (color != null)
			{
				cell.setBackground(color);
			}
		}

		private void updateDataIntoCells(ViewerCell cell)
		{
			if ((!(cell.getElement() instanceof IdfInstalled)) && cell.getElement() == null)
				return;

			IdfInstalled idfToolSet = (IdfInstalled) cell.getElement();
			switch (cell.getColumnIndex())
			{
			case 0:
				break;
			case 1:
				cell.setText(idfToolSet.getName());
				break;
			case 2:
				cell.setText(idfToolSet.getPath());
				break;
			case 3:
				break;
			}
		}

		private void createButtonsForFirstCol(ViewerCell cell)
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
			IdfInstalled idfInstalled = (IdfInstalled) cell.getElement();
			Button setActiveButton = new Button(buttonComposite, SWT.RADIO);
			setActiveButton.setSelection(isIdfInstalledActive(idfInstalled));
			setActiveButton.setData(IDF_TOOL_SET_BTN_KEY, idfInstalled);
			setActiveButton.addListener(SWT.Selection, e -> {
				Button btn = (Button) e.widget;
//				ToolsActivationJob toolsActivationJob = new ToolsActivationJob((IDFToolSet) btn.getData(IDF_TOOL_SET_BTN_KEY),
//						null, null);
//				ToolsActivationJobListener toolsActivationJobListener = new ToolsActivationJobListener(
//						ESPIDFMainTablePage.this);
//				toolsActivationJob.addJobChangeListener(toolsActivationJobListener);
//				toolsActivationJob.schedule();
				btn.setEnabled(false);
			});

			editor.grabHorizontal = true;
			editor.setEditor(buttonComposite, item, cell.getColumnIndex());
		}
		
		private void createButtonsForLastCol(ViewerCell cell)
		{
			TableItem item = (TableItem) cell.getItem();
			Rectangle cellBounds = cell.getBounds();
			// using a unique key to store the editor to avoid creating multiple editors for the same cell
			String EDITOR_KEY = "action_editor_last";
			if (item.getData(EDITOR_KEY) != null)
			{
				return; // This cell already has an editor
			}
			TableEditor editor = new TableEditor(tableViewer.getTable());
			Composite buttonComposite = new Composite(tableViewer.getTable(), SWT.NONE);
			FillLayout fillLayout = new FillLayout(SWT.HORIZONTAL);
			buttonComposite.setLayout(fillLayout);
			buttonComposite.redraw();
			item.setData(EDITOR_KEY, editor);
			IdfInstalled idfInstalled = (IdfInstalled) cell.getElement();
			
			int buttonHeight = Math.min(cellBounds.height - 6, 30);
			
			if (isIdfInstalledActive(idfInstalled))
			{
				Button reloadButton = new Button(buttonComposite, SWT.PUSH | SWT.FLAT);
				reloadButton.pack();
				reloadButton.setData(IDF_TOOL_SET_BTN_KEY, idfInstalled);
				reloadButton.setImage(UIPlugin.getImage(RELOAD_ICON));
				reloadButton.setToolTipText(Messages.EspIdfManagerReloadBtnToolTip);
				reloadButton.addListener(SWT.Selection, e -> {
					Button btn = (Button) e.widget;
					IdfInstalled selectedToolSet = (IdfInstalled) btn.getData(IDF_TOOL_SET_BTN_KEY);
//					ToolsActivationJob toolsActivationJob = new ToolsActivationJob(selectedToolSet, IDFUtil.getPythonExecutable(), IDFUtil.getGitExecutablePathFromSystem());
//					ToolsActivationJobListener toolsActivationJobListener = new ToolsActivationJobListener(ESPIDFMainTablePage.this);
//					toolsActivationJob.addJobChangeListener(toolsActivationJobListener);
//					toolsActivationJob.schedule();
				});
				
				reloadButton.setSize(cellBounds.width, buttonHeight);
				reloadButton.addListener(SWT.Paint, e-> e.gc.drawRectangle(reloadButton.getBounds()));
				reloadButton.redraw();
			}

//			Button removeButton = new Button(buttonComposite, SWT.PUSH | SWT.FLAT);
//			removeButton.pack(); 
//			removeButton.setData(IDF_TOOL_SET_BTN_KEY, idfInstalled);
//			removeButton.setImage(UIPlugin.getImage(REMOVE_ICON));
//			removeButton.setToolTipText(Messages.EspIdfManagerDeleteBtnToolTip);
//			removeButton.addListener(SWT.Selection, e -> {
//				Button btn = (Button) e.widget;
//				IdfInstalled selectedToolSet = (IdfInstalled) btn.getData(IDF_TOOL_SET_BTN_KEY);
//				performDeleteOperation(selectedToolSet);
//				refreshEditorUI();
//			});
//			removeButton.setSize(cellBounds.width, buttonHeight);
//			removeButton.redraw();
			
			editor.grabHorizontal = true;
			editor.grabVertical = true;
			editor.horizontalAlignment = SWT.CENTER;
			editor.verticalAlignment = SWT.CENTER;
//			editor.minimumHeight = removeButton.getSize().y;
//			editor.minimumWidth = removeButton.getSize().x;
			editor.setEditor(buttonComposite, item, cell.getColumnIndex());
			buttonComposite.layout(true, true);
			buttonComposite.redraw();
			editor.layout();
		    tableViewer.getTable().layout(true, true);
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

	private boolean isIdfInstalledActive(IdfInstalled idfInstalled)
	{
		// TODO Auto-generated method stub
		return false;
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
			IdfInstalled p1 = (IdfInstalled) e1;
			IdfInstalled p2 = (IdfInstalled) e2;
			int rc = 0;
			switch (propertyIndex)
			{
			case 0:
				rc = p1.getName().compareTo(p2.getName());
				break;
			case 1:
				rc = p1.getPath().compareTo(p2.getPath());
				break;
			case 2:
				Boolean p1State = isIdfInstalledActive(p1);
				Boolean p2State = isIdfInstalledActive(p2);
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
