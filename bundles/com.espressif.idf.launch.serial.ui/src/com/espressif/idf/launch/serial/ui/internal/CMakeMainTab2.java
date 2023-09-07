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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.json.simple.JSONArray;

import com.espressif.idf.core.DefaultBoardProvider;
import com.espressif.idf.core.IDFDynamicVariables;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.DfuCommandsUtil;
import com.espressif.idf.core.util.EspConfigParser;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.core.variable.OpenocdDynamicVariable;
import com.espressif.idf.launch.serial.SerialFlashLaunchTargetProvider;
import com.espressif.idf.launch.serial.util.ESPFlashUtil;
import com.espressif.idf.ui.EclipseUtil;
import com.espressif.idf.ui.LaunchBarListener;

@SuppressWarnings("restriction")
public class CMakeMainTab2 extends GenericMainTab {
	private static final String EMPTY_CONFIG_OPTIONS = "%s" + File.separator + "%s -s %s"; //$NON-NLS-1$ //$NON-NLS-2$
	private Combo flashOverComboButton;
	private Combo fFlashVoltage;
	private Combo fTarget;
	private Map<String, JSONArray> boardConfigsMap;
	private Combo fTargetName;
	private boolean isFlashOverJtag;
	private boolean isJtagFlashAvailable;
	private Text fProjText;
	private IProject selectedProject;
	private Composite mainComposite;
	private EnumMap<FlashInterface, List<Composite>> switchComposites = new EnumMap<>(FlashInterface.class);
	private EnumMap<FlashInterface, List<GridData>> switchGridDatas = new EnumMap<>(FlashInterface.class);
	private Text uartAgrumentsField;
	private Text jtagArgumentsField;
	private Text dfuArgumentsField;
	private Label dfuErrorLbl;
	private Combo comboTargets;
	private ILaunchBarManager launchBarManager = Activator.getService(ILaunchBarManager.class);

	public enum FlashInterface {
		UART, JTAG, DFU;

		public static String[] getNames() {
			return Arrays.stream(FlashInterface.values()).map(Enum::name).toArray(String[]::new);
		}

	}

	@Override
	public void createControl(Composite parent) {
		LaunchBarListener.setIgnoreJtagTargetChange(true);
		parent.addDisposeListener(e -> LaunchBarListener.setIgnoreJtagTargetChange(false));

		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setFont(parent.getFont());
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);

		isJtagFlashAvailable = ESPFlashUtil.checkIfJtagIsAvailable();
		setControl(mainComposite);
		createJtagFlashButton(mainComposite);
		createDfuTargetComposite(mainComposite);
		createProjectGroup(mainComposite, 0);
		createUartComposite(mainComposite);
		createJtagflashComposite(mainComposite);
		createDfuArgumentField(mainComposite);

