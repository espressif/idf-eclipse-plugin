package com.espressif.idf.ui.tracing;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.ui.UIPlugin;



public class AppLvlTracingDialog extends TitleAreaDialog {
	
	private Text outFilePath;
	private Spinner pollTimer;
	private Spinner traceSize;
	private Spinner stopTmo;
	private Button waitForHalt;
	private Spinner skipSize;
	private Button browseBtn;
	private TclClient tclClient;
	
	private String pathToProject;
	private Button browseParseScriptBtn;
	private Text openocdLog;
	private Button startParseBtn;
	private MessageConsoleStream console;
	private Text parseScritPath;
	private String elfFilePath;
	private Text parseCommandTxt;
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public AppLvlTracingDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(Messages.AppLvlTracingDialog_Title);
		setTitleImage(UIPlugin.getImage("icons/espressif_logo.png")); //$NON-NLS-1$
		setMessage(Messages.AppLvlTracingDialog_Description);
		Composite area = (Composite) super.createDialogArea(parent);

		Composite container = new Composite(area, SWT.NONE);
		GridData gdContainer = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		container.setLayoutData(gdContainer);
		container.setLayout(new GridLayout(4, false));
		
		Label pollTimerLbl = new Label(container, SWT.NONE);
		pollTimerLbl.setText(Messages.AppLvlTracing_PollPeriod);
		pollTimer = new Spinner(container, SWT.BORDER);
		pollTimer.setMinimum(0);
		pollTimer.setMaximum(Integer.MAX_VALUE);
		Composite unitsOneLblComp = new Composite(container, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		unitsOneLblComp.setLayout(layout);
		Label pollTimerUnitsLbl = new Label(unitsOneLblComp, SWT.NONE);
		waitForHalt = new Button(unitsOneLblComp, SWT.CHECK);
		waitForHalt.setText(Messages.AppLvlTracing_WaitForHalt);
		pollTimerUnitsLbl.setText(ITracingConstants.UNIT_SECONDS); 
		

		new Label(container, SWT.NONE);
		Label traceSizeLbl = new Label(container, SWT.NONE);
		traceSizeLbl.setText(Messages.AppLvlTracing_MaxTraceSize);
		traceSize = new Spinner(container, SWT.BORDER);
		traceSize.setMinimum(-1);
		traceSize.setMaximum(Integer.MAX_VALUE);
		traceSize.setSelection(-1);
		Label traceSizeUnitsLbl = new Label(container, SWT.NONE);
		traceSizeUnitsLbl.setText(ITracingConstants.UNIT_BYTES);
		
		new Label(container, SWT.NONE);
		Label timeoutLbl = new Label(container, SWT.NONE);
		timeoutLbl.setText(Messages.AppLvlTracing_StopTmo);
		stopTmo = new Spinner(container, SWT.BORDER);
		stopTmo.setMinimum(-1);
		stopTmo.setMaximum(Integer.MAX_VALUE);
		stopTmo.setSelection(-1);
		Label timeoutUnitsLbl = new Label(container, SWT.NONE);
		timeoutUnitsLbl.setText(ITracingConstants.UNIT_SECONDS);
		
		new Label(container, SWT.NONE);
		Label bytesToSkipLbl = new Label(container, SWT.NONE);
		bytesToSkipLbl.setText(Messages.AppLvlTracing_BytesToSKip);
		skipSize = new Spinner(container, SWT.BORDER);
		skipSize.setMinimum(0);
		skipSize.setMaximum(Integer.MAX_VALUE);
		Label bytesUnitsLbl = new Label(container, SWT.NONE);
		bytesUnitsLbl.setText(ITracingConstants.UNIT_BYTES);
		
		new Label(container, SWT.NONE);
		Label outFileLbl = new Label(container, SWT.NONE);
		outFileLbl.setText(Messages.AppLvlTracing_OutFile);
		outFilePath = new Text(container, SWT.BORDER);
		outFilePath.setText(pathToProject);
		GridData gdOutFile = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		outFilePath.setLayoutData(gdOutFile);
		browseBtn = new Button(container, SWT.NONE);
		browseBtn.setText(Messages.AppLvlTracingDialog_Browse);
		browseBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browseButtonSelected(Messages.AppLvlTracingDialog_OutputFileDirLbl,
						outFilePath);
			}
		});

		Label parseDumpFileLbl = new Label(container, SWT.NONE);
		parseDumpFileLbl.setText(Messages.AppLvlTracing_TraceScript);
		parseScritPath = new Text(container, SWT.BORDER);
		GridData gdParseScriptPath = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		parseScritPath.setLayoutData(gdParseScriptPath);
		parseScritPath.setText(
				IDFUtil.getIDFPath() + File.separator + "tools" + File.separator + "esp_app_trace" + File.separator
				+ "logtrace_proc.py"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		browseParseScriptBtn = new Button(container, SWT.NONE);
		browseParseScriptBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(getParentShell(), SWT.OPEN);
				fd.setText(Messages.AppLvlTracing_ScriptBrowseLbl); // $NON-NLS-1$
				fd.setFilterExtensions(new String[] { ".py" }); //$NON-NLS-1$
				parseScritPath.setText(fd.open());
			}
		});
		browseParseScriptBtn.setText(Messages.AppLvlTracingDialog_Browse);

		Label startParseLbl = new Label(container, SWT.NONE);
		startParseLbl.setText(Messages.AppLvlTracing_StartParsingCommandLbl);
		parseCommandTxt = new Text(container, SWT.BORDER | SWT.H_SCROLL);
		GridData gdParseCommandTxt = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		gdParseCommandTxt.widthHint = 500;
		parseCommandTxt.setLayoutData(gdParseCommandTxt);
		parseCommandTxt.setText(getDefaultParseCommand());
		startParseBtn = new Button(container, SWT.NONE);
		startParseBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				activateTracingConsoleView();
				runTraceCommand();

			}
		});
		startParseBtn.setText(Messages.AppLvlTracing_StartParse);

		new Label(container, SWT.NONE);
		openocdLog = new Text(container, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
		openocdLog.setText("Output will appear here"); //$NON-NLS-1$
		GridData gdOpenocdLog = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		gdOpenocdLog.widthHint = 500;
		gdOpenocdLog.heightHint = 306;
		openocdLog.setLayoutData(gdOpenocdLog);

		return area;

	}

	private void runTraceCommand()
	{
		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		try
		{
			List<String> arguments = new ArrayList<String>(Arrays.asList(parseCommandTxt.getText().split(" "))); //$NON-NLS-1$
			Map<String, String> environment = new HashMap<>(System.getenv());
			IStatus status = processRunner.runInBackground(arguments, Path.ROOT, environment);
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(), IDFCorePlugin.errorStatus("Status can't be null", null)); //$NON-NLS-1$
				return;
			}

			console.println(status.getMessage());
			console.println();

		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);

		}
	}

	private String getDefaultParseCommand()
	{
		if (parseScritPath == null || outFilePath == null || elfFilePath == null)
		{
			return ""; //$NON-NLS-1$
		}
		List<String> arguments = new ArrayList<String>();
		arguments.add(IDFUtil.getIDFPythonEnvPath());
		arguments.add(parseScritPath.getText());
		arguments.add(outFilePath.getText().replace("file://", "")); //$NON-NLS-1$ //$NON-NLS-2$
		arguments.add(elfFilePath);
		return String.join(" ", arguments); //$NON-NLS-1$
	}

	private void activateTracingConsoleView()
	{
		MessageConsole msgConsole = findConsole(Messages.AppLvlTracing_ConsoleName);
		msgConsole.clearConsole();
		console = msgConsole.newMessageStream();
		msgConsole.activate();
		openConsoleView();
	}

	private MessageConsole findConsole(String name)
	{
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
		{
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		}
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}


	private void openConsoleView()
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

	private void browseButtonSelected(String title, Text text) {
		DirectoryDialog dialog = new DirectoryDialog(getParentShell(), SWT.NONE);
		dialog.setText(title);
		String str = text.getText().trim();
		int lastSeparatorIndex = str.lastIndexOf(File.separator);
		if (lastSeparatorIndex != -1)
			dialog.setFilterPath(str.substring(0, lastSeparatorIndex));
		str = dialog.open();
		str = str.replace(File.separator, "/"); //$NON-NLS-1$
		str = wrapOutputFilePath(str);
		outFilePath.setText(str);
	}
	
	private String wrapOutputFilePath(String baseFilePath) {
		baseFilePath = baseFilePath + "/trace.log"; //$NON-NLS-1$
		baseFilePath = baseFilePath.replace("/", "//"); //$NON-NLS-1$ //$NON-NLS-2$
		baseFilePath = "file://" + baseFilePath; //$NON-NLS-1$
		return baseFilePath;
	}
	
	public void setProjectPath(IResource project) {
		pathToProject = project.getLocation().toString();

		elfFilePath = project.getProject().getFolder("build").getFile(project.getName().concat(".elf")).getLocation() //$NON-NLS-1$ //$NON-NLS-2$
				.toString();
		pathToProject = wrapOutputFilePath(pathToProject);

	}
	
	@Override
	protected void okPressed()
	{
	}
	
	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button button = createButton(parent, IDialogConstants.OK_ID, ITracingConstants.START_LABEL, true);
		button.addListener(SWT.Selection, new Listener() {

			private ClientWorker clientWorker;

			@Override
			public void handleEvent(Event event)
			{

				if (button.getText().contentEquals(ITracingConstants.START_LABEL)) {
					String waitForHaltStringValue = waitForHalt.getSelection() ? "1" : "0"; //$NON-NLS-1$ //$NON-NLS-2$
					tclClient = new TclClient();
					tclClient.startTracing(new String[] { outFilePath.getText(), pollTimer.getText(),
							traceSize.getText(), stopTmo.getText(), waitForHaltStringValue,
							skipSize.getText() });
					openocdLog.setText(""); //$NON-NLS-1$
					clientWorker = new ClientWorker(tclClient, openocdLog);
					Thread thread = new Thread(clientWorker);
					thread.start();
					button.setText(ITracingConstants.STOP_LABEL);
				} else {
					tclClient.stopTracing();
					Thread thread = new Thread(clientWorker);
					thread.start();
					button.setText(ITracingConstants.START_LABEL);
				}
				
			}
		});
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(800, 700);
	}

	@Override
	protected boolean isResizable()
	{
		return true;
	}
}
