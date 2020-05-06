/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;

import com.espressif.idf.core.util.IDFUtil;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFSizeArchivesMemoryHandler extends AbstractIDFSizeHandler
{
	@Override
	protected List<String> getCommandArgs(String pythonExecutablenPath, IPath mapPath)
	{
		List<String> arguments = new ArrayList<String>();
		arguments.add(pythonExecutablenPath);
		arguments.add(IDFUtil.getIDFSizeScriptFile().getAbsolutePath());
		arguments.add(mapPath.toOSString());
		arguments.add("--archives"); //$NON-NLS-1$

		return arguments;
	}

}
