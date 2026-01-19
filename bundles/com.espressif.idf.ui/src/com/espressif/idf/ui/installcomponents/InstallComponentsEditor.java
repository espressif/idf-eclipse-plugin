/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.installcomponents;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.installcomponents.container.InstallComponentsCompositePage;

/**
 * Install IDF Components editor page
 * 
 * @author Ali Azam Rana
 *
 */
public class InstallComponentsEditor extends MultiPageEditorPart
{
	public static String EDITOR_ID = "com.espressif.idf.ui.installComponents"; //$NON-NLS-1$

	private IFile componentsJsonFile;
	private IProject project;
	private InstallComponentsCompositePage installComponentsCompositePage;

	public InstallComponentsEditor()
	{
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException
	{
		super.init(site, input);
		FileEditorInput editorInput = (FileEditorInput) getEditorInput();
		componentsJsonFile = editorInput.getFile();
		project = componentsJsonFile.getProject();
		setPartName(project.getName());
		installComponentsCompositePage = new InstallComponentsCompositePage(
				componentsJsonFile, project);
	}

	@Override
	protected void createPages()
	{
		String osString = componentsJsonFile.getLocation().toOSString();
		Logger.log("Editor input:" + osString); //$NON-NLS-1$

		try
		{
			createComponentsInstallPage();
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
	}

	private void createComponentsInstallPage() throws IOException
	{
		Composite parent = new Composite(getContainer(), SWT.NONE);

		GridLayout mainLayout = new GridLayout(1, false);
		mainLayout.marginWidth = 10;
		mainLayout.marginHeight = 10;
		mainLayout.verticalSpacing = 10;
		parent.setLayout(mainLayout);

		Text searchText = new Text(parent, SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL | SWT.BORDER);
		searchText.setMessage(Messages.InstallComponentsEditor_SearchComponentMsg);
		searchText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		searchText.addModifyListener(e -> installComponentsCompositePage.filterComponents(searchText.getText()));

		ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		scrolledComposite.setLayout(new GridLayout());
		scrolledComposite.setAlwaysShowScrollBars(true);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite subContainer = new Composite(scrolledComposite, SWT.NONE);
		GridLayout gridLayout = new GridLayout(4, true);
		subContainer.setLayout(gridLayout);

		installComponentsCompositePage.createControls(subContainer);
		scrolledComposite.setContent(subContainer);
		scrolledComposite.setMinSize(subContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		scrolledComposite.addListener(SWT.Resize, event -> {
			int width = scrolledComposite.getClientArea().width;
			scrolledComposite.setMinSize(subContainer.computeSize(width, SWT.DEFAULT));
		});

		int index = addPage(parent);
		setPageText(index, "Install Components"); //$NON-NLS-1$
	}

	@Override
	public void doSave(IProgressMonitor monitor)
	{
	}

	@Override
	public void doSaveAs()
	{
	}

	@Override
	public boolean isDirty()
	{
		return false;
	}

	@Override
	public boolean isSaveAsAllowed()
	{
		return false;
	}

	@Override
	public void setFocus()
	{
	}
	
	@Override
	public void dispose()
	{
		super.dispose();
		installComponentsCompositePage.dispose();
	}
}
