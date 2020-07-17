/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.ui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.SDKConfigUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.sdk.config.core.IJsonServerConfig;
import com.espressif.idf.sdk.config.core.KConfigMenuItem;
import com.espressif.idf.sdk.config.core.KConfigMenuProcessor;
import com.espressif.idf.sdk.config.core.server.CommandType;
import com.espressif.idf.sdk.config.core.server.ConfigServerManager;
import com.espressif.idf.sdk.config.core.server.IJsonConfigOutput;
import com.espressif.idf.sdk.config.core.server.IMessageHandlerListener;
import com.espressif.idf.sdk.config.core.server.JsonConfigProcessor;
import com.espressif.idf.sdk.config.core.server.JsonConfigServer;
import com.espressif.idf.ui.dialogs.HelpPopupDialog;

/**
 * SDK Configuration editor which represents the UI for the all sdkconfig fields
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
@SuppressWarnings("unchecked")
public class SDKConfigurationEditor extends MultiPageEditorPart
		implements ISaveablePart, IMessageHandlerListener, IPageChangedListener
{

	public static String EDITOR_ID = "com.espressif.idf.sdk.config.ui.editor"; //$NON-NLS-1$

	private static final String ICONS_INFO_OBJ_GIF = "icons/help.gif"; //$NON-NLS-1$

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

	private HelpPopupDialog infoDialog;

	private CommandType type;

	private ScrolledComposite sc;

	public SDKConfigurationEditor()
	{
		super();
		addPageChangedListener(this);
	}

	/**
	 * Creates the pages of the SDK configuration editor.
	 */
	@Override
	protected void createPages()
	{

		IWorkbench workbench = PlatformUI.getWorkbench();
		IProgressService progressService = workbench.getProgressService();
		final IRunnableWithProgress runnable = monitor -> {
			monitor.beginTask(Messages.SDKConfigurationEditor_LaunchSDKConfigEditor, 3);

			try
			{
				// 1. Getting kconfig_menus.json
				String configMenuJsonPath = new SDKConfigUtil().getConfigMenuFilePath(project);
				if (configMenuJsonPath == null || !new File(configMenuJsonPath).exists())
				{
					String errorMsg = Messages.SDKConfigurationEditor_UnableFindKConfigFile + configMenuJsonPath;
					createErrorPage(errorMsg);
					return;
				}
				monitor.worked(1);

				monitor.setTaskName(Messages.SDKConfigurationEditor_StartingJSONConfigServer);

				// 2. Getting output from the configuration server
				initConfigServer(project);
				monitor.worked(2);

			}
			catch (Exception x) {
				throw new InvocationTargetException(x, x.getMessage());
			}
		};
		try
		{
			progressService.busyCursorWhile(runnable);
		}
		catch (Exception e)
		{
			Logger.log(SDKConfigUIPlugin.getDefault(), e);
		}

		if (valuesJsonMap == null)
		{
			String errorMsg = Messages.SDKConfigurationEditor_ErrorRetrievingOutput;
			createErrorPage(errorMsg);
			return;
		}

		// 3. Build the UI
		createDesignPage();
		createSourcePage();

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

		Composite parent = new Composite(getContainer(), SWT.NONE);

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

		treeViewer = transfersTree.getViewer();

		// Create the tree viewer as a child of the composite parent
		treeViewer.setContentProvider(new ConfigContentProvider(project));
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

		sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setLayout((new GridLayout(1, false)));
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = 500;
		sc.setLayoutData(gridData);

		// UI for the selected element
		updateUIComposite = new Group(sc, SWT.V_SCROLL);
		updateUIComposite.setLayout((new GridLayout(1, false)));
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
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
			if (kConfigMenuItem.getTitle().equals(IJsonServerConfig.COMPONENT_CONFIG_TITLE))
			{
				return kConfigMenuItem;
			}
		}

		return null;
	}

	/**
	 * @param errorMessage
	 */
	private void createErrorPage(String errorMessage)
	{
		Composite parent = new Composite(getContainer(), SWT.V_SCROLL);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		parent.setLayout(layout);

		new Label(parent, SWT.NONE).setText(errorMessage);

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

		// Create console
		MessageConsole msgConsole = createConsole("JSON Configuration Server Console"); //$NON-NLS-1$
		msgConsole.clearConsole();

		MessageConsoleStream console = msgConsole.newMessageStream();
		msgConsole.activate();
		openConsoleView();

		configServer = ConfigServerManager.INSTANCE.getServer(project);

		// register the editor with the server to notify about the events
		configServer.addListener(this);
		configServer.addConsole(console);

		// will wait and check for the server response
		JsonConfigProcessor jsonProcessor = new JsonConfigProcessor();

		int MAX_NO_OF_ATTEMPTS = 120; // timeout
		String sdkconfigTimeout = getSystemProperty("sdkconfig.timeout"); //$NON-NLS-1$
		if (!StringUtil.isEmpty(sdkconfigTimeout))
		{
			MAX_NO_OF_ATTEMPTS = Integer.valueOf(sdkconfigTimeout);
		}
		if (isReady(MAX_NO_OF_ATTEMPTS, 1000, jsonProcessor))
		{
			String response = jsonProcessor.getInitialOutput(serverMessage);
			IJsonConfigOutput output = configServer.getOutput(response, false);
			valuesJsonMap = output.getValuesJsonMap();
			visibleJsonMap = output.getVisibleJsonMap();
			rangesJsonMap = output.getRangesJsonMap();
		}
	}

	protected void update() throws ParseException
	{
		JsonConfigProcessor jsonProcessor = new JsonConfigProcessor();
		String response = jsonProcessor.getInitialOutput(serverMessage);
		if (response == null)
		{
			return;
		}

		IJsonConfigOutput output = configServer.getOutput(response, true);
		valuesJsonMap = output.getValuesJsonMap();
		visibleJsonMap = output.getVisibleJsonMap();
		rangesJsonMap = output.getRangesJsonMap();

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
			if (type == CommandType.CONNECTION_CLOSED || waitCount >= maxAttempts)
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
	@Override
	public void dispose()
	{
		// Kill the Config server process
		if (configServer != null)
		{
			configServer.destroy();
		}
		ConfigServerManager.INSTANCE.deleteServer(project);
		super.dispose();
	}

	/**
	 * Saves the SDK configuration editor
	 */
	@Override
	public void doSave(IProgressMonitor monitor)
	{
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(IJsonServerConfig.VERSION, 2);
		jsonObject.put(IJsonServerConfig.SET, modifiedJsonMap);
		jsonObject.put(IJsonServerConfig.SAVE, null);

		String command = jsonObject.toJSONString();
		configServer.execute(command, CommandType.SAVE);

		modifiedJsonMap.clear();
		isDirty = false;
		editorDirtyStateChanged();
	}

	@Override
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
	@Override
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
	@Override
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
			Logger.log(SDKConfigUIPlugin.getDefault(), e);
		}
		return null;

	}

	protected void hookListeners()
	{
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
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

	@SuppressWarnings("deprecation")
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
		if (selectedElement == null || updateUIComposite == null || updateUIComposite.isDisposed()
				|| valuesJsonMap == null)
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

		updateUIComposite.setLayout((new GridLayout(3, false)));
		updateUIComposite.setText(selectedElement.getTitle());
		GridData updateCompsiteGD = new GridData(SWT.FILL, SWT.FILL, true, true);
		updateCompsiteGD.verticalIndent = 10;
		updateUIComposite.setLayoutData(updateCompsiteGD);

		renderMenuItems(selectedElement);

		sc.setContent(updateUIComposite);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(updateUIComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sc.setShowFocusedControl(true);

		updateUIComposite.layout(true);
	}

	protected void renderMenuItems(KConfigMenuItem selectedElement)
	{
		// add children here
		List<KConfigMenuItem> children = selectedElement.getChildren();
		for (KConfigMenuItem kConfigMenuItem : children)
		{
			String type = kConfigMenuItem.getType();
			String configKey = kConfigMenuItem.getId();
			Object configValue = valuesJsonMap.get(configKey);
			boolean isVisible = (visibleJsonMap.get(configKey) != null ? (boolean) visibleJsonMap.get(configKey)
					: false);
			Object newConfigValue = modifiedJsonMap.get(configKey);
			String helpInfo = kConfigMenuItem.getHelp();

			if (isVisible && type.equals(IJsonServerConfig.STRING_TYPE))
			{
				Label labelName = new Label(updateUIComposite, SWT.NONE);
				labelName.setText(kConfigMenuItem.getTitle());

				Text textControl = new Text(updateUIComposite, SWT.SINGLE | SWT.BORDER);
				GridData gridData = new GridData();
				gridData.widthHint = 250;
				textControl.setLayoutData(gridData);
				textControl.setToolTipText(helpInfo);
				if (configValue != null)
				{
					textControl.setText(newConfigValue != null ? (String) newConfigValue : (String) configValue);
				}
				textControl.addModifyListener(addModifyListener(configKey, textControl));
				addTooltipImage(kConfigMenuItem);

			}
			else if (isVisible && type.equals(IJsonServerConfig.HEX_TYPE))
			{
				Label labelName = new Label(updateUIComposite, SWT.NONE);
				labelName.setText(kConfigMenuItem.getTitle());

				Text textControl = new Text(updateUIComposite, SWT.SINGLE | SWT.BORDER);
				GridData gridData = new GridData();
				gridData.widthHint = 250;
				textControl.setLayoutData(gridData);
				textControl.setToolTipText(helpInfo);
				if (configValue != null)
				{
					textControl.setText(newConfigValue != null ? Long.toString((long) newConfigValue)
							: Long.toString((long) configValue));
				}
				textControl.addModifyListener(addModifyListener(configKey, textControl));
				addTooltipImage(kConfigMenuItem);
			}
			else if (isVisible && type.equals(IJsonServerConfig.BOOL_TYPE))
			{
				Button button = new Button(updateUIComposite, SWT.CHECK);
				button.setText(kConfigMenuItem.getTitle());
				button.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, 2, 1));
				button.setToolTipText(helpInfo);
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
				addTooltipImage(kConfigMenuItem);
			}

			else if (isVisible && type.equals(IJsonServerConfig.INT_TYPE))
			{
				Label labelName = new Label(updateUIComposite, SWT.NONE);
				labelName.setText(kConfigMenuItem.getTitle());

				Text text = new Text(updateUIComposite, SWT.SINGLE | SWT.BORDER);
				GridData gridData = new GridData();
				gridData.widthHint = 250;
				text.setLayoutData(gridData);
				text.setToolTipText(helpInfo);

				if (configValue != null)
				{
					text.setText(newConfigValue != null ? String.valueOf(newConfigValue) : String.valueOf(configValue));

				}
				text.addModifyListener(addModifyListener(configKey, text));
				addTooltipImage(kConfigMenuItem);

			}
			else if (isVisible && type.equals(IJsonServerConfig.MENU_TYPE))
			{
				renderMenuItems(kConfigMenuItem);
			}
			else if (type.equals(IJsonServerConfig.CHOICE_TYPE))
			{
				Logger.logTrace(SDKConfigUIPlugin.getDefault(),
						"Config key >" + configKey + " visiblity status >" + isVisible); //$NON-NLS-1$ //$NON-NLS-2$
				
				if (isExist(visibleJsonMap, configKey))
				{
					List<KConfigMenuItem> choiceItems = kConfigMenuItem.getChildren();
					Label labelName = new Label(updateUIComposite, SWT.NONE);
					labelName.setText(kConfigMenuItem.getTitle());

					Combo choiceCombo = new Combo(updateUIComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
					choiceCombo.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, 1, 1));

					GridData gridData = new GridData();
					gridData.widthHint = 250;
					choiceCombo.setLayoutData(gridData);

					int index = 0;
					for (KConfigMenuItem item : choiceItems)
					{
						String localConfigKey = item.getId();
						if (isExist(visibleJsonMap, localConfigKey))
						{
							choiceCombo.add(item.getTitle());
							choiceCombo.setData(item.getTitle(), localConfigKey);
							
							//Check if this selected?
							if (isExist(valuesJsonMap, localConfigKey))
							{
								choiceCombo.select(index);
							}
							index++;
						}

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
					addTooltipImage(kConfigMenuItem);
				}
			}

			// kConfigMenuItem has children?
			if (!type.equals(IJsonServerConfig.CHOICE_TYPE) && !type.equals(IJsonServerConfig.MENU_TYPE)
					&& kConfigMenuItem.hasChildren())
			{
				renderMenuItems(kConfigMenuItem);
			}
		}
	}

	protected boolean isExist(JSONObject jsonMap, String key)
	{
		return jsonMap.get(key) != null ? (boolean) jsonMap.get(key) : false;
	}

	protected void addTooltipImage(KConfigMenuItem kConfigMenuItem)
	{
		Label labelName = new Label(updateUIComposite, SWT.NONE);
		labelName.setImage(SDKConfigUIPlugin.getImage(ICONS_INFO_OBJ_GIF));
		labelName.addListener(SWT.MouseUp, getMouseClickListener(kConfigMenuItem));
		labelName.setToolTipText(Messages.SDKConfigurationEditor_Help);
	}

	private Listener getMouseClickListener(KConfigMenuItem kConfigMenuItem)
	{
		return new Listener()
		{
			@Override
			public void handleEvent(Event event)
			{
				String help = kConfigMenuItem.getHelp();
				String title = kConfigMenuItem.getTitle();
				String configKey = kConfigMenuItem.getId();
				
				//frame message
				StringBuilder message = new StringBuilder();
				message.append(title);
				message.append("\n\n");
				message.append(help);
				
				//get range info
				Object range = rangesJsonMap.get(configKey);
				if (range != null)
				{
					message.append("\n\n");
					message.append("Range Information:");
					message.append("\n");
					message.append(range.toString());
				}
				
				if (StringUtil.isEmpty(help))
				{
					String msg = MessageFormat.format(Messages.SDKConfigurationEditor_NoHelpAvailable, configKey);
					Logger.log(SDKConfigUIPlugin.getDefault(), msg);
					return;
				}
				Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				if (infoDialog != null)
				{
					infoDialog.close();
				}
				infoDialog = new HelpPopupDialog(activeShell, Messages.SDKConfigurationEditor_Help + " > " + configKey, //$NON-NLS-1$
						message.toString()); // $NON-NLS-2$
				infoDialog.open();

			}
		};
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
		configServer.execute(command, CommandType.SET);
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
	public void notifyRequestServed(String message, CommandType type)
	{
		this.serverMessage = message;
		this.type = type;
		Logger.log(SDKConfigUIPlugin.getDefault(), message);

		if (selectedElement != null)
		{
			try
			{
				// reset the modified map
				if (type == CommandType.LOAD)
				{
					modifiedJsonMap.clear();
					isDirty = false;
					editorDirtyStateChanged();
				}

				// fetch the latest values
				update();

				// Update in UI thread
				Display.getDefault().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						if (!treeViewer.getControl().isDisposed())
						{
							treeViewer.refresh();
							updateUI(selectedElement);
						}
					}
				});
			}
			catch (ParseException e1)
			{
				Logger.log(SDKConfigUIPlugin.getDefault(), e1);
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
		}

	}

	private MessageConsole createConsole(String name)
	{
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
		{
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		}
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	private void openConsoleView()
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView(IConsoleConstants.ID_CONSOLE_VIEW);
				}
				catch (PartInitException e)
				{
					Logger.log(e);
				}
			}
		});
	}

	public String getSystemProperty(String option)
	{
		if (option == null)
		{
			return null;
		}
		return System.getProperty(option);
	}
}
