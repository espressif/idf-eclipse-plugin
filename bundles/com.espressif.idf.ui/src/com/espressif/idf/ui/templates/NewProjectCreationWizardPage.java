/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.templates;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.io.IOException;
import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea.IErrorMessageReporter;

import com.espressif.idf.core.util.IDFUtil;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 */
@SuppressWarnings("restriction")
public class NewProjectCreationWizardPage extends AbstractTemplatesSelectionPage
{
	private Text projectNameField;
	private Button fUseTemplate;
	private ITemplateNode fInitialTemplateId;
	private ProjectContentsLocationArea locationArea;
	// initial value stores
	private String initialProjectFieldValue;

	/**
	 * Constructor
	 * 
	 * @param templateNodes a list of ITemplateNode objects
	 * @param message       message to provide to the user
	 */
	public NewProjectCreationWizardPage(ITemplateNode templateNodes, String message)
	{
		super(templateNodes, message);
		setTitle(Messages.NewProjectWizardPage_Header);
		setDescription(Messages.NewProjectWizardPage_DescriptionString);
	}

	@Override
	public void createAbove(Composite container, int span)
	{
		createProjectNameGroup(container);
		if (this.templateElements == null || this.templateElements.getChildren().isEmpty())
		{
			Label label = new Label(container, SWT.NONE);
			label.setText(Messages.NewProjectWizardPage_NoTemplateFoundMessage);
			return;
		}
		
		fUseTemplate = new Button(container, SWT.CHECK);
		
		fUseTemplate.setText(Messages.TemplateListSelectionPage_SelectTemplate_Desc);
		GridData gd = new GridData();
		gd.horizontalSpan = span;
		fUseTemplate.setLayoutData(gd);
		fUseTemplate.addSelectionListener(widgetSelectedAdapter(e -> {
			templateViewer.getControl().setEnabled(fUseTemplate.getSelection());
			filteredTree.setEnabled(fUseTemplate.getSelection());
			if (!fUseTemplate.getSelection())
				setDescription(""); //$NON-NLS-1$
			else
				setDescription(Messages.TemplateListSelectionPage_Template_Wizard_Desc);

			setDescriptionEnabled(fUseTemplate.getSelection());
			getContainer().updateButtons();
		}));
		fUseTemplate.setSelection(false);
	}

