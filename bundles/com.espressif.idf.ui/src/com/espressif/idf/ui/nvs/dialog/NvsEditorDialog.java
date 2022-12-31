package com.espressif.idf.ui.nvs.dialog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.espressif.idf.core.build.NvsTableBean;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.NvsBeanValidator;
import com.espressif.idf.core.util.NvsPartitionGenerator;
import com.espressif.idf.core.util.NvsTableDataService;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.EclipseUtil;

public class NvsEditorDialog extends TitleAreaDialog
{

	private Table csvTable;
	private TableViewer tableViewer;
	@SuppressWarnings("nls")
	private String[] columnNames = { "Key", "Type", "Encoding", "Value" };
	private Text sizeText;
	private IFile csvFile;
	private CellEditor[] cellEditors;
	private boolean isPageValid;
	private Composite encryptionComposite;
	private Text encryptionKeyText;
	private Button generateEncryptionKeyCheckBox;
	private Button encryptAction;
	private Button generateButton;
	private String saveErrorMsg;

	enum GeneratePartitionValidationError
	{
		SIZE_ERROR, ENCRYPTION_PATH_ERROR;
	}

	private enum ErrorListenerMap
	{
		INSTANCE;
		
		private EnumMap<GeneratePartitionValidationError, String> validationErrors = new EnumMap<>(
				GeneratePartitionValidationError.class);
		private NvsEditorDialog dialog;

		void setDialog(NvsEditorDialog dialog)
		{
			this.dialog = dialog;
		}

		public void put(GeneratePartitionValidationError key, String errMsg)
		{
			validationErrors.put(key, errMsg);
			dialog.setGenerationButtonStatus();
			dialog.updateErrorMessage();
		}

		public void remove(GeneratePartitionValidationError key)
		{
			validationErrors.remove(key);
			dialog.setGenerationButtonStatus();
			dialog.updateErrorMessage();
		}
	}



	public NvsEditorDialog(Shell parentShell)
	{
		super(parentShell);
		ErrorListenerMap.INSTANCE.setDialog(this);
	}

	public void setCsvFile(IFile csvFile)
	{
		this.csvFile = csvFile;
	}

	@Override
	public void create()
	{
		super.create();
		setTitle(Messages.NvsTableEditorDialog_Title);
		setMessage(Messages.NvsEditorDialog_DefaultMessage, IMessageProvider.INFORMATION);
		getShell().getLayout();
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		getShell().setText(Messages.NvsTableEditorDialog_Title);
		createEnctyptionLable(parent);
		createSizeOfPartitionLable(parent);

		Group group = new Group(parent, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setLayout(new GridLayout(2, false));

		createTable(group);
		createToolButtonBar(group);
		createColumns();
		createTableViewer();
		return super.createDialogArea(parent);
	}

	private void createToolButtonBar(Composite parent)
	{
		Composite userButtonComp = new Composite(parent, SWT.NONE);
		userButtonComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		userButtonComp.setLayout(new GridLayout());

		encryptAction = new Button(userButtonComp, SWT.CHECK);
		encryptAction.setText(Messages.NvsTableEditorIsEncryptedActionMsg);
		encryptAction.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		encryptAction.addSelectionListener(new SelectionAdapter()
		{
			
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Stream.of(encryptionComposite.getChildren()).forEach(t -> t.setEnabled(encryptAction.getSelection()));
				if (encryptAction.getSelection())
				{
					generateEncryptionKeyCheckBox.notifyListeners(SWT.Selection, null);
				}
				else
				{
					ErrorListenerMap.INSTANCE.remove(GeneratePartitionValidationError.ENCRYPTION_PATH_ERROR);
				}
			}

		});

		Button addButton = new Button(userButtonComp, SWT.PUSH);
		addButton.setText(Messages.NvsTableEditorDialog_AddRowAction);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		addButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(t -> getAddRowAction()));

		Button deleteRowButton = new Button(userButtonComp, SWT.PUSH);
		deleteRowButton.setText(Messages.NvsTableEditorDialog_DeleteSelectedAction);
		deleteRowButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		deleteRowButton
				.addSelectionListener(SelectionListener.widgetSelectedAdapter(t -> getDeleteRowAction()));

