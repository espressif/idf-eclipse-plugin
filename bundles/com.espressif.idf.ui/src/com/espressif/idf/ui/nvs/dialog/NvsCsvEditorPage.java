/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.nvs.dialog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.BackingStoreException;

import com.espressif.idf.core.build.NvsTableBean;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.NvsBeanValidator;
import com.espressif.idf.core.util.NvsPartitionGenerator;
import com.espressif.idf.core.util.NvsTableDataService;
import com.espressif.idf.core.util.StringUtil;

/**
 * Manages the UI controls and logic for the NVS CSV Editor. This class is instantiated by NvsEditor and contains the UI
 * previously held in NvsEditorDialog.
 */
public class NvsCsvEditorPage
{
	// --- Preference Constants ---
	private static final String PLUGIN_ID = "com.espressif.idf.core"; //$NON-NLS-1$

	private static final String PREF_PARTITION_SIZE = "nvsPartitionSize"; //$NON-NLS-1$
	private static final String PREF_ENCRYPT_ENABLED = "nvsEncryptEnabled"; //$NON-NLS-1$
	private static final String PREF_GENERATE_KEY_ENABLED = "nvsGenerateKeyEnabled"; //$NON-NLS-1$
	private static final String PREF_ENCRYPTION_KEY_PATH = "nvsEncryptionKeyPath"; //$NON-NLS-1$
	// --- End of Preference Constants ---

	private static final String DEFAULT_PARTITION_SIZE = "0x3000"; //$NON-NLS-1$
	private Composite mainControl;
	private IFile csvFile;
	private Consumer<Boolean> dirtyStateListener;

	private Text statusText;
	private Table csvTable;
	private TableViewer tableViewer;
	@SuppressWarnings("nls")
	private String[] columnNames = { "Key", "Type", "Encoding", "Value" };
	private Text sizeText;

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

	private final EnumMap<GeneratePartitionValidationError, String> validationErrors = new EnumMap<>(
			GeneratePartitionValidationError.class);

	public NvsCsvEditorPage(Composite parent, IFile csvFile, Consumer<Boolean> dirtyStateListener)
	{
		this.csvFile = csvFile;
		this.dirtyStateListener = dirtyStateListener;

		mainControl = new Composite(parent, SWT.NONE);
		mainControl.setLayout(new GridLayout(1, false));
	}

