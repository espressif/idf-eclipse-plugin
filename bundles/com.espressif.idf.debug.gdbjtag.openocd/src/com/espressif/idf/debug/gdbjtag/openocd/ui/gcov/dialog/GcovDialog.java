package com.espressif.idf.debug.gdbjtag.openocd.ui.gcov.dialog;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.json.simple.JSONArray;

import com.espressif.idf.core.DefaultBoardProvider;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.EspConfigParser;
import com.espressif.idf.debug.gdbjtag.openocd.Activator;
import com.espressif.idf.debug.gdbjtag.openocd.DynamicVariableResolver;
import com.espressif.idf.debug.gdbjtag.openocd.preferences.DefaultPreferences;
import com.espressif.idf.debug.gdbjtag.openocd.ui.Messages;
import com.espressif.idf.ui.LogMessagesThread;
import com.espressif.idf.ui.UIPlugin;

public class GcovDialog extends TitleAreaDialog
{
	private static final String EMPTY_CONFIG_OPTIONS = "-s ${openocd_path}/share/openocd/scripts";
	private static final String HARD_CODED_DUMP_CMD = "esp gcov dump";
	private static final String INSTANT_RUN_TIME_DUMP_CMD = "esp gcov";

	private Combo flashVoltageCombo;
	private Combo targetCombo;
	private Combo targetBoardCombo;
	private Button hardCodeDumpButton;
	private Button instantRuntimeDumpButton;
	private Text openOcdText;
	private ListViewer listViewer;
	
	private LogMessagesThread logMessagesThread;
	private Queue<String> logQueue;

	private Map<String, JSONArray> boardConfigsMap;

	private ILaunchBarManager launchBarManager;
	private DefaultPreferences fDefaultPreferences;

	private IProject project;

	private OpenOcdProcess openOcd;

	public GcovDialog(Shell parentShell, IProject project)
	{
		super(parentShell);
		fDefaultPreferences = Activator.getInstance().getDefaultPreferences();
		this.project = project;

		logQueue = new ConcurrentLinkedQueue<String>();
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		setMessage("GCOV Report and Dump Generator");
		setTitle("GCOV Dialog");
		setTitleImage(UIPlugin.getImage("icons/espressif_logo.png"));
		Composite area = (Composite) super.createDialogArea(parent);
		Composite mainComposite = new Composite(area, SWT.NONE);
		mainComposite.setLayout(new GridLayout(1, false));
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Composite openOcdComposite = new Composite(mainComposite, SWT.NONE);

		openOcdComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		openOcdComposite.setLayout(new GridLayout(1, false));

		createOpenOcdGroup(openOcdComposite);

		Composite buttonComposite = new Composite(mainComposite, SWT.NONE);
		GridLayout gl_buttonComposite = new GridLayout(2, false);
		buttonComposite.setLayout(gl_buttonComposite);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		hardCodeDumpButton = new Button(buttonComposite, SWT.NONE);
		hardCodeDumpButton.setText("Hard-coded Dump");
		hardCodeDumpButton.addSelectionListener(new GcovDumpButtonSelectionAdapter(true));

		instantRuntimeDumpButton = new Button(buttonComposite, SWT.NONE);
		instantRuntimeDumpButton.setText("Instant Run-Time Dump");
		instantRuntimeDumpButton.addSelectionListener(new GcovDumpButtonSelectionAdapter(false));

		Label labelOpenOcdText = new Label(buttonComposite, SWT.HORIZONTAL);
		labelOpenOcdText.setText("OpenOcd Logs");

		Composite logsComposite = new Composite(mainComposite, SWT.NONE);
		logsComposite.setLayout(new FillLayout(SWT.VERTICAL));
		GridData logsGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 7);
		logsGridData.heightHint = 200;
		logsComposite.setLayoutData(logsGridData);

		openOcdText = new Text(logsComposite,
				SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		GridData textGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 7);
		textGridData.heightHint = 200;
		openOcdText.setLayoutData(textGridData);
		
