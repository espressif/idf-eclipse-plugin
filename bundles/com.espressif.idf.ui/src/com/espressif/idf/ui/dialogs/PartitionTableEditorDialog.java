package com.espressif.idf.ui.dialogs;

import java.awt.Button;

import org.commonmark.node.Text;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

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
		csvTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);

		TableColumn nameColumn = new TableColumn(csvTable, SWT.NULL);
		nameColumn.setText(columnNames[0]);

		TableColumn typeColumn = new TableColumn(csvTable, SWT.NULL);
		typeColumn.setText(columnNames[1]);

		TableColumn subTypeColumn = new TableColumn(csvTable, SWT.NULL);
		subTypeColumn.setText(columnNames[2]);

		TableColumn offSetColumn = new TableColumn(csvTable, SWT.NULL);
		offSetColumn.setText(columnNames[3]);

		TableColumn sizeColumn = new TableColumn(csvTable, SWT.NULL);
		sizeColumn.setText(columnNames[4]);

		TableColumn encryptedColumn = new TableColumn(csvTable, SWT.NULL);
		encryptedColumn.setText(columnNames[5]);

		tableViewer = new TableViewer(csvTable);
		return super.createDialogArea(parent);
	}

}
