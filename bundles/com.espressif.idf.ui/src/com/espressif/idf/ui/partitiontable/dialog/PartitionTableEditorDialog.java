package com.espressif.idf.ui.partitiontable.dialog;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;

import com.espressif.idf.core.build.PartitionTableBean;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.PartitionBeanValidator;
import com.espressif.idf.core.util.StringUtil;

public class PartitionTableEditorDialog extends Dialog
{
	private Table csvTable;
	private TableViewer tableViewer;
	@SuppressWarnings("nls")
	private final String[] columnNames = { "Name", "Type", "Sub Type", "Offset", "Size", "Encrypted" };
	private final String FLAGS_VALUE = "encrypted"; //$NON-NLS-1$
	private Action addAction;
	private Action saveAction;
	private Action deleteAction;
	private CellEditor[] cellEditors;
	private ToolBar toolBar;
	private IFile csvFile;

	public PartitionTableEditorDialog(Shell parentShell)
	{
		super(parentShell);
	}

	@Override
	public void create()
	{
	}

	public void create(IFile csvFile)
	{
		this.csvFile = csvFile;
		super.create();
		this.getShell().setText(Messages.PartitionTableEditorDialog_Title);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		createToolBar(parent);
		createTable(parent);
		createColumns();
		createTableViewer();

		return super.createDialogArea(parent);
	}

	private void createTable(Composite parent)
	{
		csvTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		csvTable.setHeaderVisible(true);
		csvTable.setLinesVisible(true);
		csvTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		csvTable.addMouseTrackListener(new MouseTrackListener()
		{
			Shell errorToolTip;

			@Override
			public void mouseHover(MouseEvent event)
			{

				ViewerCell item = tableViewer.getCell(new Point(event.x, event.y));
				if (item != null)
				{
					if (errorToolTip != null && !errorToolTip.isDisposed())
						errorToolTip.dispose();
					errorToolTip = new Shell(getShell(), SWT.ON_TOP | SWT.TOOL);
					errorToolTip.setLayout(new FillLayout());
					Label label = new Label(errorToolTip, SWT.NONE);
					label.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
					label.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
					label.setText(new PartitionBeanValidator().validateBean((PartitionTableBean) item.getElement(),
							item.getColumnIndex()));

					Point size = errorToolTip.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					Rectangle rect = item.getBounds();
					Point pt = csvTable.toDisplay(rect.x, rect.y);
					errorToolTip.setBounds(pt.x, pt.y, size.x, size.y);
					errorToolTip.setVisible(true);
					csvTable.addMouseMoveListener(e -> errorToolTip.dispose());
				}
			}

			@Override
			public void mouseExit(MouseEvent e)
			{
			}

			@Override
			public void mouseEnter(MouseEvent e)
			{
			}
		});
	}

	private void createToolBar(Composite parent)
	{
		createAddNewAction();
		createDeleteAction();
		createSaveAction();
		toolBar = new ToolBar(parent, SWT.RIGHT | SWT.FLAT);
		ToolBarManager manager = new ToolBarManager(toolBar);
		manager.add(addAction);
		manager.add(deleteAction);
		manager.add(saveAction);
		manager.update(true);
	}

