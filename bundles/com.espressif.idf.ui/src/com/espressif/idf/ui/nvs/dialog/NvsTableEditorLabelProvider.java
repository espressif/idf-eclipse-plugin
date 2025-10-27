package com.espressif.idf.ui.nvs.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.build.NvsTableBean;
import com.espressif.idf.core.util.NvsBeanValidator;
import com.espressif.idf.core.util.StringUtil;

public class NvsTableEditorLabelProvider extends CellLabelProvider
{

	private final NvsBeanValidator validator = new NvsBeanValidator();

	@Override
	public void update(ViewerCell cell)
	{
		NvsTableBean bean = (NvsTableBean) cell.getElement();
		int columnIndex = cell.getColumnIndex();

		switch (columnIndex)
		{
		case 0:
			cell.setText(bean.getKey());
			break;
		case 1:
			cell.setText(bean.getType());
			break;
		case 2:
			cell.setText(bean.getEncoding());
			break;
		case 3:
			cell.setText(bean.getValue());
			break;
		default:
			cell.setText(StringUtil.EMPTY);
			break;
		}

		String cellStatus = validator.validateBean(bean, columnIndex);
		if (!cellStatus.isBlank())
		{
			cell.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
			cell.setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR));
		}
		else
		{
			cell.setForeground(null);
			cell.setImage(null);
		}

		cell.setBackground(null);
	}

	@Override
	public String getToolTipText(Object element)
	{
		StringBuilder tooltip = new StringBuilder();
		NvsTableBean bean = (NvsTableBean) element;

		for (int col = 0; col < 4; col++)
		{
			String status = validator.validateBean(bean, col);
			if (!status.isBlank())
			{
				tooltip.append("Column ").append(col).append(": ").append(status).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}

		return tooltip.isEmpty() ? null : tooltip.toString().trim();
	}
}