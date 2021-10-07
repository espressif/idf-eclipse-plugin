package com.espressif.idf.ui.installcomponents.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.espressif.idf.ui.installcomponents.Messages;
import com.espressif.idf.ui.installcomponents.vo.ComponentVO;

public class InstallIDFComponentsDialog extends TitleAreaDialog
{
	private final List<ComponentVO> componentVOs;

	public InstallIDFComponentsDialog(Shell parentShell, List<ComponentVO> componentVOs)
	{
		super(parentShell);
		this.componentVOs = componentVOs;
	}
	
	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(3, false);
		container.setLayout(layout);
		GridData comboLayoutData = new GridData();
		comboLayoutData.grabExcessHorizontalSpace = true;
		comboLayoutData.horizontalAlignment = GridData.FILL;
		comboLayoutData.horizontalSpan = 2;

		return container;
	}

	@Override
	public void create()
	{
		super.create();
		setTitle(Messages.InstallIDFComponentsDialog_Title);
		setMessage(Messages.InstallIDFComponentsDialog_InformationMessage, IMessageProvider.INFORMATION);
	}

}
