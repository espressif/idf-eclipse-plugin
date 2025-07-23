/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd.dsf.process;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;

import org.eclipse.cdt.dsf.gdb.launching.GDBProcess;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.internal.core.NullStreamsProxy;
import org.eclipse.debug.ui.IDebugUIConstants;

import com.espressif.idf.debug.gdbjtag.openocd.dsf.process.monitors.StreamsProxy;

/**
 * Customised process class for the Idf based processes that will require a custom console based on the settings
 * provided in the espressif configurations
 * 
 * @author Ali Azam Rana
 *
 */
@SuppressWarnings("restriction")
public class IdfRuntimeProcess extends GDBProcess
{
	private boolean fCaptureOutput = true;
	private StreamsProxy streamsProxy;

	public IdfRuntimeProcess(ILaunch launch, Process process, String name, Map<String, String> attributes)
	{
		super(launch, process, name, attributes);
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
		// Use Eclipse Common tab attribute for output file and append
		final String outputFileName = getAttributeSafe(getLaunch().getLaunchConfiguration()::getAttribute,
				IDebugUIConstants.ATTR_CAPTURE_IN_FILE, "");

		final boolean append = getAttributeSafe(getLaunch().getLaunchConfiguration()::getAttribute,
				IDebugUIConstants.ATTR_APPEND_TO_FILE, false);
		streamsProxy = new StreamsProxy(this, getSystemProcess(), charset, getLabel(), outputFileName, append);
		return streamsProxy;
	}

	@Override
	public void terminate() throws DebugException
	{
		super.terminate();
		streamsProxy.kill();
	}

	private <T> T getAttributeSafe(AttributeGetter<T> getter, String attribute, T defaultValue)
	{
		try
		{
			return getter.get(attribute, defaultValue);
		}
		catch (CoreException e)
		{
			DebugPlugin.log(e);
			return defaultValue;
		}
	}

	@FunctionalInterface
	interface AttributeGetter<T>
	{
		T get(String attribute, T defaultValue) throws CoreException;
	}
}