	private void createTableViewer()
	{
		tableViewer = new TableViewer(csvTable);
		tableViewer.setContentProvider(new IStructuredContentProvider()
		{

			@SuppressWarnings("unchecked")
			@Override
			public Object[] getElements(Object inputElement)
			{
				List<PartitionTableBean> list = (List<PartitionTableBean>) inputElement;
				return list.toArray();
			}
		});

		try
		{
			List<PartitionTableBean> list = PartitionTableBean.parseCsv(Paths.get(csvFile.getLocationURI()));
			tableViewer.setInput(list);
			for (PartitionTableBean bean : list)
			{
				tableViewer.update(bean, null);
			}
		}
		catch (IOException e)
		{
			Logger.log(e);
		}

		tableViewer.setLabelProvider(new PartitionTableLabelProvider());

		ColumnViewerToolTipSupport.enableFor(tableViewer);
		// Set cell editors
		tableViewer.setColumnProperties(columnNames);
		cellEditors = new CellEditor[6];
		cellEditors[0] = new TextCellEditor(csvTable);
		cellEditors[1] = new ComboBoxTextCellEditor(csvTable, PartitionTableBean.getTypeValues(), SWT.BORDER);
		cellEditors[2] = new ComboBoxTextCellEditor(csvTable, PartitionTableBean.getSubTypeValues(StringUtil.EMPTY),
				SWT.BORDER);
		cellEditors[3] = cellEditors[0];
		cellEditors[4] = cellEditors[0];
		cellEditors[5] = new CheckboxCellEditor(csvTable, SWT.CHECK | SWT.BORDER);
		tableViewer.setCellEditors(cellEditors);
		tableViewer.setCellModifier(new ICellModifier()
		{

			@Override
			public void modify(Object element, String property, Object value)
			{
				int index = -1;
				for (int i = 0; i < columnNames.length; i++)
				{
					if (columnNames[i].equals(property))
					{
						index = i;
						break;
					}
				}
				PartitionTableBean bean = null;
				if (element instanceof Item)
				{
					TableItem item = (TableItem) element;
					bean = (PartitionTableBean) item.getData();
				}
				else
				{
					bean = (PartitionTableBean) element;
				}

				switch (index)
				{
				case 0:
					bean.setName((String) value);
					break;
				case 1:
					if (value instanceof String)
					{
						bean.setType((String) value);
					}
					else
					{
						bean.setType(PartitionTableBean.getTypeValues()[(int) value]);
					}
					((ComboBoxCellEditor) cellEditors[2]).setItems(PartitionTableBean.getSubTypeValues(bean.getType()));
					tableViewer.refresh();
					break;
				case 2:
					tableViewer.refresh();
					if (value instanceof String)
					{
						bean.setSubType((String) value);
					}
					else
					{
						bean.setSubType(PartitionTableBean.getSubTypeValues(bean.getType())[(int) value]);
					}
					break;
				case 3:
					bean.setOffSet((String) value);
					break;
				case 4:
					bean.setSize((String) value);
					break;
				case 5:
					bean.setFlag((boolean) value ? FLAGS_VALUE : StringUtil.EMPTY);
					break;
				default:
					break;
				}
				tableViewer.update(bean, null);
			}

			@Override
			public Object getValue(Object element, String property)
			{
				int index = -1;
				for (int i = 0; i < columnNames.length; i++)
				{
					if (columnNames[i].equals(property))
					{
						index = i;
						break;
					}
				}
				PartitionTableBean bean = (PartitionTableBean) element;
				String stringValue;
				String[] choices;
				int i;
				switch (index)
				{
				case 0:
					return bean.getName();
				case 1:
					stringValue = bean.getType();
					choices = PartitionTableBean.getTypeValues();
					i = choices.length - 1;
					while (!stringValue.equals(choices[i]) && i > 0)
						--i;
					if (i == 0 && !stringValue.equals(choices[i]))
					{
						return stringValue;
					}
					return i;
				case 2:
					((ComboBoxCellEditor) cellEditors[2]).setItems(PartitionTableBean.getSubTypeValues(bean.getType()));
					stringValue = bean.getSubType();
					choices = PartitionTableBean.getSubTypeValues(bean.getType());
					i = choices.length - 1;
					while (!stringValue.equals(choices[i]) && i > 0)
						--i;

					if (i == 0 && !stringValue.equals(choices[i]))
					{
						return stringValue;
					}
					return i;
				case 3:
					return bean.getOffSet();
				case 4:
					return bean.getSize();
				case 5:
					return bean.getFlag().contentEquals(FLAGS_VALUE) ? true : false; // $NON-NLS-1$
				default:
					break;
				}
				return null;
			}

			@Override
			public boolean canModify(Object element, String property)
			{
				return true;
			}
		});
	}

