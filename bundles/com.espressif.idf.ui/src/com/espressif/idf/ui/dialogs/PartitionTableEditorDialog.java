package com.espressif.idf.ui.dialogs;

import java.awt.Button;

import org.commonmark.node.Text;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

public class PartitionTableEditorDialog extends TitleAreaDialog
{
	private Text csvPathTextFieldText;
	private Button csvSearchButton;
	private Table csvTable;
	private TableViewer tableViewer;
	private String[] columnNames = { "Name", "Type", "Sub Type", "Offset", "Size", "Encrypted" };

	public PartitionTableEditorDialog(Shell parentShell)
	{
		super(parentShell);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void create()
	{
		// TODO Auto-generated method stub
		super.create();
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(4, false);
		container.setLayout(layout);
		return super.createDialogArea(parent);
	}

}
