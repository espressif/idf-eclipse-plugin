package com.espressif.idf.ui.wizard;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.ui.EclipseUtil;

public class NewComponentWizardPage extends WizardPage
{

	private Text componentName;
	private Composite container;

	protected NewComponentWizardPage(IStructuredSelection selection)
	{
		super(Messages.NewIdfComponentWizard_Page);
		setTitle(Messages.NewIdfComponentWizard_Component_Title);
		setDescription(Messages.NewIdfComponentWizard_CompDesc);
		setControl(componentName);
	}

	public String createIdfComponent()
	{
		List<String> commands = new ArrayList<>();
		commands.add(IDFUtil.getIDFPythonEnvPath());
		commands.add(IDFUtil.getIDFPythonScriptFile().getAbsolutePath());
		commands.add("-C"); //$NON-NLS-1$
		commands.add("components"); //$NON-NLS-1$
		commands.add("create-component"); //$NON-NLS-1$
		commands.add(componentName.getText());
		Map<String, String> envMap = new IDFEnvironmentVariables().getEnvMap();
		return runCommand(commands, envMap);
	}

	private String runCommand(List<String> arguments, Map<String, String> env)
	{
		String exportCmdOp = ""; //$NON-NLS-1$
		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		try
		{
			IProject selectedProject = EclipseUtil.getSelectedProjectInExplorer(); 
			IPath newPath = selectedProject.getLocation();
			IStatus status = processRunner.runInBackground(arguments, newPath, env);
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(), IDFCorePlugin.errorStatus("Status can't be null", null)); //$NON-NLS-1$

			}

			// process export command output
			exportCmdOp = status.getMessage();
			Logger.log(exportCmdOp);
		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);
		}
		return exportCmdOp;
	}

	@Override
	public void createControl(Composite parent)
	{
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		Label label = new Label(container, SWT.NONE);
		label.setText(Messages.NewIdfComponentWizard_Component_name);
		componentName = new Text(container, SWT.BORDER);
		componentName.setText(""); //
		componentName.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				setPageComplete(validatePage());
			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		componentName.setLayoutData(gd);
		setControl(container);
		setPageComplete(false);
	}

	protected boolean validatePage()
	{

		if (componentName.getText().isEmpty()) // $NON-NLS-1$
		{
			setErrorMessage(Messages.NewIdfComponentWizard_NameCantBeEmptyErr);
			return false;
		}

		if (componentName.getText().contains(" ")) //$NON-NLS-1$
		{
			setErrorMessage(Messages.NewIdfComponentWizard_NameCantIncludeSpaceErr);
			return false;
		}
		if (checkIfComponentExists())
		{
			setErrorMessage(Messages.NewIdfComponentWizard_NameAlreadyExistsErr);
			return false;
		}
		setErrorMessage(null);
		return true;
	}

	private boolean checkIfComponentExists()
	{
		IProject selectedProject = EclipseUtil.getSelectedProjectInExplorer();
		IPath newPath = selectedProject.getLocation().append("/components"); //$NON-NLS-1$
		Path absPath = Paths.get(newPath.toString() + "\\" + componentName.getText()); //$NON-NLS-1$
		if (Files.exists(absPath))
		{
			return true;
		}
		return false;
	}
}