	private void createProjectNameGroup(Composite container)
	{
		Composite mainComposite = new Composite(container, SWT.NONE);
		mainComposite.setLayout(new GridLayout());
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite projectNameGroup = new Composite(mainComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		projectNameGroup.setLayout(layout);
		projectNameGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		Label projectNameLabel = new Label(projectNameGroup, SWT.NONE);
		projectNameLabel.setText(IDEWorkbenchMessages.WizardNewProjectCreationPage_nameLabel);
		
		
		projectNameField = new Text(projectNameGroup, SWT.BORDER);
		projectNameField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		projectNameField.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				if (getErrorMessage() == null || getErrorMessage().isEmpty())
				{
					setPageComplete(true);
				}
				else
				{
					setPageComplete(false);
				}
			}
		});
		
		
		locationArea = new ProjectContentsLocationArea(getErrorReporter(), mainComposite);
		if (initialProjectFieldValue != null)
		{
			locationArea.updateProjectName(initialProjectFieldValue);
		}
	}
	
	/**
	 * Get an error reporter for the receiver.
	 * 
	 * @return IErrorMessageReporter
	 */
	private IErrorMessageReporter getErrorReporter()
	{
		return (errorMessage, infoOnly) -> {
			if (infoOnly)
			{
				setMessage(errorMessage, IStatus.INFO);
				setErrorMessage(null);
			}
			else
				setErrorMessage(errorMessage);
			boolean valid = errorMessage == null;
			if (valid)
			{
				valid = validatePage();
			}

			setPageComplete(valid);
		};
	}
	
	/**
	 * Returns the useDefaults.
	 * 
	 * @return boolean
	 */
	public boolean useDefaults()
	{
		return locationArea.isDefault();
	}
	
	public IPath getLocationPath()
	{
		return new Path(locationArea.getProjectLocation());
	}
	
	public URI getLocationURI()
	{
		return locationArea.getProjectLocationURI();
	}
	
	/**
	 * Sets the initial project name that this page will use when created. The name is ignored if the
	 * createControl(Composite) method has already been called. Leading and trailing spaces in the name are ignored.
	 * Providing the name of an existing project will not necessarily cause the wizard to warn the user. Callers of this
	 * method should first check if the project name passed already exists in the workspace.
	 *
	 * @param name initial project name for this page
	 *
	 * @see IWorkspace#validateName(String, int)
	 *
	 */
	public void setInitialProjectName(String name)
	{
		if (name == null)
		{
			initialProjectFieldValue = null;
		}
		else
		{
			initialProjectFieldValue = name.trim();
			if (locationArea != null)
			{
				locationArea.updateProjectName(name.trim());
			}
		}
	}
	
	/**
	 * Set the location to the default location if we are set to useDefaults.
	 */
	void setLocationForSelection()
	{
		locationArea.updateProjectName(getProjectNameFieldValue());
	}

	
	/**
	 * Returns the value of the project name field with leading and trailing spaces removed.
	 *
	 * @return the project name in the field
	 */
	private String getProjectNameFieldValue()
	{
		if (projectNameField == null)
		{
			return ""; //$NON-NLS-1$
		}

		return projectNameField.getText().trim();
	}
	
	protected boolean validatePage()
	{
		IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();

		String worspaceLocation = workspace.getRoot().getLocation().toOSString();
		if (!useDefaults())
		{
			worspaceLocation = locationArea.getProjectLocation();
		}

		if (!IDFUtil.checkIfIdfSupportsSpaces() && worspaceLocation.contains(" ")) //$NON-NLS-1$
		{
			setErrorMessage(com.espressif.idf.ui.wizard.Messages.WizardNewProjectCreationPage_WorkspaceLocCantIncludeSpaceErr);
			return false;
		}

		String projectFieldContents = getProjectNameFieldValue();
		if (projectFieldContents.equals("")) //$NON-NLS-1$
		{
			setErrorMessage(null);
			setMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectNameEmpty);
			return false;
		}

		if (!IDFUtil.checkIfIdfSupportsSpaces() && projectFieldContents.contains(" ")) //$NON-NLS-1$
		{
			setErrorMessage(com.espressif.idf.ui.wizard.Messages.WizardNewProjectCreationPage_NameCantIncludeSpaceErr);
			return false;
		}

		IStatus nameStatus = workspace.validateName(projectFieldContents, IResource.PROJECT);
		if (!nameStatus.isOK())
		{
			setErrorMessage(nameStatus.getMessage());
			return false;
		}

		IProject handle = getProjectHandle();
		if (handle.exists())
		{
			setErrorMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectExistsMessage);
			return false;
		}

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectNameFieldValue());
		locationArea.setExistingProject(project);

		String validLocationMessage = locationArea.checkValidLocation();
		if (validLocationMessage != null)
		{ // there is no destination location given
			setErrorMessage(validLocationMessage);
			return false;
		}

		setErrorMessage(null);
		setMessage(null);
		return true;
	}

	/**
	 * Creates a project resource handle for the current project name field value. The project handle is created
	 * relative to the workspace root.
	 * <p>
	 * This method does not create the project resource; this is the responsibility of <code>IProject::create</code>
	 * invoked by the new project resource wizard.
	 * </p>
	 *
	 * @return the new project resource handle
	 */
	public IProject getProjectHandle()
	{
		return ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
	}
	
	/**
	 * Returns the current project name as entered by the user, or its anticipated initial value.
	 *
	 * @return the project name, its anticipated initial value, or <code>null</code> if no project name is known
	 */
	public String getProjectName()
	{
		if (projectNameField == null)
		{
			return initialProjectFieldValue;
		}

		return getProjectNameFieldValue();
	}
	
	@Override
	public boolean isPageComplete()
	{
		return true; // will always to finish the page without template selection also
	}

	@Override
	protected void initializeViewer()
	{
		if (getInitialTemplateId() != null)
			try
			{
				selectInitialTemplate();
			}
			catch (IOException e)
			{
				e.printStackTrace(); // TODO log the exception
			}
	}

	/**
	 * Initialize the template viewer selection with default template id - eg: hello_world idf example
	 * 
	 * @throws IOException
	 */
	private void selectInitialTemplate() throws IOException
	{
		templateViewer.reveal(getInitialTemplateId());
		templateViewer.setSelection(new StructuredSelection(getInitialTemplateId()), true);
		String description = new TemplatesManager().getDescription(getInitialTemplateId());
		setDescriptionText(description);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event)
	{
		super.selectionChanged(event);
		ITemplateNode selectedElement = getSelection();
		if (selectedElement != null)
		{
			String projectName = new TemplatesManager().getProjectName(getSelection());
			projectNameField.setText(projectName);
		}
	}

	@Override
	public boolean canFlipToNextPage()
	{
		return false; // there is nothing more
	}

	/**
	 * @return ITemplateNode selection from the tree viewer.
	 */
	public ITemplateNode getSelection()
	{
		IStructuredSelection ssel = templateViewer.getStructuredSelection();
		if (fUseTemplate.getSelection() && ssel != null && !ssel.isEmpty())
		{
			ITemplateNode firstElement = (ITemplateNode) ssel.getFirstElement();
			if (firstElement.getType() == IResource.PROJECT)
			{
				return firstElement;
			}
		}

		return null;
	}

	/**
	 * @return Returns the fInitialTemplateId.
	 */
	public ITemplateNode getInitialTemplateId()
	{
		return fInitialTemplateId;
	}

	/**
	 * @param templateNode The fInitialTemplateId to set.
	 */
	public void setInitialTemplateId(ITemplateNode templateNode)
	{
		fInitialTemplateId = templateNode;
	}

	@Override
	public void setVisible(boolean visible)
	{
		if (visible && fUseTemplate != null )
		{
			if (fUseTemplate.getSelection() == false)
				templateViewer.getControl().setEnabled(false);
			fUseTemplate.setEnabled(true);
			templateViewer.refresh();
		}
		super.setVisible(visible);
	}

}
