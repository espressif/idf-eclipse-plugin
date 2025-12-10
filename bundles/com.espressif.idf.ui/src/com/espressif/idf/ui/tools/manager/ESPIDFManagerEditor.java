/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.manager;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
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
			ESPIDFMainTablePage espidfMainTablePage = ESPIDFMainTablePage.getInstance(eimInput.getEimJson());
			espidfMainTablePage.createPage(parent);
			if (eimInput.isFirstStartup())
			{
				espidfMainTablePage.setupInitialEspIdf();
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
