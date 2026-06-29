/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.manager.pages;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
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
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsoleStream;
import org.osgi.service.prefs.BackingStoreException;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.EimConstants;
import com.espressif.idf.core.tools.EimIdfConfiguratinParser;
import com.espressif.idf.core.tools.SetupToolsInIde;
import com.espressif.idf.core.tools.ToolInitializer;
import com.espressif.idf.core.tools.eimjson.model.EimConfigModel;
import com.espressif.idf.core.tools.eimjson.model.EimInstallationModel;
import com.espressif.idf.core.tools.eimjson.presentation.EimInstallationPresentation;
import com.espressif.idf.core.tools.eimjson.presentation.EimInstallationPresentationRenderer;
import com.espressif.idf.core.tools.eimjson.presentation.EimInstallationPresentationRendererFactory;
import com.espressif.idf.core.tools.util.ToolsUtility;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.IDFConsole;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.tools.EimButtonLaunchListener;
import com.espressif.idf.ui.tools.Messages;
import com.espressif.idf.ui.tools.SetupToolsJobListener;
import com.espressif.idf.ui.tools.manager.render.EimInstallationStatusSwtMapper;

/**
 * Main UI class for all listing and interacting with the tools
 * 
 * @author Ali Azam Rana
 * @author Denys Almazov
 *
 */
public class ESPIDFMainTablePage
{

