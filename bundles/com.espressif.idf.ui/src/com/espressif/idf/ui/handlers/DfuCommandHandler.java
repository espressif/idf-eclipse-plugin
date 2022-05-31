/*******************************************************************************
 * Copyright 2022-2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.handlers;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.espressif.idf.ui.update.AbstractToolsHandler;

public class DfuCommandHandler extends AbstractToolsHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
	     Command command = event.getCommand();
	     HandlerUtil.toggleCommandState(command);
		return event;
	}

	@Override
	protected void execute() {
	}
	
}
