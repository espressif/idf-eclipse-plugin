package com.espressif.idf.debug.gdbjtag.openocd.heaptracing;

import org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction;
import org.eclipse.cdt.debug.ui.breakpointactions.IBreakpointActionPage;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.swt.widgets.Composite;

public class HeapTracingBreakpointActionPage extends PlatformObject implements IBreakpointActionPage
{
	private HeapTracingAction heapTraceAction;
	private HeapTraceComposite heapTraceComposite;

	@Override
	public void actionDialogCanceled()
	{
	}

	@Override
	public void actionDialogOK()
	{
		heapTraceAction.setFileName(heapTraceComposite.getHeapTraceFile());
		heapTraceAction.setStartHeapTracing(heapTraceComposite.isStartHeapTracing());
	}
	
	public HeapTracingAction getAction()
	{
		return heapTraceAction;
	}

	@Override
	public Composite createComposite(IBreakpointAction action, Composite composite, int style)
	{
		heapTraceAction = (HeapTracingAction) action;
		heapTraceComposite = new HeapTraceComposite(composite, style, this);
		return heapTraceComposite;
	}
}