		Composite viewerComposite = new Composite(mainComposite, SWT.NONE);
		viewerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		viewerComposite.setLayout(new GridLayout(1, false));

		listViewer = new ListViewer(viewerComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData listViewerGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		listViewer.getControl().setLayoutData(listViewerGridData);

		// Then add your items to the viewer
		String[] items = {"Item 1", "Item 2", "Item 3"}; // replace with your items
		listViewer.add(items);
		
		logMessagesThread = new LogMessagesThread(logQueue, openOcdText, parent.getDisplay());
		logMessagesThread.start();
		return mainComposite;
	}

	private void createOpenOcdGroup(Composite parent)
	{

		Group group = new Group(parent, SWT.NONE);
		{
			GridLayout layout = new GridLayout();
			group.setLayout(layout);
			group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			group.setText(Messages.getString("DebuggerTab.gdbServerGroup_Text"));
		}

		Composite comp = new Composite(group, SWT.NONE);
		{
			GridLayout layout = new GridLayout();
			layout.numColumns = 5;
			layout.marginHeight = 0;
			comp.setLayout(layout);
			comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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
		}

		String selectedTarget = getLaunchTarget();
		EspConfigParser parser = new EspConfigParser();
		String openOCDPath = new IDFEnvironmentVariables().getEnvValue(IDFEnvironmentVariables.OPENOCD_SCRIPTS);
		if (!openOCDPath.isEmpty() && parser.hasBoardConfigJson()) // $NON-NLS-1$
		{
			{
				Label label = new Label(comp, SWT.NONE);
				label.setText(Messages.getString("DebuggerTab.flashVoltageLabel"));
				label.setToolTipText(Messages.getString("DebuggerTab.flashVoltageToolTip"));
				GridData gd = new GridData();
				gd.widthHint = 80;
				gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns - 1;
				flashVoltageCombo = new Combo(comp, SWT.SINGLE | SWT.BORDER);
				flashVoltageCombo.setItems(parser.getEspFlashVoltages().toArray(new String[0]));
				flashVoltageCombo.setText("default"); //$NON-NLS-1$

				flashVoltageCombo.addSelectionListener(new SelectionAdapter()
				{
					@Override
					public void widgetSelected(SelectionEvent e)
					{
						targetBoardCombo.notifyListeners(SWT.Selection, null);
					}
				});
				flashVoltageCombo.setLayoutData(gd);
			}
			{
				Label label = new Label(comp, SWT.NONE);
				label.setText(Messages.getString("DebuggerTab.configTargetLabel"));
				label.setToolTipText(Messages.getString("DebuggerTab.configTargetToolTip"));
				GridData gd = new GridData();
				gd.widthHint = 80;
				gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns - 1;
				targetCombo = new Combo(comp, SWT.SINGLE | SWT.BORDER);
				targetCombo.setItems(parser.getTargets().toArray(new String[0]));
				targetCombo.setText(selectedTarget);
				targetCombo.addSelectionListener(new SelectionAdapter()
				{
					@Override
					public void widgetSelected(SelectionEvent e)
					{
						String selectedItem = targetCombo.getText();
						boardConfigsMap = parser.getBoardsConfigs(selectedItem);
						targetBoardCombo
								.setItems(parser.getBoardsConfigs(selectedItem).keySet().toArray(new String[0]));
						targetBoardCombo.select(new DefaultBoardProvider().getIndexOfDefaultBoard(selectedItem,
								targetBoardCombo.getItems()));
						targetBoardCombo.notifyListeners(SWT.Selection, null);
					}
				});
				targetCombo.setLayoutData(gd);
			}
			{
				Label label = new Label(comp, SWT.NONE);
				label.setText(Messages.getString("DebuggerTab.configBoardLabel"));
				label.setToolTipText(Messages.getString("DebuggerTab.configBoardTooTip"));

				GridData gd = new GridData();
				gd.widthHint = 250;
				gd.horizontalSpan = ((GridLayout) comp.getLayout()).numColumns - 1;
				targetBoardCombo = new Combo(comp, SWT.SINGLE | SWT.BORDER);
				targetBoardCombo.setItems(parser.getBoardsConfigs(selectedTarget).keySet().toArray(new String[0]));
				boardConfigsMap = parser.getBoardsConfigs(selectedTarget);

				targetBoardCombo.select(
						new DefaultBoardProvider().getIndexOfDefaultBoard(selectedTarget, targetBoardCombo.getItems()));
				targetBoardCombo.setLayoutData(gd);
			}
		}
	}

