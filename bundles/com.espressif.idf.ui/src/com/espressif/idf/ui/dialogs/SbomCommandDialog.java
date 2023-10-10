package com.espressif.idf.ui.dialogs;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.ide.IDE;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.IDFConsole;
import com.espressif.idf.ui.update.Messages;

public class SbomCommandDialog extends TitleAreaDialog
{

	private static final String DEFAULT_OUTPUT_FILE_NAME = "sbom.txt"; //$NON-NLS-1$
	private static final String ESP_IDF_SBOM_COMMAND_NAME = "esp_idf_sbom"; //$NON-NLS-1$
	protected static final String[] EXTENSIONS = { ".json" }; //$NON-NLS-1$
	private MessageConsoleStream console;
	private Button saveOutputToFileCheckBoxButton;
	private Text outputFileText;
	private IProject selectedProject;
	private Text projectDescriptionPathText;
	private String projectDescription;
	private boolean saveOutputFileStatus;
	private String outputFilePath;

	public SbomCommandDialog(Shell parentShell)
	{
		super(parentShell);
	}

	@Override
	public void create()
	{
		super.create();
		setTitle(Messages.SbomCommandDialog_SbomTitle);
		setMessage(
				Messages.SbomCommandDialog_SbomInfoMsg);
		setDefaults();
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText(Messages.SbomCommandDialog_SbomTitle);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite area = (Composite) super.createDialogArea(parent);

		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(3, false);
		container.setLayout(layout);
		
		saveOutputToFileCheckBoxButton = new Button(container, SWT.CHECK);
		saveOutputToFileCheckBoxButton.setText(Messages.SbomCommandDialog_RedirectOutputCheckBoxLbl);
		saveOutputToFileCheckBoxButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));

		
		Label projectDescriptionPathLbl = new Label(container, SWT.NONE);
		projectDescriptionPathLbl.setText(Messages.SbomCommandDialog_ProjectDescriptionPathLbl);
		projectDescriptionPathText = new Text(container, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL);
		projectDescriptionPathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Button projectDescriptionBtn = new Button(container, SWT.PUSH);
		projectDescriptionBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1));
		projectDescriptionBtn.setText(Messages.SbomCommandDialog_BrowseBtnTxt);
		projectDescriptionBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog fileSelectionDialog = new FileDialog(getParentShell());
				fileSelectionDialog.setFilterExtensions(EXTENSIONS);
				fileSelectionDialog.setFilterPath(buildProjectDescriptionPath());
				String selectedFilePath = fileSelectionDialog.open();

				if (selectedFilePath != null && !selectedFilePath.isEmpty())
				{
					projectDescriptionPathText.setText(selectedFilePath);
				}
				super.widgetSelected(e);
			}
		});
		

		Label outputFileLbl = new Label(container, SWT.NONE);
		outputFileLbl.setText(Messages.SbomCommandDialog_OutputFilePathLbl);
		outputFileText = new Text(container, SWT.NONE);
		outputFileText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Button outputFileBrowseBtn = new Button(container, SWT.PUSH);
		outputFileBrowseBtn.setText(Messages.SbomCommandDialog_BrowseBtnTxt);
		outputFileBrowseBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog fileSelectionDialog = new FileDialog(getParentShell());
				fileSelectionDialog.setFilterPath(buildProjectDescriptionPath());
				String selectedFilePath = fileSelectionDialog.open();

				if (selectedFilePath != null && !selectedFilePath.isEmpty())
				{
					outputFileText.setText(selectedFilePath);
				}
				super.widgetSelected(e);
			}
		});
		saveOutputToFileCheckBoxButton.addListener(SWT.Selection, e -> {
			outputFileLbl.setVisible(saveOutputToFileCheckBoxButton.getSelection());
			outputFileText.setVisible(saveOutputToFileCheckBoxButton.getSelection());
			outputFileBrowseBtn.setVisible(saveOutputToFileCheckBoxButton.getSelection());
			container.requestLayout();
		});
		saveOutputToFileCheckBoxButton.setSelection(false);
		saveOutputToFileCheckBoxButton.notifyListeners(SWT.Selection, null);

		outputFileText.addListener(SWT.Modify, e -> getButton(IDialogConstants.OK_ID).setEnabled(validateInput()));
		projectDescriptionPathText.addListener(SWT.Modify,
				e -> getButton(IDialogConstants.OK_ID).setEnabled(validateInput()));
		return super.createDialogArea(parent);
	}

	protected String runCommand(List<String> arguments, Path workDir, Map<String, String> env)
	{
		String exportCmdOp = StringUtil.EMPTY;
		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		try
		{
			IStatus status = processRunner.runInBackground(arguments, workDir, env);
			if (status == null)
			{
				IStatus errorStatus = IDFCorePlugin.errorStatus(Messages.SbomCommandDialog_StatusCantBeNullErrorMsg, null);
				Logger.log(IDFCorePlugin.getPlugin(), errorStatus);
				return errorStatus.getMessage();
			}

			exportCmdOp = status.getMessage();
			Logger.log(exportCmdOp);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		return exportCmdOp;
	}

	@Override
	protected void okPressed()
	{
		console = new IDFConsole().getConsoleStream(Messages.IDFToolsHandler_ToolsManagerConsole, null, false);
		Job espIdfSbomJob = new Job(Messages.SbomCommandDialog_EspIdfSbomJobName)
		{

			protected IStatus run(IProgressMonitor monitor)
			{
				if (!getEspIdfSbomInstalledStatus())
				{
					installEspIdfSbom();
				}
				runEspIdfSbomCommand();
				return Status.OK_STATUS;
			}
		};
		espIdfSbomJob.schedule();
		projectDescription = projectDescriptionPathText.getText();
		saveOutputFileStatus = saveOutputToFileCheckBoxButton.getSelection();
		outputFilePath = outputFileText.getText();
		super.okPressed();
	}

	@Override
	protected boolean isResizable()
	{
		return true;
	}

	private void setDefaults()
	{
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService()
				.getSelection();
		if (selection instanceof IStructuredSelection)
		{
			Object element = ((IStructuredSelection) selection).getFirstElement();

			if (element instanceof IResource)
			{
				selectedProject = ((IResource) element).getProject();
			}
		}
		
		projectDescriptionPathText
				.setText(buildProjectDescriptionPath());
		if (!Files.exists(Paths.get(projectDescriptionPathText.getText())))
		{
			setMessage(Messages.SbomCommandDialog_ProjectDescDoesntExistDefaultErrorMsg);
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}

		outputFileText.setText(
				selectedProject.getLocationURI().getPath().concat(File.separator).concat(DEFAULT_OUTPUT_FILE_NAME));

	}

	private String buildProjectDescriptionPath()
	{
		return selectedProject.getLocationURI().getPath().concat(File.separator).concat("build") //$NON-NLS-1$
				.concat(File.separator).concat("project_description.json"); //$NON-NLS-1$
	}

	private void runEspIdfSbomCommand()
	{
		Map<String, String> environment = new HashMap<>(System.getenv());
		List<String> arguments = new ArrayList<>();
		final String pythonEnvPath = IDFUtil.getIDFPythonEnvPath();
		arguments.add(pythonEnvPath);
		arguments.add("-m"); //$NON-NLS-1$
		arguments.add(ESP_IDF_SBOM_COMMAND_NAME);
		arguments.add("create"); //$NON-NLS-1$
		arguments.add(projectDescription);
		if (saveOutputFileStatus)
		{
			arguments.add("--output-file"); //$NON-NLS-1$
			arguments.add(outputFilePath);
		}
		String cmdOutput = runCommand(arguments, null, environment);
		cmdOutput = cmdOutput.isEmpty() && saveOutputFileStatus
				? String.format(Messages.SbomCommandDialog_ConsoleRedirectedOutputFormatString, outputFilePath)
				: cmdOutput;
		console.getConsole().addPatternMatchListener(getPatternMatchListener());
		console.println(cmdOutput);

	}

	private IPatternMatchListener getPatternMatchListener()
	{
		return new IPatternMatchListener()
		{

			public void matchFound(PatternMatchEvent event)
			{

				try
				{
					IHyperlink hepHyperlink = createHyperlinkWhichOpensFileInEditor();
					console.getConsole().addHyperlink(hepHyperlink, event.getOffset(), event.getLength());
				}
				catch (BadLocationException e)
				{
					Logger.log(e);
				}
			}

			private IHyperlink createHyperlinkWhichOpensFileInEditor()
			{
				return new IHyperlink()
				{
					public void linkActivated()
					{
						IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(outputFilePath));
						if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists())
						{
							IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
							try
							{
								IDE.openEditorOnFileStore(page, fileStore);
							}
							catch (PartInitException e)
							{
								Logger.log(e);
							}
						}

					}

					public void linkEntered()
					{
					}

					public void linkExited()
					{
					}
				};
			}

			public void disconnect()
			{
			}

			public void connect(TextConsole console)
			{
			}

			public String getPattern()
			{
				return outputFilePath;
			}

			public String getLineQualifier()
			{
				return null;
			}

			public int getCompilerFlags()
			{
				return 0;
			}
		};
	}

	private boolean validateInput()
	{
		boolean validateStatus = true;
		if (!Files.exists(Paths.get(projectDescriptionPathText.getText())))
		{
			validateStatus = false;
			setErrorMessage(Messages.SbomCommandDialog_ProjectDescDoesntExistsErrorMsg);
		}

		if (saveOutputToFileCheckBoxButton.getSelection()
				&& checkIfFileIsNotWritable(Paths.get(outputFileText.getText())))
		{
			validateStatus = false;
			setErrorMessage(Messages.SbomCommandDialog_OutputFileNotWritabbleErrorMsg);
		}

		if (validateStatus)
		{
			setErrorMessage(null);
		}
		return validateStatus;
	}

	private boolean checkIfFileIsNotWritable(java.nio.file.Path pathToFile)
	{
		return Files.exists(pathToFile) && !Files.isWritable(pathToFile);
	}

	private void installEspIdfSbom()
	{
		Map<String, String> environment = new HashMap<>(System.getenv());
		List<String> arguments = new ArrayList<>();
		final String pythonEnvPath = IDFUtil.getIDFPythonEnvPath();
		arguments.add(pythonEnvPath);
		arguments.add("-m"); //$NON-NLS-1$
		arguments.add("pip"); //$NON-NLS-1$
		arguments.add("install"); //$NON-NLS-1$
		arguments.add(ESP_IDF_SBOM_COMMAND_NAME);
		String cmdOutput = runCommand(arguments, null, environment);
		console.println(cmdOutput);

	}

	private boolean getEspIdfSbomInstalledStatus()
	{
		Map<String, String> environment = new HashMap<>(System.getenv());
		List<String> arguments = new ArrayList<>();
		final String pythonEnvPath = IDFUtil.getIDFPythonEnvPath();
		arguments.add(pythonEnvPath);
		arguments.add("-m"); //$NON-NLS-1$
		arguments.add("pip"); //$NON-NLS-1$
		arguments.add("list"); //$NON-NLS-1$
		String cmdOutput = runCommand(arguments, null, environment);
		return cmdOutput.contains("esp-idf-sbom"); //$NON-NLS-1$
	}
}
