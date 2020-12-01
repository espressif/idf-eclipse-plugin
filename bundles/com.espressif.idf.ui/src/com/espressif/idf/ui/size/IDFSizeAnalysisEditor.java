/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size;

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
import org.eclipse.ui.part.MultiPageSelectionProvider;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.SDKConfigJsonReader;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFSizeAnalysisEditor extends MultiPageEditorPart
{

	public static String EDITOR_ID = "com.espressif.idf.ui.editor.idfsize"; //$NON-NLS-1$
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
		createDetailsPage();
	}

	@Override
	public void doSave(IProgressMonitor monitor)
	{
		// No-impl
	}

	@Override
	public void doSaveAs()
	{
		// No-impl
	}

	@Override
	public boolean isSaveAsAllowed()
	{
		return false;
	}

	/**
	 * Create Size Analysis Details Page
	 */
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

		new IDFSizeDetailsComposite().createPartControl(tableComposite, file);

		int index = addPage(parent);
		setPageText(index, "Details"); //$NON-NLS-1$
	}

	/**
	 * Creates Size Analysis Overview Page
	 */
	private void createOverviewPage()
	{
		Composite parent = new Composite(getContainer(), SWT.NONE);
		parent.setLayout(new FillLayout());

		new IDFSizeOverviewComposite().createPartControl(parent, file, getTarget());

		int index = addPage(parent);
		setPageText(index, "Overview"); //$NON-NLS-1$
	}
	
	private String getTarget()
	{
		return new SDKConfigJsonReader(project).getValue("IDF_TARGET");
	}
}
