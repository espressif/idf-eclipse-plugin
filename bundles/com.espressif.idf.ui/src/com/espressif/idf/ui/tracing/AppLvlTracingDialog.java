package com.espressif.idf.ui.tracing;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Text;

import com.espressif.idf.ui.update.DirectorySelectionDialog;


public class AppLvlTracingDialog extends TitleAreaDialog {
	
	private Text outFilePath;
	private Spinner pollTimer;
	private Spinner traceSize;
	private Spinner stopTwo;
	private Spinner waitForHalt;
	private Spinner skipSize;
	private Button browseBtn;
	private TclClient tclClient;
	
	private String pathToProject;
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public AppLvlTracingDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		new Label(area, SWT.NONE);
		
		Composite composite = new Composite(area, SWT.NONE);
		GridData gdComposite = new GridData(SWT.CENTER, SWT.FILL, false, false, 1, 1);
		gdComposite.heightHint = 232;
		composite.setLayoutData(gdComposite);
		composite.setLayout(new GridLayout(1, false));
		Composite container = new Composite(composite, SWT.NONE);
		GridData gdContainer = new GridData(SWT.CENTER, SWT.FILL, false, false, 1, 1);
		gdContainer.heightHint = 200;
		container.setLayoutData(gdContainer);
		container.setLayout(new GridLayout(4, false));
		
		Label pollTimerLbl = new Label(container, SWT.NONE);
		pollTimerLbl.setText("Poll Period:"); //$NON-NLS-1$
		pollTimer = new Spinner(container, SWT.BORDER);
		Label pollTimerUnitsLbl = new Label(container, SWT.NONE);
		pollTimerUnitsLbl.setText(ITracingConstants.UNIT_SECONDS); 
		
		new Label(container, SWT.NONE);
		Label traceSizeLbl = new Label(container, SWT.NONE);
		traceSizeLbl.setText("Trace Size:"); //$NON-NLS-1$
		traceSize = new Spinner(container, SWT.BORDER);
		traceSize.setMinimum(-1);
		traceSize.setSelection(-1);
		Label traceSizeUnitsLbl = new Label(container, SWT.NONE);
		traceSizeUnitsLbl.setText(ITracingConstants.UNIT_BYTES);
		
		new Label(container, SWT.NONE);
		Label timeoutLbl = new Label(container, SWT.NONE);
		timeoutLbl.setText("Idle Timeout:"); //$NON-NLS-1$
		stopTwo = new Spinner(container, SWT.BORDER);
		stopTwo.setMinimum(-1);
		stopTwo.setSelection(-1);
		Label timeoutUnitsLbl = new Label(container, SWT.NONE);
		timeoutUnitsLbl.setText(ITracingConstants.UNIT_SECONDS);
		
		new Label(container, SWT.NONE);
		Label waitForHaltLbl = new Label(container, SWT.NONE);
		waitForHaltLbl.setText("Wait for Halt:"); //$NON-NLS-1$
		waitForHalt = new Spinner(container, SWT.BORDER);
		Label waitForHaltUnitsLbl = new Label(container, SWT.NONE);
		waitForHaltUnitsLbl.setText(ITracingConstants.UNIT_SECONDS);
		
		new Label(container, SWT.NONE);
		Label bytesToSkipLbl = new Label(container, SWT.NONE);
		bytesToSkipLbl.setText("Bytes to Skip:"); //$NON-NLS-1$
		skipSize = new Spinner(container, SWT.BORDER);
		Label bytesUnitsLbl = new Label(container, SWT.NONE);
		bytesUnitsLbl.setText(ITracingConstants.UNIT_BYTES);
		
		new Label(container, SWT.NONE);
		Label outFileLbl = new Label(container, SWT.NONE);
		outFileLbl.setText("Out File:"); //$NON-NLS-1$
		outFilePath = new Text(container, SWT.BORDER);
		outFilePath.setText(pathToProject);
		GridData gdOutFile = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gdOutFile.widthHint = 306;
		outFilePath.setLayoutData(gdOutFile);
		browseBtn = new Button(container, SWT.NONE);
		browseBtn.setText("Browse"); //$NON-NLS-1$
		browseBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browseButtonSelected("test",
						outFilePath);
			}
		});
		return area;
	}

	private void browseButtonSelected(String title, Text text) {
		DirectoryDialog dialog = new DirectoryDialog(getParentShell(), SWT.NONE);
		dialog.setText(title);
		String str = text.getText().trim();
		int lastSeparatorIndex = str.lastIndexOf(File.separator);
		if (lastSeparatorIndex != -1)
			dialog.setFilterPath(str.substring(0, lastSeparatorIndex));
		str = dialog.open();
		str = str.replace(File.separator, "/");
		str = wrapOutputFilePath(str);
		outFilePath.setText(str);
	}
	
	private String wrapOutputFilePath(String baseFilePath) {
		baseFilePath = baseFilePath + "/trace.log"; //$NON-NLS-1$
		baseFilePath = baseFilePath.replace("/", "//"); //$NON-NLS-1$
		baseFilePath = "file://" + baseFilePath; //$NON-NLS-1$
		return baseFilePath;
	}
	
	public void setProjectPath(IResource project) {
		pathToProject = project.getLocation().toString();
		pathToProject = wrapOutputFilePath(pathToProject);
	}
	
	@Override
	protected void okPressed()
	{
	}
	
	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button button = createButton(parent, IDialogConstants.OK_ID, ITracingConstants.START_LABEL, true);
		button.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				if (button.getText().contentEquals(ITracingConstants.START_LABEL)) {
					tclClient = new TclClient();
					tclClient.startTracing( new String[] {outFilePath.getText(), pollTimer.getText(), traceSize.getText(), stopTwo.getText(), waitForHalt.getText(), skipSize.getText()});
					button.setText(ITracingConstants.STOP_LABEL);
				} else {
					tclClient.stopTracing();
					button.setText(ITracingConstants.START_LABEL);
				}
				
			}
		});
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(529, 358);
	}
}
