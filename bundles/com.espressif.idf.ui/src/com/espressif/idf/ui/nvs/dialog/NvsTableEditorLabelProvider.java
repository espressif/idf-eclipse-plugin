package com.espressif.idf.ui.nvs.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.build.NvsTableBean;
import com.espressif.idf.core.util.NvsBeanValidator;

public abstract class NvsTableEditorLabelProvider extends CellLabelProvider
{

	protected NvsBeanValidator validator = new NvsBeanValidator();

	/**
	 * Subclasses must tell the provider which column index they are for (0, 1, 2, or 3).
	 */
	public abstract int getColumnIndex();

	/**
	 * Subclasses must provide the text for their specific column.
	 */
	public abstract String getColumnText(NvsTableBean bean);

	@Override
	public void update(ViewerCell cell)
	{
		NvsTableBean bean = (NvsTableBean) cell.getElement();

		// 1. Set text (delegated to subclass)
		cell.setText(getColumnText(bean));

		// 2. Set color/image (uses column index from subclass)
		String cellStatus = validator.validateBean(bean, getColumnIndex());
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

	/**
	 * This method is called by ColumnViewerToolTipSupport *only* when the mouse is over this provider's column. It
	 * provides the tooltip for ONLY this cell.
	 */
	@Override
	public String getToolTipText(Object element)
	{
		NvsTableBean bean = (NvsTableBean) element;

		// 1. Get validation status for this specific cell
		String status = validator.validateBean(bean, getColumnIndex());

		// 2. Return the validation message or null (no tooltip)
		return status.isBlank() ? null : status;
	}
}