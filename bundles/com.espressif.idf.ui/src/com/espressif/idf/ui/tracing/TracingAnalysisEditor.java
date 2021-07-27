/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.tracing;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.espressif.idf.core.logging.Logger;

/**
 * Tracing 
 * @author Ali Azam Rana
 *
 */
public class TracingAnalysisEditor extends MultiPageEditorPart
{
	public static String EDITOR_ID = "com.espressif.idf.ui.editor.traceAnalysis"; //$NON-NLS-1$
	private IProject project;
	private IFile file;
	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException
	{
		super.init(site, input);
		FileEditorInput editorInput = (FileEditorInput) getEditorInput();
		file = editorInput.getFile();
		project = file.getProject();
		setPartName(project.getName());
	}
	
	@Override
	protected void createPages()
	{
		String osString = file.getLocation().toOSString();
		Logger.log("Editor input:" + osString); //$NON-NLS-1$
		
		createOverviewPage();
	}
	
	private void createOverviewPage()
	{
		Composite parent = new Composite(getContainer(), SWT.NONE);
		parent.setLayout(new FillLayout());
		
		TracingOverviewComposite heapTracingOverviewComposite = new TracingOverviewComposite(file);
		
		heapTracingOverviewComposite.createPartControl(parent);

		int index = addPage(parent);
		setPageText(index, Messages.TracingAnalysisEditor_OverviewTab); //$NON-NLS-1$
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
	public boolean isSaveAsAllowed()
	{
		return false;
	}

}
