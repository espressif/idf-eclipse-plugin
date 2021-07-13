package com.espressif.idf.debug.gdbjtag.openocd.heaptracing;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class HeapTraceComposite extends Composite
{
	public static final String COMPOSITE_NAME = "start_heap_trace_composite";

	private Text textHeapTraceFile;
	private Button btnStartHeapTraceRadio;
	private Button btnStopHeapTraceRadio;
	private Button btnBrowse;

	private boolean startHeapTracing;

	public HeapTraceComposite(Composite parent, int style,
			HeapTracingBreakpointActionPage heapTracingBreakpointActionPage)
	{
		super(parent, style);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		setLayout(gridLayout);

		Group radioBtnGroup = new Group(this, SWT.NONE);
		radioBtnGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
		radioBtnGroup.setText("Action");
		GridData groupGridData = new GridData();
		groupGridData.horizontalSpan = 3;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.grabExcessHorizontalSpace = true;
		radioBtnGroup.setLayoutData(groupGridData);

		btnStartHeapTraceRadio = new Button(radioBtnGroup, SWT.RADIO);
		btnStartHeapTraceRadio.setText("Start Heap Trace");
		btnStartHeapTraceRadio.addSelectionListener(new BtnRadioSelectionListener(true));

		btnStopHeapTraceRadio = new Button(radioBtnGroup, SWT.RADIO);
		btnStopHeapTraceRadio.setText("Stop Heap Trace");
		btnStopHeapTraceRadio.addSelectionListener(new BtnRadioSelectionListener(false));

		Label messageToLogLabel = new Label(this, SWT.NONE);
		messageToLogLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		messageToLogLabel.setText("Heap Dump File Name: "); //$NON-NLS-1$

		textHeapTraceFile = new Text(this, SWT.BORDER);
		textHeapTraceFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		btnBrowse = new Button(this, SWT.PUSH);
		btnBrowse.setText("Browse");
		btnBrowse.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog dialog = new FileDialog(parent.getDisplay().getActiveShell(), SWT.SAVE);
				dialog.setFilterNames(new String[] { "*.svdat" });
				dialog.setFilterExtensions(new String[] { "*.svdat" });
				dialog.setFilterPath(System.getProperty("user.dir"));
				String fName = dialog.open();
				textHeapTraceFile.setText(fName);
			}
		});

		if (heapTracingBreakpointActionPage.getAction().isStartHeapTracing())
		{
			String fName = heapTracingBreakpointActionPage.getAction().getFileName();
			textHeapTraceFile.setText(fName == null ? "" : fName);
			textHeapTraceFile.setEnabled(true);
			btnStartHeapTraceRadio.setSelection(true);
			btnStopHeapTraceRadio.setSelection(false);
			btnBrowse.setEnabled(true);
		}
		else
		{
			textHeapTraceFile.setEnabled(false);
			btnStartHeapTraceRadio.setSelection(false);
			btnStopHeapTraceRadio.setSelection(true);
			btnBrowse.setEnabled(false);
		}
	}

	public String getHeapTraceFile()
	{
		return textHeapTraceFile.getText();
	}

	public boolean isStartHeapTracing()
	{
		return startHeapTracing;
	}

	public void setStartHeapTracing(boolean startHeapTracing)
	{
		this.startHeapTracing = startHeapTracing;
	}

	private class BtnRadioSelectionListener extends SelectionAdapter
	{
		private boolean startHeapTrace;

		private BtnRadioSelectionListener(boolean startHeapTrace)
		{
			this.startHeapTrace = startHeapTrace;
		}

		@Override
		public void widgetSelected(SelectionEvent e)
		{
			if (startHeapTrace)
			{
				startHeapTrace();
			}
			else
			{
				stopHeapTrace();
			}
		}

		private void stopHeapTrace()
		{
			textHeapTraceFile.setEnabled(false);
			btnStartHeapTraceRadio.setSelection(false);
			btnStopHeapTraceRadio.setSelection(true);
			setStartHeapTracing(false);
			btnBrowse.setEnabled(false);
		}

		private void startHeapTrace()
		{
			textHeapTraceFile.setEnabled(true);
			btnStopHeapTraceRadio.setSelection(false);
			btnStartHeapTraceRadio.setSelection(true);
			setStartHeapTracing(true);
			btnBrowse.setEnabled(true);
		}
	}
}
