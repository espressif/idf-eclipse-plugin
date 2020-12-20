/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.templates;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class TemplateListSelectionPage extends AbstractTemplatesSelectionPage
{
	private Button fUseTemplate;
	private ITemplateNode fInitialTemplateId;

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

	@Override
	public boolean isPageComplete()
	{
		return true; // will always to finish the page without template selection also
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

}
