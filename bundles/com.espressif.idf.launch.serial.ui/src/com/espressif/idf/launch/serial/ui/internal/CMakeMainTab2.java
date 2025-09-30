/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * copied from:
 * org.eclipse.ui.externaltools.internal.launchConfigurations.GenericMainTab
 *******************************************************************************/

package com.espressif.idf.launch.serial.ui.internal;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.cdt.cmake.core.ICMakeBuildConfiguration;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.ui.corebuild.GenericMainTab;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.ide.IDEEncoding;

import com.espressif.idf.core.IDFDynamicVariables;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.DfuCommandsUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.core.variable.JtagDynamicVariable;
import com.espressif.idf.core.variable.OpenocdDynamicVariable;
import com.espressif.idf.launch.serial.util.ESPFlashUtil;
import com.espressif.idf.swt.custom.StyledInfoText;
import com.espressif.idf.swt.custom.TextWithButton;
import com.espressif.idf.ui.EclipseUtil;

@SuppressWarnings("restriction")
public class CMakeMainTab2 extends GenericMainTab
{
	private static final String DOCS_ESPRESSIF_FLASH_ENCRYPTION_HTML = "https://docs.espressif.com/projects/espressif-ide/en/latest/flashdevice.html#customize-flash-arguments"; //$NON-NLS-1$
	private static final String DEFAULT_JTAG_CONFIG_OPTIONS = String.format("-s ${%s} ${%s}", //$NON-NLS-1$
			OpenocdDynamicVariable.OPENOCD_SCRIPTS, JtagDynamicVariable.JTAG_FLASH_ARGS);
	private Combo flashOverComboButton;
	private boolean isFlashOverJtag;
	private boolean isJtagFlashAvailable;
	private Text fProjText;
	private IProject selectedProject;
	private Composite mainComposite;
	private EnumMap<FlashInterface, List<Composite>> switchComposites = new EnumMap<>(FlashInterface.class);
	private EnumMap<FlashInterface, List<GridData>> switchGridDatas = new EnumMap<>(FlashInterface.class);
	private TextWithButton uartAgrumentsField;
	private TextWithButton jtagArgumentsField;
	private TextWithButton dfuArgumentsField;
	private Button checkOpenSerialMonitorButton;
	private Combo fEncodingCombo;
	private Button flashEncryptionCheckbox;

	public enum FlashInterface
	{
		UART, JTAG, DFU;

		public static String[] getNames()
		{
			return Arrays.stream(FlashInterface.values()).map(Enum::name).toArray(String[]::new);
		}

	}

	@Override
	public void createControl(Composite parent)
	{
		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setFont(parent.getFont());
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);
		StyledInfoText styledInfoText = new StyledInfoText(mainComposite);
		styledInfoText.setMouseListenerAction(() -> {
			initializeFromDefaults();
			scheduleUpdateJob();
		});

		isJtagFlashAvailable = ESPFlashUtil.checkIfJtagIsAvailable();
		setControl(mainComposite);
		createJtagFlashButton(mainComposite);
		createOpenSerialMonitorGroup(mainComposite);
		createProjectGroup(mainComposite, 0);
		createUartComposite(mainComposite);
		createJtagflashComposite(mainComposite);
		createDfuArgumentField(mainComposite);

