package com.espressif.idf.ui.nvs.dialog;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.nvs.handlers.NvsEditorHandler;

public class NvsEditor extends MultiPageEditorPart
{
	public NvsEditor()
	{
		PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
			try
			{
				new NvsEditorHandler().execute(null);
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
