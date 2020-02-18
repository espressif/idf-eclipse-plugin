/*******************************************************************************
 * Copyright (c) 2010, 2013 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Doug Schaefer (WRS) - Initial API and implementation
 *******************************************************************************/
package com.espressif.idf.ui.wizard;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * 
 * Import an existing IDF Project
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ImportIDFProjectWizardPage extends WizardPage
{

	private Text projectName;
	private Text location;
	private IWorkspaceRoot root;

	/**
	 * True if the user entered a non-empty string in the project name field. In that state, we avoid automatically
	 * filling the project name field with the directory name (last segment of the location) he has entered.
	 */
	boolean projectNameSetByUser;
	private Button copyCheckbox;
	private boolean copyProject;

	protected ImportIDFProjectWizardPage()
	{
		super(Messages.ImportIDFProjectWizardPage_0);
		setTitle(Messages.ImportIDFProjectWizardPage_1);
		setDescription(Messages.ImportIDFProjectWizardPage_2);

		root = ResourcesPlugin.getWorkspace().getRoot();
	}

	@Override
	public void createControl(Composite parent)
	{
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		addProjectNameSelector(comp);
		addSourceSelector(comp);
		setControl(comp);
		createOptions(comp);
	}

	public void createOptions(Composite comp)
	{
		copyCheckbox = new Button(comp, SWT.CHECK);
		copyCheckbox.setText(Messages.ImportIDFProjectWizardPage_CopyIntoWorkspace);
		copyCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		copyCheckbox.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				copyProject = copyCheckbox.getSelection();
			}
		});

	}

	public void addProjectNameSelector(Composite parent)
	{
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setText(Messages.ImportIDFProjectWizardPage_3);

		projectName = new Text(group, SWT.BORDER);
		projectName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		projectName.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				validatePage();
				if (getProjectName().isEmpty())
				{
					projectNameSetByUser = false;
				}
			}
		});

		// Note that the modify listener gets called not only when the user enters text but also when we
		// programatically set the field. This listener only gets called when the user modifies the field
		projectName.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				projectNameSetByUser = true;
			}
		});
	}

	/**
	 * Validates the contents of the page, setting the page error message and Finish button state accordingly
	 *
	 */
	protected void validatePage()
	{
		// Don't generate an error if project name or location is empty, but do disable Finish button.
		String msg = null;
		boolean complete = true; // ultimately treated as false if msg != null

		String name = getProjectName();
		if (name.isEmpty())
		{
			complete = false;
		}
		else
		{
			IStatus status = ResourcesPlugin.getWorkspace().validateName(name, IResource.PROJECT);
			if (!status.isOK())
			{
				msg = status.getMessage();
			}
			else
			{
				IProject project = root.getProject(name);
				if (project.exists())
				{
					msg = Messages.ImportIDFProjectWizardPage_4;

				}
			}
		}
		if (msg == null)
		{
			String loc = getLocation();
			if (loc.isEmpty())
			{
				complete = false;
			}
			else
			{
				final File file = new File(loc);
				if (file.isDirectory())
				{
					// Ensure we can create files in the directory.
					if (!file.canWrite())
						msg = Messages.ImportIDFProjectWizardPage_5;
					// Set the project name to the directory name but not if the user has supplied a name
					// (bugzilla 368987). Use a job to ensure proper sequence of activity, as setting the Text
					// will invoke the listener, which will invoke this method.
					else if (!projectNameSetByUser && !name.equals(file.getName()))
					{
						WorkbenchJob wjob = new WorkbenchJob("update project name") //$NON-NLS-1$
						{
							@Override
							public IStatus runInUIThread(IProgressMonitor monitor)
							{
								if (!projectName.isDisposed())
								{
									projectName.setText(file.getName());
								}
								return Status.OK_STATUS;
							}
						};
						wjob.setSystem(true);
						wjob.schedule();
					}
				}
				else
				{
					msg = Messages.ImportIDFProjectWizardPage_6;
				}
			}
		}

		setErrorMessage(msg);
		setPageComplete((msg == null) && complete);
	}

	/** @deprecated Replaced by {@link #validatePage()} */
	@Deprecated
	public void validateProjectName()
	{
		validatePage();
	}

	public void addSourceSelector(Composite parent)
	{
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setText(Messages.ImportIDFProjectWizardPage_7);

		location = new Text(group, SWT.BORDER);
		location.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		location.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				validatePage();
			}
		});
		validatePage();

		Button browse = new Button(group, SWT.NONE);
		browse.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		browse.setText(Messages.ImportIDFProjectWizardPage_8);
		browse.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				DirectoryDialog dialog = new DirectoryDialog(location.getShell());
				dialog.setMessage(Messages.ImportIDFProjectWizardPage_9);
				String dir = dialog.open();
				if (dir != null)
					location.setText(dir);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
			}
		});
	}

	/** @deprecated Replaced by {@link #validatePage()} */
	@Deprecated
	void validateSource()
	{
		validatePage();
	}

	public String getProjectName()
	{
		return projectName.getText().trim();
	}

	public String getLocation()
	{
		return location.getText().trim();
	}

	public boolean canCopyIntoWorkspace()
	{
		return copyProject;
	}

}