		argumentField = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		argumentField.setVisible(false);
	}

	@Override
	protected void updateArgument(ILaunchConfiguration configuration)
	{
		// We don't need the default argument field so there is nothing to update
	}

	/**
	 * Creates the controls needed to edit the argument and prompt for argument attributes of an external tool
	 *
	 * @param parent the composite to create the controls in
	 */
	protected void createArgumentComponent(Composite parent, TextWithButton argumentField)
	{
		Group group = new Group(parent, SWT.NONE);
		String groupName = Messages.CMakeMainTab2_Arguments;
		group.setText(groupName);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_BOTH);
		group.setLayout(layout);
		group.setLayoutData(gridData);
		group.setFont(parent.getFont());

		argumentField.setParent(group);
		argumentField.addTraverseListener(event -> {
			if (event.detail == SWT.TRAVERSE_RETURN && (event.stateMask & SWT.MODIFIER_MASK) != 0)
			{
				event.doit = true;
			}
		});

		gridData = new GridData(GridData.FILL_BOTH);
		gridData.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		argumentField.setLayoutData(gridData);
		argumentField.addModifyListener(fListener);
		addControlAccessibleListener(argumentField.getControl(), group.getText());

		Composite composite = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		composite.setLayout(layout);
		composite.setLayoutData(gridData);
		composite.setFont(parent.getFont());
		Button argumentVariablesButton = createPushButton(composite, Messages.CMakeMainTab2_Variables, null);
		argumentVariablesButton.addListener(SWT.Selection, e -> handleVariablesButtonSelected(argumentField));
		Label instruction = new Label(group, SWT.NONE);
		instruction.setText(Messages.CMakeMainTab2_Note);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;
		instruction.setLayoutData(gridData);
	}

	private void handleVariablesButtonSelected(TextWithButton textField)
	{
		String variable = getVariable();
		if (variable != null)
			textField.insert(variable);
	}

	private String getVariable()
	{
		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
		dialog.open();
		return dialog.getVariableExpression();
	}

	protected void createUartComposite(Composite parent)
	{

		Composite defaultComposite = new Composite(parent, SWT.NONE);
		switchComposites.putIfAbsent(FlashInterface.UART, new ArrayList<>());
		switchComposites.get(FlashInterface.UART).add(defaultComposite);
		defaultComposite.setFont(parent.getFont());
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		GridData defaultCompositeGridData = new GridData(GridData.FILL_HORIZONTAL);
		defaultCompositeGridData.exclude = true;
		switchGridDatas.putIfAbsent(FlashInterface.UART, new ArrayList<>());
		switchGridDatas.get(FlashInterface.UART).add(defaultCompositeGridData);
		defaultComposite.setLayout(layout);
		defaultComposite.setLayoutData(defaultCompositeGridData);

		createLocationComponent(defaultComposite);
		createWorkDirectoryComponent(defaultComposite);

		GridData locationAndWorkDirGroupData = new GridData(SWT.FILL, SWT.NONE, true, false);
		locationField.getParent().setLayoutData(locationAndWorkDirGroupData);
		workDirectoryField.getParent().setLayoutData(locationAndWorkDirGroupData);

		uartAgrumentsField = new TextWithButton(parent, SWT.WRAP | SWT.BORDER);

		createArgumentComponent(defaultComposite, uartAgrumentsField);
		createFlashEncryptionCheckbox(defaultComposite);
		createVerticalSpacer(defaultComposite, 1);
	}

	private void createFlashEncryptionCheckbox(Composite parent)
	{
		Group flashGroup = new Group(parent, SWT.NONE);
		flashGroup.setText(Messages.CMakeMainTab2_FlashEncryptionGroup);
		flashGroup.setLayout(new GridLayout(1, false));
		flashGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		flashEncryptionCheckbox = new Button(flashGroup, SWT.CHECK);
		flashEncryptionCheckbox.setText(Messages.CMakeMainTab2_FlashEncryptionCheckbox);
		flashEncryptionCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

		Link flashEncryptionNote = new Link(flashGroup, SWT.WRAP);
		flashEncryptionNote.setText(Messages.CMakeMainTab2_FlashEncryptionNote);

		flashEncryptionNote.addListener(SWT.Selection, e -> Program.launch(DOCS_ESPRESSIF_FLASH_ENCRYPTION_HTML));
	}

	protected void createJtagflashComposite(Composite parent)
	{

		Composite jtagComposite = new Composite(parent, SWT.NONE);
		switchComposites.putIfAbsent(FlashInterface.JTAG, new ArrayList<>());
		switchComposites.get(FlashInterface.JTAG).add(jtagComposite);
		jtagComposite.setFont(parent.getFont());
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		GridData jtagCompositeGridData = new GridData(GridData.FILL_HORIZONTAL);
		jtagCompositeGridData.exclude = true;
		switchGridDatas.putIfAbsent(FlashInterface.JTAG, new ArrayList<>());
		switchGridDatas.get(FlashInterface.JTAG).add(jtagCompositeGridData);
		jtagComposite.setLayout(layout);
		jtagComposite.setLayoutData(jtagCompositeGridData);

		jtagArgumentsField = new TextWithButton(parent, SWT.WRAP | SWT.BORDER);
		createArgumentComponent(jtagComposite, jtagArgumentsField);
		createVerticalSpacer(jtagComposite, 1);
	}

	private Composite createDfuComposite(Composite parent)
	{
		Composite dfuComposite = new Composite(parent, SWT.NONE);
		switchComposites.putIfAbsent(FlashInterface.DFU, new ArrayList<>());
		switchComposites.get(FlashInterface.DFU).add(dfuComposite);
		dfuComposite.setFont(parent.getFont());
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		GridData dfuCompositeGridData = new GridData(GridData.FILL_HORIZONTAL);
		dfuCompositeGridData.exclude = true;
		switchGridDatas.putIfAbsent(FlashInterface.DFU, new ArrayList<>());
		switchGridDatas.get(FlashInterface.DFU).add(dfuCompositeGridData);
		dfuComposite.setLayout(layout);
		dfuComposite.setLayoutData(dfuCompositeGridData);
		return dfuComposite;
	}

	private void createDfuArgumentField(Composite parent)
	{
		Composite dfuComposite = createDfuComposite(parent);

		dfuArgumentsField = new TextWithButton(parent, SWT.WRAP | SWT.BORDER);
		createArgumentComponent(dfuComposite, dfuArgumentsField);
		createVerticalSpacer(dfuComposite, 1);
	}

	private void createProjectGroup(Composite parent, int colSpan)
	{
		Group projectGroup = new Group(parent, SWT.NONE);
		GridLayout projLayout = new GridLayout();
		projLayout.numColumns = 2;
		projLayout.marginHeight = 0;
		projLayout.marginWidth = 0;
		projectGroup.setLayout(projLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		projectGroup.setLayoutData(gd);
		Label fProjLabel = new Label(projectGroup, SWT.NONE);
		fProjLabel.setText(LaunchMessages.CMainTab_ProjectColon);
		gd = new GridData();
		gd.horizontalSpan = 2;
		fProjLabel.setLayoutData(gd);
		fProjText = new Text(projectGroup, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fProjText.setLayoutData(gd);
		Button fProjButton = createPushButton(projectGroup, LaunchMessages.Launch_common_Browse_1, null);
		fProjText.addModifyListener(evt -> updateLaunchConfigurationDialog());
		fProjButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent evt)
			{
				chooseProject();
				updateLaunchConfigurationDialog();
			}
		});
	}

	private void chooseProject()
	{
		ICProject[] projects;
		try
		{
			projects = CoreModel.getDefault().getCModel().getCProjects();
			ILabelProvider labelProvider = new CElementLabelProvider();
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
			dialog.setTitle(LaunchMessages.CMainTab_Project_Selection);
			dialog.setMessage(LaunchMessages.CMainTab_Choose_project_to_constrain_search_for_program);
			dialog.setElements(projects);

			String initialProjectName = fProjText != null ? fProjText.getText().trim() : StringUtil.EMPTY;
			ICProject cProject = initialProjectName.isEmpty() ? null
					: CoreModel.getDefault().getCModel().getCProject(fProjText.getText());
			if (cProject != null)
			{
				dialog.setInitialSelections(new Object[] { cProject });
			}
			if (dialog.open() == Window.OK)
			{
				selectedProject = ((ICProject) dialog.getFirstResult()).getProject();
			}
			if (fProjText != null && selectedProject != null)
			{
				fProjText.setText(selectedProject.getName());
			}
		}
		catch (CModelException e)
		{
			Logger.log(e);
		}
	}

	private void createJtagFlashButton(Composite parent)
	{
		Composite c = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		c.setLayout(layout);
		Label flashOverComboLabel = new Label(c, SWT.NONE);
		flashOverComboLabel.setText(Messages.CMakeMainTab2_FlashComboLbl);
		flashOverComboButton = new Combo(c, SWT.DROP_DOWN | SWT.READ_ONLY);
		flashOverComboButton.setItems(FlashInterface.getNames());
		flashOverComboButton.addSelectionListener(new SelectionAdapter()
		{

			@Override
			public void widgetSelected(SelectionEvent e)
			{

				FlashInterface flashInterface = FlashInterface.valueOf(((Combo) e.widget).getText());
				switch (flashInterface)
				{
				case UART:
					isFlashOverJtag = false;
					break;
				case JTAG:
					isFlashOverJtag = true;
					break;
				case DFU:
					isFlashOverJtag = false;
					break;
				default:
					break;
				}
				switchUI(flashInterface);
				updateLaunchConfigurationDialog();
			}

		});

		if (!isJtagFlashAvailable)
		{
			Label lbl = new Label(c, SWT.NONE);
			lbl.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
			lbl.setText(Messages.CMakeMainTab2_JtagFlashingNotSupportedMsg);
			flashOverComboButton
					.remove(Arrays.asList(flashOverComboButton.getItems()).indexOf(FlashInterface.JTAG.name()));
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
	{
		super.setDefaults(configuration);

		selectedProject = getSelectedProject();
		if (selectedProject != null)
		{
			initializeCProject(selectedProject, configuration);
		}
		try
		{
			configuration.doSave();
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
	}

	private IProject getSelectedProject()
	{
		List<IProject> projectList = new ArrayList<>(1);
		Display.getDefault().syncExec(() -> {
			IProject project = EclipseUtil.getSelectedProjectInExplorer();
			if (project != null)
				projectList.add(project);

		});
		try
		{
			ICProject[] projects = CoreModel.getDefault().getCModel().getCProjects();
			projectList.addAll(Stream.of(projects).map(ICProject::getProject).collect(Collectors.toList()));
		}
		catch (CModelException e)
		{
			Logger.log(e);
		}

		return !projectList.isEmpty() ? projectList.get(0) : null;
	}

	protected void initializeCProject(IProject project, ILaunchConfigurationWorkingCopy config)
	{
		String name = null;
		if (project != null && project.exists())
		{
			name = project.getName();
			config.setMappedResources(new IResource[] { project });

			ICProjectDescription projDes = CCorePlugin.getDefault().getProjectDescription(project);
			if (projDes != null && projDes.getActiveConfiguration() != null)
			{
				String buildConfigID = projDes.getActiveConfiguration().getId();
				config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, buildConfigID);
			}

		}
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, name);
	}

	private void switchUI(FlashInterface flashInterface)
	{

		switchGridDatas.values().forEach(list -> list.forEach(gridData -> gridData.exclude = true));
		switchComposites.values().forEach(list -> list.forEach(composite -> composite.setVisible(false)));

		switchGridDatas.get(flashInterface).forEach(gridData -> gridData.exclude = false);
		switchComposites.get(flashInterface).forEach(composite -> composite.setVisible(true));

		mainComposite.requestLayout();

		// Update the layout and size of the parent composite if it is a ScrolledComposite
		if (mainComposite.getParent() instanceof ScrolledComposite sc)
		{
			mainComposite.setSize(mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			sc.setMinSize(mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			mainComposite.layout(true, true);
		}
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig)
	{
		boolean isConfigValid = super.isValid(launchConfig);
		boolean hasProject = false;
		try
		{
			hasProject = launchConfig.getMappedResources() != null
					&& launchConfig.getMappedResources()[0].getProject().exists();
			// Manually check for "-B" in cmakeArgs here because CMakeBuildTab2's isValid() method is not being called
			String cmakeArgs = launchConfig.getAttribute(ICMakeBuildConfiguration.CMAKE_ARGUMENTS, StringUtil.EMPTY);
			if (cmakeArgs.contains("-B")) //$NON-NLS-1$
			{
				setErrorMessage(Messages.CMakeMainTab2_CmakeArgsDeprecatedBArgMessage);
				return false;
			}
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		String projectName = fProjText.getText().trim();
		if (projectName.length() == 0)
		{
			setErrorMessage(LaunchMessages.CMainTab_Project_not_specified);
			return false;
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (!project.exists())
		{
			setErrorMessage(LaunchMessages.Launch_common_Project_does_not_exist);
			return false;
		}
		if (!project.isOpen())
		{
			setErrorMessage(LaunchMessages.CMainTab_Project_must_be_opened);
			return false;
		}

		return isConfigValid && hasProject && validateEncoding();
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration)
	{
		super.performApply(configuration);
		try
		{
			ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
			wc.setAttribute(IDFLaunchConstants.DFU,
					flashOverComboButton.getText().contentEquals(FlashInterface.DFU.name()));
			if (selectedProject != null)
			{
				initializeCProject(selectedProject, wc);
			}

			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText());
			wc.setAttribute(IDFLaunchConstants.FLASH_OVER_JTAG, isFlashOverJtag);
			// For the case, when user wants to edit arguments line somehow and save changes

			wc.setAttribute(IDFLaunchConstants.ATTR_JTAG_FLASH_ARGUMENTS, jtagArgumentsField.getText());
			wc.setAttribute(IDFLaunchConstants.ATTR_SERIAL_FLASH_ARGUMENTS, uartAgrumentsField.getText());
			wc.setAttribute(IDFLaunchConstants.ATTR_DFU_FLASH_ARGUMENTS, dfuArgumentsField.getText());
			wc.setAttribute(IDFLaunchConstants.OPEN_SERIAL_MONITOR, checkOpenSerialMonitorButton.getSelection());
			if (checkOpenSerialMonitorButton.getSelection())
				wc.setAttribute(IDFLaunchConstants.SERIAL_MONITOR_ENCODING, fEncodingCombo.getText());
			wc.setAttribute(IDFLaunchConstants.FLASH_ENCRYPTION_ENABLED, flashEncryptionCheckbox.getSelection());

			wc.doSave();
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration)
	{
		super.initializeFrom(configuration);
		updateFlashEncryptionGroup(configuration);
		updateStartSerialMonitorGroup(configuration);
		updateProjetFromConfig(configuration);
		updateFlashOverStatus(configuration);
		updateArgumentsWithDefaultFlashCommand(configuration);
		switchUI(FlashInterface.values()[flashOverComboButton.getSelectionIndex()]);
	}

	private void updateFlashEncryptionGroup(ILaunchConfiguration configuration)
	{
		boolean isFlashEncryptionEnabled = false;
		try
		{
			isFlashEncryptionEnabled = configuration.getAttribute(IDFLaunchConstants.FLASH_ENCRYPTION_ENABLED, false);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		flashEncryptionCheckbox.setSelection(isFlashEncryptionEnabled);
	}

	private void updateProjetFromConfig(ILaunchConfiguration configuration)
	{
		String projectName = StringUtil.EMPTY;
		try
		{
			projectName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					StringUtil.EMPTY);
		}
		catch (CoreException ce)
		{
			Logger.log(ce);
		}
		if (!fProjText.getText().equals(projectName))
			fProjText.setText(projectName);
	}

	private void updateFlashOverStatus(ILaunchConfiguration configuration)
	{
		boolean isDfu = false;
		try
		{
			isDfu = configuration.getAttribute(IDFLaunchConstants.DFU, false);
			if (isDfu)
			{
				int dfuIndex = Arrays.asList(flashOverComboButton.getItems()).indexOf(FlashInterface.DFU.name());
				flashOverComboButton.select(dfuIndex);
			}
			else
			{
				int uartIndex = Arrays.asList(flashOverComboButton.getItems()).indexOf(FlashInterface.UART.name());
				flashOverComboButton.select(uartIndex);
			}
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		if (!isJtagFlashAvailable)
		{
			return;
		}
		try
		{
			isFlashOverJtag = configuration.getAttribute(IDFLaunchConstants.FLASH_OVER_JTAG, isFlashOverJtag);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		if (isFlashOverJtag && !isDfu)
		{
			flashOverComboButton
					.select(Arrays.asList(flashOverComboButton.getItems()).indexOf(FlashInterface.JTAG.name()));
		}
	}

	private void updateArgumentsWithDefaultFlashCommand(ILaunchConfiguration configuration)
	{

		try
		{
			String uartFlashCommand = configuration.getAttribute(IDFLaunchConstants.ATTR_SERIAL_FLASH_ARGUMENTS,
					StringUtil.EMPTY);
			uartAgrumentsField.setText(
					uartFlashCommand.isBlank() ? ESPFlashUtil.getParseableEspFlashCommand(ESPFlashUtil.SERIAL_PORT)
							: uartFlashCommand);

			jtagArgumentsField.setText(configuration.getAttribute(IDFLaunchConstants.ATTR_JTAG_FLASH_ARGUMENTS,
					DEFAULT_JTAG_CONFIG_OPTIONS));

			dfuArgumentsField.setText(configuration.getAttribute(IDFLaunchConstants.ATTR_DFU_FLASH_ARGUMENTS,
					DfuCommandsUtil.getDfuFlashCommand()));

		}
		catch (CoreException e)
		{
			Logger.log(e);
		}

	}

	@Override
	protected void updateLocation(ILaunchConfiguration configuration)
	{
		super.updateLocation(configuration);
		try
		{
			String location = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_LOCATION, ""); //$NON-NLS-1$
			if (StringUtil.isEmpty(location))
			{
				location = VariablesPlugin.getDefault().getStringVariableManager()
						.generateVariableExpression(IDFDynamicVariables.IDF_PYTHON_ENV_PATH.name(), null);
			}
			locationField.setText(location);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}

	@Override
	protected void updateWorkingDirectory(ILaunchConfiguration configuration)
	{
		super.updateWorkingDirectory(configuration);
		File workingDir;
		if (workDirectoryField.getText().isEmpty())
		{
			try
			{
				if (configuration.getMappedResources() == null)
				{
					return;
				}
				URI locationUri = configuration.getMappedResources()[0].getProject().getLocationURI();
				if (locationUri != null)
				{
					workingDir = new File(configuration.getMappedResources()[0].getProject().getLocationURI());
					workDirectoryField.setText(newVariableExpression("workspace_loc", workingDir.getName())); //$NON-NLS-1$
				}

			}
			catch (CoreException e)
			{
				Logger.log(e);
			}
		}
	}

	private void createOpenSerialMonitorGroup(Composite mainComposite)
	{
		Group group = SWTFactory.createGroup(mainComposite, Messages.CMakeMainTab2_SerialMonitorGroup, 2, 1,
				GridData.FILL_HORIZONTAL);
		checkOpenSerialMonitorButton = new Button(group, SWT.CHECK);
		checkOpenSerialMonitorButton.setText(Messages.CMakeMainTab2_SerialMonitorBtn);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd.horizontalSpan = 2;
		checkOpenSerialMonitorButton.setLayoutData(gd);
		Label encodingLbl = new Label(group, SWT.NONE);
		encodingLbl.setText(Messages.CMakeMainTab2_SerialMonitorEncodingLbl);
		fEncodingCombo = new Combo(group, SWT.READ_ONLY);
		fEncodingCombo.setLayoutData(new GridData(GridData.BEGINNING));
		List<String> allEncodings = IDEEncoding.getIDEEncodings();
		String[] encodingArray = allEncodings.toArray(new String[0]);
		fEncodingCombo.setItems(encodingArray);
		if (encodingArray.length > 0)
			fEncodingCombo.select(0);

		fEncodingCombo.addModifyListener(e -> updateLaunchConfigurationDialog());
		checkOpenSerialMonitorButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				fEncodingCombo.setVisible(checkOpenSerialMonitorButton.getSelection());
				GridData data = (GridData) fEncodingCombo.getLayoutData();
				data.exclude = !fEncodingCombo.getVisible();
				encodingLbl.setVisible(checkOpenSerialMonitorButton.getSelection());
				data = (GridData) encodingLbl.getLayoutData();
				data.exclude = !encodingLbl.getVisible();
				mainComposite.layout(true, true);
				updateLaunchConfigurationDialog();
			}
		});
	}

	private void updateStartSerialMonitorGroup(ILaunchConfiguration configuration)
	{
		try
		{
			checkOpenSerialMonitorButton
					.setSelection(configuration.getAttribute(IDFLaunchConstants.OPEN_SERIAL_MONITOR, true));
			checkOpenSerialMonitorButton.notifyListeners(SWT.Selection, null);
			int encodingIndex = fEncodingCombo
					.indexOf(configuration.getAttribute(IDFLaunchConstants.SERIAL_MONITOR_ENCODING, StringUtil.EMPTY));
			encodingIndex = encodingIndex == -1 ? 0 : encodingIndex;
			fEncodingCombo.select(encodingIndex);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}

	}

	private boolean validateEncoding()
	{
		if (checkOpenSerialMonitorButton.getSelection() && fEncodingCombo.getSelectionIndex() == -1
				&& !isValidEncoding(fEncodingCombo.getText().trim()))
		{
			setErrorMessage(LaunchConfigurationsMessages.CommonTab_15);
			return false;
		}
		return true;
	}

	private boolean isValidEncoding(String enc)
	{
		try
		{
			return Charset.isSupported(enc);
		}
		catch (IllegalCharsetNameException e)
		{
			// This is a valid exception
			return false;
		}
	}

	private void initializeFromDefaults()
	{
		uartAgrumentsField.setText(ESPFlashUtil.getParseableEspFlashCommand(ESPFlashUtil.SERIAL_PORT));
		jtagArgumentsField.setText(DEFAULT_JTAG_CONFIG_OPTIONS);
		dfuArgumentsField.setText(DfuCommandsUtil.getDfuFlashCommand());
	}
}
