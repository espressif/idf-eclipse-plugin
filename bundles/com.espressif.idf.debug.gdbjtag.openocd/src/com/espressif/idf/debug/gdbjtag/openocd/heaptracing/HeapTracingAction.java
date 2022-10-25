/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.

 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.debug.gdbjtag.openocd.heaptracing;

import java.text.MessageFormat;

import org.eclipse.cdt.debug.core.breakpointactions.AbstractBreakpointAction;
import org.eclipse.cdt.debug.core.breakpointactions.ICLIDebugActionEnabler;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Break point action class for heap tracing
 * 
 * @author Ali Azam Rana
 *
 */
@SuppressWarnings("restriction")
public class HeapTracingAction extends AbstractBreakpointAction
{
	public static final String ID = "com.espressif.idf.debug.gdbjtag.openocd.heaptracing.HeapTracingAction"; //$NON-NLS-1$
	private String command;
	private String fileName;
	private static final String COMMAND_ATT = "command"; //$NON-NLS-1$
	private static final String FILENAME_ATT = "fileName"; //$NON-NLS-1$
	private static final String START_ATT = "start"; //$NON-NLS-1$
	private static final String START_COMMAND = "mon esp sysview_mcore start file://"; //$NON-NLS-1$
	private static final String STOP_COMMAND = "mon esp sysview_mcore stop"; //$NON-NLS-1$
	private static final String DEFAULT_NAME = "Heap Tracing Action"; //$NON-NLS-1$
	private boolean startHeapTracing;

	public void setCommand(String command)
	{
		this.command = command;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public String getFileName()
	{
		return fileName.replace('\\', '/');
	}

	public String getCommand()
	{
		if (startHeapTracing)
		{
			command = START_COMMAND.concat(getFileName());
		}
		else
		{
			command = STOP_COMMAND;
		}

		return command;
	}

	@Override
	public String getMemento()
	{
		try
		{
			Document doc = DebugPlugin.newDocument();
			Element rootElement = doc.createElement(COMMAND_ATT);
			rootElement.setAttribute(COMMAND_ATT, command);
			rootElement.setAttribute(FILENAME_ATT, fileName);
			rootElement.setAttribute(START_ATT, String.valueOf(startHeapTracing));
			doc.appendChild(rootElement);
			return DebugPlugin.serializeDocument(doc);
		}
		catch (
				DOMException
				| CoreException e)
		{
			CDebugUIPlugin.log(e);

		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public void initializeFromMemento(String data)
	{
		try
		{
			Element root = DebugPlugin.parseDocument(data);
			String value = root.getAttribute(COMMAND_ATT);
			if (value == null)
				value = ""; //$NON-NLS-1$
			command = value;
			value = root.getAttribute(FILENAME_ATT);
			if (value == null)
				value = ""; //$NON-NLS-1$
			fileName = value;
			value = root.getAttribute(START_ATT);
			if (value == null)
			{
				startHeapTracing = false;
			}
			else
			{
				startHeapTracing = Boolean.parseBoolean(value);
			}
		}
		catch (Exception e)
		{
			CDebugUIPlugin.log(e);
		}
	}

	@Override
	public String getDefaultName()
	{
		return DEFAULT_NAME;
	}

	@Override
	public String getSummary()
	{
		String summary = getCommand();
		if (summary.length() > 32)
			summary = summary.substring(0, 32);
		return summary;
	}

	@Override
	public String getTypeName()
	{
		return DEFAULT_NAME;
	}

	@Override
	public String getIdentifier()
	{
		return ID;
	}

	@Override
	public IStatus execute(IBreakpoint breakpoint, IAdaptable context, IProgressMonitor monitor)
	{
		ICLIDebugActionEnabler enabler = context.getAdapter(ICLIDebugActionEnabler.class);
		if (enabler != null)
		{
			try
			{
				enabler.execute(getCommand());
			}
			catch (Exception e)
			{
				return errorStatus(e);
			}
		}
		else
		{
			return new Status(IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(),
					IInternalCDebugUIConstants.INTERNAL_ERROR,
					"Your debugger integration does not support direct execution of commands", null); //$NON-NLS-1$
		}
		return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
	}

	private IStatus errorStatus(Exception ex)
	{
		String errorMsg = MessageFormat.format("Could not execute debugger command action {0}", //$NON-NLS-1$
				new Object[] { getSummary() });
		return new Status(IStatus.ERROR, getIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, errorMsg, ex);
	}

	public boolean isStartHeapTracing()
	{
		return startHeapTracing;
	}

	public void setStartHeapTracing(boolean startHeapTracing)
	{
		this.startHeapTracing = startHeapTracing;
	}
}