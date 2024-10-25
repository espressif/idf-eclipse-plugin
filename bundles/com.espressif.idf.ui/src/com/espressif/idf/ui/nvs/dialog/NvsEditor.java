package com.espressif.idf.ui.nvs.dialog;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.nvs.handlers.NvsEditorHandler;

public class NvsEditor extends MultiPageEditorPart
{

	public NvsEditor()
	{
		try
		{
			new NvsEditorHandler().execute(null);
		}
		catch (ExecutionException e)
		{
			Logger.log(e);
		}

	}

	protected void createPages()
	{
		// TODO Auto-generated method stub
		getSite().getPage().closeEditor(this, false);
	}

	public void doSave(IProgressMonitor monitor)
	{
		// TODO Auto-generated method stub

	}

	public void doSaveAs()
	{
		// TODO Auto-generated method stub

	}

	public boolean isSaveAsAllowed()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
