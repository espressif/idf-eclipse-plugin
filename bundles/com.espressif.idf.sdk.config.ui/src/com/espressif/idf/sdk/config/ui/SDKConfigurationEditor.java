/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.ui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.progress.IProgressService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
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
import com.espressif.idf.ui.IDFConsole;

/**
 * SDK Configuration editor which represents the UI for the all sdkconfig fields
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
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
		// remember current build folder for the project IEP-1250
		final String buildFolder = getCurrentBuildFolder();

		final IRunnableWithProgress runnable = monitor -> {
			monitor.beginTask(Messages.SDKConfigurationEditor_LaunchSDKConfigEditor, 3);

			try
			{
				// if sdkconfig is located in the build folder then temporary setting this folder as build folder on the
				// project level IEP-1250
				if (isSdkConfigLocatedInBuildFolder())
				{
					IDFUtil.setBuildDir(project, getSdkConfigParentFolderOpt().get().getLocation().toOSString());
				}
				// 1. Getting kconfig_menus.json
				final String configMenuJsonPath = new SDKConfigUtil().getConfigMenuFilePath(project);
				if (configMenuJsonPath == null || !new File(configMenuJsonPath).exists())
				{
					Display.getDefault().asyncExec(() -> {

						String errorMsg = Messages.SDKConfigurationEditor_UnableFindKConfigFile + configMenuJsonPath;
						createErrorPage(errorMsg);
					});
					return;
				}
				monitor.worked(1);

				monitor.setTaskName(Messages.SDKConfigurationEditor_StartingJSONConfigServer);

				// 2. Getting output from the configuration server
				initConfigServer(project);
				monitor.worked(2);

			}
			catch (Exception x)
			{
				// rollback build folder if something went wrong
				rollbackBuildFolder(buildFolder);
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

		setPartName(getPartName() + " (" + getFile().getName() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ s

		// 3. Build the UI
		createDesignPage();
		createSourcePage();

		// rollback build folder after UI is built
		rollbackBuildFolder(buildFolder);
	}

	/**
	 * sdkconfig source editor
	 */
	private void createSourcePage()
	{
		try
		{
			TextEditor editor = new TextEditor();
			Object control = (editor).getAdapter(Control.class);
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

		Group treeComposite = new Group(parent, SWT.V_SCROLL | SWT.TOP);
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 2;
		treeComposite.setLayout(layout);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, false, true);
		layoutData.widthHint = 250;
		treeComposite.setLayoutData(layoutData);

		FilteredTree transfersTree = createFilteredTree(treeComposite);
		transfersTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		treeViewer = transfersTree.getViewer();

		// Create the tree viewer as a child of the composite parent
		treeViewer.setContentProvider(new ConfigContentProvider(project, getFile()));
		treeViewer.setLabelProvider(new ConfigLabelProvider());

		treeViewer.setUseHashlookup(true);

		// layout the tree viewer below the text field
		treeViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

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

	private IFile getFile()
	{
		return ((FileEditorInput) getEditorInput()).getFile();
	}

	/**
	 * @return current project
	 */
	protected IProject getProject()
	{
		IFileEditorInput editorInput = (IFileEditorInput) getEditorInput();
		IFile file = editorInput.getFile();
		return file.getProject();
	}

	/**
	 * @param project
	 * @throws IOException
	 * @throws ParseException
	 */
	protected void initConfigServer(IProject project) throws IOException
	{

		// Create console
		MessageConsoleStream console = new IDFConsole().getConsoleStream("JSON Configuration Server Console", null, //$NON-NLS-1$
				false);

		configServer = ConfigServerManager.INSTANCE.getServer(project, getFile());

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

	protected void update()
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
	private boolean isReady(int maxAttempts, long sleepInterval, JsonConfigProcessor jsonProcessor)
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
		ConfigServerManager.INSTANCE.deleteServer(project, getFile());
		super.dispose();
	}

	/**
	 * Saves the SDK configuration editor
	 */
	@Override
	public void doSave(IProgressMonitor monitor)
	{
		JSONObject jsonObject = new JSONObject();
		var version = configServer.getOutput().getVersion();
		jsonObject.put(IJsonServerConfig.VERSION, version);

		if (!modifiedJsonMap.isEmpty())
		{
			jsonObject.put(IJsonServerConfig.SET, modifiedJsonMap);

			valuesJsonMap.putAll(modifiedJsonMap);
		}
		else
		{
			jsonObject.put(IJsonServerConfig.SET, new JSONObject());
		}

		String filePath = getFile().getLocation().toOSString();
		jsonObject.put(IJsonServerConfig.SAVE, filePath);

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
		treeViewer.addSelectionChangedListener(event ->
		{
			if (event.getSelection() instanceof IStructuredSelection)
			{
				KConfigMenuItem selectedElement = (KConfigMenuItem) event.getStructuredSelection().getFirstElement();
				updateUI(selectedElement);

			}
		});
	}

	@SuppressWarnings("deprecation")
	private FilteredTree createFilteredTree(Group group)
	{
		PatternFilter patternFilter = new SDKConfigurationFilter();
		int style = SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER;
		return new FilteredTree(group, style, patternFilter, true)
		{
			@Override
			protected TreeViewer doCreateTreeViewer(Composite parent, int style)
			{
				return new TreeViewer(parent, style);
			}
		};
	}

	private void updateUI(KConfigMenuItem selectedElement)
	{
		if (selectedElement == null || updateUIComposite == null || updateUIComposite.isDisposed()
				|| valuesJsonMap == null)
		{
			return;
		}

		this.selectedElement = selectedElement;

		for (Control control : updateUIComposite.getChildren())
		{
			control.dispose();
		}

		updateUIComposite.setLayout((new GridLayout(4, false)));
		updateUIComposite.setText(selectedElement.getTitle());

		GridData updateCompsiteGD = new GridData(SWT.FILL, SWT.FILL, true, true);
		updateCompsiteGD.verticalIndent = 10;
		updateUIComposite.setLayoutData(updateCompsiteGD);

		ConfigActionHandler handler = new ConfigActionHandler()
		{
			@Override
			public void onCommandExecuted(JSONObject jsonMap)
			{
				executeCommand(jsonMap);
			}

			@Override
			public void onTextModified(String key, Object value)
			{
				isDirty = true;
				editorDirtyStateChanged();
				modifiedJsonMap.put(key, value);
			}

			@Override
			public void onResetRequested(String key)
			{
				executeResetCommand(key);
			}

			@Override
			public void onMenuResetRequested(KConfigMenuItem menu)
			{
				List<String> childIds = new ArrayList<>();
				collectAllChildIds(menu, childIds);

				if (!childIds.isEmpty())
				{
					executeResetChildrenCommand(childIds);
				}
				else if (menu.getId() != null)
				{
					executeResetCommand(menu.getId());
				}
			}
		};

		ConfigUIRenderer renderer = new ConfigUIRenderer(updateUIComposite, valuesJsonMap, visibleJsonMap,
				modifiedJsonMap, rangesJsonMap, isResetSupported(), handler);

		renderer.renderFullMenu(selectedElement);

		sc.setContent(updateUIComposite);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(updateUIComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sc.setShowFocusedControl(true);

		updateUIComposite.layout(true);
	}
	private boolean isResetSupported()
	{
		return configServer.getOutput().getVersion() >= 3;
	}

	/**
	 * Recursively collects all configuration IDs for a menu and its sub-menus
	 */
	private void collectAllChildIds(KConfigMenuItem item, List<String> childIds)
	{
		if (item == null || item.getChildren() == null)
		{
			return;
		}
		for (KConfigMenuItem child : item.getChildren())
		{
			if (child.getId() != null && !child.getId().isEmpty())
			{
				childIds.add(child.getId());
			}
			collectAllChildIds(child, childIds);
		}
	}

	protected void executeCommand(JSONObject jsonObj)
	{
		isDirty = true;
		editorDirtyStateChanged();

		valuesJsonMap.putAll(jsonObj);

		JSONObject jsonObject = new JSONObject();
		var version = configServer.getOutput().getVersion();
		jsonObject.put(IJsonServerConfig.VERSION, version);
		jsonObject.put(IJsonServerConfig.SET, jsonObj);

		String command = jsonObject.toJSONString();
		configServer.execute(command, CommandType.SET);
	}

	protected void executeResetCommand(String idToReset)
	{
		long version = configServer.getOutput().getVersion();
		if (version >= 3)
		{
			isDirty = true;
			editorDirtyStateChanged();

			JSONObject jsonObject = new JSONObject();
			jsonObject.put(IJsonServerConfig.VERSION, 3);

			JSONArray resetArray = new JSONArray();
			resetArray.add(idToReset);
			jsonObject.put(IJsonServerConfig.RESET, resetArray);

			String command = jsonObject.toJSONString();
			configServer.execute(command, CommandType.RESET);
		}

	}

	protected void executeResetChildrenCommand(List<String> idsToReset)
	{
		long version = configServer.getOutput().getVersion();
		if (version >= 3)
		{
			if (idsToReset == null || idsToReset.isEmpty())
				return;

			isDirty = true;
			editorDirtyStateChanged();

			JSONObject jsonObject = new JSONObject();
			jsonObject.put(IJsonServerConfig.VERSION, 3);

			JSONArray resetArray = new JSONArray();
			resetArray.addAll(idsToReset);
			jsonObject.put(IJsonServerConfig.RESET, resetArray);

			String command = jsonObject.toJSONString();
			configServer.execute(command, CommandType.RESET);
		}
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
	 * (non-Javadoc) * @see
	 * com.espressif.idf.sdk.config.core.server.IMessageHandlerListener#notifyRequestServed(java.lang.String)
	 */
	@Override
	public void notifyRequestServed(String message, CommandType type)
	{
		this.serverMessage = message;
		this.type = type;
		Logger.log(SDKConfigUIPlugin.getDefault(), message);

		if (selectedElement != null)
		{
			if (type == CommandType.LOAD || type == CommandType.RESET)
				{
					modifiedJsonMap.clear();
					isDirty = (type == CommandType.RESET);

					Display.getDefault().asyncExec(this::editorDirtyStateChanged);
				}

				if (type == CommandType.SAVE)
				{
					Display.getDefault().asyncExec(() -> {
						try
						{
							getFile().refreshLocal(org.eclipse.core.resources.IResource.DEPTH_ZERO,
									new NullProgressMonitor());
						}
						catch (CoreException e)
						{
							Logger.log(SDKConfigUIPlugin.getDefault(), e);
						}
					});
				}

				update();

				Display.getDefault().asyncExec(() ->
				{
					if (!treeViewer.getControl().isDisposed())
					{
						treeViewer.refresh();
						updateUI(selectedElement);
					}
				});
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
			}
		}

	}

	public String getSystemProperty(String option)
	{
		if (option == null)
		{
			return null;
		}
		return System.getProperty(option);
	}

	private String getCurrentBuildFolder()
	{
		String buildFolder = StringUtil.EMPTY;
		try
		{
			IDFUtil.getBuildDir(project);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return buildFolder;
	}

	private void rollbackBuildFolder(String buildFolder)
	{
		try
		{
			IDFUtil.setBuildDir(project, buildFolder);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
	}

	private boolean isSdkConfigLocatedInBuildFolder()
	{
		Optional<IContainer> sdkConfigParentOpt = getSdkConfigParentFolderOpt();
		return sdkConfigParentOpt.isPresent() && sdkConfigParentOpt.get().exists(Path
				.fromPortableString(IDFConstants.CONFIG_FOLDER + IPath.SEPARATOR + IDFConstants.KCONFIG_MENUS_JSON));
	}

	private Optional<IContainer> getSdkConfigParentFolderOpt()
	{
		if (getEditorInput() instanceof IFileEditorInput editorInput)
		{
			IFile sdkConfigFile = editorInput.getFile();
			return Optional.ofNullable(sdkConfigFile.getParent());
		}
		return Optional.empty();
	}
}