	/**
	 * Creates the main UI controls for the editor page.
	 */
	public void createControl()
	{

		// Framed Text widget for status messages
		statusText = new Text(mainControl, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.heightHint = 60; // Approx 3-4 lines
		statusText.setLayoutData(gd);
		statusText.setBackground(mainControl.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

		createEnctyptionLable(mainControl);
		createSizeOfPartitionLable(mainControl);

		Group group = new Group(mainControl, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setLayout(new GridLayout(2, false));

		createTable(group);
		createToolButtonBar(group);
		createTableViewer();

		// Load saved preferences
		loadPreferences();

		// Initial setup
		setMessage(Messages.NvsEditorDialog_DefaultMessage, IMessageProvider.INFORMATION);
		encryptAction.notifyListeners(SWT.Selection, new Event());
		validateGenerationState(); // Set initial button state
	}

	/**
	 * Runs all validation checks for the "Generate Partition" action, updates the error map, and sets the button and
	 * error message status.
	 */
	private void validateGenerationState()
	{
		// 1. Start with a clean slate
		validationErrors.clear();

		// 2. Run all individual validation checks
		String sizeError = validateSize();
		if (!sizeError.isBlank())
		{
			validationErrors.put(GeneratePartitionValidationError.SIZE_ERROR, sizeError);
		}

		String encKeyError = validateEncKeyPath();
		if (!encKeyError.isBlank())
		{
			validationErrors.put(GeneratePartitionValidationError.ENCRYPTION_PATH_ERROR, encKeyError);
		}

		// 3. Update all dependent UI from one single place
		setGenerationButtonStatus();
		updateErrorMessage();
	}

	/**
	 * Sets the status message in the top Text widget.
	 */
	private void setMessage(String message, int messageType)
	{
		if (statusText != null && !statusText.isDisposed())
		{
			statusText.setText(message != null ? message : ""); //$NON-NLS-1$

			Display display = statusText.getDisplay();
			Color foreground;

			switch (messageType)
			{
			case IMessageProvider.ERROR:
				foreground = display.getSystemColor(SWT.COLOR_RED);
				break;
			case IMessageProvider.WARNING:
				foreground = display.getSystemColor(SWT.COLOR_DARK_YELLOW);
				break;
			case IMessageProvider.INFORMATION:
			default:
				foreground = display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
				break;
			}
			statusText.setForeground(foreground);
		}
	}

	/**
	 * Helper to set an error message or clear it.
	 */
	private void setErrorMessage(String message)
	{
		if (message == null)
		{
			setMessage(Messages.NvsEditorDialog_DefaultMessage, IMessageProvider.INFORMATION);
		}
		else
		{
			setMessage(message, IMessageProvider.ERROR);
		}
	}

	public void setFocus()
	{
		if (csvTable != null && !csvTable.isDisposed())
		{
			csvTable.setFocus();
		}
		else if (mainControl != null && !mainControl.isDisposed())
		{
			mainControl.setFocus();
		}
	}

	/**
	 * Called by NvsEditor's doSave() to perform the save logic. * @return true if save was successful, false otherwise.
	 */
	public boolean getSaveAction()
	{
		boolean isPageValid;
		@SuppressWarnings("unchecked")
		List<NvsTableBean> beansToSave = (List<NvsTableBean>) tableViewer.getInput();
		isPageValid = validateBeansBeforeSaving(beansToSave);
		updateErrorMessage();
		if (!isPageValid)
		{
			return false; // Save failed validation
		}

		new NvsTableDataService().saveCsv(csvFile, beansToSave);

		String baseMessage = Messages.NvsTableEditorDialog_SaveInfoMsg;
		String status = statusText.getText();

		if (status != null && !status.isBlank())
		{
			baseMessage = baseMessage + StringUtil.LINE_SEPARATOR + status;
		}

		setMessage(baseMessage, IMessageProvider.INFORMATION);
		Logger.log(Messages.NvsTableEditorDialog_SaveInfoMsg);
		try
		{
			csvFile.getProject().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		dirtyStateListener.accept(false);
		savePreferences();
		return true;
	}

	/**
	 * Notifies the editor that the content is dirty.
	 */
	public void markDirty()
	{
		@SuppressWarnings("unchecked")
		List<NvsTableBean> beansToSave = (List<NvsTableBean>) tableViewer.getInput();
		validateBeansBeforeSaving(beansToSave);
		updateErrorMessage();
		dirtyStateListener.accept(true);
	}

	/**
	 * Updates the status message label based on validation state.
	 */
	public void updateErrorMessage()
	{
		String newErrorMessage = StringUtil.EMPTY;

		if (saveErrorMsg == null)
		{
			saveErrorMsg = StringUtil.EMPTY;
		}

		if (saveErrorMsg != null && !saveErrorMsg.isBlank())
		{
			newErrorMessage = String.format(Messages.NvsEditorDialog_ComplexSaveErrorMsg, saveErrorMsg);
		}
		if (!validationErrors.isEmpty())
		{
			if (newErrorMessage != null && !newErrorMessage.isBlank())
				newErrorMessage = newErrorMessage.concat(StringUtil.LINE_SEPARATOR).concat(" "); //$NON-NLS-1$

			List<String> valuesArr = new ArrayList<>();
			for (Entry<GeneratePartitionValidationError, String> errorEntry : validationErrors.entrySet())
			{
				valuesArr.add(errorEntry.getValue());
			}
			newErrorMessage += String.format(Messages.NvsEditorDialog_GenerateButtonComplexErrorMsg,
					String.join("; ", valuesArr)); //$NON-NLS-1$

		}

		setErrorMessage(newErrorMessage);
	}

	// ========================================================================
	// UI Creation and Logic Methods (from NvsEditorDialog)
	// ========================================================================

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
					generateEncryptionKeyCheckBox.notifyListeners(SWT.Selection, new Event());
				}
				markDirty();
				validateGenerationState();
			}

		});