	private void createColumns()
	{
		TableColumn nameColumn = new TableColumn(csvTable, SWT.NONE);
		nameColumn.setText(columnNames[0]);

		TableColumn typeColumn = new TableColumn(csvTable, SWT.NONE);
		typeColumn.setText(columnNames[1]);

		TableColumn subTypeColumn = new TableColumn(csvTable, SWT.NONE);
		subTypeColumn.setText(columnNames[2]);

		TableColumn offSetColumn = new TableColumn(csvTable, SWT.NONE);
		offSetColumn.setText(columnNames[3]);

		TableColumn sizeColumn = new TableColumn(csvTable, SWT.NONE);
		sizeColumn.setText(columnNames[4]);

		TableColumn encryptedColumn = new TableColumn(csvTable, SWT.NONE);
		encryptedColumn.setText(columnNames[5]);

		nameColumn.setWidth(100);
		typeColumn.setWidth(100);
		subTypeColumn.setWidth(100);
		offSetColumn.setWidth(100);
		sizeColumn.setWidth(100);
		encryptedColumn.setWidth(100);
	}

	private void createAddNewAction()
	{
		addAction = new Action(Messages.PartitionTableEditorDialog_AddRowAction)
		{
			@SuppressWarnings("unchecked")
			@Override
			public void run()
			{
				List<PartitionTableBean> beansToSave = (List<PartitionTableBean>) tableViewer.getInput();
				beansToSave.add(new PartitionTableBean());
				tableViewer.setInput(beansToSave);
				tableViewer.refresh();
				csvTable.select(csvTable.getItemCount() - 1);
			}
		};

	}

	private void createDeleteAction()
	{
		deleteAction = new Action(Messages.PartitionTableEditorDialog_DeleteSelectedAction)
		{
			@SuppressWarnings("unchecked")
			@Override
			public void run()
			{
				List<PartitionTableBean> beansToSave = (List<PartitionTableBean>) tableViewer.getInput();
				boolean confirmDelete = MessageDialog.openConfirm(getShell(),
						Messages.PartitionTableEditorDialog_DeleteConfirmationAction,
						Messages.PartitionTableEditorDialog_ConfirmDeleteMsg);
				if (!confirmDelete)
				{
					return;
				}
				beansToSave.remove(tableViewer.getElementAt(csvTable.getSelectionIndex()));
				tableViewer.setInput(beansToSave);
				tableViewer.refresh();
				super.run();
			}
		};

	}

	private void createSaveAction()
	{
		saveAction = new Action(Messages.PartitionTableEditorDialog_SaveAction)
		{
			@SuppressWarnings("unchecked")
			@Override
			public void run()
			{
				List<PartitionTableBean> beansToSave = (List<PartitionTableBean>) tableViewer.getInput();
				if (validateBeansBeforeSaving(beansToSave))
				{
					PartitionTableBean.saveCsv(csvFile, beansToSave);
					MessageDialog.openInformation(getShell(), Messages.PartitionTableEditorDialog_SaveInfoTitle,
							Messages.PartitionTableEditorDialog_SaveInfoMsg);
				}

			}
		};
	}

	private boolean validateBeansBeforeSaving(List<PartitionTableBean> beans)
	{
		String errorMsg = StringUtil.EMPTY; // $NON-NLS-1$
		for (PartitionTableBean bean : beans)
		{
			for (int i = 0; i < columnNames.length; i++)
			{
				errorMsg = new PartitionBeanValidator().validateBean(bean, i);
				if (!errorMsg.isBlank())
				{
					MessageDialog.openError(getShell(), Messages.PartitionTableEditorDialog_SaveErrorTitle,
							Messages.PartitionTableEditorDialog_SaveErrorMsg);
					return false;
				}
			}
		}

		return true;

	}

	@Override
	protected boolean isResizable()
	{
		return true;
	}
}
