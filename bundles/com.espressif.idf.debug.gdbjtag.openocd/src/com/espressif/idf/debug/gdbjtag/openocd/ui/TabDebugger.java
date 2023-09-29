/*******************************************************************************
 * Copyright (c) 2007, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andy Jin - Hardware debugging UI improvements, bug 229946
 *     Anna Dushistova (MontaVista) - bug 241279 
 *              - Hardware Debugging: Host name or ip address not saving in 
 *                the debug configuration
 *     Andy Jin (QNX) - Added DSF debugging, bug 248593
 *     Bruce Griffith, Sage Electronic Engineering, LLC - bug 305943
 *              - API generalization to become transport-independent (e.g. to
 *                allow connections via serial ports and pipes).
 *     Liviu Ionescu - ARM & RISC-V versions
 *     Jonah Graham - fix for Neon
 ******************************************************************************/

package com.espressif.idf.debug.gdbjtag.openocd.ui;

import java.io.File;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConstants;
import org.eclipse.cdt.debug.gdbjtag.ui.GDBJtagImages;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.embedcdt.core.EclipseUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.json.simple.JSONArray;

import com.espressif.idf.core.DefaultBoardProvider;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.EspConfigParser;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.debug.gdbjtag.openocd.Activator;
import com.espressif.idf.debug.gdbjtag.openocd.Configuration;
import com.espressif.idf.debug.gdbjtag.openocd.ConfigurationAttributes;
import com.espressif.idf.debug.gdbjtag.openocd.preferences.DefaultPreferences;
import com.espressif.idf.debug.gdbjtag.openocd.preferences.PersistentPreferences;
import com.espressif.idf.debug.gdbjtag.openocd.ui.preferences.GlobalMcuPage;
import com.espressif.idf.debug.gdbjtag.openocd.ui.preferences.WorkspaceMcuPage;
import com.espressif.idf.debug.gdbjtag.openocd.ui.properties.ProjectMcuPage;
import com.espressif.idf.launch.serial.SerialFlashLaunchTargetProvider;
import com.espressif.idf.launch.serial.ui.internal.NewSerialFlashTargetWizard;
import com.espressif.idf.ui.LaunchBarListener;

/**
 * @since 7.0
 */
public class TabDebugger extends AbstractLaunchConfigurationTab
{

	// ------------------------------------------------------------------------

	private static final String TAB_NAME = "Debugger"; //$NON-NLS-1$
	private static final String TAB_ID = Activator.PLUGIN_ID + ".ui.debuggertab"; //$NON-NLS-1$
	private static final String EMPTY_CONFIG_OPTIONS = "-s ${openocd_path}/share/openocd/scripts"; //$NON-NLS-1$
	// ------------------------------------------------------------------------

	private ILaunchConfiguration fConfiguration;

	private Button fDoStartGdbServer;
	private Text fGdbServerGdbPort;
	private Text fGdbServerTelnetPort;
	private Text fGdbServerTclPort;

	private Text fGdbServerExecutable;
	private Button fGdbServerBrowseButton;
	private Button fGdbServerVariablesButton;
	private Text fGdbServerPathLabel;

	private Text fGdbServerOtherOptions;

	private Button fDoGdbServerAllocateConsole;
	private Button fDoGdbServerAllocateTelnetConsole;

	private Combo fFlashVoltage;
	private Combo fTarget;
	private Combo fTargetName;
	private Map<String, JSONArray> boardConfigsMap;

	private Button fDoStartGdbClient;
	private Text fGdbClientExecutable;
	private Button fGdbClientBrowseButton;
	private Button fGdbClientVariablesButton;
	private Text fGdbClientOtherOptions;
	private Text fGdbClientOtherCommands;

	private Link fLink;

	private Text fTargetIpAddress;
	private Text fTargetPortNumber;

	protected Button fUpdateThreadlistOnSuspend;

	private DefaultPreferences fDefaultPreferences;
	private PersistentPreferences fPersistentPreferences;

	private ILaunchBarManager launchBarManager;
	private ILaunchConfigurationWorkingCopy lastAppliedConfiguration;

	// ------------------------------------------------------------------------

	protected TabDebugger(TabStartup tabStartup)
	{
		super();

		fDefaultPreferences = Activator.getInstance().getDefaultPreferences();
		fPersistentPreferences = Activator.getInstance().getPersistentPreferences();
		launchBarManager = Activator.getService(ILaunchBarManager.class);
	}

	// ------------------------------------------------------------------------

	@Override
	public String getName()
	{
		return TAB_NAME;
	}

	@Override
	public Image getImage()
	{
		return GDBJtagImages.getDebuggerTabImage();
	}

	@Override
	public void createControl(Composite parent)
	{
		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.TabDebugger.createControl() "); //$NON-NLS-1$
		}

		LaunchBarListener.setIgnoreJtagTargetChange(true);
		parent.addDisposeListener(event -> {
			String targetNameFromUI = fTargetName.getText();
			// TODO: find a better way to roll back target change in the launch bar when Cancel was pressed.
			// We have to do like this because we don't have access to the cancel button
			Job revertTargetJob = new Job(Messages.TabDebugger_SettingTargetJob)
			{
				protected IStatus run(IProgressMonitor monitor)
				{
					try
					{
						String targetName = lastAppliedConfiguration.getOriginal()
								.getAttribute(IDFLaunchConstants.TARGET_FOR_JTAG, targetNameFromUI);
						ILaunchTargetManager launchTargetManager = Activator.getService(ILaunchTargetManager.class);
						ILaunchTarget selectedTarget = Stream.of(launchTargetManager.getLaunchTargets())
								.filter(target -> target.getId().contentEquals((targetName))).findFirst().orElseGet(() -> null);
						launchBarManager.setActiveLaunchTarget(selectedTarget);
						return Status.OK_STATUS;
					}
					catch (CoreException e)
					{
						Logger.log(e);
						return Status.CANCEL_STATUS;
					}
				}
			};
			revertTargetJob.schedule(100);
			LaunchBarListener.setIgnoreJtagTargetChange(false);
		});