		Button saveButton = new Button(userButtonComp, SWT.PUSH);
		saveButton.setText(Messages.NvsTableEditorDialog_SaveAction);
		saveButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		saveButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(t -> getSaveAction()));

		generateButton = new Button(userButtonComp, SWT.PUSH);
		generateButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		generateButton.setText(Messages.NvsTableEditorGeneratePartitionActionMsg);
		generateButton.addSelectionListener(
				SelectionListener.widgetSelectedAdapter(t -> getGeneratePartitionAction()));
		encryptAction.notifyListeners(SWT.Selection, null);
		generateButton.setEnabled(false);
		setErrorMessage(null);
	}

	private void createEnctyptionLable(Composite parent)
	{
		encryptionComposite = encryptionComposite == null ? new Composite(parent, SWT.NONE) : encryptionComposite;
		encryptionComposite.setLayout(new GridLayout(3, false));

		generateEncryptionKeyCheckBox = new Button(encryptionComposite, SWT.CHECK);
		generateEncryptionKeyCheckBox.setText(Messages.NvsEditorDialog_GenEncKeyCheckBoxTxt);
		generateEncryptionKeyCheckBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		List<Control> canBeDisposedList = new ArrayList<>();
		Label encyptionKeyLbl = new Label(encryptionComposite, SWT.NONE);
		encyptionKeyLbl.setText(Messages.NvsEditorDialog_PathToEncrKeyLbl);
		encyptionKeyLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		encryptionKeyText = new Text(encryptionComposite, SWT.BORDER);
		ControlDecoration deco = new ControlDecoration(encryptionKeyText, SWT.RIGHT);
		Image image = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
				.getImage();
		encryptionKeyText.setMessage(Messages.NvsEditorDialog_PathToKeysTxt);
		encryptionKeyText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		encryptionKeyText.addModifyListener(e -> {
			String errMsg = validateEncKeyPath();
			if (errMsg.isBlank())
			{
				deco.setImage(null);
				deco.setDescriptionText(null);
				return;
			}
			deco.setDescriptionText(errMsg);
			deco.setImage(image);
		});
		Button encyptionKeyBrowseButton = new Button(encryptionComposite, SWT.PUSH);
		encyptionKeyBrowseButton.setText(Messages.NvsEditorDialog_EncKeyBrowseTxt);
		encyptionKeyBrowseButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
				dlg.setFilterPath(csvFile.getProject().getLocationURI().toString());
				dlg.setFilterExtensions(new String[] {".bin"}); //$NON-NLS-1$
				dlg.setText(Messages.NvsEditorDialog_EncrPartitionKeyDlgTxt);
				String dir = dlg.open();
				if (dir != null)
				{
					encryptionKeyText.setText(dir);
					encryptionKeyText.getParent().pack();
				}
			}
		});
		encyptionKeyBrowseButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		((GridData) encryptionKeyText.getLayoutData()).minimumWidth = IDialogConstants.ENTRY_FIELD_WIDTH;
		canBeDisposedList.add(encyptionKeyLbl);
		canBeDisposedList.add(encryptionKeyText);
		canBeDisposedList.add(encyptionKeyBrowseButton);
		generateEncryptionKeyCheckBox.addSelectionListener(new SelectionAdapter()
		{

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (generateEncryptionKeyCheckBox.getSelection())
				{
					canBeDisposedList.forEach(t -> t.setEnabled(false));
					ErrorListenerMap.INSTANCE.remove(GeneratePartitionValidationError.ENCRYPTION_PATH_ERROR);
					deco.setImage(null);
					return;
				}
				canBeDisposedList.forEach(t -> t.setEnabled(true));
				encryptionKeyText.notifyListeners(SWT.Modify, null);
			}
		});
		generateEncryptionKeyCheckBox.setSelection(true);
		generateEncryptionKeyCheckBox.notifyListeners(SWT.Selection, null);
		setErrorMessage(null);
	}

	private void createSizeOfPartitionLable(Composite parent)
	{
		Composite sizeComposite = new Composite(parent, SWT.NONE);
		sizeComposite.setLayout(new GridLayout(2, false));


		Label sizeOfPartitionLbl = new Label(sizeComposite, SWT.NONE);
		sizeOfPartitionLbl.setText(Messages.NvsTableEditorSizeOfPartitionLblMsg);
		sizeOfPartitionLbl.setLayoutData(new GridData(GridData.FILL_BOTH));
		sizeText = new Text(sizeComposite, SWT.BORDER);
		sizeText.setLayoutData(new GridData(GridData.FILL_BOTH));
		sizeText.setMessage(Messages.NvsEditorDialog_DefaultSizeMsg);
		ControlDecoration deco = new ControlDecoration(sizeText, SWT.RIGHT);
		Image image = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
				.getImage();
		sizeText.addModifyListener(e -> {
			String errMsg = validateSize();
			if (errMsg.isBlank())
			{
				deco.setImage(null);
				deco.setDescriptionText(null);
				return;
			}
			deco.setImage(image);
			deco.setDescriptionText(errMsg);

		});

	}

	private void createTableViewer()
	{
		tableViewer = new TableViewer(csvTable);

		tableViewer.setContentProvider((IStructuredContentProvider) input -> {
			@SuppressWarnings("unchecked")
			List<NvsTableBean> list = (List<NvsTableBean>) input;
			return list.toArray();
		});
		
		try
		{
			List<NvsTableBean> list = new NvsTableDataService().parseCsv(Paths.get(csvFile.getLocationURI()));
			tableViewer.setInput(list);
			list.forEach(bean -> tableViewer.update(bean, columnNames));
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
		tableViewer.setLabelProvider(new NvsTableEditorLabelProvider());

		// Set cell editors
		tableViewer.setColumnProperties(columnNames);
		cellEditors = new CellEditor[4];
		cellEditors[0] = new TextCellEditor(csvTable);
		cellEditors[1] = new ComboBoxCellEditor(csvTable, NvsTableDataService.getTypes(), SWT.READ_ONLY);
		cellEditors[2] = new ComboBoxCellEditor(csvTable, NvsTableDataService.getEncodings(StringUtil.EMPTY),
				SWT.READ_ONLY);
		cellEditors[3] = new TextCellEditor(csvTable);
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

				NvsTableBean bean = null;
				if (element instanceof Item)
				{
					TableItem item = (TableItem) element;
					bean = (NvsTableBean) item.getData();
				}
				else
				{
					bean = (NvsTableBean) element;
				}

				switch (index)
				{
				case 0:
					bean.setKey((String) value);
					break;
				case 1:
					String newType = NvsTableDataService.getTypes()[(int) value];
					if (newType.contentEquals(bean.getType()))
					{
						break;
					}
					bean.setType(newType);
					((ComboBoxCellEditor) cellEditors[2])
							.setItems(NvsTableDataService.getEncodings(bean.getType()));
					tableViewer.getCellModifier().modify(element, columnNames[2], 0);
					break;
				case 2:
					bean.setEncoding(NvsTableDataService.getEncodings(bean.getType())[(int) value]);
					break;
				case 3:
					bean.setValue((String) value);
					break;
				default:
					break;
				}
				tableViewer.update(bean, columnNames);
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

				NvsTableBean bean = (NvsTableBean) element;
				String stringValue;
				String[] choices;
				int i;

				switch (index)
				{
				case 0:
					return bean.getKey();
				case 1:
					stringValue = bean.getType();
					choices = NvsTableDataService.getTypes();
					i = choices.length - 1;
					while (!stringValue.equals(choices[i]) && i > 0)
						--i;

					return i;
				case 2:
					((ComboBoxCellEditor) cellEditors[2]).setItems(NvsTableDataService.getEncodings(bean.getType()));
					stringValue = bean.getEncoding();
					choices = NvsTableDataService.getEncodings(bean.getType());
					i = choices.length - 1;
					while (!stringValue.equals(choices[i]) && i > 0)
						--i;

					return i;
				case 3:
					return bean.getValue();

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
		TableColumn keyColumn = new TableColumn(csvTable, SWT.NONE);
		keyColumn.setText(columnNames[0]);

		TableColumn typeColumn = new TableColumn(csvTable, SWT.NONE);
		typeColumn.setText(columnNames[1]);

		TableColumn encriptionColumn = new TableColumn(csvTable, SWT.NONE);
		encriptionColumn.setText(columnNames[2]);

		TableColumn valueColumn = new TableColumn(csvTable, SWT.NONE);
		valueColumn.setText(columnNames[3]);

		keyColumn.setWidth(100);
		typeColumn.setWidth(100);
		encriptionColumn.setWidth(100);
		valueColumn.setWidth(100);

	}

	private void createTable(Composite parent)
	{
		csvTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		csvTable.setHeaderVisible(true);
		csvTable.setLinesVisible(true);
		csvTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		((GridData) csvTable.getLayoutData()).widthHint = 1000;

		csvTable.addMouseTrackListener(new MouseTrackAdapter()
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
					label.setText(new NvsBeanValidator().validateBean((NvsTableBean) item.getElement(),
							item.getColumnIndex()));

					Point size = errorToolTip.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					Rectangle rect = item.getBounds();
					Point pt = csvTable.toDisplay(rect.x, rect.y);
					errorToolTip.setBounds(pt.x, pt.y, size.x, size.y);
					errorToolTip.setVisible(true);
					csvTable.addMouseMoveListener(e -> errorToolTip.dispose());
				}
			}

		});
	}

	private void getGeneratePartitionAction()
	{
		String errorMsg = validateSize();
		errorMsg = errorMsg.isBlank() ? validateEncKeyPath() : errorMsg;
		String infoMsg = errorMsg.isEmpty()
				? NvsPartitionGenerator.generateNvsPartititon(encryptAction.getSelection(), getEncKeyPath(),
						sizeText.getText(), csvFile)
				: errorMsg;
		MessageDialog.openInformation(getShell(), Messages.NvsEditorDialog_GenPartitionInfDialTitle,
				infoMsg);
		Logger.log(infoMsg);
		try
		{
			EclipseUtil.getSelectedProjectInExplorer().refreshLocal(IResource.DEPTH_INFINITE,
					new NullProgressMonitor());
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}

	}

	public void setGenerationButtonStatus()
	{
		if (generateButton != null)
			generateButton.setEnabled(ErrorListenerMap.INSTANCE.validationErrors.isEmpty());
	}

	private String validateEncKeyPath()
	{
		String encKeyPath = getEncKeyPath().orElseGet(() -> StringUtil.EMPTY);

		if (encryptAction.getSelection() && !generateEncryptionKeyCheckBox.getSelection()
				&& !new File(encKeyPath).canRead())
		{
			ErrorListenerMap.INSTANCE.put(GeneratePartitionValidationError.ENCRYPTION_PATH_ERROR,
					Messages.NvsEditorDialog_EncKeyCantBeReadErrMsg);
			return Messages.NvsEditorDialog_EncKeyCantBeReadErrMsg;
		}
		ErrorListenerMap.INSTANCE.remove(GeneratePartitionValidationError.ENCRYPTION_PATH_ERROR);

		return StringUtil.EMPTY;
	}


	private String validateSize()
	{
		Long decodedSize = 0l;
		try
		{
			decodedSize = Long.decode(sizeText.getText());
		}
		catch (NumberFormatException e)
		{
			ErrorListenerMap.INSTANCE.put(GeneratePartitionValidationError.SIZE_ERROR,
					Messages.NvsEditorDialog_SizeValidationDecodedErr + e.getMessage());
			return Messages.NvsEditorDialog_SizeValidationDecodedErr + e.getMessage();
		}
		if (decodedSize < 4096 || decodedSize % 4096 != 0)
		{
			ErrorListenerMap.INSTANCE.put(GeneratePartitionValidationError.SIZE_ERROR,
					Messages.NvsEditorDialog_WrongSizeFormatErrMsg);
			return Messages.NvsEditorDialog_WrongSizeFormatErrMsg;
		}
		ErrorListenerMap.INSTANCE.remove(GeneratePartitionValidationError.SIZE_ERROR);
		return StringUtil.EMPTY;
	}

	private Optional<String> getEncKeyPath()
	{
		if (encryptionKeyText == null || encryptionKeyText.isDisposed())
		{
			return Optional.empty();
		}

		return Optional.of(encryptionKeyText.getText());
	}

	private void getSaveAction()
	{
		@SuppressWarnings("unchecked")
		List<NvsTableBean> beansToSave = (List<NvsTableBean>) tableViewer.getInput();
		isPageValid = validateBeansBeforeSaving(beansToSave);
		updateErrorMessage();
		if (!isPageValid)
		{
			return;
		}
		new NvsTableDataService().saveCsv(csvFile, beansToSave);
		MessageDialog.openInformation(getShell(), Messages.NvsTableEditorDialog_SaveInfoTitle,
				Messages.NvsTableEditorDialog_SaveInfoMsg);
		Logger.log(Messages.NvsTableEditorDialog_SaveInfoMsg);
		try
		{
			// Sometimes the file is not saved in Linux because explorer is not updated
			EclipseUtil.getSelectedProjectInExplorer().refreshLocal(IResource.DEPTH_INFINITE,
					new NullProgressMonitor());
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
	}

	private boolean validateBeansBeforeSaving(List<NvsTableBean> beansToSave)
	{

		String errorMsg;
		if (beansToSave.isEmpty())
		{
			saveErrorMsg = Messages.NvsEditorDialog_EmptyTableErrorMsg;
			return false;
		}
		if (!validateFirstBean(beansToSave.get(0)))
		{
			return false;
		}

		for (NvsTableBean bean : beansToSave)
		{
			for (int i = 0; i < columnNames.length; i++)
			{
				errorMsg = new NvsBeanValidator().validateBean(bean, i);
				if (!errorMsg.isBlank())
				{
					saveErrorMsg = errorMsg;
					return false;
				}
			}
		}
		saveErrorMsg = StringUtil.EMPTY;
		return true;
	}

	private boolean validateFirstBean(NvsTableBean nvsTableBean)
	{
		String errorMsg = new NvsBeanValidator().validateFirstBean(nvsTableBean);
		if (!errorMsg.isBlank())
		{
			saveErrorMsg = errorMsg;
			return false;
		}
		return true;
	}

	private void getDeleteRowAction()
	{
		@SuppressWarnings("unchecked")
		List<NvsTableBean> beansToSave = (List<NvsTableBean>) tableViewer.getInput();
		boolean confirmDelete = MessageDialog.openConfirm(getShell(),
				Messages.NvsTableEditorDialog_DeleteSelectedAction, Messages.NvsEditorDialog_ConfirmDeleteMsg);
		if (!confirmDelete)
		{
			return;
		}
		beansToSave.remove(tableViewer.getElementAt(csvTable.getSelectionIndex()));
		tableViewer.setInput(beansToSave);
		tableViewer.refresh();
		getShell().pack();

	}

	private void getAddRowAction()
	{
		@SuppressWarnings("unchecked")
		List<NvsTableBean> beansToSave = (List<NvsTableBean>) tableViewer.getInput();
		beansToSave.add(new NvsTableBean());
		tableViewer.setInput(beansToSave);
		tableViewer.refresh();
		csvTable.select(csvTable.getItemCount() - 1);
		getShell().pack();

	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, Messages.NvsTableEditorSaveAndQuitButtonLable, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void okPressed()
	{
		getSaveAction();
		if (isPageValid)
			super.okPressed();
	}

	public void updateErrorMessage()
	{
		String newErrorMessage = StringUtil.EMPTY;
		EnumMap<GeneratePartitionValidationError, String> validationErrors = ErrorListenerMap.INSTANCE.validationErrors;
		if (saveErrorMsg != null && !saveErrorMsg.isBlank())
		{
			newErrorMessage = String.format(Messages.NvsEditorDialog_ComplexSaveErrorMsg, saveErrorMsg);
		}
		if (!validationErrors.isEmpty())
		{
			newErrorMessage = newErrorMessage.isBlank() ? newErrorMessage
					: newErrorMessage.concat(StringUtil.LINE_SEPARATOR).concat(" "); //$NON-NLS-1$
			List<String> valuesArr = new ArrayList<>();
			for (Entry<GeneratePartitionValidationError, String> errorEntry : validationErrors.entrySet())
			{
				valuesArr.add(errorEntry.getValue());
			}
			newErrorMessage += String.format(Messages.NvsEditorDialog_GenerateButtonComplexErrorMsg,
					String.join("; ", valuesArr)); //$NON-NLS-1$

		}

		newErrorMessage = newErrorMessage.isBlank() ? null : newErrorMessage;
		super.setErrorMessage(newErrorMessage);
	}

	@Override
	protected boolean isResizable()
	{
		return true;
	}

}
