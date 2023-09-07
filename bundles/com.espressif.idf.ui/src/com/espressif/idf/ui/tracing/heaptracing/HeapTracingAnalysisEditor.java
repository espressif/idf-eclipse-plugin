/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.tracing.heaptracing;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.ProjectDescriptionReader;
import com.espressif.idf.ui.tracing.Messages;
import com.espressif.idf.ui.tracing.TracingJsonParser;

/**
 * Tracing
 * 
 * @author Ali Azam Rana
 *
 */
public class HeapTracingAnalysisEditor extends MultiPageEditorPart
{
	public static final String EDITOR_ID = "com.espressif.idf.ui.editor.heapTraceAnalysis"; //$NON-NLS-1$
	private IProject project;
	private IFile memoryDumpFile;
	private IFile elfSymbolsFile;
	private TracingJsonParser tracingJsonParser;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException
	{
		super.init(site, input);
		FileEditorInput editorInput = (FileEditorInput) getEditorInput();
		memoryDumpFile = editorInput.getFile();
		project = memoryDumpFile.getProject();
		setPartName(project.getName());
		elfSymbolsFile = new ProjectDescriptionReader(project).getAppElfFile();
		try
		{
			tracingJsonParser = new TracingJsonParser(memoryDumpFile.getRawLocation().toOSString(),
					this.elfSymbolsFile);
		}
		catch (Exception execption)
		{
			Logger.log(execption);
		}
	}

	@Override
	protected void createPages()
	{
		String osString = memoryDumpFile.getLocation().toOSString();
		Logger.log("Editor input:" + osString); //$NON-NLS-1$

		createOverviewPage();
		createDetailsPage();
		createCallersConsolidatedPage();
	}

	private void createCallersConsolidatedPage()
	{
		Composite parent = new Composite(getContainer(), SWT.NONE);
		parent.setLayout(new FillLayout());
		HeapTracingCallersViewComposite tracingCallersViewComposite = new HeapTracingCallersViewComposite(
				tracingJsonParser);
		tracingCallersViewComposite.createPartControl(parent);
		int index = addPage(parent);
		setPageText(index, Messages.TracingCallersConsolodiatedView_Tab);
	}

	private void createOverviewPage()
	{
		Composite parent = new Composite(getContainer(), SWT.NONE);
		parent.setLayout(new FillLayout());

		HeapTracingOverviewComposite heapTracingOverviewComposite = new HeapTracingOverviewComposite(tracingJsonParser);

		heapTracingOverviewComposite.createPartControl(parent);

		int index = addPage(parent);
		setPageText(index, Messages.TracingAnalysisEditor_OverviewTab);
	}

	private void createDetailsPage()
	{

		Composite parent = new Composite(getContainer(), SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		parent.setLayout(layout);

		GridData layoutData = new GridData();
		layoutData.grabExcessVerticalSpace = true;
		layoutData.verticalAlignment = GridData.FILL;

		Font font = parent.getFont();

		// Create table composite
		Composite tableComposite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 150;
		gridData.widthHint = 400;
		tableComposite.setLayout(layout);
		tableComposite.setLayoutData(gridData);
		tableComposite.setFont(font);

		HeapTracingDetailsComposite tracingDetailsComposite = new HeapTracingDetailsComposite(tracingJsonParser);
		tracingDetailsComposite.createPartControl(tableComposite);

		int index = addPage(parent);
		setPageText(index, "Details"); //$NON-NLS-1$
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
