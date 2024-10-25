package com.espressif.idf.ui.partitiontable.dialog;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.partitiontable.handlers.PartitionTableEditorHandler;

public class PartitionTableEditor extends MultiPageEditorPart
{

	public PartitionTableEditor()
	{
		try
		{
			new PartitionTableEditorHandler().execute(null);
		}
		catch (ExecutionException e)
		{
			Logger.log(e);
		}
	}

	protected void createPages()
	{
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
