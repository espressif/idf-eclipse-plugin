package com.espressif.idf.ui.partitiontable.dialog;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.partitiontable.handlers.PartitionTableEditorHandler;

public class PartitionTableEditor extends MultiPageEditorPart
{

	public PartitionTableEditor()
	{
		PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
			try
			{
				new PartitionTableEditorHandler().execute(null);
			}
			catch (ExecutionException e)
			{
				Logger.log(e);
			}
		});

	}

	protected void createPages()
	{
		Composite emptyPage = new Composite(getContainer(), SWT.NONE);

		int index = addPage(emptyPage);
		setPageText(index, "Empty Page"); //$NON-NLS-1$
		getSite().getShell().getDisplay().asyncExec(() -> getSite().getPage().closeEditor(this, false));
	}

	public void doSave(IProgressMonitor monitor)
	{
		// Nothing to do

	}

	public void doSaveAs()
	{
		// Nothing to do
	}

	public boolean isSaveAsAllowed()
	{
		return false;
	}

}
