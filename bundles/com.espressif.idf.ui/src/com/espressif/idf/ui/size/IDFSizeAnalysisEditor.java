/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.SDKConfigJsonReader;
import com.espressif.idf.core.util.StringUtil;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFSizeAnalysisEditor extends MultiPageEditorPart
{

	public static String EDITOR_ID = "com.espressif.idf.ui.editor.idfsize"; //$NON-NLS-1$
	private IProject project;
	private IFile file;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException
	{
		super.init(site, input);
		FileEditorInput editorInput = (FileEditorInput) getEditorInput();
		file = editorInput.getFile();
		project = file.getProject();
		setPartName(project.getName());
	}

	@Override
	protected void createPages()
	{
		String osString = file.getLocation().toOSString();
		Logger.log("Editor input:" + osString); //$NON-NLS-1$
		
		if (!verifyVersion())
		{
			createErrorMessageOnEditor();
		}
		else 
		{
			createOverviewPage();
			createChartsComposite();
			createDetailsPage();			
		}
	}
	
	private boolean verifyVersion()
	{
		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
		String idfVersion = idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.ESP_IDF_VERSION);
		if (StringUtil.isEmpty(idfVersion))
		{
			return false;
		}
		
		if (idfVersion.toLowerCase().startsWith("v")) //$NON-NLS-1$
		{
			idfVersion = idfVersion.substring(1);
		}
		
		String[] parts = idfVersion.split("\\.");
		if (parts.length < 2)
		{
			return false; // Unexpected version format
		}

		try
		{
			int major = Integer.parseInt(parts[0]);
			int minor = Integer.parseInt(parts[1]);

			// Check if version >= 5.1
			if (major > 5)
			{
				return true;
			}
			else if (major == 5 && minor > 1)
			{
				return true;
			}
		}
		catch (NumberFormatException e)
		{
			return false;
		}
		
		return false;

	}

	@Override
	public void doSave(IProgressMonitor monitor)
	{
		// No-impl
	}

	@Override
	public void doSaveAs()
	{
		// No-impl
	}

	@Override
	public boolean isSaveAsAllowed()
	{
		return false;
	}
	
	private void createErrorMessageOnEditor()
	{
		Composite parent = new Composite(getContainer(), SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.verticalSpacing = 20;
		layout.marginTop = 100;
		parent.setLayout(layout);

		// Main error message with bold system font
		Label title = new Label(parent, SWT.CENTER);
		title.setText(Messages.IDFSizeEditorVersionError);
		GridData titleData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		title.setLayoutData(titleData);

		// Apply bold style to default system font
		FontDescriptor boldFontDescriptor = FontDescriptor.createFrom(title.getFont()).setStyle(SWT.BOLD);
		title.setFont(boldFontDescriptor.createFont(parent.getDisplay()));

		// Subtext explanation
		Label explanation = new Label(parent, SWT.CENTER | SWT.WRAP);
		explanation.setText(Messages.IDFSizeEditorVersionErrorDescription);
		GridData explanationData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		explanationData.widthHint = 500;
		explanation.setLayoutData(explanationData);

		// Close Editor Button
		Button closeBtn = new Button(parent, SWT.PUSH);
		closeBtn.setText(Messages.IDFSizeEditorCloseButton);
		GridData buttonData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		closeBtn.setLayoutData(buttonData);
		closeBtn.addListener(SWT.Selection, e -> getSite().getPage().closeEditor(this, false));

		int index = addPage(parent);
		setPageText(index, Messages.IDFSizeEditorError);
	}


	/**
	 * Create Size Analysis Details Page
	 */
	private void createDetailsPage()
	{

		Composite parent = new Composite(getContainer(), SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		parent.setLayout(layout);

		GridData layoutData = new GridData();
		layoutData.grabExcessVerticalSpace = true;
		layoutData.verticalAlignment = GridData.FILL;

		Font font = parent.getFont();

		// Create table composite
		Composite tableComposite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 150;
		gridData.widthHint = 400;
		tableComposite.setLayout(layout);
		tableComposite.setLayoutData(gridData);
		tableComposite.setFont(font);

		new IDFSizeDetailsComposite().createPartControl(tableComposite, file);

		int index = addPage(parent);
		setPageText(index, Messages.IDFSizeEditorDetails);
	}

	/**
	 * Creates Size Analysis Overview Page
	 */
	private void createOverviewPage()
	{
		Composite parent = new Composite(getContainer(), SWT.NONE);
		parent.setLayout(new FillLayout());
		parent.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		new IDFSizeOverviewComposite().createPartControl(parent, file, getTarget());

		int index = addPage(parent);
		setPageText(index, Messages.IDFSizeEditorOverview);
	}
	
	private void createChartsComposite()
	{
		Composite parent = new Composite(getContainer(), SWT.NONE);
		parent.setLayout(new FillLayout());
		
		new IDFSizeChartsComposite().createPartControl(parent, file, getTarget());
		int index = addPage(parent);
		setPageText(index, Messages.IDFSizeEditorCharts);
	}

	private String getTarget()
	{
		return new SDKConfigJsonReader(project).getValue("IDF_TARGET"); //$NON-NLS-1$
	}
}
