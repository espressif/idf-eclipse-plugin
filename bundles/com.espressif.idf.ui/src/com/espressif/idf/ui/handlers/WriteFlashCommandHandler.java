package com.espressif.idf.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.ui.dialogs.WriteFlashDialog;

public class WriteFlashCommandHandler extends AbstractHandler
{

	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		WriteFlashDialog flashDialog = new WriteFlashDialog(activeShell);
		flashDialog.create();
		flashDialog.open();
		return null;
	}

}