		Button addButton = new Button(userButtonComp, SWT.PUSH);
		addButton.setText(Messages.NvsTableEditorDialog_AddRowAction);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		addButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(t -> getAddRowAction()));

		Button deleteRowButton = new Button(userButtonComp, SWT.PUSH);
		deleteRowButton.setText(Messages.NvsTableEditorDialog_DeleteSelectedAction);
		deleteRowButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		deleteRowButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(t -> getDeleteRowAction()));

		Button saveButton = new Button(userButtonComp, SWT.PUSH);
		saveButton.setText(Messages.NvsTableEditorDialog_SaveAction);
		saveButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		saveButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(t -> getSaveAction()));

		generateButton = new Button(userButtonComp, SWT.PUSH);
		generateButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		generateButton.setText(Messages.NvsTableEditorGeneratePartitionActionMsg);
		generateButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(t -> getGeneratePartitionAction()));
	}

	private void createEnctyptionLable(Composite parent)
	{
		encryptionComposite = encryptionComposite == null ? new Composite(parent, SWT.NONE) : encryptionComposite;
		if (parent.getLayout() instanceof GridLayout)
		{
			encryptionComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		}

		encryptionComposite.setLayout(new GridLayout(4, false));

		generateEncryptionKeyCheckBox = new Button(encryptionComposite, SWT.CHECK);
		generateEncryptionKeyCheckBox.setText(Messages.NvsEditorDialog_GenEncKeyCheckBoxTxt);
		generateEncryptionKeyCheckBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));

		List<Control> canBeDisposedList = new ArrayList<>();

		// --- Column 1: Label ---
		Label encyptionKeyLbl = new Label(encryptionComposite, SWT.NONE);
		encyptionKeyLbl.setText(Messages.NvsEditorDialog_PathToEncrKeyLbl);
		// Don't grab horizontal space
		encyptionKeyLbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		// --- Column 2: Text ---
		encryptionKeyText = new Text(encryptionComposite, SWT.BORDER);
		encryptionKeyText.setMessage(Messages.NvsEditorDialog_PathToKeysTxt);
		GridData textData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textData.minimumWidth = IDialogConstants.ENTRY_FIELD_WIDTH;
		encryptionKeyText.setLayoutData(textData);

		// --- Column 3: Error Icon Label (Manual) ---
		Image image = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
				.getImage();
		Label errorIconLabel = new Label(encryptionComposite, SWT.NONE);

		errorIconLabel.setImage(image);
		errorIconLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		// --- Column 4: Button ---
		Button encyptionKeyBrowseButton = new Button(encryptionComposite, SWT.PUSH);
		encyptionKeyBrowseButton.setText(Messages.NvsEditorDialog_EncKeyBrowseTxt);
		encyptionKeyBrowseButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		// --- Listener for Text (controls the icon label) ---
		encryptionKeyText.addModifyListener(e -> {
			String errMsg = validateEncKeyPath();
			if (errMsg.isBlank())
			{
				errorIconLabel.setImage(null);
				errorIconLabel.setToolTipText(null);
			}
			else
			{
				errorIconLabel.setImage(image);
				errorIconLabel.setToolTipText(errMsg);
			}
			markDirty();
			validateGenerationState();
		});

		encyptionKeyBrowseButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				FileDialog dlg = new FileDialog(mainControl.getShell(), SWT.OPEN);
				dlg.setFilterPath(csvFile.getProject().getLocation().toString());
				dlg.setFilterExtensions(new String[] { "*.bin", "*" }); //$NON-NLS-1$ //$NON-NLS-2$
				dlg.setText(Messages.NvsEditorDialog_EncrPartitionKeyDlgTxt);
				String dir = dlg.open();
				if (dir != null)
				{
					encryptionKeyText.setText(dir);
					encryptionKeyText.getParent().pack();
				}
			}
		});

		canBeDisposedList.add(encyptionKeyLbl);
		canBeDisposedList.add(encryptionKeyText);
		canBeDisposedList.add(errorIconLabel); // Add the icon label to the list
		canBeDisposedList.add(encyptionKeyBrowseButton);

		generateEncryptionKeyCheckBox.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (generateEncryptionKeyCheckBox.getSelection())
				{
					canBeDisposedList.forEach(t -> t.setEnabled(false));
					errorIconLabel.setImage(null); // Hide error when disabled
					errorIconLabel.setToolTipText(null);
				}
				else
				{
					canBeDisposedList.forEach(t -> t.setEnabled(true));
					encryptionKeyText.notifyListeners(SWT.Modify, new Event());
				}
				markDirty();
				validateGenerationState();
			}
		});
	}

	private void createSizeOfPartitionLable(Composite parent)
	{
		Composite sizeComposite = new Composite(parent, SWT.NONE);
		if (parent.getLayout() instanceof GridLayout)
		{
			sizeComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		}
		sizeComposite.setLayout(new GridLayout(2, false));

		Label sizeOfPartitionLbl = new Label(sizeComposite, SWT.NONE);
		sizeOfPartitionLbl.setText(Messages.NvsTableEditorSizeOfPartitionLblMsg);
		sizeOfPartitionLbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		sizeText = new Text(sizeComposite, SWT.BORDER);
		sizeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
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
			}
			else
			{
				deco.setImage(image);
				deco.setDescriptionText(errMsg);
			}
			markDirty();
			validateGenerationState();
		});

	}

	private void createTableViewer()
	{
		tableViewer = new TableViewer(csvTable);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		ColumnViewerToolTipSupport.enableFor(tableViewer);

		final CellEditor[] cellEditors = new CellEditor[4];
		cellEditors[0] = new TextCellEditor(csvTable);
		cellEditors[1] = new ComboBoxCellEditor(csvTable, NvsTableDataService.getTypes(), SWT.READ_ONLY);
		cellEditors[2] = new ComboBoxCellEditor(csvTable, NvsTableDataService.getEncodings(StringUtil.EMPTY),
				SWT.READ_ONLY);
		cellEditors[3] = new TextCellEditor(csvTable);

		// --- Column 0: Key ---
		TableViewerColumn colKey = new TableViewerColumn(tableViewer, SWT.NONE);
		colKey.getColumn().setText(columnNames[0]);
		colKey.getColumn().setWidth(100);
		colKey.setLabelProvider(new NvsTableEditorLabelProvider()
		{
			@Override
			public int getColumnIndex()
			{
				return 0;
			}

			@Override
			public String getColumnText(NvsTableBean bean)
			{
				return bean.getKey();
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
		});
		colKey.setEditingSupport(new EditingSupport(tableViewer)
		{
			protected CellEditor getCellEditor(Object element)
			{
				return cellEditors[0];
			}

			protected boolean canEdit(Object element)
			{
				return true;
			}

			protected Object getValue(Object element)
			{
				return ((NvsTableBean) element).getKey();
			}

			protected void setValue(Object element, Object value)
			{
				((NvsTableBean) element).setKey((String) value);
				tableViewer.update(element, null);
				markDirty();
			}
		});

		// --- Column 1: Type ---
		TableViewerColumn colType = new TableViewerColumn(tableViewer, SWT.NONE);
		colType.getColumn().setText(columnNames[1]);
		colType.getColumn().setWidth(100);
		colType.setLabelProvider(new NvsTableEditorLabelProvider()
		{
			@Override
			public int getColumnIndex()
			{
				return 1;
			}

			@Override
			public String getColumnText(NvsTableBean bean)
			{
				return bean.getType();
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
				if (tableViewer.getElementAt(0).equals(element))
				{
					return Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
				}
				return null;
			}

		});
		colType.setEditingSupport(new EditingSupport(tableViewer)
		{
			protected CellEditor getCellEditor(Object element)
			{
				return cellEditors[1];
			}

			protected boolean canEdit(Object element)
			{
				return true;
			}

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
				((ComboBoxCellEditor) cellEditors[2]).setItems(encodings);
				if (encodings.length > 0)
				{
					bean.setEncoding(encodings[0]);
				}

				tableViewer.update(element, new String[] { columnNames[1], columnNames[2] });
				markDirty();
			}
		});

		// --- Column 2: Encoding ---
		TableViewerColumn colEncoding = new TableViewerColumn(tableViewer, SWT.NONE);
		colEncoding.getColumn().setText(columnNames[2]);
		colEncoding.getColumn().setWidth(100);
		colEncoding.setLabelProvider(new NvsTableEditorLabelProvider()
		{
			@Override
			public int getColumnIndex()
			{
				return 2;
			}

			@Override
			public String getColumnText(NvsTableBean bean)
			{
				return bean.getEncoding();
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
				if (tableViewer.getElementAt(0).equals(element))
				{
					return Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
				}
				return null;
			}
		});
		colEncoding.setEditingSupport(new EditingSupport(tableViewer)
		{
			protected CellEditor getCellEditor(Object element)
			{
				NvsTableBean bean = (NvsTableBean) element;
				((ComboBoxCellEditor) cellEditors[2]).setItems(NvsTableDataService.getEncodings(bean.getType()));
				return cellEditors[2];
			}

			protected boolean canEdit(Object element)
			{
				return true;
			}

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

			protected void setValue(Object element, Object value)
			{
				NvsTableBean bean = (NvsTableBean) element;
				String[] encodings = NvsTableDataService.getEncodings(bean.getType());
				if (encodings.length > (int) value)
				{
					bean.setEncoding(encodings[(int) value]);
				}
				tableViewer.update(element, null);
				markDirty();
			}
		});

		// --- Column 3: Value ---
		TableViewerColumn colValue = new TableViewerColumn(tableViewer, SWT.NONE);
		colValue.getColumn().setText(columnNames[3]);
		colValue.getColumn().setWidth(150);
		colValue.setLabelProvider(new NvsTableEditorLabelProvider()
		{
			@Override
			public int getColumnIndex()
			{
				return 3;
			}

			@Override
			public String getColumnText(NvsTableBean bean)
			{
				return bean.getValue();
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
				if (tableViewer.getElementAt(0).equals(element))
				{
					return Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
				}
				return null;
			}
		});
		colValue.setEditingSupport(new EditingSupport(tableViewer)
		{
			protected CellEditor getCellEditor(Object element)
			{
				return cellEditors[3];
			}

			protected boolean canEdit(Object element)
			{
				return true;
			}

			protected Object getValue(Object element)
			{
				return ((NvsTableBean) element).getValue();
			}

			protected void setValue(Object element, Object value)
			{
				((NvsTableBean) element).setValue((String) value);
				tableViewer.update(element, null);
				markDirty();
			}
		});

		try
		{
			List<NvsTableBean> list = new NvsTableDataService().parseCsv(Paths.get(csvFile.getLocationURI()));
			tableViewer.setInput(list);
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
	}

	private void createTable(Composite parent)
	{
		csvTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		csvTable.setHeaderVisible(true);
		csvTable.setLinesVisible(true);
		csvTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		((GridData) csvTable.getLayoutData()).widthHint = 1000; // Keep hint as a minimum
	}

	private void getGeneratePartitionAction()
	{
		validateGenerationState();

		if (validationErrors.isEmpty())
		{
			String infoMsg = NvsPartitionGenerator.generateNvsPartititon(encryptAction.getSelection(), getEncKeyPath(),
					sizeText.getText(), csvFile);

			String status = statusText != null ? statusText.getText() : null;
			if (status != null && !status.isBlank())
			{
				infoMsg = infoMsg + StringUtil.LINE_SEPARATOR + status.trim();
			}

			setMessage(infoMsg, IMessageProvider.INFORMATION);
			Logger.log(infoMsg);
		}
		else
		{
			Logger.log("NVS Partition Generation failed validation."); //$NON-NLS-1$
		}

		try
		{
			csvFile.getProject().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
	}

	public void setGenerationButtonStatus()
	{
		if (generateButton != null)
			generateButton.setEnabled(validationErrors.isEmpty());
	}

	private String validateEncKeyPath()
	{
		String encKeyPath = getEncKeyPath().orElseGet(() -> StringUtil.EMPTY);

		if (encryptAction.getSelection() && !generateEncryptionKeyCheckBox.getSelection()
				&& !new File(encKeyPath).canRead())
		{
			return Messages.NvsEditorDialog_EncKeyCantBeReadErrMsg;
		}

		return StringUtil.EMPTY;
	}

	private String validateSize()
	{
		Long decodedSize = 0L;
		try
		{
			decodedSize = Long.decode(sizeText.getText());
		}
		catch (NumberFormatException e)
		{
			return Messages.NvsEditorDialog_SizeValidationDecodedErr + e.getMessage();
		}
		if (decodedSize < 4096 || decodedSize % 4096 != 0)
		{
			return Messages.NvsEditorDialog_WrongSizeFormatErrMsg;
		}
		return StringUtil.EMPTY;
	}

	private Optional<String> getEncKeyPath()
	{
		if (encryptionKeyText == null || !encryptionKeyText.isEnabled())
		{
			return Optional.empty();
		}

		return Optional.of(encryptionKeyText.getText());
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
		NvsTableBean selectedElement = (NvsTableBean) tableViewer.getElementAt(csvTable.getSelectionIndex());
		if (selectedElement == null)
		{
			return;
		}

		if (selectedElement.equals(tableViewer.getElementAt(0)))
		{
			setMessage(Messages.NvsEditorDialog_FirstRowIsFixedInfoMsg, IMessageProvider.INFORMATION);
			return;
		}
		boolean confirmDelete = MessageDialog.openConfirm(mainControl.getShell(),
				Messages.NvsTableEditorDialog_DeleteSelectedAction, Messages.NvsEditorDialog_ConfirmDeleteMsg);
		if (!confirmDelete)
		{
			return;
		}
		beansToSave.remove(selectedElement);
		tableViewer.setInput(beansToSave);
		tableViewer.refresh();
		markDirty();
	}

	private void getAddRowAction()
	{
		@SuppressWarnings("unchecked")
		List<NvsTableBean> beansToSave = (List<NvsTableBean>) tableViewer.getInput();
		beansToSave.add(new NvsTableBean());
		tableViewer.setInput(beansToSave);
		tableViewer.refresh();
		csvTable.select(csvTable.getItemCount() - 1);
		tableViewer.refresh();
		markDirty();
	}

	// ========================================================================
	// Preference Handling Methods
	// ========================================================================

	/**
	 * Returns the preference node for the current project.
	 */
	private IEclipsePreferences getProjectPreferences()
	{
		IProject project = csvFile.getProject();
		IScopeContext projectScope = new ProjectScope(project);
		return projectScope.getNode(PLUGIN_ID);
	}

	/**
	 * Loads settings from the project's preferences and applies them to the UI.
	 */
	private void loadPreferences()
	{
		IEclipsePreferences prefs = getProjectPreferences();

		// Load and set values, using your class defaults as fallbacks
		sizeText.setText(prefs.get(PREF_PARTITION_SIZE, DEFAULT_PARTITION_SIZE));
		encryptAction.setSelection(prefs.getBoolean(PREF_ENCRYPT_ENABLED, false));
		generateEncryptionKeyCheckBox.setSelection(prefs.getBoolean(PREF_GENERATE_KEY_ENABLED, true));
		encryptionKeyText.setText(prefs.get(PREF_ENCRYPTION_KEY_PATH, StringUtil.EMPTY));
	}

	/**
	 * Saves the current UI settings to the project's preferences.
	 */
	private void savePreferences()
	{
		IEclipsePreferences prefs = getProjectPreferences();

		// Store the current values
		prefs.put(PREF_PARTITION_SIZE, sizeText.getText());
		prefs.putBoolean(PREF_ENCRYPT_ENABLED, encryptAction.getSelection());
		prefs.putBoolean(PREF_GENERATE_KEY_ENABLED, generateEncryptionKeyCheckBox.getSelection());
		prefs.put(PREF_ENCRYPTION_KEY_PATH, encryptionKeyText.getText());

		try
		{
			// Flush the changes to disk
			prefs.flush();
		}
		catch (BackingStoreException e)
		{
			Logger.log(e);
		}
	}
}
