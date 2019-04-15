/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.ui;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.sdk.config.core.IJsonServerConfig;
import com.espressif.idf.sdk.config.core.KConfigMenuItem;
import com.espressif.idf.sdk.config.core.KConfigMenuProcessor;
import com.espressif.idf.sdk.config.core.SDKConfigUtil;
import com.espressif.idf.sdk.config.core.server.IMessageHandlerListener;
import com.espressif.idf.sdk.config.core.server.JsonConfigProcessor;
import com.espressif.idf.sdk.config.core.server.JsonConfigServer;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
@SuppressWarnings("unchecked")
public class SDKConfigurationEditor extends MultiPageEditorPart
		implements ISaveablePart, IMessageHandlerListener, IPageChangedListener
{

	public static String EDITOR_ID = "com.espressif.idf.sdk.config.ui.editor"; //$NON-NLS-1$

	private TreeViewer treeViewer;

	private Group updateUIComposite;

	private IProject project;

	private boolean isDirty;

	private JsonConfigServer configServer;

	private JSONObject valuesJsonMap;

	private JSONObject visibleJsonMap;

	private JSONObject rangesJsonMap;

	private String serverMessage;

	private KConfigMenuItem selectedElement;

	/**
	 * Captures only text field changes
	 */
	private JSONObject modifiedJsonMap = new JSONObject();

	public SDKConfigurationEditor()
	{
		super();
		addPageChangedListener(this);
	}

	/**
	 * Creates the pages of the SDK configuration editor.
	 */
	protected void createPages()
	{
		String configMenuJsonPath = null;
		try
		{
			configMenuJsonPath = new SDKConfigUtil().getConfigMenuFilePath(project);
			if (configMenuJsonPath == null || !new File(configMenuJsonPath).exists())
			{
				createErrorPage(configMenuJsonPath);
			}
			else
			{
				initConfigServer(project);
				createDesignPage();
				createSourcePage();
			}
		}
		catch (Exception e)
		{
			IDFCorePlugin.log(e);
		}
	}

	/**
	 * sdkconfig source editor
	 */
	private void createSourcePage()
	{
		try
		{
			TextEditor editor = new TextEditor();
			Object control = ((AbstractTextEditor) editor).getAdapter(Control.class);
			if (control instanceof StyledText)
			{
				((StyledText) control).setEditable(false);
			}
			int index = addPage(editor, getEditorInput());
			setPageText(index, Messages.SDKConfigurationEditor_Preview);
		}
		catch (PartInitException e)
		{
			ErrorDialog.openError(getSite().getShell(), Messages.SDKConfigurationEditor_Error, null, e.getStatus());
		}
	}

	/**
	 * sdkconfig UI configuration editor
	 */
	private void createDesignPage()
	{

		Composite parent = new Composite(getContainer(), SWT.V_SCROLL);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		parent.setLayout(layout);

		GridData layoutData = new GridData();
		layoutData.grabExcessVerticalSpace = true;
		layoutData.verticalAlignment = GridData.FILL;

		Group treeComposite = new Group(parent, SWT.V_SCROLL | SWT.TOP);
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 2;
		treeComposite.setLayout(layout);
		treeComposite.setLayoutData(layoutData);

		FilteredTree transfersTree = createFilteredTree(treeComposite);
		transfersTree.setLayoutData(new GridData(GridData.FILL_BOTH));

		treeViewer = (TreeViewer) transfersTree.getViewer();

		// Create the tree viewer as a child of the composite parent
		treeViewer.setContentProvider(new ConfigContentProvider());
		treeViewer.setLabelProvider(new ConfigLabelProvider());

		treeViewer.setUseHashlookup(true);

		// layout the tree viewer below the text field
		layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.verticalAlignment = GridData.FILL;
		treeViewer.getControl().setLayoutData(layoutData);

		hookListeners();

		KConfigMenuItem initalInput = getInitalInput();
		treeViewer.setInput(initalInput);
		treeViewer.expandToLevel(getComponentConfigElement(initalInput), 1);

		// UI for the selected element
		updateUIComposite = new Group(parent, SWT.V_SCROLL);
		updateUIComposite.setLayout((new GridLayout(1, false)));
		GridData gridData = new GridData(SWT.NONE, SWT.NONE, true, true);
		gridData.heightHint = 500;
		updateUIComposite.setLayoutData(gridData);

		// select first element
		if (initalInput != null)
		{
			List<KConfigMenuItem> children = initalInput.getChildren();
			if (!children.isEmpty())
			{
				KConfigMenuItem kConfigMenuItem = children.get(0);
				updateUI(kConfigMenuItem);
			}
		}

		int index = addPage(parent);
		setPageText(index, Messages.SDKConfigurationEditor_Design);
	}

	/**
	 * @param initalInput
	 * @return
	 */
	private KConfigMenuItem getComponentConfigElement(KConfigMenuItem initalInput)
	{
		if (initalInput == null)
		{
			return null;
		}
		
		List<KConfigMenuItem> children = initalInput.getChildren();
		for (KConfigMenuItem kConfigMenuItem : children)
		{
			if (kConfigMenuItem.getTitle().equals(IJsonServerConfig.COMPONENT_CONFIG_ID))
			{
				return kConfigMenuItem;
			}
		}
			
		return null;
	}

	/**
	 * 
	 * @param kconfigMenuJsonFile
	 */
	private void createErrorPage(String kconfigMenuJsonFile)
	{
		Composite parent = new Composite(getContainer(), SWT.V_SCROLL);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		parent.setLayout(layout);

		new Label(parent, SWT.NONE)
				.setText(Messages.SDKConfigurationEditor_UnableFindKConfigFile + kconfigMenuJsonFile);

		int index = addPage(parent);
		setPageText(index, Messages.SDKConfigurationEditor_Design);

	}

	/**
	 * @return current project
	 */
	protected IProject getProject()
	{
		IFileEditorInput editorInput = (IFileEditorInput) getEditorInput();
		IFile file = editorInput.getFile();
		if (file.getParent() instanceof IProject)
		{
			return (IProject) file.getParent();
		}
		return null;
	}

	/**
	 * @param project
	 * @throws IOException
	 * @throws ParseException
	 */
	protected void initConfigServer(IProject project) throws IOException, ParseException
	{
		configServer = new JsonConfigServer(project);

		// register the editor with the server to notify about the events
		configServer.addListener(this);
		configServer.start();

		// will wait and check for the server response
		JsonConfigProcessor jsonProcessor = new JsonConfigProcessor();
		if (isReady(5, 1000, jsonProcessor))
		{
			String initialOutput = jsonProcessor.getInitialOutput(serverMessage);

			JSONParser parser = new JSONParser();
			JSONObject jsonObj = (JSONObject) parser.parse(initialOutput);
			if (jsonObj != null)
			{
				valuesJsonMap = (JSONObject) jsonObj.get(IJsonServerConfig.VALUES);
				visibleJsonMap = (JSONObject) jsonObj.get(IJsonServerConfig.VISIBLE);
				rangesJsonMap = (JSONObject) jsonObj.get(IJsonServerConfig.RANGES);
			}
		}
	}

	protected void update() throws ParseException
	{
		JsonConfigProcessor jsonProcessor = new JsonConfigProcessor();
		String output = jsonProcessor.getInitialOutput(serverMessage);
		if (output == null)
		{
			return;
		}

		JSONParser parser = new JSONParser();
		JSONObject jsonObj = (JSONObject) parser.parse(output);
		if (jsonObj != null)
		{
			// newly updated values and visible items
			JSONObject visibleJson = (JSONObject) jsonObj.get(IJsonServerConfig.VISIBLE);
			JSONObject valuesJson = (JSONObject) jsonObj.get(IJsonServerConfig.VALUES);
			JSONObject rangesJson = (JSONObject) jsonObj.get(IJsonServerConfig.RANGES);

			// Updated visible items
			Set<String> newVisibleKeyset = visibleJson.keySet();
			for (String key : newVisibleKeyset)
			{
				visibleJsonMap.put(key, visibleJson.get(key));
			}

			// Updated values
			Set<String> newValuesKeyset = valuesJson.keySet();
			for (String key : newValuesKeyset)
			{
				valuesJsonMap.put(key, valuesJson.get(key));
			}

			// Updated ranges
			Set<String> newRangesKeyset = rangesJson.keySet();
			for (String key : newRangesKeyset)
			{
				rangesJsonMap.put(key, rangesJson.get(key));
			}
		}
	}

	/**
	 * @param maxAttempts
	 * @param sleepInterval
	 * @param jsonProcessor
	 * @return
	 * @throws IOException
	 */
	private boolean isReady(int maxAttempts, long sleepInterval, JsonConfigProcessor jsonProcessor) throws IOException
	{
		int waitCount = 0;
		while (serverMessage == null || jsonProcessor.getInitialOutput(serverMessage) == null)
		{
			if (waitCount >= maxAttempts)
			{
				return false;
			}
			try
			{
				Thread.sleep(sleepInterval);
			}
			catch (Exception e)
			{
				// ignore
			}
			waitCount++;
		}
		return true;
	}

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this <code>IWorkbenchPart</code> method disposes all
	 * nested editors. Subclasses may extend.
	 */
	public void dispose()
	{
		// Kill the Config server process
		configServer.destroy();
		super.dispose();
	}

	/**
	 * Saves the SDK configuration editor
	 */
	public void doSave(IProgressMonitor monitor)
	{
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(IJsonServerConfig.VERSION, 2);
		jsonObject.put(IJsonServerConfig.SET, modifiedJsonMap);
		jsonObject.put(IJsonServerConfig.SAVE, null);

		String command = jsonObject.toJSONString();
		configServer.execute(command);

		modifiedJsonMap.clear();
		isDirty = false;
		editorDirtyStateChanged();
	}

	public void doSaveAs()
	{
		// No-op
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker)
	{
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}

	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method checks that the input is an instance of
	 * <code>IFileEditorInput</code>.
	 */
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException
	{
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException(Messages.SDKConfigurationEditor_InvalidInput);
		super.init(site, editorInput);

		this.project = getProject();
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed()
	{
		return true;
	}

	/**
	 * @return
	 */
	public KConfigMenuItem getInitalInput()
	{
		KConfigMenuProcessor jsonReader = new KConfigMenuProcessor(project);
		try
		{
			return jsonReader.reader();
		}
		catch (Exception e)
		{
			IDFCorePlugin.log(e);
		}
		return null;

	}

	protected void hookListeners()
	{
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent event)
			{
				if (event.getSelection() instanceof IStructuredSelection)
				{
					KConfigMenuItem selectedElement = (KConfigMenuItem) event.getStructuredSelection()
							.getFirstElement();
					updateUI(selectedElement);

				}
			}

		});
	}

	private FilteredTree createFilteredTree(Group group)
	{
		int style = SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER;
		FilteredTree transfersTree = new FilteredTree(group, style, new PatternFilter(), true)
		{
			@Override
			protected TreeViewer doCreateTreeViewer(Composite parent, int style)
			{
				return new TreeViewer(parent, style);
			}
		};
		return transfersTree;
	}

	private void updateUI(KConfigMenuItem selectedElement)
	{
		if (selectedElement == null)
		{
			return;
		}

		this.selectedElement = selectedElement;

		// dispose old elements
		Control[] updateUICompositeControls = updateUIComposite.getChildren();
		for (Control control : updateUICompositeControls)
		{
			control.dispose();
		}

		updateUIComposite.setLayout((new GridLayout(2, false)));
		updateUIComposite.setText(selectedElement.getTitle());
		GridData updateCompsiteGD = new GridData(SWT.FILL, SWT.FILL, true, true);
		updateCompsiteGD.verticalIndent = 10;
		updateUIComposite.setLayoutData(updateCompsiteGD);

		renderMenuItems(selectedElement);
		updateUIComposite.layout(true);
		updateUIComposite.getParent().layout(true);
	}

	protected void renderMenuItems(KConfigMenuItem selectedElement)
	{

		// add children here
		List<KConfigMenuItem> children = selectedElement.getChildren();
		for (KConfigMenuItem kConfigMenuItem : children)
		{
			String type = kConfigMenuItem.getType();

			String configKey = kConfigMenuItem.getName();
			Object configValue = valuesJsonMap.get(configKey);
			Object isEnabled = visibleJsonMap.get(configKey);
			Object newConfigValue = modifiedJsonMap.get(configKey);

			String tooltip = kConfigMenuItem.getHelp();

			if (type.equals(IJsonServerConfig.STRING_TYPE))
			{
				Label labelName = new Label(updateUIComposite, SWT.NONE);
				labelName.setText(kConfigMenuItem.getTitle());
				labelName.setEnabled(Boolean.valueOf((boolean) isEnabled));

				Text textControl = new Text(updateUIComposite, SWT.SINGLE | SWT.BORDER);
				GridData gridData = new GridData();
				gridData.widthHint = 250;
				textControl.setLayoutData(gridData);
				textControl.setEnabled(Boolean.valueOf((boolean) isEnabled));
				textControl.setToolTipText(tooltip);
				if (configValue != null)
				{
					textControl.setText(newConfigValue != null ? (String) newConfigValue : (String) configValue);
				}
				textControl.addModifyListener(addModifyListener(configKey, textControl));
			}
			else if (type.equals(IJsonServerConfig.HEX_TYPE))
			{
				Label labelName = new Label(updateUIComposite, SWT.NONE);
				labelName.setText(kConfigMenuItem.getTitle());
				labelName.setEnabled(Boolean.valueOf((boolean) isEnabled));

				Text textControl = new Text(updateUIComposite, SWT.SINGLE | SWT.BORDER);
				GridData gridData = new GridData();
				gridData.widthHint = 250;
				textControl.setLayoutData(gridData);
				textControl.setEnabled(Boolean.valueOf((boolean) isEnabled));
				textControl.setToolTipText(tooltip);
				if (configValue != null)
				{
					textControl.setText(newConfigValue != null ? Long.toString((long) newConfigValue)
							: Long.toString((long) configValue));
				}
				textControl.addModifyListener(addModifyListener(configKey, textControl));

			}
			else if (type.equals(IJsonServerConfig.BOOL_TYPE))
			{

				Button button = new Button(updateUIComposite, SWT.CHECK);
				button.setText(kConfigMenuItem.getTitle());
				button.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, 2, 1));
				button.setEnabled(Boolean.valueOf((boolean) isEnabled));
				button.setToolTipText(tooltip);
				if (configValue != null)
				{
					button.setSelection((boolean) configValue);
				}
				button.addSelectionListener(new SelectionAdapter()
				{
					@Override
					public void widgetSelected(SelectionEvent e)
					{
						JSONObject jsonObj = new JSONObject();
						jsonObj.put(configKey, button.getSelection());
						executeCommand(jsonObj);
					}

				});

			}

			else if (type.equals(IJsonServerConfig.INT_TYPE))
			{
				Label labelName = new Label(updateUIComposite, SWT.NONE);
				labelName.setText(kConfigMenuItem.getTitle());
				labelName.setEnabled(Boolean.valueOf((boolean) isEnabled));

				Text text = new Text(updateUIComposite, SWT.SINGLE | SWT.BORDER);
				GridData gridData = new GridData();
				gridData.widthHint = 250;
				text.setLayoutData(gridData);
				text.setEnabled(Boolean.valueOf((boolean) isEnabled));
				text.setToolTipText(tooltip);

				if (configValue != null)
				{
					text.setText(newConfigValue != null ? String.valueOf(newConfigValue) : String.valueOf(configValue));

				}
				text.addModifyListener(addModifyListener(configKey, text));

			}
			else if (type.equals(IJsonServerConfig.CHOICE_TYPE))
			{
				Label labelName = new Label(updateUIComposite, SWT.NONE);
				labelName.setText(kConfigMenuItem.getTitle());
				labelName.setEnabled(false);

				Combo choiceCombo = new Combo(updateUIComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
				choiceCombo.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, 1, 1));

				GridData gridData = new GridData();
				gridData.widthHint = 250;
				choiceCombo.setLayoutData(gridData);
				choiceCombo.setEnabled(false);

				List<KConfigMenuItem> choiceItems = kConfigMenuItem.getChildren();
				int index = 0;
				for (KConfigMenuItem item : choiceItems)
				{
					String localConfigKey = item.getName();
					choiceCombo.add(item.getTitle());
					choiceCombo.setData(item.getTitle(), localConfigKey);

					isEnabled = valuesJsonMap.get(localConfigKey);
					if (isEnabled != null && isEnabled.equals(true))
					{
						choiceCombo.select(index);
						choiceCombo.setEnabled(Boolean.valueOf((boolean) isEnabled));
						labelName.setEnabled(Boolean.valueOf((boolean) isEnabled));
					}
					index++;
				}

				choiceCombo.addSelectionListener(new SelectionAdapter()
				{
					@Override
					public void widgetSelected(SelectionEvent e)
					{
						Object key = choiceCombo.getData(choiceCombo.getText());
						if (key != null)
						{
							JSONObject jsonObj = new JSONObject();
							jsonObj.put(key, true);
							executeCommand(jsonObj);

						}
					}
				});
			}

			// kConfigMenuItem has children?
			if (!type.equals(IJsonServerConfig.CHOICE_TYPE) && !type.equals(IJsonServerConfig.MENU_TYPE)
					&& kConfigMenuItem.hasChildren())
			{
				renderMenuItems(kConfigMenuItem);
			}
		}
	}

	protected ModifyListener addModifyListener(String configKey, Text textControl)
	{
		return new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				isDirty = true;
				editorDirtyStateChanged();
				modifiedJsonMap.put(configKey, textControl.getText().trim());
			}
		};
	}

	protected void executeCommand(JSONObject jsonObj)
	{
		isDirty = true;
		editorDirtyStateChanged();

		JSONObject jsonObject = new JSONObject();
		jsonObject.put(IJsonServerConfig.VERSION, 2);
		jsonObject.put(IJsonServerConfig.SET, jsonObj);

		String command = jsonObject.toJSONString();
		configServer.execute(command);
	}

	@Override
	public boolean isDirty()
	{
		return isDirty;
	}

	/**
	 * Called to indicate that the editor has been made dirty or the changes have been saved.
	 */
	public void editorDirtyStateChanged()
	{
		firePropertyChange(IEditorPart.PROP_DIRTY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.espressif.idf.sdk.config.core.server.IMessageHandlerListener#notifyRequestServed(java.lang.String)
	 */
	@Override
	public void notifyRequestServed(String message)
	{
		this.serverMessage = message;

		if (selectedElement != null)
		{
			try
			{
				update();

				// Update in UI thread
				Display.getDefault().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						updateUI(selectedElement);
					}
				});
			}
			catch (ParseException e1)
			{
				IDFCorePlugin.log(e1);
			}

		}
	}

	@Override
	public void pageChanged(PageChangedEvent event)
	{
		IEditorPart activeEditor = getActiveEditor();
		if (activeEditor instanceof TextEditor)
		{
			String msg1 = Messages.SDKConfigurationEditor_SaveChanges;
			String msg2 = Messages.SDKConfigurationEditor_ChangesWontbeSaved;
			String title = Messages.SDKConfigurationEditor_SDKConfiguration;

			if (isDirty())
			{
				boolean isOkay = MessageDialog.openQuestion(Display.getDefault().getActiveShell(), title,
						MessageFormat.format("{0} \n\n{1}", msg1, msg2)); //$NON-NLS-1$
				if (isOkay)
				{
					doSave(new NullProgressMonitor());
				}
				return;
			}

			MessageDialog.openWarning(Display.getDefault().getActiveShell(), title, msg2);
		}

	}

}
