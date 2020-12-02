/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.templates;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.io.IOException;

import org.eclipse.core.internal.resources.LocalMetaArea;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

import com.espressif.idf.ui.wizard.WizardNewProjectCreationPage;


/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class TemplateListSelectionPage extends AbstractTemplatesSelectionPage
{
	private Text projectNameField;
	private Button fUseTemplate;
	private ITemplateNode fInitialTemplateId;
	private WizardNewProjectCreationPage mainPage;
	private boolean isPageCreated = false; 
	/**
	 * Constructor
	 * 
	 * @param templateNodes a list of ITemplateNode objects
	 * @param message       message to provide to the user
	 */
	public TemplateListSelectionPage(ITemplateNode templateNodes, String message)
	{
		super(templateNodes, message);
		setTitle(Messages.TemplateListSelectionPage_Templates);
		setDescription(Messages.TemplateListSelectionPage_Templates_Desc);
	}

	@Override
	public void createAbove(Composite container, int span)
	{
		Composite group = new Composite(container, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		Label projectNameLabel = new Label(group, SWT.NONE);
		projectNameLabel.setText(IDEWorkbenchMessages.WizardNewProjectCreationPage_nameLabel);
		
		projectNameField = new Text(group, SWT.BORDER);
		projectNameField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		//group.setVisible(false);
		projectNameField.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				mainPage.changeProjectNameFromTemplatePage(projectNameField.getText());
				setErrorMessage(mainPage.getErrorMessage());
				setMessage(mainPage.getMessage());
				if(getErrorMessage() == null || getErrorMessage().isEmpty()) {
					setPageComplete(true);
					mainPage.setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}
		});
		
		fUseTemplate = new Button(container, SWT.CHECK);
		fUseTemplate.setText(Messages.TemplateListSelectionPage_SelectTemplate_Desc);
		GridData gd = new GridData();
		gd.horizontalSpan = span;
		fUseTemplate.setLayoutData(gd);
		fUseTemplate.addSelectionListener(widgetSelectedAdapter(e -> {
			//group.setVisible(true);
			templateViewer.getControl().setEnabled(fUseTemplate.getSelection());
			if (!fUseTemplate.getSelection())
				setDescription(""); //$NON-NLS-1$
			else
				setDescription(Messages.TemplateListSelectionPage_Template_Wizard_Desc);

			setDescriptionEnabled(fUseTemplate.getSelection());
			getContainer().updateButtons();
		}));
		fUseTemplate.setSelection(false);
		isPageCreated = true;
	}

	@Override
	protected void initializeViewer()
	{
		if (getInitialTemplateId() != null)
			try
			{
				selectInitialTemplate();
			} catch (IOException e)
			{
				e.printStackTrace(); //TODO log the exception
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

	public boolean isCreated() 
	{
		return isPageCreated;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) 
	{
		super.selectionChanged(event);
	}
	
	@Override
	public void setProjectName(String projectName) 
	{
		if (!projectName.equals(this.projectNameField.getText())) 
		{
			this.projectNameField.setText(projectName);
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
		if (visible)
		{
			if (fUseTemplate.getSelection() == false)
				templateViewer.getControl().setEnabled(false);
			fUseTemplate.setEnabled(true);
			templateViewer.refresh();
		}
		super.setVisible(visible);
	}
	
	public void setMainPage(WizardNewProjectCreationPage mainPage) 
	{
		this.mainPage = mainPage;
	}
}
