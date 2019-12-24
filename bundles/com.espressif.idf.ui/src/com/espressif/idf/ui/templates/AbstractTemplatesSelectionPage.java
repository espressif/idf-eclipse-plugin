/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.templates;

import java.io.IOException;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.espressif.idf.core.util.StringUtil;

/**
 * IDF Templates selection wizard page
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public abstract class AbstractTemplatesSelectionPage extends BaseWizardSelectionPage implements IExecutableExtension
{
	protected TreeViewer templateViewer;
	protected ITemplateNode templateElements;
	private WizardSelectedAction doubleClickAction = new WizardSelectedAction();

	private class WizardSelectedAction extends Action
	{
		public WizardSelectedAction()
		{
			super("wizardSelection"); //$NON-NLS-1$
		}

		@Override
		public void run()
		{
			selectionChanged(new SelectionChangedEvent(templateViewer, templateViewer.getStructuredSelection()));
			advanceToNextPage();
		}
	}

	public AbstractTemplatesSelectionPage(ITemplateNode wizardElements, String message)
	{
		super("ListSelection", message); //$NON-NLS-1$
		this.templateElements = wizardElements;
	}

	public void advanceToNextPage()
	{
		getContainer().showPage(getNextPage());
	}

	public ITemplateNode getWizardElements()
	{
		return templateElements;
	}

	@Override
	public void createControl(Composite parent)
	{
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 10;
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		createAbove(container, 1);
		Label label = new Label(container, SWT.NONE);
		label.setText(getLabel());
		GridData gd = new GridData();
		label.setLayoutData(gd);

		SashForm sashForm = new SashForm(container, SWT.HORIZONTAL);
		gd = new GridData(GridData.FILL_BOTH);
		// limit the width of the sash form to avoid the wizard
		// opening very wide. This is just preferred size -
		// it can be made bigger by the wizard
		// See bug #83356
		gd.widthHint = 400;
		gd.heightHint = 500;
		sashForm.setLayoutData(gd);

		templateViewer = createTreeViewer(sashForm);
		templateViewer.setContentProvider(new TemplatesContentProvider());
		templateViewer.setLabelProvider(new TemplatesLabelProvider());
		templateViewer.setComparator(getViewerComparator());
		templateViewer.addDoubleClickListener(event -> doubleClickAction.run());
		createDescriptionIn(sashForm);
		createBelow(container, 1);
		templateViewer.setInput(templateElements);
		initializeViewer();
		templateViewer.addSelectionChangedListener(this);

		Dialog.applyDialogFont(container);
		setControl(container);
	}

	/**
	 * Creates the template tree viewer
	 * 
	 * @param templatesGroup
	 * @return template viewer
	 */
	private TreeViewer createTreeViewer(Composite templatesGroup)
	{
		int style = SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER;
		final TreeViewer treeViewer = new TreeViewer(templatesGroup, style);
		treeViewer.setContentProvider(new TemplatesContentProvider());
		TemplatesLabelProvider labelProvider = new TemplatesLabelProvider();
		treeViewer.setLabelProvider(labelProvider);
		treeViewer.setComparator(getViewerComparator());
		treeViewer.setUseHashlookup(true);

		return treeViewer;
	}

	/**
	 * Comparator to give high priority to projects compared to folders.
	 * 
	 * @return template viewer comparator
	 */
	private ViewerComparator getViewerComparator()
	{
		return new ViewerComparator()
		{
			@Override
			public int compare(Viewer testViewer, Object e1, Object e2)
			{
				int message1 = ((ITemplateNode) e1).getType();
				int message2 = ((ITemplateNode) e2).getType();
				return message2 - message1;
			}
		};
	}

	protected void createAbove(Composite container, int span)
	{
	}

	protected void createBelow(Composite container, int span)
	{
	}

	protected void initializeViewer()
	{
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event)
	{
		setErrorMessage(null);
		IStructuredSelection selection = event.getStructuredSelection();
		ITemplateNode currentWizardSelection = null;
		Iterator<?> iter = selection.iterator();
		if (iter.hasNext())
			currentWizardSelection = (ITemplateNode) iter.next();
		if (currentWizardSelection == null)
		{
			setDescriptionText(StringUtil.EMPTY); // $NON-NLS-1$
			setSelectedNode(null);
			return;
		}
		final ITemplateNode finalSelection = currentWizardSelection;

		String description = StringUtil.EMPTY;
		try
		{
			description = new TemplatesManager().getDescription(finalSelection);
		} catch (IOException e)
		{
			e.printStackTrace();//TODO log the exception
		}
		setDescriptionText(description);
		getContainer().updateButtons();
	}

	public IWizardPage getNextPage(boolean shouldCreate)
	{
		if (!shouldCreate)
			return super.getNextPage();
		IWizardNode selectedNode = getSelectedNode();
		selectedNode.dispose();
		IWizard wizard = selectedNode.getWizard();
		if (wizard == null)
		{
			super.setSelectedNode(null);
			return null;
		}
		if (shouldCreate)
			// Allow the wizard to create its pages
			wizard.addPages();
		return wizard.getStartingPage();
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException
	{
	}

	@Override
	public boolean canFlipToNextPage()
	{
		IStructuredSelection ssel = templateViewer.getStructuredSelection();
		return ssel != null && !ssel.isEmpty();
	}
}