		if (!(parent instanceof ScrolledComposite))
		{
			ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
			sc.setFont(parent.getFont());
			sc.setExpandHorizontal(true);
			sc.setExpandVertical(true);
			sc.setShowFocusedControl(true);
			this.createControl(sc);
			Control control = this.getControl();
			if (control != null)
			{
				sc.setContent(control);
				sc.setMinSize(control.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				this.setControl(control.getParent());
			}
			return;

		}
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout layout = new GridLayout();
		comp.setLayout(layout);

		createGdbServerGroup(comp);

		createGdbClientControls(comp);

		createRemoteControl(comp);
		fUpdateThreadlistOnSuspend = new Button(comp, SWT.CHECK);
		fUpdateThreadlistOnSuspend.setText(Messages.getString("DebuggerTab.update_thread_list_on_suspend_Text")); //$NON-NLS-1$
		fUpdateThreadlistOnSuspend
				.setToolTipText(Messages.getString("DebuggerTab.update_thread_list_on_suspend_ToolTipText")); //$NON-NLS-1$

		Link restoreDefaults;
		{
			restoreDefaults = new Link(comp, SWT.NONE);
			restoreDefaults.setText(Messages.getString("DebuggerTab.restoreDefaults_Link")); //$NON-NLS-1$
			restoreDefaults.setToolTipText(Messages.getString("DebuggerTab.restoreDefaults_ToolTipText")); //$NON-NLS-1$

			GridData gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = SWT.RIGHT;
			gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns;
			restoreDefaults.setLayoutData(gd);
		}

		// --------------------------------------------------------------------

		fUpdateThreadlistOnSuspend.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				updateLaunchConfigurationDialog();
			}
		});

		restoreDefaults.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				initializeFromDefaults();
				scheduleUpdateJob();
			}
		});
	}

	private void browseButtonSelected(String title, Text text)
	{
		FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
		dialog.setText(title);
		String str = text.getText().trim();
		int lastSeparatorIndex = str.lastIndexOf(File.separator);
		if (lastSeparatorIndex != -1)
			dialog.setFilterPath(str.substring(0, lastSeparatorIndex));
		str = dialog.open();
		if (str != null)
			text.setText(str);
	}

	private void variablesButtonSelected(Text text)
	{
		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
		if (dialog.open() == StringVariableSelectionDialog.OK)
		{
			text.insert(dialog.getVariableExpression());
		}
	}

	private static void showNoTargetMessage(String selectedTarget)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				boolean isYes = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
						Messages.IDFLaunchTargetNotFoundIDFLaunchTargetNotFoundTitle,
						Messages.IDFLaunchTargetNotFoundMsg1 + selectedTarget + Messages.IDFLaunchTargetNotFoundMsg2
								+ Messages.IDFLaunchTargetNotFoundMsg3);
				if (isYes)
				{
					NewSerialFlashTargetWizard wizard = new NewSerialFlashTargetWizard();
					WizardDialog dialog = new WizardDialog(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
					dialog.open();
				}
			}
		});
	}

	private void createGdbServerGroup(Composite parent)
	{

		Group group = new Group(parent, SWT.NONE);
		{
			GridLayout layout = new GridLayout();
			group.setLayout(layout);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			group.setLayoutData(gd);
			group.setText(Messages.getString("DebuggerTab.gdbServerGroup_Text")); //$NON-NLS-1$
		}

		Composite comp = new Composite(group, SWT.NONE);
		{
			GridLayout layout = new GridLayout();
			layout.numColumns = 5;
			layout.marginHeight = 0;
			comp.setLayout(layout);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			comp.setLayoutData(gd);
		}

		{
			Composite local = new Composite(comp, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 1;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.makeColumnsEqualWidth = true;
			local.setLayout(layout);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns;
			local.setLayoutData(gd);

			fDoStartGdbServer = new Button(local, SWT.CHECK);
			fDoStartGdbServer.setText(Messages.getString("DebuggerTab.doStartGdbServer_Text")); //$NON-NLS-1$
			fDoStartGdbServer.setToolTipText(Messages.getString("DebuggerTab.doStartGdbServer_ToolTipText")); //$NON-NLS-1$
			gd = new GridData(GridData.FILL_HORIZONTAL);
			fDoStartGdbServer.setLayoutData(gd);
		}
		{
			Label label = new Label(comp, SWT.NONE);
			label.setText(Messages.getString("DebuggerTab.gdbServerExecutable_Label")); //$NON-NLS-1$
			label.setToolTipText(Messages.getString("DebuggerTab.gdbServerExecutable_ToolTipText")); //$NON-NLS-1$

			Composite local = new Composite(comp, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 3;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			local.setLayout(layout);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns - 1;
			local.setLayoutData(gd);
			{
				fGdbServerExecutable = new Text(local, SWT.SINGLE | SWT.BORDER);
				gd = new GridData(GridData.FILL_HORIZONTAL);
				fGdbServerExecutable.setLayoutData(gd);

				fGdbServerBrowseButton = new Button(local, SWT.NONE);
				fGdbServerBrowseButton.setText(Messages.getString("DebuggerTab.gdbServerExecutableBrowse")); //$NON-NLS-1$

				fGdbServerVariablesButton = new Button(local, SWT.NONE);
				fGdbServerVariablesButton.setText(Messages.getString("DebuggerTab.gdbServerExecutableVariable")); //$NON-NLS-1$
			}
		}

		{
			Label label = new Label(comp, SWT.NONE);
			label.setText(Messages.getString("DebuggerTab.gdbServerActualPath_Label")); //$NON-NLS-1$

			fGdbServerPathLabel = new Text(comp, SWT.SINGLE | SWT.BORDER);
			GridData gd = new GridData(SWT.FILL, 0, true, false);
			gd.horizontalSpan = 4;
			fGdbServerPathLabel.setLayoutData(gd);

			fGdbServerPathLabel.setEnabled(true);
			fGdbServerPathLabel.setEditable(false);
		}

		{
			Label label = new Label(comp, SWT.NONE);
			label.setText(""); //$NON-NLS-1$

			fLink = new Link(comp, SWT.NONE);
			fLink.setText(Messages.getString("DebuggerTab.gdbServerActualPath_link")); //$NON-NLS-1$
			GridData gd = new GridData();
			gd.horizontalSpan = 4;
			fLink.setLayoutData(gd);
		}

		{
			Label label = new Label(comp, SWT.NONE);
			label.setText(Messages.getString("DebuggerTab.gdbServerGdbPort_Label")); //$NON-NLS-1$
			label.setToolTipText(Messages.getString("DebuggerTab.gdbServerGdbPort_ToolTipText")); //$NON-NLS-1$

			fGdbServerGdbPort = new Text(comp, SWT.SINGLE | SWT.BORDER);
			GridData gd = new GridData();
			gd.widthHint = 60;
			gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns - 1;
			fGdbServerGdbPort.setLayoutData(gd);
		}

		{
			Label label = new Label(comp, SWT.NONE);
			label.setText(Messages.getString("DebuggerTab.gdbServerTelnetPort_Label")); //$NON-NLS-1$
			label.setToolTipText(Messages.getString("DebuggerTab.gdbServerTelnetPort_ToolTipText")); //$NON-NLS-1$

			fGdbServerTelnetPort = new Text(comp, SWT.SINGLE | SWT.BORDER);
			GridData gd = new GridData();
			gd.widthHint = 60;
			gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns - 1;
			fGdbServerTelnetPort.setLayoutData(gd);
		}

		{
			Label label = new Label(comp, SWT.NONE);
			label.setText(Messages.getString("DebuggerTab.gdbServerTclPort_Label")); //$NON-NLS-1$
			label.setToolTipText(Messages.getString("DebuggerTab.gdbServerTclPort_ToolTipText")); //$NON-NLS-1$

			fGdbServerTclPort = new Text(comp, SWT.SINGLE | SWT.BORDER);
			GridData gd = new GridData();
			gd.widthHint = 60;
			gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns - 1;
			fGdbServerTclPort.setLayoutData(gd);
		}

		String selectedTarget = getLaunchTarget();
		EspConfigParser parser = new EspConfigParser();
		String openOCDPath = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.OPENOCD_SCRIPTS);
		if (!openOCDPath.isEmpty() && parser.hasBoardConfigJson()) // $NON-NLS-1$
		{
			{
				Label label = new Label(comp, SWT.NONE);
				label.setText(Messages.getString("DebuggerTab.flashVoltageLabel")); //$NON-NLS-1$
				label.setToolTipText(Messages.getString("DebuggerTab.flashVoltageToolTip")); //$NON-NLS-1$
				GridData gd = new GridData();
				gd.widthHint = 80;
				gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns - 1;
				fFlashVoltage = new Combo(comp, SWT.SINGLE | SWT.BORDER);
				fFlashVoltage.setItems(parser.getEspFlashVoltages().toArray(new String[0]));
				fFlashVoltage.setText("default"); //$NON-NLS-1$

				fFlashVoltage.addSelectionListener(new SelectionAdapter()
				{
					@Override
					public void widgetSelected(SelectionEvent e)
					{
						fTargetName.notifyListeners(SWT.Selection, null);
					}
				});
				fFlashVoltage.setLayoutData(gd);
			}
			{
				Label label = new Label(comp, SWT.NONE);
				label.setText(Messages.getString("DebuggerTab.configTargetLabel")); //$NON-NLS-1$
				label.setToolTipText(Messages.getString("DebuggerTab.configTargetToolTip")); //$NON-NLS-1$
				GridData gd = new GridData();
				gd.widthHint = 80;
				gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns - 1;
				fTarget = new Combo(comp, SWT.SINGLE | SWT.BORDER);
				fTarget.setItems(parser.getTargets().toArray(new String[0]));
				fTarget.setText(selectedTarget);
				fTarget.addSelectionListener(new SelectionAdapter()
				{
					@Override
					public void widgetSelected(SelectionEvent e)
					{
						String updatedSelectedTarget = getLaunchTarget();
						String selectedItem = fTarget.getText();
						if (!selectedItem.contentEquals(updatedSelectedTarget))
						{
							updateLaunchBar(selectedItem);
						}
						fGdbClientExecutable.setText(IDFUtil.getXtensaToolchainExecutablePathByTarget(selectedItem));
						boardConfigsMap = parser.getBoardsConfigs(selectedItem);
						fTargetName.setItems(parser.getBoardsConfigs(selectedItem).keySet().toArray(new String[0]));
						fTargetName.select(new DefaultBoardProvider().getIndexOfDefaultBoard(selectedItem,
								fTargetName.getItems()));
						fTargetName.notifyListeners(SWT.Selection, null);
					}

					private void updateLaunchBar(String selectedItem)
					{
						ILaunchTarget target = findSuitableTargetForSelectedItem(selectedItem);
						try
						{
							if (target != null)
							{
								launchBarManager.setActiveLaunchTarget(target);
							}
							else
							{
								showNoTargetMessage(selectedItem);
							}
							IDFUtil.getXtensaToolchainExecutablePathByTarget(selectedItem);

						}
						catch (CoreException e1)
						{
							Logger.log(e1);
						}
					}

					private ILaunchTarget findSuitableTargetForSelectedItem(String selectedItem)
					{
						ILaunchTargetManager launchTargetManager = Activator.getService(ILaunchTargetManager.class);
						ILaunchTarget[] targets = launchTargetManager
								.getLaunchTargetsOfType("com.espressif.idf.launch.serial.core.serialFlashTarget"); //$NON-NLS-1$
						ILaunchTarget suitableTarget = null;

						for (ILaunchTarget target : targets)
						{
							String idfTarget = target.getAttribute("com.espressif.idf.launch.serial.core.idfTarget", //$NON-NLS-1$
									null);
							String targetSerialPort = target
									.getAttribute(SerialFlashLaunchTargetProvider.ATTR_SERIAL_PORT, ""); //$NON-NLS-1$
							if (idfTarget.contentEquals(selectedItem))
							{
								if (targetSerialPort.contentEquals(getSerialPort()))
								{
									return target;
								}
								suitableTarget = target;
							}
						}
						return suitableTarget;
					}
				});
				fTarget.setLayoutData(gd);
			}
			{
				Label label = new Label(comp, SWT.NONE);
				label.setText(Messages.getString("DebuggerTab.configBoardLabel")); //$NON-NLS-1$
				label.setToolTipText(Messages.getString("DebuggerTab.configBoardTooTip")); //$NON-NLS-1$

				GridData gd = new GridData();
				gd.widthHint = 250;
				gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns - 1;
				fTargetName = new Combo(comp, SWT.SINGLE | SWT.BORDER);
				fTargetName.setItems(parser.getBoardsConfigs(selectedTarget).keySet().toArray(new String[0]));
				boardConfigsMap = parser.getBoardsConfigs(selectedTarget);

				fTargetName.select(
						new DefaultBoardProvider().getIndexOfDefaultBoard(selectedTarget, fTargetName.getItems()));
				fTargetName.addSelectionListener(new SelectionAdapter()
				{
					@SuppressWarnings("unchecked")
					@Override
					public void widgetSelected(SelectionEvent e)
					{
						String selectedVoltage = fFlashVoltage.getText();
						String selectedItem = fTargetName.getText();
						String configOptionString = EMPTY_CONFIG_OPTIONS;
						if (!selectedVoltage.equals("default")) //$NON-NLS-1$
						{
							configOptionString = configOptionString + " -c 'set ESP32_FLASH_VOLTAGE " + selectedVoltage //$NON-NLS-1$
									+ "'"; //$NON-NLS-1$
						}
						if (!selectedItem.isEmpty())
						{
							for (String config : (String[]) boardConfigsMap.get(selectedItem).toArray(new String[0]))
							{
								configOptionString = configOptionString + " -f " + config; //$NON-NLS-1$
							}
							fGdbServerOtherOptions.setText(configOptionString);
						}

					}

				});
				fTargetName.setLayoutData(gd);
			}
		}

		{
			Label label = new Label(comp, SWT.NONE);
			label.setText(Messages.getString("DebuggerTab.gdbServerOther_Label")); //$NON-NLS-1$
			label.setToolTipText(Messages.getString("DebuggerTab.gdbServerOther_ToolTipText")); //$NON-NLS-1$
			GridData gd = new GridData();
			gd.verticalAlignment = SWT.TOP;
			label.setLayoutData(gd);

			fGdbServerOtherOptions = new Text(comp, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
			gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.heightHint = 60;
			gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns - 1;
			fGdbServerOtherOptions.setLayoutData(gd);
		}

		{
			Composite local = new Composite(comp, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.makeColumnsEqualWidth = true;
			local.setLayout(layout);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns;
			local.setLayoutData(gd);

			fDoGdbServerAllocateConsole = new Button(local, SWT.CHECK);
			fDoGdbServerAllocateConsole.setText(Messages.getString("DebuggerTab.gdbServerAllocateConsole_Label")); //$NON-NLS-1$
			fDoGdbServerAllocateConsole
					.setToolTipText(Messages.getString("DebuggerTab.gdbServerAllocateConsole_ToolTipText")); //$NON-NLS-1$
			gd = new GridData(GridData.FILL_HORIZONTAL);
			fDoGdbServerAllocateConsole.setLayoutData(gd);

			fDoGdbServerAllocateTelnetConsole = new Button(local, SWT.CHECK);
			fDoGdbServerAllocateTelnetConsole
					.setText(Messages.getString("DebuggerTab.gdbServerAllocateTelnetConsole_Label")); //$NON-NLS-1$
			fDoGdbServerAllocateTelnetConsole
					.setToolTipText(Messages.getString("DebuggerTab.gdbServerAllocateTelnetConsole_ToolTipText")); //$NON-NLS-1$
			gd = new GridData(GridData.FILL_HORIZONTAL);
			fDoGdbServerAllocateTelnetConsole.setLayoutData(gd);

			// update doStartGdbServerChanged() too
			fDoGdbServerAllocateTelnetConsole.setEnabled(false);
		}

		// ----- Actions ------------------------------------------------------

		ModifyListener scheduleUpdateJobModifyListener = new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				scheduleUpdateJob();
			}
		};

		SelectionAdapter scheduleUpdateJobSelectionAdapter = new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				scheduleUpdateJob();
			}
		};

		fDoStartGdbServer.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doStartGdbServerChanged();
				if (fDoStartGdbServer.getSelection())
				{
					fTargetIpAddress.setText(DefaultPreferences.REMOTE_IP_ADDRESS_LOCALHOST);
				}
				scheduleUpdateJob();
			}
		});

		fGdbServerExecutable.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{

				scheduleUpdateJob(); // provides much better performance for
										// Text listeners
				updateGdbServerActualPath();
			}
		});

		fGdbServerBrowseButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				browseButtonSelected(Messages.getString("DebuggerTab.gdbServerExecutableBrowse_Title"), //$NON-NLS-1$
						fGdbServerExecutable);
			}
		});

		fGdbServerVariablesButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				variablesButtonSelected(fGdbServerExecutable);
			}
		});

		fLink.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{

				String text = e.text;
				if (Activator.getInstance().isDebugging())
				{
					System.out.println(text);
				}

				int ret = -1;
				if ("global".equals(text)) //$NON-NLS-1$
				{
					ret = PreferencesUtil.createPreferenceDialogOn(parent.getShell(), GlobalMcuPage.ID, null, null)
							.open();
				}
				else if ("workspace".equals(text)) //$NON-NLS-1$
				{
					ret = PreferencesUtil.createPreferenceDialogOn(parent.getShell(), WorkspaceMcuPage.ID, null, null)
							.open();
				}
				else if ("project".equals(text)) //$NON-NLS-1$
				{
					assert (fConfiguration != null);
					IProject project = EclipseUtils.getProjectByLaunchConfiguration(fConfiguration);
					ret = PreferencesUtil
							.createPropertyDialogOn(parent.getShell(), project, ProjectMcuPage.ID, null, null, 0)
							.open();
				}

				if (ret == Window.OK)
				{
					updateGdbServerActualPath();
				}
			}
		});

		fGdbServerGdbPort.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{

				// make the target port the same
				fTargetPortNumber.setText(fGdbServerGdbPort.getText());
				scheduleUpdateJob();
			}
		});

		fGdbServerTelnetPort.addModifyListener(scheduleUpdateJobModifyListener);
		fGdbServerTclPort.addModifyListener(scheduleUpdateJobModifyListener);

		fGdbServerOtherOptions.addModifyListener(scheduleUpdateJobModifyListener);

		fDoGdbServerAllocateConsole.addSelectionListener(scheduleUpdateJobSelectionAdapter);
		fDoGdbServerAllocateTelnetConsole.addSelectionListener(scheduleUpdateJobSelectionAdapter);
	}

	private String getLaunchTarget()
	{
		launchBarManager = Activator.getService(ILaunchBarManager.class);
		String selectedTarget = ""; //$NON-NLS-1$
		try
		{
			selectedTarget = launchBarManager.getActiveLaunchTarget().getAttribute(IDFLaunchConstants.ATTR_IDF_TARGET,
					""); //$NON-NLS-1$
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return selectedTarget;
	}

	private void createGdbClientControls(Composite parent)
	{

		Group group = new Group(parent, SWT.NONE);
		{
			GridLayout layout = new GridLayout();
			group.setLayout(layout);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			group.setLayoutData(gd);
			group.setText(Messages.getString("DebuggerTab.gdbSetupGroup_Text")); //$NON-NLS-1$
		}

		Composite comp = new Composite(group, SWT.NONE);
		{
			GridLayout layout = new GridLayout();
			layout.numColumns = 5;
			layout.marginHeight = 0;
			comp.setLayout(layout);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			comp.setLayoutData(gd);
		}

		{
			fDoStartGdbClient = new Button(comp, SWT.CHECK);
			fDoStartGdbClient.setText(Messages.getString("DebuggerTab.doStartGdbClient_Text")); //$NON-NLS-1$
			fDoStartGdbClient.setToolTipText(Messages.getString("DebuggerTab.doStartGdbClient_ToolTipText")); //$NON-NLS-1$
			GridData gd = new GridData();
			gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns;
			fDoStartGdbClient.setLayoutData(gd);
		}

		{
			Label label = new Label(comp, SWT.NONE);
			label.setText(Messages.getString("DebuggerTab.gdbCommand_Label")); //$NON-NLS-1$
			label.setToolTipText(Messages.getString("DebuggerTab.gdbCommand_ToolTipText")); //$NON-NLS-1$

			Composite local = new Composite(comp, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 3;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			local.setLayout(layout);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns - 1;
			local.setLayoutData(gd);
			{
				fGdbClientExecutable = new Text(local, SWT.SINGLE | SWT.BORDER);
				gd = new GridData(GridData.FILL_HORIZONTAL);
				fGdbClientExecutable.setLayoutData(gd);

				fGdbClientBrowseButton = new Button(local, SWT.NONE);
				fGdbClientBrowseButton.setText(Messages.getString("DebuggerTab.gdbCommandBrowse")); //$NON-NLS-1$

				fGdbClientVariablesButton = new Button(local, SWT.NONE);
				fGdbClientVariablesButton.setText(Messages.getString("DebuggerTab.gdbCommandVariable")); //$NON-NLS-1$
			}
		}

		{
			Label label = new Label(comp, SWT.NONE);
			label.setText(Messages.getString("DebuggerTab.gdbOtherOptions_Label")); //$NON-NLS-1$
			label.setToolTipText(Messages.getString("DebuggerTab.gdbOtherOptions_ToolTipText")); //$NON-NLS-1$
			GridData gd = new GridData();
			label.setLayoutData(gd);

			fGdbClientOtherOptions = new Text(comp, SWT.SINGLE | SWT.BORDER);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns - 1;
			fGdbClientOtherOptions.setLayoutData(gd);
		}

		{
			Label label = new Label(comp, SWT.NONE);
			label.setText(Messages.getString("DebuggerTab.gdbOtherCommands_Label")); //$NON-NLS-1$
			label.setToolTipText(Messages.getString("DebuggerTab.gdbOtherCommands_ToolTipText")); //$NON-NLS-1$
			GridData gd = new GridData();
			gd.verticalAlignment = SWT.TOP;
			label.setLayoutData(gd);

			fGdbClientOtherCommands = new Text(comp, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
			gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.heightHint = 60;
			gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns - 1;
			fGdbClientOtherCommands.setLayoutData(gd);
		}

		// ----- Actions ------------------------------------------------------

		fDoStartGdbClient.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doStartGdbClientChanged();
				scheduleUpdateJob();
			}
		});

		fGdbClientExecutable.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{

				scheduleUpdateJob(); // provides much better performance for
										// Text listeners
				updateGdbClientActualPath();
			}
		});

		fGdbClientOtherOptions.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				scheduleUpdateJob();
			}
		});

		fGdbClientOtherCommands.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				scheduleUpdateJob();
			}
		});

		fGdbClientBrowseButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				browseButtonSelected(Messages.getString("DebuggerTab.gdbCommandBrowse_Title"), fGdbClientExecutable); //$NON-NLS-1$
			}
		});

		fGdbClientVariablesButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				variablesButtonSelected(fGdbClientExecutable);
			}
		});
	}

	private void createRemoteControl(Composite parent)
	{

		Group group = new Group(parent, SWT.NONE);
		{
			group.setText(Messages.getString("DebuggerTab.remoteGroup_Text")); //$NON-NLS-1$
			GridLayout layout = new GridLayout();
			group.setLayout(layout);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			group.setLayoutData(gd);
		}

		Composite comp = new Composite(group, SWT.NONE);
		{
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginHeight = 0;
			comp.setLayout(layout);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			comp.setLayoutData(gd);
		}

		// Create entry fields for TCP/IP connections
		{
			Label label = new Label(comp, SWT.NONE);
			label.setText(Messages.getString("DebuggerTab.ipAddressLabel")); //$NON-NLS-1$

			fTargetIpAddress = new Text(comp, SWT.BORDER);
			GridData gd = new GridData();
			gd.widthHint = 125;
			fTargetIpAddress.setLayoutData(gd);

			label = new Label(comp, SWT.NONE);
			label.setText(Messages.getString("DebuggerTab.portNumberLabel")); //$NON-NLS-1$

			fTargetPortNumber = new Text(comp, SWT.BORDER);
			gd = new GridData();
			gd.widthHint = 125;
			fTargetPortNumber.setLayoutData(gd);
		}

		// ---- Actions -------------------------------------------------------

		// Add watchers for user data entry
		fTargetIpAddress.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				scheduleUpdateJob(); // provides much better performance for
										// Text listeners
			}
		});
		fTargetPortNumber.addVerifyListener(new VerifyListener()
		{
			@Override
			public void verifyText(VerifyEvent e)
			{
				e.doit = Character.isDigit(e.character) || Character.isISOControl(e.character);
			}
		});
		fTargetPortNumber.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				scheduleUpdateJob(); // provides much better performance for
										// Text listeners
			}
		});
	}

	private void updateGdbServerActualPath()
	{

		assert (fConfiguration != null);
		String fullCommand = Configuration.getGdbServerCommand(fConfiguration, fGdbServerExecutable.getText());
		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.TabDebugger.updateGdbServerActualPath() \"" + fullCommand + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		fGdbServerPathLabel.setText(fullCommand);
	}

	private void updateGdbClientActualPath()
	{

		assert (fConfiguration != null);
		String fullCommand = Configuration.getGdbClientCommand(fConfiguration, fGdbClientExecutable.getText());
		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.TabDebugger.updateGdbClientActualPath() \"" + fullCommand + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		// fGdbClientPathLabel.setText(fullCommand);
	}

	private void doStartGdbServerChanged()
	{

		boolean enabled = fDoStartGdbServer.getSelection();

		fGdbServerExecutable.setEnabled(enabled);
		fGdbServerBrowseButton.setEnabled(enabled);
		fGdbServerVariablesButton.setEnabled(enabled);
		fGdbServerOtherOptions.setEnabled(enabled);

		fGdbServerGdbPort.setEnabled(enabled);
		fGdbServerTelnetPort.setEnabled(enabled);
		fGdbServerTclPort.setEnabled(enabled);

		if (EclipseUtils.isWindows())
		{
			// Prevent disable it on Windows
			fDoGdbServerAllocateConsole.setEnabled(false);
		}
		else
		{
			fDoGdbServerAllocateConsole.setEnabled(enabled);
		}

		// Disable remote target params when the server is started
		fTargetIpAddress.setEnabled(!enabled);
		fTargetPortNumber.setEnabled(!enabled);

		fGdbServerPathLabel.setEnabled(enabled);
		fLink.setEnabled(enabled);
	}

	private void doStartGdbClientChanged()
	{

		boolean enabled = fDoStartGdbClient.getSelection();

		fGdbClientExecutable.setEnabled(enabled);
		fGdbClientBrowseButton.setEnabled(enabled);
		fGdbClientVariablesButton.setEnabled(enabled);
		fGdbClientOtherOptions.setEnabled(enabled);
		fGdbClientOtherCommands.setEnabled(enabled);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration)
	{

		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.TabDebugger.initializeFrom() " + configuration.getName()); //$NON-NLS-1$
		}

		fConfiguration = configuration;

		try
		{
			Boolean booleanDefault;
			String stringDefault;

			// JTAG options
			if (fFlashVoltage != null && fTarget != null && fTargetName != null)
			{
				fFlashVoltage.setText(
						configuration.getAttribute(IDFLaunchConstants.JTAG_FLASH_VOLTAGE, fFlashVoltage.getText()));
				fTarget.setText(configuration.getAttribute(IDFLaunchConstants.TARGET_FOR_JTAG, fTarget.getText()));
				fTarget.notifyListeners(SWT.Selection, null);
				fTargetName.setText(configuration.getAttribute(IDFLaunchConstants.JTAG_BOARD, fTargetName.getText()));

			}

			// OpenOCD GDB server
			{

				// Start server locally

				booleanDefault = fPersistentPreferences.getGdbServerDoStart();
				fDoStartGdbServer.setSelection(
						configuration.getAttribute(ConfigurationAttributes.DO_START_GDB_SERVER, booleanDefault));

				// Executable
				stringDefault = fPersistentPreferences.getGdbServerExecutable();
				fGdbServerExecutable.setText(
						configuration.getAttribute(ConfigurationAttributes.GDB_SERVER_EXECUTABLE, stringDefault));

				// Ports
				fGdbServerGdbPort.setText(
						Integer.toString(configuration.getAttribute(ConfigurationAttributes.GDB_SERVER_GDB_PORT_NUMBER,
								DefaultPreferences.GDB_SERVER_GDB_PORT_NUMBER_DEFAULT)));
				fGdbServerTelnetPort.setText(Integer
						.toString(configuration.getAttribute(ConfigurationAttributes.GDB_SERVER_TELNET_PORT_NUMBER,
								DefaultPreferences.GDB_SERVER_TELNET_PORT_NUMBER_DEFAULT)));
				fGdbServerTclPort.setText(configuration.getAttribute(ConfigurationAttributes.GDB_SERVER_TCL_PORT_NUMBER,
						DefaultPreferences.GDB_SERVER_TCL_PORT_NUMBER_DEFAULT));

				// Other options
				fGdbServerOtherOptions.setText(configuration.getAttribute(ConfigurationAttributes.GDB_SERVER_OTHER,
						fGdbServerOtherOptions.getText()));

				// Allocate server console
				if (EclipseUtils.isWindows())
				{
					fDoGdbServerAllocateConsole.setSelection(true);
				}
				else
				{
					fDoGdbServerAllocateConsole.setSelection(
							configuration.getAttribute(ConfigurationAttributes.DO_GDB_SERVER_ALLOCATE_CONSOLE,
									DefaultPreferences.DO_GDB_SERVER_ALLOCATE_CONSOLE_DEFAULT));
				}

				// Allocate telnet console
				fDoGdbServerAllocateTelnetConsole.setSelection(
						configuration.getAttribute(ConfigurationAttributes.DO_GDB_SERVER_ALLOCATE_TELNET_CONSOLE,
								DefaultPreferences.DO_GDB_SERVER_ALLOCATE_TELNET_CONSOLE_DEFAULT));

			}

			// GDB Client Setup
			{
				booleanDefault = fPersistentPreferences.getGdbClientDoStart();
				fDoStartGdbClient.setSelection(
						configuration.getAttribute(ConfigurationAttributes.DO_START_GDB_CLIENT, booleanDefault));

				// Executable
				String gdbCommandAttr = configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME,
						fGdbClientExecutable.getText());
				fGdbClientExecutable.setText(gdbCommandAttr);

				// Other options
				stringDefault = fPersistentPreferences.getGdbClientOtherOptions();
				fGdbClientOtherOptions.setText(
						configuration.getAttribute(ConfigurationAttributes.GDB_CLIENT_OTHER_OPTIONS, stringDefault));

				stringDefault = fPersistentPreferences.getGdbClientCommands();
				fGdbClientOtherCommands.setText(
						configuration.getAttribute(ConfigurationAttributes.GDB_CLIENT_OTHER_COMMANDS, stringDefault));
			}

			// Remote target
			{
				fTargetIpAddress.setText(configuration.getAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS,
						DefaultPreferences.REMOTE_IP_ADDRESS_DEFAULT)); // $NON-NLS-1$

				int storedPort = 0;
				storedPort = configuration.getAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, 0); // Default
																								// 0

				// 0 means undefined, use default
				if ((storedPort <= 0) || (65535 < storedPort))
				{
					storedPort = DefaultPreferences.REMOTE_PORT_NUMBER_DEFAULT;
				}

				String portString = Integer.toString(storedPort); // $NON-NLS-1$
				fTargetPortNumber.setText(portString);
			}

			doStartGdbServerChanged();

			// Force thread update
			boolean updateThreadsOnSuspend = configuration.getAttribute(
					IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND,
					DefaultPreferences.UPDATE_THREAD_LIST_DEFAULT);
			fUpdateThreadlistOnSuspend.setSelection(updateThreadsOnSuspend);

		}
		catch (CoreException e)
		{
			Activator.log(e.getStatus());
		}

		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.TabDebugger.initializeFrom() completed " + configuration.getName()); //$NON-NLS-1$
		}
	}

	public void initializeFromDefaults()
	{

		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.TabDebugger.initializeFromDefaults()"); //$NON-NLS-1$
		}

		String stringDefault;

		// OpenOCD GDB server
		{
			// Start server locally
			fDoStartGdbServer.setSelection(DefaultPreferences.DO_START_GDB_SERVER_DEFAULT);

			// Executable
			stringDefault = fDefaultPreferences.getGdbServerExecutable();
			fGdbServerExecutable.setText(stringDefault);

			// Ports
			fGdbServerGdbPort.setText(Integer.toString(DefaultPreferences.GDB_SERVER_GDB_PORT_NUMBER_DEFAULT));
			fGdbServerTelnetPort.setText(Integer.toString(DefaultPreferences.GDB_SERVER_TELNET_PORT_NUMBER_DEFAULT));
			fGdbServerTclPort.setText(DefaultPreferences.GDB_SERVER_TCL_PORT_NUMBER_DEFAULT);

			// Other options
			stringDefault = fDefaultPreferences.getOpenocdConfig();
			fGdbServerOtherOptions.setText(stringDefault);

			// Allocate server console
			if (EclipseUtils.isWindows())
			{
				fDoGdbServerAllocateConsole.setSelection(true);
			}
			else
			{
				fDoGdbServerAllocateConsole.setSelection(DefaultPreferences.DO_GDB_SERVER_ALLOCATE_CONSOLE_DEFAULT);
			}

			// Allocate telnet console
			fDoGdbServerAllocateTelnetConsole
					.setSelection(DefaultPreferences.DO_GDB_SERVER_ALLOCATE_TELNET_CONSOLE_DEFAULT);

			fFlashVoltage.select(0);
			fTarget.select(0);
			fTarget.notifyListeners(SWT.Selection, null);
		}

		// GDB Client Setup
		{
			fDoStartGdbClient.setSelection(DefaultPreferences.DO_START_GDB_CLIENT_DEFAULT);

			// Other options
			fGdbClientOtherOptions.setText(DefaultPreferences.GDB_CLIENT_OTHER_OPTIONS_DEFAULT);

			fGdbClientOtherCommands.setText(DefaultPreferences.GDB_CLIENT_OTHER_COMMANDS_DEFAULT);
		}

		// Remote target
		{
			fTargetIpAddress.setText(DefaultPreferences.REMOTE_IP_ADDRESS_DEFAULT); // $NON-NLS-1$

			String portString = Integer.toString(DefaultPreferences.REMOTE_PORT_NUMBER_DEFAULT); // $NON-NLS-1$
			fTargetPortNumber.setText(portString);
		}

		doStartGdbServerChanged();

		// Force thread update
		fUpdateThreadlistOnSuspend.setSelection(DefaultPreferences.UPDATE_THREAD_LIST_DEFAULT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 */
	@Override
	public String getId()
	{
		return TAB_ID;
	}

	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy)
	{
		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.TabDebugger.activated() " + workingCopy.getName()); //$NON-NLS-1$
		}
	}

	@Override
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy)
	{
		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.TabDebugger.deactivated() " + workingCopy.getName()); //$NON-NLS-1$
		}
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig)
	{
		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.TabDebugger.isValid() " + launchConfig.getName()); //$NON-NLS-1$
		}

		setErrorMessage(null);
		setMessage(null);

		boolean result = true;
		boolean hasContent = false;

		if (fDoStartGdbServer != null && fDoStartGdbServer.getSelection())
		{

			hasContent = true;
			if (fGdbServerExecutable != null && fGdbServerExecutable.getText().trim().isEmpty())
			{
				setErrorMessage("GDB server executable path?"); //$NON-NLS-1$
				result = false;
			}

			if (fGdbServerGdbPort != null && fGdbServerGdbPort.getText().trim().isEmpty())
			{
				setErrorMessage("GDB port?"); //$NON-NLS-1$
				result = false;
			}

			if (fGdbServerTelnetPort != null && fGdbServerTelnetPort.getText().trim().isEmpty())
			{
				setErrorMessage("Telnet port?"); //$NON-NLS-1$
				result = false;
			}

			if (fGdbServerTclPort != null && fGdbServerTclPort.getText().trim().isEmpty())
			{
				setErrorMessage("Tcl port?"); //$NON-NLS-1$
				result = false;
			}
		}

		if (fDoStartGdbClient != null && fDoStartGdbClient.getSelection())
		{

			hasContent = true;
			if (fGdbClientExecutable != null && fGdbClientExecutable.getText().trim().isEmpty())
			{
				result = false;
				setErrorMessage("GDB client executable name?"); //$NON-NLS-1$
			}
		}

		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.TabDebugger.isValid() " + launchConfig.getName() + " = " + result); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return hasContent && result;
	}

	@Override
	public boolean canSave()
	{
		if (fDoStartGdbServer != null && fDoStartGdbServer.getSelection())
		{
			if (fGdbServerExecutable != null && fGdbServerExecutable.getText().trim().isEmpty())
				return false;

			if (fGdbServerGdbPort != null && fGdbServerGdbPort.getText().trim().isEmpty())
				return false;

			if (fGdbServerTelnetPort != null && fGdbServerTelnetPort.getText().trim().isEmpty())
				return false;
			if (fGdbServerTclPort != null && fGdbServerTclPort.getText().trim().isEmpty())
				return false;
		}

		// Now, if any of server or client is enabled, return true
		if (fDoStartGdbServer != null && fDoStartGdbServer.getSelection())
		{
			return true;
		}
		if (fDoStartGdbClient != null && fDoStartGdbClient.getSelection())
		{
			return true;
		}
		return false;
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration)
	{

		if (Activator.getInstance().isDebugging())
		{
			System.out
					.println("openocd.TabDebugger.performApply() " + configuration.getName() + ", dirty=" + isDirty()); //$NON-NLS-1$ //$NON-NLS-2$
		}

		{
			// legacy definition; although the jtag device class is not used,
			// it must be there, to avoid NPEs
			configuration.setAttribute(IGDBJtagConstants.ATTR_JTAG_DEVICE_ID, ConfigurationAttributes.JTAG_DEVICE);
		}

		boolean booleanValue;
		String stringValue;

		// OpenOCD server
		{
			// Start server
			booleanValue = fDoStartGdbServer.getSelection();
			configuration.setAttribute(ConfigurationAttributes.DO_START_GDB_SERVER, booleanValue);
			fPersistentPreferences.putGdbServerDoStart(booleanValue);

			// Executable
			stringValue = fGdbServerExecutable.getText().trim();
			configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_EXECUTABLE, stringValue);
			fPersistentPreferences.putGdbServerExecutable(stringValue);

			// Ports
			int port;
			if (!fGdbServerGdbPort.getText().trim().isEmpty())
			{
				port = Integer.parseInt(fGdbServerGdbPort.getText().trim());
				configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_GDB_PORT_NUMBER, port);
			}
			else
			{
				Activator.log("empty fGdbServerGdbPort"); //$NON-NLS-1$
			}
			if (!fGdbServerTelnetPort.getText().trim().isEmpty())
			{
				port = Integer.parseInt(fGdbServerTelnetPort.getText().trim());
				configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_TELNET_PORT_NUMBER, port);
			}
			else
			{
				Activator.log("empty fGdbServerTelnetPort"); //$NON-NLS-1$
			}
			if (!fGdbServerTclPort.getText().trim().isEmpty())
			{
				// Not integer, since it can be 'disabled'
				String str = fGdbServerTclPort.getText().trim();
				configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_TCL_PORT_NUMBER, str);
			}
			else
			{
				Activator.log("empty fGdbServerTclPort"); //$NON-NLS-1$
			}

			// Other options
			configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_OTHER,
					fGdbServerOtherOptions.getText().trim());

			// Allocate server console
			configuration.setAttribute(ConfigurationAttributes.DO_GDB_SERVER_ALLOCATE_CONSOLE,
					fDoGdbServerAllocateConsole.getSelection());

			// Allocate semihosting console
			configuration.setAttribute(ConfigurationAttributes.DO_GDB_SERVER_ALLOCATE_TELNET_CONSOLE,
					fDoGdbServerAllocateTelnetConsole.getSelection());
		}

		// GDB client
		{
			// Start client
			booleanValue = fDoStartGdbClient.getSelection();
			configuration.setAttribute(ConfigurationAttributes.DO_START_GDB_CLIENT, booleanValue);
			fPersistentPreferences.putGdbClientDoStart(booleanValue);

			// always use remote
			configuration.setAttribute(IGDBJtagConstants.ATTR_USE_REMOTE_TARGET,
					DefaultPreferences.USE_REMOTE_TARGET_DEFAULT);

			stringValue = fGdbClientExecutable.getText().trim();
			configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME, stringValue); // DSF

			stringValue = fGdbClientOtherOptions.getText().trim();
			configuration.setAttribute(ConfigurationAttributes.GDB_CLIENT_OTHER_OPTIONS, stringValue);
			fPersistentPreferences.putGdbClientOtherOptions(stringValue);

			stringValue = fGdbClientOtherCommands.getText().trim();
			configuration.setAttribute(ConfigurationAttributes.GDB_CLIENT_OTHER_COMMANDS, stringValue);
			fPersistentPreferences.putGdbClientCommands(stringValue);
		}

		{
			if (fDoStartGdbServer.getSelection())
			{
				configuration.setAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS, "localhost"); //$NON-NLS-1$

				String str = fGdbServerGdbPort.getText().trim();
				if (!str.isEmpty())
				{
					try
					{
						int port;
						port = Integer.parseInt(str);
						configuration.setAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, port);
					}
					catch (NumberFormatException e)
					{
						Activator.log(e);
					}
				}
			}
			else
			{
				String ip = fTargetIpAddress.getText().trim();
				configuration.setAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS, ip);

				String str = fTargetPortNumber.getText().trim();
				if (!str.isEmpty())
				{
					try
					{
						int port = Integer.valueOf(str).intValue();
						configuration.setAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, port);
					}
					catch (NumberFormatException e)
					{
						Activator.log(e);
					}
				}
			}
		}

		// JTAG options
		if (fFlashVoltage != null && fTarget != null && fTargetName != null)
		{
			configuration.setAttribute(IDFLaunchConstants.JTAG_FLASH_VOLTAGE, fFlashVoltage.getText());
			configuration.setAttribute(IDFLaunchConstants.TARGET_FOR_JTAG, fTarget.getText());
			configuration.setAttribute(IDFLaunchConstants.JTAG_BOARD, fTargetName.getText());
		}

		// Force thread update
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND,
				fUpdateThreadlistOnSuspend.getSelection());

		fPersistentPreferences.flush();

		if (Activator.getInstance().isDebugging())
		{
			System.out.println(
					"openocd.TabDebugger.performApply() completed " + configuration.getName() + ", dirty=" + isDirty()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		lastAppliedConfiguration = configuration;
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
	{

		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.TabDebugger.setDefaults() " + configuration.getName()); //$NON-NLS-1$
		}

		configuration.setAttribute(IGDBJtagConstants.ATTR_JTAG_DEVICE_ID, ConfigurationAttributes.JTAG_DEVICE);

		String defaultString;
		boolean defaultBoolean;

		// These are inherited from the generic implementation.
		// Some might need some trimming.
		{
			configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND,
					IGDBLaunchConfigurationConstants.DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND_DEFAULT);
		}

		// OpenOCD GDB server setup
		{
			defaultBoolean = fPersistentPreferences.getGdbServerDoStart();
			configuration.setAttribute(ConfigurationAttributes.DO_START_GDB_SERVER, defaultBoolean);

			defaultString = fPersistentPreferences.getGdbServerExecutable();
			configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_EXECUTABLE, defaultString);

			configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_CONNECTION_ADDRESS,
					DefaultPreferences.GDB_SERVER_CONNECTION_ADDRESS_DEFAULT);

			configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_GDB_PORT_NUMBER,
					DefaultPreferences.GDB_SERVER_GDB_PORT_NUMBER_DEFAULT);
			configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_TELNET_PORT_NUMBER,
					DefaultPreferences.GDB_SERVER_TELNET_PORT_NUMBER_DEFAULT);
			configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_TCL_PORT_NUMBER,
					DefaultPreferences.GDB_SERVER_TCL_PORT_NUMBER_DEFAULT);

			configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_LOG,
					DefaultPreferences.GDB_SERVER_LOG_DEFAULT);

			configuration.setAttribute(ConfigurationAttributes.DO_GDB_SERVER_ALLOCATE_CONSOLE,
					DefaultPreferences.DO_GDB_SERVER_ALLOCATE_CONSOLE_DEFAULT);

			configuration.setAttribute(ConfigurationAttributes.DO_GDB_SERVER_ALLOCATE_TELNET_CONSOLE,
					DefaultPreferences.DO_GDB_SERVER_ALLOCATE_TELNET_CONSOLE_DEFAULT);

		}

		// GDB client setup
		{
			configuration.setAttribute(IGDBJtagConstants.ATTR_USE_REMOTE_TARGET,
					DefaultPreferences.USE_REMOTE_TARGET_DEFAULT);

			defaultString = fPersistentPreferences.getGdbClientOtherOptions();
			configuration.setAttribute(ConfigurationAttributes.GDB_CLIENT_OTHER_OPTIONS, defaultString);

			defaultString = fPersistentPreferences.getGdbClientCommands();
			configuration.setAttribute(ConfigurationAttributes.GDB_CLIENT_OTHER_COMMANDS, defaultString);
		}

		// Force thread update
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND,
				DefaultPreferences.UPDATE_THREAD_LIST_DEFAULT);
	}

	private String getSerialPort()
	{

		String serialPort = ""; //$NON-NLS-1$
		try
		{
			serialPort = launchBarManager.getActiveLaunchTarget()
					.getAttribute(SerialFlashLaunchTargetProvider.ATTR_SERIAL_PORT, ""); //$NON-NLS-1$

		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
		return serialPort;
	}

	// Get Xtensa toolchain path based on the target configured for the project
	private String getGdbClientExecutable(ILaunchConfiguration configuration)
	{
		// Find the project and current launch target configured
		IProject project = null;
		if (configuration != null)
		{
			project = EclipseUtils.getProjectByLaunchConfiguration(configuration);
		}
		String exePath = IDFUtil.getXtensaToolchainExecutablePath(project);
		return StringUtil.isEmpty(exePath) ? StringUtil.EMPTY : exePath;
	}

	// ------------------------------------------------------------------------
}
