package com.espressif.idf.ui.partitiontable.dialog;

import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Table;

public class ComboBoxTextCellEditor extends ComboBoxCellEditor
{
	public ComboBoxTextCellEditor(Table table, String[] properties, int style)
	{
		super(table, properties, style);
	}

	@Override
	protected void doSetValue(Object value)
	{
		if (value instanceof String)
		{
			((CCombo) getControl()).setText((String) value);
		}
		else
		{
			super.doSetValue(value);
		}
	}

	@Override
	protected Object doGetValue()
	{
		final Object value = super.doGetValue();
		if (value instanceof Integer && (Integer) value == -1)
		{
			return ((CCombo) getControl()).getText();
		}
		return value;
	}
}
