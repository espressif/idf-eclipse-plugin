/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd.dsf.process;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.eclipse.debug.internal.core.NullStreamsProxy;

import com.espressif.idf.debug.gdbjtag.openocd.dsf.process.monitors.StreamsProxy;

/**
 * Customised process class for the 
 * Idf based processes that will require a 
 * custom console based on the settings provided in the espressif configurations
 * @author Ali Azam Rana
 *
 */
@SuppressWarnings("restriction")
public class IdfRuntimeProcess extends RuntimeProcess
{
	private boolean fCaptureOutput = true;
	private String fName;

	public IdfRuntimeProcess(ILaunch launch, Process process, String name, Map<String, String> attributes)
	{
		super(launch, process, name, attributes);
		fName = name;
	}

	@Override
	protected IStreamsProxy createStreamsProxy()
	{
		String captureOutput = getLaunch().getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT);
		fCaptureOutput = !("false".equals(captureOutput)); //$NON-NLS-1$
		if (!fCaptureOutput)
		{
			return new NullStreamsProxy(getSystemProcess());
		}
		String encoding = getLaunch().getAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING);
		Charset charset = null;
		if (encoding != null)
		{
			try
			{
				charset = Charset.forName(encoding);
			}
			catch (
					UnsupportedCharsetException
					| IllegalCharsetNameException e)
			{
				DebugPlugin.log(e);
			}
		}
		StreamsProxy streamsProxy = new StreamsProxy(this, getSystemProcess(), charset, fName);
		return streamsProxy;
	}
}