	private static final String PREF_SORT_COL = "EspIdfManager_SortCol"; //$NON-NLS-1$
	private static final String PREF_SORT_DIR = "EspIdfManager_SortDir"; //$NON-NLS-1$
	private final IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID);

	private record IdfRow(EimInstallationModel installation, EimInstallationPresentation presentation,
			boolean isActive, String version, String name, String path)
	{
	}

	private Composite container;
	private TableViewer tableViewer;
	private Button btnActivate;
	private Button btnReinstall;
	private Button eimLaunchBtn;

	private final IdViewerComparator comparator = new IdViewerComparator();
	private EimConfigModel eimConfigModel;
	private EimInstallationPresentationRenderer presentationRenderer;

	private final EimIdfConfiguratinParser configParser;
	private final IDFConsole idfConsole = new IDFConsole();
	private String currentInstallingId = null;

	public ESPIDFMainTablePage()
	{
		this.configParser = new EimIdfConfiguratinParser();
		new ToolInitializer(InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID));
	}

	public Composite createPage(Composite parent)
	{
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));

		createHeader(container);
		createMainContent(container);
		refreshEditorUI();

		return container;
	}

	private void createHeader(Composite parent)
	{
		var headerComp = new Composite(parent, SWT.NONE);
		headerComp.setLayout(new GridLayout(1, false));
		headerComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		CLabel infoLabel = new CLabel(headerComp, SWT.NONE);
		infoLabel.setImage(headerComp.getDisplay().getSystemImage(SWT.ICON_INFORMATION));
		infoLabel.setText(Messages.IDFInfoLabel_Text);
		infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	private void createMainContent(Composite parent)
	{
		var group = new Group(parent, SWT.NONE);
		group.setText(Messages.ESPIDFMainTablePage_MainContentGroupLbl);
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

		eimLaunchBtn = createActionButton(buttonComp, Messages.EIMButtonLaunchText,
				Messages.ESPIDFMainTablePage_EIMButtonTooltip);

		eimLaunchBtn.addSelectionListener(new EimButtonLaunchListener(this, Display.getDefault(),
				getConsoleStream(false), getConsoleStream(true)));

		btnActivate = createActionButton(buttonComp, Messages.ESPIDFMainTablePage_ActiveBtnName, Messages.ESPIDFMainTablePage_ActiveBtnTooltip);
		btnReinstall = createActionButton(buttonComp, Messages.ESPIDFMainTablePage_RefreshEnvBtnName,
				Messages.ESPIDFMainTablePage_RefreshEnvBtnTooltip);

		// --- Listeners ---
		tableViewer.addSelectionChangedListener(event -> updateButtonState());

		tableViewer.addDoubleClickListener(event -> {
			var idf = getSelectedInstallation();
			if (idf != null && !idf.getId().equals(currentInstallingId) && idf.isActivatable()
					&& !ToolsUtility.isIdfInstalledActive(idf))
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
					var idf = getSelectedInstallation();
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

					.findFirst().ifPresent(row -> performToolsSetup(row.installation()));
		}
	}

	private Button createActionButton(Composite parent, String text, String tooltip)
	{
		var btn = new Button(parent, SWT.PUSH);
		btn.setText(text);
		btn.setToolTipText(tooltip);
		var gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		btn.setLayoutData(gd);
		return btn;
	}

	private void createColumns(TableViewer viewer, TableColumnLayout layout)
	{
		createCol(viewer, layout, Messages.ESPIDFMainTablePage_StatusColumnName, 15, 0, new ColumnLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				IdfRow row = (IdfRow) cell.getElement();
				EimInstallationPresentation presentation = resolvePresentation(row);
				cell.setText(presentation.getStatusText());
				cell.setForeground(EimInstallationStatusSwtMapper.foreground(presentation,
						cell.getControl().getDisplay()));
			}

			@Override
			public String getText(Object element)
			{
				return resolvePresentation((IdfRow) element).getStatusText();
			}

			@Override
			public Color getForeground(Object element)
			{
				return EimInstallationStatusSwtMapper.foreground(resolvePresentation((IdfRow) element),
						Display.getDefault());
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
				var version = ((IdfRow) e).version();
				if (StringUtil.isEmpty(version))
				{
					return Messages.ESPIDFMainTablePage_VersionDetectionFailedMsg;
				}
				return version;
			}

			@Override
			public Image getImage(Object element)
			{
				if (StringUtil.isEmpty(((IdfRow) element).version()))
				{
					return PlatformUI.getWorkbench().getSharedImages()
							.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
				}
				return super.getImage(element);
			}

			@Override
			public String getToolTipText(Object element)
			{
				var version = ((IdfRow) element).version();
				if (StringUtil.isEmpty(version))
				{
					return Messages.ESPIDFMainTablePage_VersionErrorToolTip;
				}
				return Messages.ESPIDFMainTablePage_VersionToolTip + version;
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

		ColumnViewerToolTipSupport.enableFor(viewer);
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

		boolean isInstalling = (currentInstallingId != null);

		if (isInstalling)
		{
			btnActivate.setEnabled(false);
			btnReinstall.setEnabled(false);
			return;
		}

		var selected = getSelectedRow();
		if (selected == null)
		{
			btnActivate.setEnabled(false);
		}
		else
		{
			btnActivate.setEnabled(selected.presentation().isActivateEnabled() && selected.installation().isActivatable());
		}

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

	private IdfRow getSelectedRow()
	{
		var selection = (IStructuredSelection) tableViewer.getSelection();
		if (selection.isEmpty())
			return null;
		Object first = selection.getFirstElement();
		if (first instanceof IdfRow row)
		{
			return row;
		}
		return null;
	}

	private EimInstallationModel getSelectedInstallation()
	{
		var row = getSelectedRow();
		return row != null ? row.installation() : null;
	}

	public void clearInstallingState()
	{
		currentInstallingId = null;
		if (tableViewer != null && !tableViewer.getControl().isDisposed())
		{
			tableViewer.refresh();
		}
		updateButtonState();
	}

	/**
	 * Status column presentation: while {@link #currentInstallingId} is set, show
	 * in-progress state without baking it into {@link IdfRow}. Full reload keeps
	 * steady-state presentation in each row.
	 */
	private EimInstallationPresentation resolvePresentation(IdfRow row)
	{
		if (presentationRenderer != null && currentInstallingId != null
				&& currentInstallingId.equals(row.installation().getId()))
		{
			return presentationRenderer.render(row.installation(), row.isActive(), true);
		}
		return row.presentation();
	}

	private void performToolsSetup(EimInstallationModel installation)
	{
		if (eimConfigModel == null)
		{
			Logger.log("Cannot activate ESP-IDF: eim_idf.json is not loaded"); //$NON-NLS-1$
			return;
		}

		this.currentInstallingId = installation.getId();
		tableViewer.refresh();
		updateButtonState();

		var setupJob = new SetupToolsInIde(installation, eimConfigModel, getConsoleStream(true),
				getConsoleStream(false));
		setupJob.addJobChangeListener(new SetupToolsJobListener(this, setupJob));
		setupJob.schedule();
	}

	public void refreshEditorUI()
	{
		if (container == null || container.isDisposed())
			return;

		Job refreshJob = new Job(Messages.ESPIDFMainTablePage_RefreshingIdfJobName)
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				monitor.beginTask(Messages.ESPIDFMainTablePage_ScanningProcessTaskName, IProgressMonitor.UNKNOWN);

				try
				{
					eimConfigModel = configParser.getConfigModel(true);
					presentationRenderer = eimConfigModel != null
							? EimInstallationPresentationRendererFactory
									.forSchema(eimConfigModel.getSchemaVersion())
							: null;

					List<IdfRow> rows = List.of();

					if (eimConfigModel != null && eimConfigModel.getInstallations() != null
							&& presentationRenderer != null)
					{
						monitor.subTask(Messages.ESPIDFMainTablePage_DetectingEspIdfSubTaskName);

						final EimInstallationPresentationRenderer renderer = presentationRenderer;
						try (var executor = Executors.newVirtualThreadPerTaskExecutor())
						{
							var futures = eimConfigModel.getInstallations().stream()
									.map(idf -> CompletableFuture.supplyAsync(() -> {
										boolean isActive = ToolsUtility.isIdfInstalledActive(idf);
										var presentation = renderer.render(idf, isActive, false);
										String detectedVersion = ToolsUtility.getIdfVersion(idf);
										return new IdfRow(idf, presentation, isActive, detectedVersion,
												idf.getName(), idf.getPath());
									}, executor)).toList();

							rows = futures.stream().map(CompletableFuture::join).toList();
						}
					}

					final List<IdfRow> finalRows = rows;

					Display.getDefault().asyncExec(() -> {
						if (container.isDisposed())
							return;

						var currentSelection = tableViewer.getSelection();

						tableViewer.setInput(finalRows);
						tableViewer.setSelection(currentSelection);
						updateButtonState();
					});

					return Status.OK_STATUS;
				}
				catch (Exception e)
				{
					Logger.log(e);
					return new Status(IStatus.ERROR, UIPlugin.PLUGIN_ID, Messages.ESPIDFMainTablePage_FailderRefreshMsg, e);
				} finally
				{
					monitor.done();
				}
			}
		};
		refreshJob.schedule();
	}

	public void setupInitialEspIdf()
	{
		if (container == null || container.isDisposed() || tableViewer == null
				|| prefs.getBoolean(EimConstants.INSTALL_TOOLS_FLAG, false))
		{
			return;
		}

		if (tableViewer.getTable().getItemCount() == 0)
		{
			return;
		}

		if (tableViewer.getElementAt(0) instanceof IdfRow firstIdf)
		{
			if (!firstIdf.installation().isActivatable())
			{
				return;
			}
			tableViewer.setSelection(new StructuredSelection(firstIdf), true);
			performToolsSetup(firstIdf.installation());
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
			case 0 -> resolvePresentation(r1).getStatusText()
					.compareToIgnoreCase(resolvePresentation(r2).getStatusText());
			case 1 -> r1.version().compareToIgnoreCase(r2.version());
			case 2 -> r1.name().compareToIgnoreCase(r2.name());
			case 3 -> r1.path().compareToIgnoreCase(r2.path());
			default -> 0;
			};
			return (direction == SWT.UP) ? -rc : rc;
		}
	}
}
