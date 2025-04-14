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

public class IDFSizeOverviewComposite {

	private Table table;
	private JSONObject overviewJson;
	private Font boldFont;

	private enum MemoryUnit {
		BYTES(1, "B"), KILOBYTES(1024, "KB"), MEGABYTES(1024 * 1024, "MB");

		private final long divider;
		private final String label;

		MemoryUnit(long divider, String label) {
			this.divider = divider;
			this.label = label;
		}

		public long getDivider() {
			return divider;
		}

		public String getLabel() {
			return label;
		}
	}

	private MemoryUnit selectedUnit = MemoryUnit.KILOBYTES;

	public void createPartControl(Composite parent, IFile file, String targetName) {
		parent.setLayout(new GridLayout(2, false));
		parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		Label title = new Label(parent, SWT.NONE);
		title.setText("ESP–IDF Application Memory Usage"); //$NON-NLS-1$
		title.setFont(applyBold(title.getFont()));
		title.setBackground(parent.getBackground());
		GridData titleData = new GridData(SWT.LEFT, SWT.TOP, true, false, 2, 1);
		title.setLayoutData(titleData);

		Label unitLabel = new Label(parent, SWT.NONE);
		unitLabel.setText("Size Unit:"); //$NON-NLS-1$
		unitLabel.setBackground(parent.getBackground());

		Combo unitCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		unitCombo.setItems(new String[] { "Bytes", "KB", "MB" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		unitCombo.select(1);
		unitCombo.addListener(SWT.Selection, e -> {
			selectedUnit = switch (unitCombo.getSelectionIndex()) {
				case 0 -> MemoryUnit.BYTES;
				case 2 -> MemoryUnit.MEGABYTES;
				default -> MemoryUnit.KILOBYTES;
			};
			populateTable();
		});

		// Table
		table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		String[] columns = { "Region", "Used", "Free", "Total", "Usage" };
		for (String colName : columns) {
			TableColumn col = new TableColumn(table, SWT.NONE);
			col.setText(colName);
			col.setWidth(150);
		}

		overviewJson = getIDFSizeOverviewData(file, targetName);
		populateTable();

		// Bold header
		Font headerFont = applyBold(table.getFont());
//		for (TableColumn col : table.getColumns()) {
//			col.pack();
//		}
		table.setFont(headerFont);
	}

	private void populateTable() {
		table.removeAll();

		JSONArray layout = (JSONArray) overviewJson.get("layout");
		long grandUsed = 0;
		long grandFree = 0;

		int rowIndex = 0;
		for (Object obj : layout) {
			JSONObject section = (JSONObject) obj;
			String name = (String) section.get("name");
			long used = (long) section.get("used");
			long free = (long) section.get("free");
			long total = (long) section.get("total");

			grandUsed += used;
			grandFree += free;

			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(new String[] {
				name,
				formatMemory(used),
				formatMemory(free),
				formatMemory(total),
				"" // progress bar will go here
			});
			applyBoldToColumn(item, 0);

			createProgressBar(rowIndex, used, total);
			rowIndex++;
		}

		// Total row
		TableItem totalRow = new TableItem(table, SWT.NONE);
		totalRow.setText(new String[] {
			"Total:",
			formatMemory(grandUsed),
			formatMemory(grandFree),
			formatMemory(grandUsed + grandFree),
			""
		});
		for (int i = 0; i < 5; i++) {
			applyBoldToColumn(totalRow, i);
		}
		createProgressBar(rowIndex, grandUsed, grandUsed + grandFree);
	}

	private void createProgressBar(int rowIndex, long used, long total) {
		if (total == 0) return;

		TableEditor editor = new TableEditor(table);
		editor.grabHorizontal = true;

		Composite barComposite = new Composite(table, SWT.NONE);
		barComposite.setLayout(new GridLayout(2, false));

		ProgressBar bar = new ProgressBar(barComposite, SWT.HORIZONTAL);
		bar.setMaximum((int) total);
		bar.setSelection((int) used);
		bar.setLayoutData(new GridData(80, SWT.DEFAULT));

		Label percent = new Label(barComposite, SWT.NONE);
		percent.setText(getUsagePercent(used, total));
		percent.setBackground(barComposite.getBackground());

		editor.setEditor(barComposite, table.getItem(rowIndex), 4);
	}

	private void applyBoldToColumn(TableItem item, int columnIndex) {
		Font bold = applyBold(item.getFont());
		item.setFont(columnIndex, bold);
	}

	private String formatMemory(long bytes) {
		double value = (double) bytes / selectedUnit.getDivider();
		return String.format("%.1f %s", value, selectedUnit.getLabel()); //$NON-NLS-1$
	}

	private String getUsagePercent(long used, long total) {
		if (total == 0) return "-";
		double percent = (double) used / total * 100;
		return String.format("%.1f%%", percent); //$NON-NLS-1$
	}

	private Font applyBold(Font original) {
		if (boldFont != null) return boldFont;
		FontData[] data = original.getFontData();
		for (FontData d : data) {
			d.setStyle(SWT.BOLD);
		}
		boldFont = new Font(original.getDevice(), data);
		return boldFont;
	}

	protected JSONObject getIDFSizeOverviewData(IFile file, String targetName) {
		try {
			return new IDFSizeDataManager().getIDFSizeOverview(file, targetName);
		} catch (Exception e) {
			Logger.log(e);
			return null;
		}
	}
}
