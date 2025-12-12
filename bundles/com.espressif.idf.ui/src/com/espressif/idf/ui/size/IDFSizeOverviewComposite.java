/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.espressif.idf.core.logging.Logger;

/**
 * Completely refactored overview composite to only show the basic overview of the memory size analysis
 * 
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class IDFSizeOverviewComposite
{

	private Table table;
	private JSONObject overviewJson;
	private Font boldFont;

	private enum MemoryUnit
	{
		BYTES(1, "B"), //$NON-NLS-1$
		KILOBYTES(1024, "KB"), //$NON-NLS-1$
		MEGABYTES(1024 * 1024, "MB"); //$NON-NLS-1$

		private final long divider;
		private final String label;

		MemoryUnit(long divider, String label)
		{
			this.divider = divider;
			this.label = label;
		}

		public long getDivider()
		{
			return divider;
		}

		public String getLabel()
		{
			return label;
		}
	}

	private MemoryUnit selectedUnit = MemoryUnit.KILOBYTES;

	public void createPartControl(Composite parent, IFile file, String targetName)
	{
		parent.setLayout(new GridLayout(2, false));
		parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		Label title = new Label(parent, SWT.NONE);
		title.setText(Messages.IDFSizeOverviewComposite_ApplicatoinMemoryUsage);
		title.setFont(applyBold(title.getFont()));
		title.setBackground(parent.getBackground());
		GridData titleData = new GridData(SWT.LEFT, SWT.TOP, true, false, 2, 1);
		title.setLayoutData(titleData);

		Label unitLabel = new Label(parent, SWT.NONE);
		unitLabel.setText(Messages.IDFSizeChartsComposite_SizeUnit);
		unitLabel.setBackground(parent.getBackground());

		Combo unitCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		unitCombo.setItems(new String[] { "Bytes", "KB", "MB" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		unitCombo.select(1);
		unitCombo.addListener(SWT.Selection, e -> {
			selectedUnit = switch (unitCombo.getSelectionIndex())
			{
			case 0 -> MemoryUnit.BYTES;
			case 2 -> MemoryUnit.MEGABYTES;
			default -> MemoryUnit.KILOBYTES;
			};
			populateTable();
		});

		// Table
		table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.NO_FOCUS);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		String[] columns = { "Region", "Used", "Free", "Total", "Usage" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		for (String colName : columns)
		{
			TableColumn col = new TableColumn(table, SWT.NONE);
			col.setText(colName);
			col.setWidth(150);
		}

		overviewJson = getIDFSizeOverviewData(file, targetName);
		populateTable();

		// Bold header
		Font headerFont = applyBold(table.getFont());
		table.setFont(headerFont);
	}

	private void populateTable()
	{
		table.removeAll();

		// Defensive check: Handle unexpected empty data gracefully to prevent UI crash.
		if (overviewJson == null || overviewJson.isEmpty() || !overviewJson.containsKey("layout")) //$NON-NLS-1$
		{
			TableItem errorItem = new TableItem(table, SWT.NONE);
			errorItem.setText(0,
					Messages.IDFSizeOverviewComposite_NoExpectedOutputMsg);
			errorItem.setForeground(0, table.getDisplay().getSystemColor(SWT.COLOR_RED));
			return;
		}

		JSONArray layout = (JSONArray) overviewJson.get("layout"); //$NON-NLS-1$
		long grandUsed = 0;
		long grandFree = 0;

		int rowIndex = 0;
		for (Object obj : layout)
		{
			JSONObject section = (JSONObject) obj;
			String name = (String) section.get("name"); //$NON-NLS-1$
			long used = (long) section.get("used"); //$NON-NLS-1$
			long free = (long) section.get("free"); //$NON-NLS-1$
			long total = (long) section.get("total"); //$NON-NLS-1$

			grandUsed += used;
			grandFree += free;

			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(new String[] { name, formatMemory(used), formatMemory(free), formatMemory(total), "" // progress //$NON-NLS-1$
																												// bar
																												// will
																												// go
																												// here
			});
			applyBoldToColumn(item, 0);

			createProgressBar(rowIndex, used, total);
			rowIndex++;
		}

		// Total row
		TableItem totalRow = new TableItem(table, SWT.NONE);
		totalRow.setText(new String[] { "Total:", formatMemory(grandUsed), formatMemory(grandFree), //$NON-NLS-1$
				formatMemory(grandUsed + grandFree), "" }); //$NON-NLS-1$
		for (int i = 0; i < 5; i++)
		{
			applyBoldToColumn(totalRow, i);
		}
		createProgressBar(rowIndex, grandUsed, grandUsed + grandFree);
	}

	private void createProgressBar(int rowIndex, long used, long total)
	{
		if (total == 0)
			return;

		TableEditor editor = new TableEditor(table);
		editor.grabHorizontal = true;
		editor.grabVertical = true;
		editor.horizontalAlignment = SWT.FILL;

		Composite barComposite = new Composite(table, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		barComposite.setLayout(layout);
		barComposite.setBackground(table.getBackground());

		ProgressBar bar = new ProgressBar(barComposite, SWT.HORIZONTAL);
		bar.setMaximum((int) total);
		bar.setSelection((int) used);
		GridData barData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		barData.widthHint = 100;
		bar.setLayoutData(barData);

		Label percent = new Label(barComposite, SWT.NONE);
		percent.setText(getUsagePercent(used, total));
		percent.setBackground(barComposite.getBackground());
		percent.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		editor.setEditor(barComposite, table.getItem(rowIndex), 4);
	}

	private void applyBoldToColumn(TableItem item, int columnIndex)
	{
		Font bold = applyBold(item.getFont());
		item.setFont(columnIndex, bold);
	}

	private String formatMemory(long bytes)
	{
		double value = (double) bytes / selectedUnit.getDivider();
		return String.format("%.1f %s", value, selectedUnit.getLabel()); //$NON-NLS-1$
	}

	private String getUsagePercent(long used, long total)
	{
		if (total == 0)
			return "-"; //$NON-NLS-1$
		double percent = (double) used / total * 100;
		return String.format("%.1f%%", percent); //$NON-NLS-1$
	}

	private Font applyBold(Font original)
	{
		if (boldFont != null)
			return boldFont;
		FontData[] data = original.getFontData();
		for (FontData d : data)
		{
			d.setStyle(SWT.BOLD);
		}
		boldFont = new Font(original.getDevice(), data);
		return boldFont;
	}

	protected JSONObject getIDFSizeOverviewData(IFile file, String targetName)
	{
		try
		{
			return new IDFSizeDataManager().getIDFSizeOverview(file);
		}
		catch (Exception e)
		{
			Logger.log(e);
			return null;
		}
	}
}
