package com.espressif.idf.debug.gdbjtag.openocd.ui;

import java.io.File;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.gdbjtag.ui.GDBJtagImages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.FileUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.debug.gdbjtag.openocd.Activator;
import com.espressif.idf.debug.gdbjtag.openocd.ConfigurationAttributes;
import com.espressif.idf.debug.gdbjtag.openocd.preferences.DefaultPreferences;
import com.espressif.idf.debug.gdbjtag.openocd.preferences.PersistentPreferences;

public class TabHeapTracing extends AbstractLaunchConfigurationTab
{
	private static final String TAB_NAME = "Heap Tracing";
	private static final String TAB_ID = Activator.PLUGIN_ID + ".ui.heaptracingtab";

	private DefaultPreferences fDefaultPreferences;
	private PersistentPreferences fPersistentPreferences;

	private ILaunchConfiguration fConfiguration;

	private Text txtGdbInit;
	private Text gdbInitFilePath;
	private Button chkEnableHeapTracing;
	private Button browseBtn;
	private String gdbInitContents;
	
	public TabHeapTracing()
	{
		super();
		fDefaultPreferences = Activator.getInstance().getDefaultPreferences();
		fPersistentPreferences = Activator.getInstance().getPersistentPreferences();
	}

	@Override
	public void createControl(Composite parent)
	{
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.grabExcessHorizontalSpace = true;
		chkEnableHeapTracing = new Button(comp, SWT.CHECK);
		chkEnableHeapTracing.setText("Enable Heap Tracing");
		chkEnableHeapTracing.setLayoutData(gridData);
		chkEnableHeapTracing.addSelectionListener(new CheckBoxSelectionListener());

		Label gdbInitFilePathLabel = new Label(comp, SWT.NONE);
		gdbInitFilePathLabel.setText("gdbinit File Path: ");

		gdbInitFilePath = new Text(comp, SWT.BORDER);
		gdbInitFilePath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		browseBtn = new Button(comp, SWT.PUSH);
		browseBtn.setText("Browse...");
		browseBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				browseButtonSelected("Select gdbinit file", gdbInitFilePath);
				gdbInitContents = FileUtil.readFile(gdbInitFilePath.getText());
				if (!StringUtil.isEmpty(gdbInitContents))
				{
					txtGdbInit.setText(gdbInitContents);
				}
				else
				{
					gdbInitContents = txtGdbInit.getText();
				}
			}
		});

		Label gdbInitLabel = new Label(comp, SWT.NONE);
		gdbInitLabel.setText("gdbinit:");
		gdbInitLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		txtGdbInit = new Text(comp, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gridDataGdbInit = new GridData();
		gridDataGdbInit.grabExcessHorizontalSpace = true;
		gridDataGdbInit.grabExcessVerticalSpace = true;
		gridDataGdbInit.verticalAlignment = SWT.FILL;
		gridDataGdbInit.horizontalAlignment = SWT.FILL;
		gridDataGdbInit.horizontalSpan = 3;
		txtGdbInit.setLayoutData(gridDataGdbInit);

		txtGdbInit.setEnabled(false);
		browseBtn.setEnabled(false);
		gdbInitFilePath.setEnabled(false);
	}

	private void browseButtonSelected(String title, Text text)
	{
		FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
		dialog.setText(title);
		String str = text.getText().trim();
		int lastSeparatorIndex = str.lastIndexOf(File.separator);
		if (lastSeparatorIndex != -1)
		{
			dialog.setFilterPath(str.substring(0, lastSeparatorIndex));
		}
		dialog.setFilterExtensions(new String[] { "gdbinit" });
		str = dialog.open();
		if (str != null)
		{
			text.setText(str);
		}
	}

	@Override
	public Image getImage()
	{
		return GDBJtagImages.getDebuggerTabImage();
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
	{
		configuration.setAttribute(ConfigurationAttributes.HEAP_TRACING_ENABLED, false);
		configuration.setAttribute(ConfigurationAttributes.GDBINIT_CONTENTS, (String) null);
		configuration.setAttribute(ConfigurationAttributes.GDBINIT_FILE_PATH, (String) null);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration)
	{
		fConfiguration = configuration;
		try
		{
			if (configuration.getAttribute(ConfigurationAttributes.HEAP_TRACING_ENABLED, false))
			{
				chkEnableHeapTracing.setSelection(true);
				gdbInitContents = configuration.getAttribute(ConfigurationAttributes.GDBINIT_CONTENTS, gdbInitContents);
				String filePath = configuration.getAttribute(ConfigurationAttributes.GDBINIT_FILE_PATH, "");
				gdbInitFilePath.setText(filePath);
				txtGdbInit.setText(gdbInitContents);
				txtGdbInit.setEnabled(true);
				gdbInitFilePath.setEnabled(true);
				browseBtn.setEnabled(true);
			}
			else
			{
				chkEnableHeapTracing.setSelection(false);
				gdbInitContents = configuration.getAttribute(ConfigurationAttributes.GDBINIT_CONTENTS, "");
				String filePath = configuration.getAttribute(ConfigurationAttributes.GDBINIT_FILE_PATH, "");
				gdbInitFilePath.setText(filePath);
				txtGdbInit.setText(gdbInitContents);
				txtGdbInit.setEnabled(false);
				gdbInitFilePath.setEnabled(false);
				browseBtn.setEnabled(false);
			}
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration)
	{
		configuration.setAttribute(ConfigurationAttributes.HEAP_TRACING_ENABLED, chkEnableHeapTracing.getSelection());
		configuration.setAttribute(ConfigurationAttributes.GDBINIT_CONTENTS, txtGdbInit.getText());
		configuration.setAttribute(ConfigurationAttributes.GDBINIT_FILE_PATH, gdbInitFilePath.getText());
		try
		{
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null));
			FileUtil.writeFile(project, "build/gdbinit", txtGdbInit.getText(), false);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}

	@Override
	public String getName()
	{
		return TAB_NAME;
	}

	@Override
	public String getId()
	{
		return TAB_ID;
	}

	private class CheckBoxSelectionListener extends SelectionAdapter
	{
		@Override
		public void widgetSelected(SelectionEvent e)
		{
			if (chkEnableHeapTracing.getSelection())
			{
				txtGdbInit.setEnabled(true);
				gdbInitFilePath.setEnabled(true);
				browseBtn.setEnabled(true);

			}
			else
			{
				txtGdbInit.setEnabled(false);
				browseBtn.setEnabled(false);
				gdbInitFilePath.setEnabled(false);
			}
		}
	}

}
