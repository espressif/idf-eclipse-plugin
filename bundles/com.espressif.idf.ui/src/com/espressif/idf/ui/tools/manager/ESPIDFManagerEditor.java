/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.manager;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.espressif.idf.ui.tools.Messages;
import com.espressif.idf.ui.tools.manager.pages.ESPIDFMainTablePage;

/**
 * Editor main class used for tools management
 * 
 * @author Ali Azam Rana
 *
 */
public class ESPIDFManagerEditor extends EditorPart
{
	public static final String EDITOR_ID = "com.espressif.idf.ui.manageespidf";

	private ESPIDFMainTablePage tablePage;

	/** Returns the table page of an already-open manager editor, if any. */
	public static ESPIDFMainTablePage findOpenTablePage(IWorkbenchPage workbenchPage)
	{
		if (workbenchPage == null)
		{
			return null;
		}
		for (IEditorReference ref : workbenchPage.getEditorReferences())
		{
			IEditorPart editor = ref.getEditor(false);
			if (editor instanceof ESPIDFManagerEditor manager)
			{
				return manager.tablePage;
			}
		}
		return null;
	}

	public ESPIDFMainTablePage getTablePage()
	{
		return tablePage;
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException
	{
		setSite(site);
		setInput(input);
		setPartName(Messages.EspIdfEditorTitle);
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

	@Override
	public void createPartControl(Composite parent)
	{
		IEditorInput input = getEditorInput();

		if (input instanceof EimEditorInput eimInput)
		{
			tablePage = new ESPIDFMainTablePage();
			tablePage.createPage(parent);
			if (eimInput.isFirstStartup())
			{
				tablePage.setupInitialEspIdf();
			}
		}
		else
		{
			getSite().getPage().closeEditor(this, false);
		}
	}

	@Override
	public boolean isDirty()
	{
		return false;
	}

	@Override
	public void setFocus()
	{

	}
}
