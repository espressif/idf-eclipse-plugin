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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
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
		parent.setLayout(new FillLayout(SWT.VERTICAL));
		ScrolledComposite scrolledComposite = new ScrolledComposite(parent,
				SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FILL);
		scrolledComposite.setLayout(new FillLayout(SWT.VERTICAL));
		scrolledComposite.setAlwaysShowScrollBars(true);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.addListener(SWT.Resize, event -> {
			int width = scrolledComposite.getClientArea().width;
			scrolledComposite.setMinSize(parent.computeSize(width, SWT.DEFAULT, true));
		});
		Composite subContainer = new Composite(scrolledComposite, SWT.NONE);
		GridLayout gridLayout = new GridLayout(4, true);
		subContainer.setLayout(gridLayout);

		InstallComponentsCompositePage installComponentsCompositePage = new InstallComponentsCompositePage(
				componentsJsonFile);
		installComponentsCompositePage.createControls(subContainer);

		scrolledComposite.setContent(subContainer);

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
}
