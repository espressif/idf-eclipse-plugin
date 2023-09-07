/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd.dsf.process;

import java.util.Map;

import org.eclipse.cdt.dsf.gdb.launching.GdbProcessFactory;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;

/**
 * Extended the default GdbProcessFactory to handle the processes in our openocd 
 * configuration required for having custom stream monitors and console
 * @author Ali Azam Rana
 *
 */
public class CustomIdfProcessFactory extends GdbProcessFactory
{
	public static final String ID = "com.espressif.idf.debug.gdbjtag.openocd.processFactory";
	
	@Override
	public IProcess newProcess(ILaunch launch, Process process, String label, Map<String, String> attributes)
	{
		return new IdfRuntimeProcess(launch, process, label, attributes);
	}
	
}