		argumentField = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		argumentField.setVisible(false);
	}

	@Override
	protected void updateArgument(ILaunchConfiguration configuration) {
		//We don't need the default argument field so there is nothing to update
	}

	/**
	 * Creates the controls needed to edit the argument and prompt for argument
	 * attributes of an external tool
	 *
	 * @param parent
	 *            the composite to create the controls in
	 */
	protected void createArgumentComponent(Composite parent, Text argumentField) {
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
			if (event.detail == SWT.TRAVERSE_RETURN && (event.stateMask & SWT.MODIFIER_MASK) != 0) {
				event.doit = true;
			}
		});

		gridData = new GridData(GridData.FILL_BOTH);
		gridData.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		gridData.heightHint = 100;
		argumentField.setLayoutData(gridData);
		argumentField.addModifyListener(fListener);
		addControlAccessibleListener(argumentField, group.getText());

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

	private void handleVariablesButtonSelected(Text textField) {
		String variable = getVariable();
		if (variable != null)
			textField.insert(variable);
	}

	private String getVariable() {
		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
		dialog.open();
		return dialog.getVariableExpression();
	}

	protected void createUartComposite(Composite parent) {

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

		uartAgrumentsField = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);

		createArgumentComponent(defaultComposite, uartAgrumentsField);
		createVerticalSpacer(defaultComposite, 1);
	}

	protected void createJtagflashComposite(Composite parent) {

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

		String selectedTarget = getLaunchTarget();
		EspConfigParser parser = new EspConfigParser();
		createOpenOcdSetupComponent(jtagComposite, selectedTarget, parser);

		jtagArgumentsField = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		createArgumentComponent(jtagComposite, jtagArgumentsField);
		createVerticalSpacer(jtagComposite, 1);
	}

	protected void createDfuTargetComposite(Composite parent) {
		Composite dfuComposite = createDfuComposite(parent);

		Composite targetComposite = new Composite(dfuComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		GridData targetGridData = new GridData(GridData.FILL_HORIZONTAL);
		targetComposite.setLayout(layout);
		targetComposite.setData(targetGridData);

		Label comboTargetLbl = new Label(targetComposite, SWT.NONE);
		comboTargetLbl.setText(Messages.CMakeMainTab2_TargetsComboLbl);
		comboTargets = new Combo(targetComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboTargets.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		ILaunchTargetManager launchTargetManager = Activator.getService(ILaunchTargetManager.class);
		ILaunchTarget[] targets = launchTargetManager
				.getLaunchTargetsOfType("com.espressif.idf.launch.serial.core.serialFlashTarget"); //$NON-NLS-1$
		String[] targetsWithDfuSupport = Stream.of(targets).filter(DfuCommandsUtil::isTargetSupportDfu)
				.map(ILaunchTarget::getId).toArray(String[]::new);
		comboTargets.setItems(targetsWithDfuSupport);
		comboTargets.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent evt) {
				ILaunchTarget selectedTarget = Stream.of(launchTargetManager.getLaunchTargets())
						.filter(target -> target.getId().contentEquals(((Combo) evt.widget).getText())).findFirst()
						.orElseGet(() -> null);
				if (selectedTarget != null && dfuErrorLbl != null) {
					dfuErrorLbl.setText(StringUtil.EMPTY);
				}
				if (selectedTarget != null) {
					try {
						launchBarManager.setActiveLaunchTarget(selectedTarget);
					} catch (CoreException e) {
						Logger.log(e);
					}
				}
				updateLaunchConfigurationDialog();

			}
		});

		Optional<String> suitableTarget = Stream.of(targetsWithDfuSupport).filter(t -> {
			try {
				return t.contentEquals(launchBarManager.getActiveLaunchTarget().getId());
			} catch (CoreException e) {
				Logger.log(e);
			}
			return false;
		}).findFirst();

		suitableTarget.ifPresentOrElse(t -> comboTargets.select(Arrays.asList(comboTargets.getItems()).indexOf(t)),
				() -> {
					dfuErrorLbl = new Label(targetComposite, SWT.NONE);
					dfuErrorLbl.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
					dfuErrorLbl.setText(Messages.CMakeMainTab2_WarningDfuMsg);
				});

		comboTargets.notifyListeners(SWT.Selection, null);
	}

	private Composite createDfuComposite(Composite parent) {
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

	private void createDfuArgumentField(Composite parent) {
		Composite dfuComposite = createDfuComposite(parent);

		dfuArgumentsField = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		createArgumentComponent(dfuComposite, dfuArgumentsField);
		createVerticalSpacer(dfuComposite, 1);
	}

	private void createProjectGroup(Composite parent, int colSpan) {
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
		fProjButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				chooseProject();
				updateLaunchConfigurationDialog();
			}
		});
	}

	private void chooseProject() {
		ICProject[] projects;
		try {
			projects = CoreModel.getDefault().getCModel().getCProjects();
			ILabelProvider labelProvider = new CElementLabelProvider();
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
			dialog.setTitle(LaunchMessages.CMainTab_Project_Selection);
			dialog.setMessage(LaunchMessages.CMainTab_Choose_project_to_constrain_search_for_program);
			dialog.setElements(projects);

			String initialProjectName = fProjText != null ? fProjText.getText().trim() : StringUtil.EMPTY;
			ICProject cProject = initialProjectName.isEmpty() ? null
					: CoreModel.getDefault().getCModel().getCProject(fProjText.getText());
			if (cProject != null) {
				dialog.setInitialSelections(new Object[] { cProject });
			}
			if (dialog.open() == Window.OK) {
				selectedProject = ((ICProject) dialog.getFirstResult()).getProject();
			}
			if (fProjText != null && selectedProject != null) {
				fProjText.setText(selectedProject.getName());
			}
		} catch (CModelException e) {
			Logger.log(e);
		}
	}

	private void createJtagFlashButton(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		c.setLayout(layout);
		Label flashOverComboLabel = new Label(c, SWT.NONE);
		flashOverComboLabel.setText(Messages.CMakeMainTab2_FlashComboLbl);
		flashOverComboButton = new Combo(c, SWT.DROP_DOWN | SWT.READ_ONLY);
		flashOverComboButton.setItems(FlashInterface.getNames());
		flashOverComboButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FlashInterface flashInterface = FlashInterface.valueOf(((Combo) e.widget).getText());
				switch (flashInterface) {
				case UART:
					isFlashOverJtag = false;
					break;
				case JTAG:
					isFlashOverJtag = true;
					fTarget.notifyListeners(SWT.Selection, null);
					break;
				case DFU:
					isFlashOverJtag = false;
					comboTargets.notifyListeners(SWT.Selection, null);
					break;
				default:
					break;
				}
				switchUI(flashInterface);
				updateLaunchConfigurationDialog();
			}

		});

		if (!isJtagFlashAvailable) {
			Label lbl = new Label(c, SWT.NONE);
			lbl.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
			lbl.setText(Messages.CMakeMainTab2_JtagFlashingNotSupportedMsg);
			flashOverComboButton
					.remove(Arrays.asList(flashOverComboButton.getItems()).indexOf(FlashInterface.JTAG.name()));
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		super.setDefaults(configuration);

		selectedProject = getSelectedProject();
		if (selectedProject != null) {
			initializeCProject(selectedProject, configuration);
		}
		try {
			configuration.doSave();
		} catch (CoreException e) {
			Logger.log(e);
		}
	}

	private IProject getSelectedProject() {
		List<IProject> projectList = new ArrayList<>(1);
		Display.getDefault().syncExec(() -> {
			IProject project = EclipseUtil.getSelectedProjectInExplorer();
			if (project != null)
				projectList.add(project);

		});
		try {
			ICProject[] projects = CoreModel.getDefault().getCModel().getCProjects();
			projectList.addAll(Stream.of(projects).map(ICProject::getProject).collect(Collectors.toList()));
		} catch (CModelException e) {
			Logger.log(e);
		}

		return projectList.get(0);
	}

	protected void initializeCProject(IProject project, ILaunchConfigurationWorkingCopy config) {
		String name = null;
		if (project != null && project.exists()) {
			name = project.getName();
			config.setMappedResources(new IResource[] { project });

			ICProjectDescription projDes = CCorePlugin.getDefault().getProjectDescription(project);
			if (projDes != null) {
				String buildConfigID = projDes.getActiveConfiguration().getId();
				config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, buildConfigID);
			}

		}
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, name);
	}

	private void switchUI(FlashInterface flashInterface) {

		switchGridDatas.values().forEach(list -> list.forEach(gridData -> gridData.exclude = true));
		switchComposites.values().forEach(list -> list.forEach(composite -> composite.setVisible(false)));

		switchGridDatas.get(flashInterface).forEach(gridData -> gridData.exclude = false);
		switchComposites.get(flashInterface).forEach(composite -> composite.setVisible(true));

		mainComposite.requestLayout();
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		boolean isConfigValid = super.isValid(launchConfig);
		boolean hasProject = false;
		try {
			hasProject = launchConfig.getMappedResources() != null
					&& launchConfig.getMappedResources()[0].getProject().exists();
		} catch (CoreException e) {
			Logger.log(e);
		}
		String projectName = fProjText.getText().trim();
		if (projectName.length() == 0) {
			setErrorMessage(LaunchMessages.CMainTab_Project_not_specified);
			return false;
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (!project.exists()) {
			setErrorMessage(LaunchMessages.Launch_common_Project_does_not_exist);
			return false;
		}
		if (!project.isOpen()) {
			setErrorMessage(LaunchMessages.CMainTab_Project_must_be_opened);
			return false;
		}
		if (flashOverComboButton.getText().contentEquals(FlashInterface.DFU.name())
				&& comboTargets.getSelectionIndex() == -1) {
			try {
				setErrorMessage(MessageFormat.format(Messages.CMakeMainTab2_NoDfuTargetSelectedError,
						launchBarManager.getActiveLaunchTarget().getId()));
			} catch (CoreException e) {
				Logger.log(e);
			}
			return false;
		}
		return isConfigValid && hasProject;
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);
		try {
			ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
			wc.setAttribute(IDFLaunchConstants.DFU,
					flashOverComboButton.getText().contentEquals(FlashInterface.DFU.name()));
			if (selectedProject != null) {
				initializeCProject(selectedProject, wc);
			}
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText());
			wc.setAttribute(IDFLaunchConstants.JTAG_FLASH_VOLTAGE, fFlashVoltage.getText());
			wc.setAttribute(IDFLaunchConstants.TARGET_FOR_JTAG, fTarget.getText());
			wc.setAttribute(IDFLaunchConstants.JTAG_BOARD, fTargetName.getText());
			wc.setAttribute(IDFLaunchConstants.FLASH_OVER_JTAG, isFlashOverJtag);
			//For the case, when user wants to edit arguments line somehow and save changes

			wc.setAttribute(IDFLaunchConstants.ATTR_JTAG_FLASH_ARGUMENTS, jtagArgumentsField.getText());
			wc.setAttribute(IDFLaunchConstants.ATTR_SERIAL_FLASH_ARGUMENTS, uartAgrumentsField.getText());
			wc.setAttribute(IDFLaunchConstants.ATTR_DFU_FLASH_ARGUMENTS, dfuArgumentsField.getText());

			wc.doSave();
		} catch (CoreException e) {
			Logger.log(e);
		}
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		updateProjetFromConfig(configuration);
		updateFlashOverStatus(configuration);
		updateArgumentsWithDefaultFlashCommand(configuration);
		switchUI(FlashInterface.values()[flashOverComboButton.getSelectionIndex()]);
	}

	private void updateProjetFromConfig(ILaunchConfiguration configuration) {
		String projectName = StringUtil.EMPTY;
		try {
			projectName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					StringUtil.EMPTY);
		} catch (CoreException ce) {
			Logger.log(ce);
		}
		if (!fProjText.getText().equals(projectName))
			fProjText.setText(projectName);
	}

	private void updateFlashOverStatus(ILaunchConfiguration configuration) {
		boolean isDfu = false;
		try {
			isDfu = configuration.getAttribute(IDFLaunchConstants.DFU, false);
			if (isDfu) {
				int dfuIndex = Arrays.asList(flashOverComboButton.getItems()).indexOf(FlashInterface.DFU.name());
				flashOverComboButton.select(dfuIndex);
			} else {
				int uartIndex = Arrays.asList(flashOverComboButton.getItems()).indexOf(FlashInterface.UART.name());
				flashOverComboButton.select(uartIndex);
			}
		} catch (CoreException e) {
			Logger.log(e);
		}
		if (!isJtagFlashAvailable) {
			return;
		}
		try {
			isFlashOverJtag = configuration.getAttribute(IDFLaunchConstants.FLASH_OVER_JTAG, isFlashOverJtag);
		} catch (CoreException e) {
			Logger.log(e);
		}
		if (isFlashOverJtag && !isDfu) {
			try {
				initializeJtagComboFields(configuration);
			} catch (CoreException e) {
				Logger.log(e);
			}

			flashOverComboButton
					.select(Arrays.asList(flashOverComboButton.getItems()).indexOf(FlashInterface.JTAG.name()));
		}
	}

	private void updateArgumentsWithDefaultFlashCommand(ILaunchConfiguration configuration) {

		try {
			String uartFlashCommand = configuration.getAttribute(IDFLaunchConstants.ATTR_SERIAL_FLASH_ARGUMENTS,
					StringUtil.EMPTY);
			uartAgrumentsField.setText(
					uartFlashCommand.isBlank() ? ESPFlashUtil.getParseableEspFlashCommand(ESPFlashUtil.SERIAL_PORT)
							: uartFlashCommand);

			jtagArgumentsField.setText(
					configuration.getAttribute(IDFLaunchConstants.ATTR_JTAG_FLASH_ARGUMENTS, StringUtil.EMPTY));

			dfuArgumentsField.setText(configuration.getAttribute(IDFLaunchConstants.ATTR_DFU_FLASH_ARGUMENTS,
					DfuCommandsUtil.getDfuFlashCommand()));

		} catch (CoreException e) {
			Logger.log(e);
		}

	}

	private void initializeJtagComboFields(ILaunchConfiguration configuration) throws CoreException {
		fFlashVoltage
				.setText(configuration.getAttribute(IDFLaunchConstants.JTAG_FLASH_VOLTAGE, fFlashVoltage.getText()));
		fTarget.setText(configuration.getAttribute(IDFLaunchConstants.TARGET_FOR_JTAG, fTarget.getText()));
		fTarget.notifyListeners(SWT.Selection, null);
		fTargetName.setText(configuration.getAttribute(IDFLaunchConstants.JTAG_BOARD, fTargetName.getText()));
		fTargetName.notifyListeners(SWT.Selection, null);
	}

	private static void showNoTargetMessage(String selectedTarget) {
		Display.getDefault().asyncExec(() -> {
			boolean isYes = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
					Messages.IDFLaunchTargetNotFoundIDFLaunchTargetNotFoundTitle,
					Messages.IDFLaunchTargetNotFoundMsg1 + selectedTarget + Messages.IDFLaunchTargetNotFoundMsg2
							+ Messages.IDFLaunchTargetNotFoundMsg3);
			if (isYes) {
				NewSerialFlashTargetWizard wizard = new NewSerialFlashTargetWizard();
				WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						wizard);
				dialog.open();
			}
		});
	}

	private void createOpenOcdSetupComponent(Composite parent, String selectedTarget, EspConfigParser parser) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 6;
		group.setText(Messages.CMakeMainTab2_OpeonOcdSetupGroupTitle);
		group.setLayout(gridLayout);
		{
			Label label = new Label(group, SWT.NONE);
			label.setText(Messages.flashVoltageLabel);
			label.setToolTipText(Messages.flashVoltageToolTip);
			fFlashVoltage = new Combo(group, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
			fFlashVoltage.setItems(parser.getEspFlashVoltages().toArray(new String[0]));
			fFlashVoltage.setText("default"); //$NON-NLS-1$
			fFlashVoltage.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					fTargetName.notifyListeners(SWT.Selection, null);
				}
			});
		}
		{
			Label label = new Label(group, SWT.NONE);
			label.setText(Messages.configTargetLabel);
			label.setToolTipText(Messages.configTargetToolTip);
			fTarget = new Combo(group, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
			fTarget.setItems(parser.getTargets().toArray(new String[0]));
			fTarget.setText(selectedTarget);
			fTarget.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					String updatedSelectedTarget = getLaunchTarget();
					int selectedIndex = fTarget.getSelectionIndex();
					String selectedItem = StringUtil.EMPTY;
					if (selectedIndex != -1) {
						selectedItem = fTarget.getItem(fTarget.getSelectionIndex());
					}

					if (!selectedItem.contentEquals(updatedSelectedTarget) && isFlashOverJtag) {
						try {
							ILaunchConfigurationWorkingCopy wc = launchBarManager.getActiveLaunchConfiguration()
									.getWorkingCopy();
							wc.setAttribute(IDFLaunchConstants.TARGET_FOR_JTAG, selectedItem);
							wc.doSave();
						} catch (CoreException e2) {
							Logger.log(e2);
						}
						updateLaunchBar(selectedItem);
					}
					boardConfigsMap = parser.getBoardsConfigs(selectedItem);
					fTargetName.setItems(parser.getBoardsConfigs(selectedItem).keySet().toArray(new String[0]));
					fTargetName.select(
							new DefaultBoardProvider().getIndexOfDefaultBoard(selectedItem, fTargetName.getItems()));
					updateArgumentsField();
				}

				private void updateLaunchBar(String selectedItem) {
					ILaunchTarget target = findSuitableTargetForSelectedItem(selectedItem);
					try {
						if (target != null) {
							launchBarManager.setActiveLaunchTarget(target);
						} else {
							showNoTargetMessage(selectedItem);
						}

					} catch (CoreException e1) {
						Logger.log(e1);
					}
				}

				private ILaunchTarget findSuitableTargetForSelectedItem(String selectedItem) {
					ILaunchTargetManager launchTargetManager = Activator.getService(ILaunchTargetManager.class);
					ILaunchTarget[] targets = launchTargetManager
							.getLaunchTargetsOfType("com.espressif.idf.launch.serial.core.serialFlashTarget"); //$NON-NLS-1$
					ILaunchTarget suitableTarget = null;

					for (ILaunchTarget target : targets) {
						String idfTarget = target.getAttribute("com.espressif.idf.launch.serial.core.idfTarget", null); //$NON-NLS-1$
						String targetSerialPort = target.getAttribute(SerialFlashLaunchTargetProvider.ATTR_SERIAL_PORT,
								StringUtil.EMPTY);
						if (idfTarget.contentEquals(selectedItem)) {
							if (targetSerialPort.contentEquals(getSerialPort())) {
								return target;
							}
							suitableTarget = target;
						}
					}
					return suitableTarget;
				}
			});
		}
		{
			Label label = new Label(group, SWT.NONE);
			label.setText(Messages.configBoardLabel);
			label.setToolTipText(Messages.configBoardTooTip);
			fTargetName = new Combo(group, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
			fTargetName.setItems(parser.getBoardsConfigs(selectedTarget).keySet().toArray(new String[0]));
			boardConfigsMap = parser.getBoardsConfigs(selectedTarget);
			fTargetName
					.select(new DefaultBoardProvider().getIndexOfDefaultBoard(selectedTarget, fTargetName.getItems()));
			fTargetName.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateArgumentsField();
				}
			});
		}
		GridData openOcdGroupData = new GridData(SWT.FILL, SWT.NONE, true, true);
		group.setLayoutData(openOcdGroupData);
	}

	@SuppressWarnings("unchecked")
	private void updateArgumentsField() {
		String selectedVoltage = fFlashVoltage.getText();
		String selectedItem = fTargetName.getText();
		String configOptiopns = String.format(EMPTY_CONFIG_OPTIONS,
				newVariableExpression(OpenocdDynamicVariable.OPENOCD_PATH),
				newVariableExpression(OpenocdDynamicVariable.OPENOCD_EXE),
				newVariableExpression(OpenocdDynamicVariable.OPENOCD_SCRIPTS));
		if (!selectedVoltage.equals("default"))//$NON-NLS-1$
		{
			configOptiopns = configOptiopns + " -c 'set ESP32_FLASH_VOLTAGE " + selectedVoltage + "'"; //$NON-NLS-1$//$NON-NLS-2$
		}
		if (!selectedItem.isEmpty()) {
			for (String config : (String[]) boardConfigsMap.get(selectedItem).toArray(new String[0])) {
				configOptiopns = configOptiopns + " -f " + config; //$NON-NLS-1$
			}
		}
		jtagArgumentsField.setText(configOptiopns);
	}

	private String newVariableExpression(OpenocdDynamicVariable dynamicVariable) {
		return newVariableExpression(dynamicVariable.getValue(), null);
	}

	private String getLaunchTarget() {
		String selectedTarget = StringUtil.EMPTY;
		try {
			selectedTarget = launchBarManager.getActiveLaunchTarget().getAttribute(IDFLaunchConstants.ATTR_IDF_TARGET,
					StringUtil.EMPTY);
		} catch (CoreException e) {
			Logger.log(e);
		}
		return selectedTarget;
	}

	private String getSerialPort() {

		String serialPort = StringUtil.EMPTY;
		try {
			serialPort = launchBarManager.getActiveLaunchTarget()
					.getAttribute(SerialFlashLaunchTargetProvider.ATTR_SERIAL_PORT, StringUtil.EMPTY);

		} catch (CoreException e) {
			Logger.log(e);
		}
		return serialPort;
	}

	@Override
	protected void updateLocation(ILaunchConfiguration configuration) {
		super.updateLocation(configuration);
		try {
			String location = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_LOCATION, ""); //$NON-NLS-1$
			if (StringUtil.isEmpty(location)) {
				location = VariablesPlugin.getDefault().getStringVariableManager()
						.generateVariableExpression(IDFDynamicVariables.IDF_PYTHON_ENV_PATH.name(), null);
			}
			locationField.setText(location);
		} catch (Exception e) {
			Logger.log(e);
		}
	}

	@Override
	protected void updateWorkingDirectory(ILaunchConfiguration configuration) {
		super.updateWorkingDirectory(configuration);
		File workingDir;
		if (workDirectoryField.getText().isEmpty()) {
			try {
				if (configuration.getMappedResources() == null) {
					return;
				}
				workingDir = new File(configuration.getMappedResources()[0].getProject().getLocationURI());
				workDirectoryField.setText(newVariableExpression("workspace_loc", workingDir.getName())); //$NON-NLS-1$
			} catch (CoreException e) {
				Logger.log(e);
			}
		}
	}
}
