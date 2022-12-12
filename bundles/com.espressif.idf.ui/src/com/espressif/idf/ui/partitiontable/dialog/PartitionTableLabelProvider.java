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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.espressif.idf.core.build.PartitionTableBean;
import com.espressif.idf.core.util.PartitionBeanValidator;

public class PartitionTableLabelProvider extends CellLabelProvider implements ITableLabelProvider, ITableColorProvider
{

	private static final String CHECKED_KEY = "CHECKED"; //$NON-NLS-1$
	private static final String UNCHECK_KEY = "UNCHECKED"; //$NON-NLS-1$

	@Override
	public void addListener(ILabelProviderListener listener)
	{
	}

	public PartitionTableLabelProvider()
	{
		Shell shell = Display.getDefault().getActiveShell();
		if (JFaceResources.getImageRegistry().getDescriptor(CHECKED_KEY) == null)
		{
			JFaceResources.getImageRegistry().put(UNCHECK_KEY, makeShot(shell, false));
			JFaceResources.getImageRegistry().put(CHECKED_KEY, makeShot(shell, true));
		}
	}

	@Override
	public void dispose()
	{
	}

	private Image makeShot(Shell shell, boolean type)
	{
		Shell s = new Shell(shell, SWT.NO_TRIM);
		Button b = new Button(s, SWT.CHECK);
		b.setSelection(type);
		Point bsize = b.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		bsize.x = Math.max(bsize.x - 1, bsize.y - 1);
		bsize.y = Math.max(bsize.x - 1, bsize.y - 1);
		b.setSize(bsize);
		b.setLocation(1, 1);
		s.setSize(bsize);
		s.open();
		GC gc = new GC(b);

		Image image = new Image(shell.getDisplay(), bsize.x, bsize.y);
		gc.copyArea(image, -1, 0);
		gc.dispose();
		s.close();
		return image;
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
		if (columnIndex == 5)
		{
			Image checkBoxImage = ((PartitionTableBean) element).getFlag().contentEquals("encrypted") //$NON-NLS-1$
					? JFaceResources.getImage(CHECKED_KEY)
					: JFaceResources.getImage(UNCHECK_KEY);
			return checkBoxImage;
		}
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
