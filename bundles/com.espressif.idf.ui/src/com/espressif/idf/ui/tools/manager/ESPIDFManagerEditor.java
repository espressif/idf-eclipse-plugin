package com.espressif.idf.ui.tools.manager;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.espressif.idf.ui.tools.manager.pages.ESPIDFMainTablePage;

public class ESPIDFManagerEditor extends MultiPageEditorPart
{
	public static final String EDITOR_ID = "com.espressif.idf.ui.manageespidf";
	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException
	{
		super.init(site, input);
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
	protected void createPages()
	{
		ESPIDFMainTablePage espidfMainTablePage = new ESPIDFMainTablePage();
		int index = addPage(espidfMainTablePage.createPage(getContainer()));
		setPageText(index, "ESP-IDF: Manager");
	}
}
