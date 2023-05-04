/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd.dsf.process.monitors;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.eclipse.debug.internal.core.OutputStreamMonitor;

/**
 * Monitors the output stream of a system process and notifies listeners of
 * additions to the stream.
 * <p>
 * The output stream monitor reads system out (or err) via and input stream.
 * @author Ali Azam Rana
 *
 */
@SuppressWarnings("restriction")
public class CustomOutputStreamMonitor extends OutputStreamMonitor 
{

	public CustomOutputStreamMonitor(InputStream stream, Charset charset)
	{
		super(stream, charset);
	}
	
	@Override
	protected void startMonitoring()
	{
		super.startMonitoring();
	}
	
	@Override
	protected void close()
	{
		super.close();
	}
	
	@Override
	protected void kill()
	{
		super.kill();
	}
}
