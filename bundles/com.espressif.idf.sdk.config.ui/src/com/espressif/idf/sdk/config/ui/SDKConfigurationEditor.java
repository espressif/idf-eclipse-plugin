package com.espressif.idf.sdk.config.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
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

public class SDKConfigurationEditor extends MultiPageEditorPart implements ISaveablePart, IMessageHandlerListener
{

	private TreeViewer treeViewer;

	private Group updateUIComposite;

	private IProject project;

	private boolean isDirty;

	private JsonConfigServer configServer;

	private JSONObject valuesJsonMap;

	private JSONObject visibleJsonMap;

	private JSONObject modifiedValuesJsonMap = new JSONObject();

	private String serverMessage;

	public SDKConfigurationEditor()
	{
		super();
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
			int index = addPage(editor, getEditorInput());
			setPageText(index, "Source");
		}
		catch (PartInitException e)
		{
			ErrorDialog.openError(getSite().getShell(), "Error creating nested text editor", null, e.getStatus());
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
		treeViewer.expandAll();

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
		setPageText(index, "Design");
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
				.setText("Unable to find kconfig_menus.json in the build config folder.\n" + kconfigMenuJsonFile);

		int index = addPage(parent);
		setPageText(index, "Design");

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
	@SuppressWarnings("unchecked")
	public void doSave(IProgressMonitor monitor)
	{
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(IJsonServerConfig.VERSION, 2);
		jsonObject.put(IJsonServerConfig.SET, modifiedValuesJsonMap);
		jsonObject.put(IJsonServerConfig.SAVE, null);
//		jsonObject.put("load", null);

		String command = jsonObject.toJSONString();
		configServer.execute(command);

		modifiedValuesJsonMap.clear();
		isDirty = false;
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
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
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

	@SuppressWarnings("unchecked")
	private void updateUI(KConfigMenuItem selectedElement)
	{
		if (selectedElement == null)
		{
			return;
		}

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

		// add children here
		List<KConfigMenuItem> children = selectedElement.getChildren();
		for (KConfigMenuItem kConfigMenuItem : children)
		{
			String type = kConfigMenuItem.getType();

			String configKey = kConfigMenuItem.getName();
			Object configValue = valuesJsonMap.get(configKey);
			Object isEnabled = visibleJsonMap.get(configKey);

			// if it's modified will take the new value
			Object newConfigValue = modifiedValuesJsonMap.get(configKey);
			if (newConfigValue != null)
			{
				configValue = newConfigValue;
			}

			if (type.equals(IJsonServerConfig.STRING_TYPE))
			{
				Label labelName = new Label(updateUIComposite, SWT.NONE);
				labelName.setText(kConfigMenuItem.getTitle());

				Text textControl = new Text(updateUIComposite, SWT.SINGLE | SWT.BORDER);
				GridData gridData = new GridData();
				gridData.widthHint = 250;
				textControl.setLayoutData(gridData);
				textControl.setEnabled(Boolean.valueOf((boolean) isEnabled));
				if (configValue != null)
				{
					textControl.setText((String) configValue);
				}
				textControl.addModifyListener(new ModifyListener()
				{
					@Override
					public void modifyText(ModifyEvent e)
					{
						modifiedValuesJsonMap.put(configKey, textControl.getText().trim());
						isDirty = true;
						editorDirtyStateChanged();
					}
				});

			}
			else if (type.equals(IJsonServerConfig.HEX_TYPE))
			{
				Label labelName = new Label(updateUIComposite, SWT.NONE);
				labelName.setText(kConfigMenuItem.getTitle());

				Text textControl = new Text(updateUIComposite, SWT.SINGLE | SWT.BORDER);
				GridData gridData = new GridData();
				gridData.widthHint = 250;
				textControl.setLayoutData(gridData);
				textControl.setEnabled(Boolean.valueOf((boolean) isEnabled));
				if (configValue != null)
				{
					textControl.setText(Long.toString((long) configValue));
				}
				textControl.addModifyListener(new ModifyListener()
				{
					@Override
					public void modifyText(ModifyEvent e)
					{
						modifiedValuesJsonMap.put(configKey, textControl.getText().trim());
						isDirty = true;
						editorDirtyStateChanged();
					}
				});

			}
			else if (type.equals(IJsonServerConfig.BOOL_TYPE))
			{
				Button labelName = new Button(updateUIComposite, SWT.CHECK);
				labelName.setText(kConfigMenuItem.getTitle());
				labelName.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, 2, 1));
				labelName.setEnabled(Boolean.valueOf((boolean) isEnabled));
				if (configValue != null)
				{
					labelName.setSelection((boolean) configValue);
				}
				labelName.addSelectionListener(new SelectionAdapter()
				{
					@Override
					public void widgetSelected(SelectionEvent e)
					{
						// record change
						modifiedValuesJsonMap.put(configKey, labelName.getSelection());
						isDirty = true;
						editorDirtyStateChanged();
					}
				});
			}

			else if (type.equals(IJsonServerConfig.INT_TYPE))
			{
				Label labelName = new Label(updateUIComposite, SWT.NONE);
				labelName.setText(kConfigMenuItem.getTitle());

				Text text = new Text(updateUIComposite, SWT.SINGLE | SWT.BORDER);
				GridData gridData = new GridData();
				gridData.widthHint = 250;
				text.setLayoutData(gridData);
				text.setEnabled(Boolean.valueOf((boolean) isEnabled));

				if (configValue != null)
				{
					text.setText(String.valueOf(configValue));
				}
				text.addModifyListener(new ModifyListener()
				{
					@Override
					public void modifyText(ModifyEvent e)
					{
						// record change
						modifiedValuesJsonMap.put(configKey, text.getText().trim());

						isDirty = true;
						editorDirtyStateChanged();
					}
				});

			}
			else if (type.equals(IJsonServerConfig.CHOICE_TYPE))
			{
				Label labelName = new Label(updateUIComposite, SWT.NONE);
				labelName.setText(kConfigMenuItem.getTitle());

				Combo choiceCombo = new Combo(updateUIComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
				choiceCombo.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, 1, 1));

				GridData gridData = new GridData();
				gridData.widthHint = 250;
				choiceCombo.setLayoutData(gridData);

				List<KConfigMenuItem> choiceItems = kConfigMenuItem.getChildren();
				int index = 0;
				for (KConfigMenuItem item : choiceItems)
				{
					String localConfigKey = item.getName();

					choiceCombo.add(item.getTitle());
					choiceCombo.setData(item.getTitle(), localConfigKey);

					Object object = modifiedValuesJsonMap.get(localConfigKey);

					if (object == null)
					{
						object = valuesJsonMap.get(localConfigKey);
					}
					if (object != null && object.equals(true))
					{
						choiceCombo.select(index);
					}
					index++;
				}

				choiceCombo.addSelectionListener(new SelectionAdapter()
				{
					@Override
					public void widgetSelected(SelectionEvent e)
					{
						Object data = choiceCombo.getData(choiceCombo.getText());
						if (data != null)
						{
							modifiedValuesJsonMap.put(data, true);

							isDirty = true;
							editorDirtyStateChanged();

						}
					}
				});
			}
		}
		updateUIComposite.layout(true);
		updateUIComposite.getParent().layout(true);
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

	@Override
	public void notifyRequestServed(String message)
	{
		this.serverMessage = message;
//		System.out.println("server message:" + message);
	}

}
