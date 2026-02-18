/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.manager.pages;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.console.MessageConsoleStream;
import org.osgi.service.prefs.BackingStoreException;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.EimConstants;
import com.espressif.idf.core.tools.EimIdfConfiguratinParser;
import com.espressif.idf.core.tools.SetupToolsInIde;
import com.espressif.idf.core.tools.ToolInitializer;
import com.espressif.idf.core.tools.util.ToolsUtility;
import com.espressif.idf.core.tools.vo.EimJson;
import com.espressif.idf.core.tools.vo.IdfInstalled;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.IDFConsole;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.tools.EimButtonLaunchListener;
import com.espressif.idf.ui.tools.Messages;
import com.espressif.idf.ui.tools.SetupToolsJobListener;

/**
 * Main UI class for all listing and interacting with the tools
 * 
 * @author Ali Azam Rana
 * @author Denys Almazov
 *
 */
public class ESPIDFMainTablePage
{

	private static final String HTTPS_DL_ESPRESSIF_COM_DL_ESP_IDF_SUPPORT_PERIODS_SVG = "https://dl.espressif.com/dl/esp-idf/support-periods.svg"; //$NON-NLS-1$
	private static final String PREF_SORT_COL = "EspIdfManager_SortCol"; //$NON-NLS-1$
	private static final String PREF_SORT_DIR = "EspIdfManager_SortDir"; //$NON-NLS-1$
	private final IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID);

	private record IdfRow(IdfInstalled original, boolean isActive, String version, String name, String path)
	{
	}

	private Composite container;
	private TableViewer tableViewer;
	private Button btnActivate;
	private Button btnReinstall;
	private Button eimLaunchBtn;

	private final IdViewerComparator comparator = new IdViewerComparator();
	private EimJson eimJson;

	private final EimIdfConfiguratinParser configParser;
	private final ToolInitializer toolInitializer;

	private final IDFConsole idfConsole = new IDFConsole();
	private IdfInstalled currentInstallingNode = null;

	public ESPIDFMainTablePage(EimJson eimJson)
	{
		this.eimJson = eimJson;
		this.configParser = new EimIdfConfiguratinParser();
		this.toolInitializer = new ToolInitializer(InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID));
	}

	public Composite createPage(Composite parent)
	{
		new LocalResourceManager(JFaceResources.getResources(), parent);

		container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));

		createHeader(container);
		createMainContent(container);

		if (eimJson != null)
		{
			refreshEditorUI();
		}

		return container;
	}

	private void createHeader(Composite parent)
	{
		var headerComp = new Composite(parent, SWT.NONE);
		headerComp.setLayout(new GridLayout(1, false));
		headerComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		var guideLink = new Link(headerComp, SWT.WRAP);
		guideLink.setText(Messages.IDFGuideLinkLabel_Text);
		guideLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		guideLink.addListener(SWT.Selection, e -> {
			try
			{
				Desktop.getDesktop().browse(new URI(HTTPS_DL_ESPRESSIF_COM_DL_ESP_IDF_SUPPORT_PERIODS_SVG));
			}
			catch (Exception ex)
			{
				Logger.log(ex.toString());
			}
		});


	}

	private void createMainContent(Composite parent)
	{
		var group = new Group(parent, SWT.NONE);
		group.setText("Installed IDF Versions");
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// --- Table ---
		var tableComp = new Composite(group, SWT.NONE);
		var tableLayout = new TableColumnLayout();
		tableComp.setLayout(tableLayout);
		tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		tableViewer = new TableViewer(tableComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		var table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.addListener(SWT.MeasureItem, e -> e.height = 28);

		tableViewer.setContentProvider(ArrayContentProvider.getInstance());

		int savedCol = prefs.getInt(PREF_SORT_COL, 1);
		int savedDir = prefs.getInt(PREF_SORT_DIR, SWT.DOWN);

		comparator.restoreState(savedCol, savedDir);
		tableViewer.setComparator(comparator);

		createColumns(tableViewer, tableLayout);
		if (savedCol >= 0 && savedCol < table.getColumnCount())
		{
			table.setSortColumn(table.getColumn(savedCol));
			table.setSortDirection(savedDir);
		}

		// --- Buttons ---
		var buttonComp = new Composite(group, SWT.NONE);
		buttonComp.setLayout(new GridLayout(1, true));
		buttonComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		eimLaunchBtn = createActionButton(buttonComp, StringUtil.EMPTY, StringUtil.EMPTY);
		updateLaunchButtonState();

		eimLaunchBtn.addSelectionListener(new EimButtonLaunchListener(this, Display.getDefault(),
				getConsoleStream(false), getConsoleStream(true)));

		btnActivate = createActionButton(buttonComp, "Activate Selected", "Set this version as the active ESP-IDF");
		btnReinstall = createActionButton(buttonComp, "Refresh Environment",
				"Refresh toolchains, Python virtual environment, and IDE settings for the CURRENTLY ACTIVE version");

		// --- Listeners ---
		tableViewer.addSelectionChangedListener(event -> updateButtonState());

		tableViewer.addDoubleClickListener(event -> {
			var idf = getSelectedIdf();
			if (idf != null && !idf.equals(currentInstallingNode) && !ToolsUtility.isIdfInstalledActive(idf))
				{
					performToolsSetup(idf);
				}

		});

		SelectionAdapter btnListener = new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (e.widget == btnActivate)
				{
					// Activate depends on SELECTION
					var idf = getSelectedIdf();
					if (idf != null)
						performToolsSetup(idf);
				}
				else if (e.widget == btnReinstall)
				{
					// Update Environment depends on ACTIVE STATUS (ignores selection)
					performUpdateOnActiveIdf();
				}
			}
		};
		btnActivate.addSelectionListener(btnListener);
		btnReinstall.addSelectionListener(btnListener);

		updateButtonState();
	}

	private void performUpdateOnActiveIdf()
	{
		if (tableViewer.getInput() instanceof List<?> list)
		{
			list.stream().filter(IdfRow.class::isInstance).map(o -> (IdfRow) o).filter(
					IdfRow::isActive)

					.findFirst().ifPresent(row -> performToolsSetup(row.original()));
		}
	}

	private Button createActionButton(Composite parent, String text, String tooltip)
	{
		var btn = new Button(parent, SWT.PUSH);
		btn.setText(text);
		btn.setToolTipText(tooltip);
		var gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.widthHint = 140;
		btn.setLayoutData(gd);
		return btn;
	}

	private void createColumns(TableViewer viewer, TableColumnLayout layout)
	{
		createCol(viewer, layout, "Status", 15, 0, new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				var row = (IdfRow) element;
				if (row.original().equals(currentInstallingNode))
					return "Setting up...";
				return row.isActive() ? "\u2713 Active" : StringUtil.EMPTY; //$NON-NLS-1$
			}

			@Override
			public Color getForeground(Object element)
			{
				var row = (IdfRow) element;
				if (row.original().equals(currentInstallingNode))
					return Display.getDefault().getSystemColor(SWT.COLOR_DARK_YELLOW);
				return row.isActive() ? Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN)
						: Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
			}

			@Override
			public Image getImage(Object element)
			{
				return null;
			}
		});

		createCol(viewer, layout, Messages.EspIdfManagerVersionCol, 20, 1, new ColumnLabelProvider()
		{
			@Override
			public String getText(Object e)
			{
				return ((IdfRow) e).version();
			}
		});

		createCol(viewer, layout, Messages.EspIdfManagerNameCol, 20, 2, new ColumnLabelProvider()
		{
			@Override
			public String getText(Object e)
			{
				return ((IdfRow) e).name();
			}
		});

		createCol(viewer, layout, Messages.EspIdfManagerLocationCol, 45, 3, new ColumnLabelProvider()
		{
			@Override
			public String getText(Object e)
			{
				return ((IdfRow) e).path();
			}
		});
	}

	private void createCol(TableViewer viewer, TableColumnLayout layout, String title, int weight, int sortIndex,
			ColumnLabelProvider labelProvider)
	{
		var col = new TableViewerColumn(viewer, SWT.NONE);
		col.getColumn().setText(title);
		col.setLabelProvider(labelProvider);
		layout.setColumnData(col.getColumn(), new ColumnWeightData(weight, 100, true));
		col.getColumn().addListener(SWT.Selection, e -> {
			comparator.setColumn(sortIndex);
			updateSortIndicator(col.getColumn());
			saveSortState();
		});
	}

	private void saveSortState()
	{
		prefs.putInt(PREF_SORT_COL, comparator.getPropertyIndex());
		prefs.putInt(PREF_SORT_DIR, comparator.getDirection());
		try
		{
			prefs.flush();
		}
		catch (BackingStoreException e)
		{
			Logger.log(e.toString());
		}
	}

	private void updateSortIndicator(TableColumn column)
	{
		var table = tableViewer.getTable();
		table.setSortColumn(column);
		table.setSortDirection(comparator.getDirection());
		tableViewer.refresh();
	}
	

	private void updateButtonState()
	{
		if (btnActivate == null || btnActivate.isDisposed())
			return;

		boolean isInstalling = (currentInstallingNode != null);

		if (isInstalling)
		{
			btnActivate.setEnabled(false);
			btnReinstall.setEnabled(false);
			return;
		}

		// 1. Activate Button Logic: Linked to SELECTION
		var selected = getSelectedIdf();
		if (selected == null)
		{
			btnActivate.setEnabled(false);
		}
		else
		{
			// Can only activate if it is NOT currently active
			btnActivate.setEnabled(!ToolsUtility.isIdfInstalledActive(selected));
		}

		// 2. Update Button Logic: Linked to GLOBAL STATE (Always enabled if an active ID exists)
		// We do not care what is selected; we only care if there is an active IDF to update.
		btnReinstall.setEnabled(hasActiveIdf());
	}

	private boolean hasActiveIdf()
	{
		if (tableViewer.getInput() instanceof List<?> list)
		{
			return list.stream().filter(IdfRow.class::isInstance).map(o -> (IdfRow) o).anyMatch(IdfRow::isActive);
		}
		return false;
	}

	private IdfInstalled getSelectedIdf()
	{
		var selection = (IStructuredSelection) tableViewer.getSelection();
		if (selection.isEmpty())
			return null;
		Object first = selection.getFirstElement();
		if (first instanceof IdfRow firstRow)
		{
			return firstRow.original();
		}
		else if (first instanceof IdfInstalled rawInstalled)
		{
			return rawInstalled;
		}
		return null;
	}

	private void performToolsSetup(IdfInstalled idf)
	{
		this.currentInstallingNode = idf;
		tableViewer.refresh();
		updateButtonState();

		var setupJob = new SetupToolsInIde(idf, eimJson, getConsoleStream(true), getConsoleStream(false));
		setupJob.addJobChangeListener(new SetupToolsJobListener(this, setupJob));
		setupJob.schedule();
	}

	public void refreshEditorUI()
	{
		if (container == null || container.isDisposed())
			return;

		CompletableFuture.supplyAsync(() -> {
			try
			{
				var newJson = configParser.getEimJson(true);
				if (newJson != null && newJson.getIdfInstalled() != null)
				{
					var gitPath = newJson.getGitPath();
					return newJson.getIdfInstalled().stream()
							.map(idf -> new IdfRow(idf, ToolsUtility.isIdfInstalledActive(idf),
									ToolsUtility.getIdfVersion(idf, gitPath), idf.getName(), idf.getPath()))
							.toList();
				}
				return List.<IdfRow>of();
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}).thenAcceptAsync(rows -> {
			if (container.isDisposed())
				return;
			var currentSelection = tableViewer.getSelection();
			this.currentInstallingNode = null;
			try
			{
				this.eimJson = configParser.getEimJson(false);
			}
			catch (Exception e)
			{
				Logger.log(e.toString());
			}
			updateLaunchButtonState();
			tableViewer.setInput(rows);
			tableViewer.setSelection(currentSelection);
			updateButtonState();
		}, Display.getDefault()::asyncExec).exceptionally(ex -> {
			Logger.log(ex.getCause() != null ? ex.getCause().toString() : ex.toString());
			return null;
		});
	}

	private void updateLaunchButtonState()
	{
		if (eimLaunchBtn != null && !eimLaunchBtn.isDisposed())
		{
			boolean installed = toolInitializer.isEimInstalled();
			eimLaunchBtn.setText(installed ? Messages.EIMButtonLaunchText : Messages.EIMButtonDownloadText);
			eimLaunchBtn.getParent().layout();
		}
	}

	public void setupInitialEspIdf()
	{
		if (tableViewer.getInput() instanceof List<?> list && !list.isEmpty())
		{
			var prefs = InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID);
			if (!prefs.getBoolean(EimConstants.INSTALL_TOOLS_FLAG, false))
			{
				Object first = list.get(0);
				if (first instanceof IdfRow selectedRaw)
				{
					performToolsSetup(selectedRaw.original());
				}
			}
		}
	}

	private MessageConsoleStream getConsoleStream(boolean errorStream)
	{
		return idfConsole.getConsoleStream(Messages.IDFToolsHandler_ToolsManagerConsole, null, errorStream, true);
	}

	private class IdViewerComparator extends ViewerComparator
	{
		private int propertyIndex = 0;
		private int direction = SWT.DOWN;

		public void setColumn(int column)
		{
			if (column == this.propertyIndex)
			{
				direction = (direction == SWT.DOWN) ? SWT.UP : SWT.DOWN;
			}
			else
			{
				this.propertyIndex = column;
				direction = SWT.DOWN;
			}
		}

		public int getPropertyIndex()
		{
			return propertyIndex;
		}

		public void restoreState(int column, int dir)
		{
			this.propertyIndex = column;
			this.direction = dir;
		}

		public int getDirection()
		{
			return direction;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2)
		{
			var r1 = (IdfRow) e1;
			var r2 = (IdfRow) e2;
			int rc = switch (propertyIndex)
			{
			case 0 -> Boolean.compare(r1.isActive(), r2.isActive());
			case 1 -> r1.version().compareToIgnoreCase(r2.version());
			case 2 -> r1.name().compareToIgnoreCase(r2.name());
			case 3 -> r1.path().compareToIgnoreCase(r2.path());
			default -> 0;
			};
			return (direction == SWT.UP) ? -rc : rc;
		}
	}
}
