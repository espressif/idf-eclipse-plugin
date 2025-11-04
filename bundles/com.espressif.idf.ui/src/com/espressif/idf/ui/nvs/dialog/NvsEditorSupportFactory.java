/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.nvs.dialog;

import java.util.Map;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.build.NvsTableBean;
import com.espressif.idf.core.util.NvsTableDataService;
import com.espressif.idf.core.util.StringUtil;

/**
 * Factory class responsible for creating LabelProviders and EditingSupport for the NvsCsvEditorPage's TableViewer.
 */
public class NvsEditorSupportFactory
{
	private TableViewer tableViewer;
	private Map<NvsColumn, CellEditor> cellEditors;
	private Runnable markDirtyRunnable;

	/**
	 * Constructor that takes the state from the NvsCsvEditorPage.
	 */
	public NvsEditorSupportFactory(TableViewer tableViewer, Map<NvsColumn, CellEditor> cellEditors,
			Runnable markDirtyRunnable)
	{
		this.tableViewer = tableViewer;
		this.cellEditors = cellEditors;
		this.markDirtyRunnable = markDirtyRunnable;
	}

	/**
	 * Factory method to create the correct LabelProvider for a given column.
	 */
	public NvsTableEditorLabelProvider createLabelProvider(NvsColumn column)
	{
		return new NvsTableEditorLabelProvider()
		{
			@Override
			public int getColumnIndex()
			{
				return column.getIndex();
			}

			@Override
			public String getColumnText(NvsTableBean bean)
			{
				switch (column)
				{
				case KEY:
					return bean.getKey();
				case TYPE:
					return bean.getType();
				case ENCODING:
					return bean.getEncoding();
				case VALUE:
					return bean.getValue();
				default:
					return StringUtil.EMPTY;
				}
			}

			@Override
			public String getToolTipText(Object element)
			{
				if (tableViewer.getElementAt(0).equals(element))
				{
					return Messages.NvsEditorDialog_FirstRowIsFixedInfoMsg;
				}
				return super.getToolTipText(element);
			}

			@Override
			public Color getBackground(Object element)
			{
				if (column != NvsColumn.KEY && tableViewer.getElementAt(0).equals(element))
				{
					return Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
				}
				return null;
			}
		};
	}

	/**
	 * Factory method to create the correct EditingSupport for a given column.
	 */
	public EditingSupport createEditingSupport(NvsColumn column)
	{
		switch (column)
		{
		case KEY:
			return new NvsKeyEditingSupport();
		case TYPE:
			return new NvsTypeEditingSupport();
		case ENCODING:
			return new NvsEncodingEditingSupport();
		case VALUE:
			return new NvsValueEditingSupport();
		default:
			return null;
		}
	}

	private abstract class BaseNvsEditingSupport extends EditingSupport
	{
		public BaseNvsEditingSupport()
		{
			super(NvsEditorSupportFactory.this.tableViewer);
		}

		@Override
		protected boolean canEdit(Object element)
		{
			return !tableViewer.getElementAt(0).equals(element);
		}
	}

	private class NvsKeyEditingSupport extends EditingSupport
	{
		public NvsKeyEditingSupport()
		{
			super(NvsEditorSupportFactory.this.tableViewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element)
		{
			return cellEditors.get(NvsColumn.KEY);
		}

		@Override
		protected boolean canEdit(Object element)
		{
			return true;
		}

		@Override
		protected Object getValue(Object element)
		{
			return ((NvsTableBean) element).getKey();
		}

		@Override
		protected void setValue(Object element, Object value)
		{
			((NvsTableBean) element).setKey((String) value);
			tableViewer.update(element, null);
			markDirtyRunnable.run();
		}
	}

	private class NvsTypeEditingSupport extends BaseNvsEditingSupport
	{
		@Override
		protected CellEditor getCellEditor(Object element)
		{
			return cellEditors.get(NvsColumn.TYPE);
		}

		@Override
		protected Object getValue(Object element)
		{
			String stringValue = ((NvsTableBean) element).getType();
			String[] choices = NvsTableDataService.getTypes();
			for (int i = 0; i < choices.length; i++)
			{
				if (stringValue.equals(choices[i]))
					return i;
			}
			return 0;
		}

		@Override
		protected void setValue(Object element, Object value)
		{
			NvsTableBean bean = (NvsTableBean) element;
			String newType = NvsTableDataService.getTypes()[(int) value];
			if (newType.contentEquals(bean.getType()))
			{
				return;
			}
			bean.setType(newType);

			String[] encodings = NvsTableDataService.getEncodings(bean.getType());
			((ComboBoxCellEditor) cellEditors.get(NvsColumn.ENCODING)).setItems(encodings);
			if (encodings.length > 0)
			{
				bean.setEncoding(encodings[0]);
			}

			tableViewer.update(element,
					new String[] { NvsColumn.TYPE.getDisplayName(), NvsColumn.ENCODING.getDisplayName() });
			markDirtyRunnable.run();
		}
	}

	private class NvsEncodingEditingSupport extends BaseNvsEditingSupport
	{
		@Override
		protected CellEditor getCellEditor(Object element)
		{
			NvsTableBean bean = (NvsTableBean) element;
			((ComboBoxCellEditor) cellEditors.get(NvsColumn.ENCODING))
					.setItems(NvsTableDataService.getEncodings(bean.getType()));
			return cellEditors.get(NvsColumn.ENCODING);
		}

		@Override
		protected Object getValue(Object element)
		{
			NvsTableBean bean = (NvsTableBean) element;
			String stringValue = bean.getEncoding();
			String[] choices = NvsTableDataService.getEncodings(bean.getType());
			for (int i = 0; i < choices.length; i++)
			{
				if (stringValue.equals(choices[i]))
					return i;
			}
			return 0;
		}

		@Override
		protected void setValue(Object element, Object value)
		{
			NvsTableBean bean = (NvsTableBean) element;
			String[] encodings = NvsTableDataService.getEncodings(bean.getType());
			if (encodings.length > (int) value)
			{
				bean.setEncoding(encodings[(int) value]);
			}
			tableViewer.update(element, null);
			markDirtyRunnable.run();
		}
	}

	private class NvsValueEditingSupport extends BaseNvsEditingSupport
	{
		@Override
		protected CellEditor getCellEditor(Object element)
		{
			return cellEditors.get(NvsColumn.VALUE);
		}

		@Override
		protected Object getValue(Object element)
		{
			return ((NvsTableBean) element).getValue();
		}

		@Override
		protected void setValue(Object element, Object value)
		{
			((NvsTableBean) element).setValue((String) value);
			tableViewer.update(element, null);
			markDirtyRunnable.run();
		}
	}
}