	@Override
	public boolean close()
	{
		boolean returnValue = super.close();
		if (openOcd != null)
		{
			if (openOcd.openOcdProcess != null)
			{
				openOcd.openOcdProcess.destroyForcibly();
			}
		}
		return returnValue;
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

	private class GcovDumpButtonSelectionAdapter extends SelectionAdapter
	{
		private boolean isHardCodeDump;
		private Thread openOcdThread;

		private GcovDumpButtonSelectionAdapter(boolean isHardCodeDump)
		{
			this.isHardCodeDump = isHardCodeDump;
		}

		@Override
		public void widgetSelected(SelectionEvent e)
		{
			try
			{
				startOpenOcd();
			}
			catch (Exception e1)
			{
				Logger.log(e1);
			}
		}
		

		private void startOpenOcd() throws Exception
		{
			if (openOcd == null)
			{
				openOcd = new OpenOcdProcess();
			}
			openOcd.isHardCodeDump = isHardCodeDump;
			if (openOcdThread != null && openOcdThread.getState() != State.TERMINATED)
			{
				openOcd.openOcdProcess.destroy();
			}
			
			openOcdThread = new Thread(openOcd);
			openOcdThread.start();	
		}
	}

	private class OpenOcdProcess extends Thread
	{
		private List<String> openOcdArguments;
		private Process openOcdProcess;
		private ProcessBuilder processBuilder;
		private boolean isHardCodeDump;
		
		private OpenOcdProcess()
		{
			processBuilder = new ProcessBuilder();
		}
		
		@Override
		public void run()
		{
			loadArgsForOpenOcd();
			try
			{
				processBuilder.command(openOcdArguments);
				processBuilder.redirectErrorStream(true);
				IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
				processBuilder.directory(project.getLocation().toFile());
				processBuilder.environment().putAll(idfEnvironmentVariables.getEnvMap());
				logQueue.add("Running openocd: " + openOcdArguments);
				openOcdProcess = processBuilder.start();
				try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(openOcdProcess.getInputStream())))
				{
					String line;
					while ((line = reader.readLine()) != null)
					{
						logQueue.add(line);
					}
				}
				catch (Exception e)
				{
					Logger.log(e);
				}
			}
			catch (Exception e)
			{
				Logger.log(e);
			}
		}
		
		@SuppressWarnings("unchecked") //$NON-NLS-1$
		private void loadArgsForOpenOcd()
		{
			openOcdArguments = new ArrayList<>();
			Display.getDefault().syncExec(() -> {
				String openOcdExecutable = fDefaultPreferences.getGdbServerExecutable();
				openOcdExecutable = DynamicVariableResolver.resolveAll(openOcdExecutable, project);
				String selectedVoltage = flashVoltageCombo.getText();
				String selectedItem = targetBoardCombo.getText();
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
				}
				openOcdArguments.add(openOcdExecutable);
				openOcdArguments.addAll(
						Arrays.asList(DynamicVariableResolver.resolveAll(configOptionString, project).split(" ")));
				
				openOcdArguments.add("-c"); //$NON-NLS-1$
				openOcdArguments.add("init"); //$NON-NLS-1$
				
				openOcdArguments.add("-c"); //$NON-NLS-1$
				if (isHardCodeDump)
				{
					openOcdArguments.add(HARD_CODED_DUMP_CMD);
				}
				else 
				{
					openOcdArguments.add(INSTANT_RUN_TIME_DUMP_CMD);	
				}
				
				Logger.log(openOcdArguments.toString());
			});

		}
	}
}
