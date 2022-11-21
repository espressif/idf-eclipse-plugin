package com.espressif.idf.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.espressif.idf.ui.dialogs.TelemetryDialog;

public class TelemetryHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Display display = Display.getDefault();
		display.syncExec((new Runnable()
		{
			@Override
			public void run()
			{

				Shell shell = new Shell(display);

				new TelemetryDialog(shell).open();
			}
		}));

		return null;
	}
}
