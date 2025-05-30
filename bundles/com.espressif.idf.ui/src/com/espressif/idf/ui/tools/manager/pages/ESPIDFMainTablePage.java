/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.manager.pages;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
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
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.console.MessageConsoleStream;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.EimConstants;
import com.espressif.idf.core.tools.EimIdfConfiguratinParser;
import com.espressif.idf.core.tools.SetupToolsInIde;
import com.espressif.idf.core.tools.util.ToolsUtility;
import com.espressif.idf.core.tools.vo.EimJson;
import com.espressif.idf.core.tools.vo.IdfInstalled;
import com.espressif.idf.ui.IDFConsole;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.tools.Messages;
import com.espressif.idf.ui.tools.SetupToolsJobListener;

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
	private TableViewerColumn nameColumn;
	
	private TableColumnLayout tableColumnLayout;
	private Composite tableComposite;
	private List<IdfInstalled> idfInstalledList;
	private static EimJson eimJson;
	private EimIdfConfiguratinParser eimIdfConfiguratinParser;
	
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
		createGuideLink(container);
		createIdfTable(container);
		return container;
	}
	
	private void createGuideLink(Composite composite)
	{
		Link guideLink = new Link(composite, SWT.WRAP);
		guideLink.setText(Messages.IDFGuideLinkLabel_Text);
		guideLink.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		guideLink.addListener(SWT.Selection, e -> {
			try
			{
				java.awt.Desktop.getDesktop().browse(new java.net.URI(
						"https://dl.espressif.com/dl/esp-idf/support-periods.svg"));
			}
			catch (Exception ex)
			{
				Logger.log(ex);
			}
		});
	}
	
	public void setupInitialEspIdf()
	{
		if (idfInstalledList != null && idfInstalledList.size() == 1)
		{
			// activate the only available esp-idf first check if its not already active
			Preferences scopedPreferenceStore = InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID);
			if (!scopedPreferenceStore.getBoolean(EimConstants.INSTALL_TOOLS_FLAG, false))
			{
				SetupToolsInIde setupToolsInIde = new SetupToolsInIde(idfInstalledList.get(0), eimJson,
						getConsoleStream(true), getConsoleStream(false));
				SetupToolsJobListener toolsActivationJobListener = new SetupToolsJobListener(ESPIDFMainTablePage.this,
						setupToolsInIde);
				setupToolsInIde.addJobChangeListener(toolsActivationJobListener);
				setupToolsInIde.schedule();
			}
		}
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
		
		if (nameColumn != null && nameColumn.getColumn() != null)
		{
			nameColumn.getColumn().dispose();
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
		
		nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		nameColumn.getColumn().setText(Messages.EspIdfManagerNameCol);
		nameColumn.setLabelProvider(new IdfManagerTableColumnLabelProvider());
		setComparatorForCols(nameColumn, colIndex++);
		tableColumnLayout.setColumnData(nameColumn.getColumn(), new ColumnWeightData(3, 50, true));

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
				if (ToolsUtility.isIdfInstalledActive(idfInstalled))
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

			IdfInstalled idfInstalled = (IdfInstalled) cell.getElement();
			switch (cell.getColumnIndex())
			{
			case 0:
				break;
			case 1:
				cell.setText(getIdfVersion(idfInstalled));
				break;
			case 2:
				cell.setText(idfInstalled.getName());
				break;
			case 3:
				cell.setText(idfInstalled.getPath());
				break;
			case 4:
				break;
			}
		}

		private String getIdfVersion(IdfInstalled idfInstalled)
		{
			String version = ToolsUtility.getIdfVersion(idfInstalled, eimJson.getGitPath());
			return version;
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
			setActiveButton.setSelection(ToolsUtility.isIdfInstalledActive(idfInstalled));
			setActiveButton.setData(IDF_TOOL_SET_BTN_KEY, idfInstalled);
			setActiveButton.addListener(SWT.Selection, e -> {
				Button btn = (Button) e.widget;
				SetupToolsInIde setupToolsInIde = new SetupToolsInIde(idfInstalled, eimJson, getConsoleStream(true), getConsoleStream(false));
				SetupToolsJobListener toolsActivationJobListener = new SetupToolsJobListener(
						ESPIDFMainTablePage.this, setupToolsInIde);
				setupToolsInIde.addJobChangeListener(toolsActivationJobListener);
				setupToolsInIde.schedule();
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
			
			if (ToolsUtility.isIdfInstalledActive(idfInstalled))
			{
				Button reloadButton = new Button(buttonComposite, SWT.PUSH | SWT.FLAT);
				reloadButton.pack();
				reloadButton.setData(IDF_TOOL_SET_BTN_KEY, idfInstalled);
				reloadButton.setImage(UIPlugin.getImage(RELOAD_ICON));
				reloadButton.setToolTipText(Messages.EspIdfManagerReloadBtnToolTip);
				reloadButton.addListener(SWT.Selection, e -> {
					Button btn = (Button) e.widget;
					IdfInstalled selectedToolSet = (IdfInstalled) btn.getData(IDF_TOOL_SET_BTN_KEY);
					SetupToolsInIde setupToolsInIde = new SetupToolsInIde(selectedToolSet, eimJson, getConsoleStream(true), getConsoleStream(false));
					SetupToolsJobListener toolsActivationJobListener = new SetupToolsJobListener(
							ESPIDFMainTablePage.this, setupToolsInIde);
					setupToolsInIde.addJobChangeListener(toolsActivationJobListener);
					setupToolsInIde.schedule();
				});
				
				reloadButton.setSize(cellBounds.width, buttonHeight);
				reloadButton.addListener(SWT.Paint, e-> e.gc.drawRectangle(reloadButton.getBounds()));
				reloadButton.redraw();
			}

			editor.grabHorizontal = true;
			editor.grabVertical = true;
			editor.horizontalAlignment = SWT.CENTER;
			editor.verticalAlignment = SWT.CENTER;
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
	
	private MessageConsoleStream getConsoleStream(boolean errorStream)
	{
		IDFConsole idfConsole = new IDFConsole();
		return idfConsole.getConsoleStream(Messages.IDFToolsHandler_ToolsManagerConsole, null, errorStream, true);
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
				Boolean p1State = ToolsUtility.isIdfInstalledActive(p1);
				Boolean p2State = ToolsUtility.isIdfInstalledActive(p2);
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
