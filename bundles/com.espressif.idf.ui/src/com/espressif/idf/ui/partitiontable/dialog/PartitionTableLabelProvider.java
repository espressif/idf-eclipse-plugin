package com.espressif.idf.ui.partitiontable.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.build.PartitionTableBean;
import com.espressif.idf.core.util.PartitionBeanValidator;

public class PartitionTableLabelProvider extends CellLabelProvider implements ITableLabelProvider, ITableColorProvider
{
	@Override
	public void addListener(ILabelProviderListener listener)
	{
	}

	public PartitionTableLabelProvider()
	{
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public boolean isLabelProperty(Object element, String property)
	{
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener)
	{
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex)
	{
		String status = new PartitionBeanValidator().validateBean((PartitionTableBean) element, columnIndex);
		if (!status.isBlank())
		{
			return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		PartitionTableBean bean = (PartitionTableBean) element;
		switch (columnIndex)
		{
		case 0:
			return bean.getName();
		case 1:
			return bean.getType();
		case 2:
			return bean.getSubType();
		case 3:
			return bean.getOffSet();
		case 4:
			return bean.getSize();
		case 5:
			return bean.getFlag();
		default:
			break;
		}
		return null;
	}

	@Override
	public Color getForeground(Object element, int columnIndex)
	{
		String status = new PartitionBeanValidator().validateBean((PartitionTableBean) element, columnIndex);
		if (!status.isBlank())
		{

			return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
		}

		return null;
	}

	@Override
	public Color getBackground(Object element, int columnIndex)
	{
		return null;
	}

	@Override
	public void update(ViewerCell cell)
	{

	}

}
