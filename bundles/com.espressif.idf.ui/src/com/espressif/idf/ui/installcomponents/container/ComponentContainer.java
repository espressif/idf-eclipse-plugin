/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.installcomponents.container;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.installcomponents.Messages;
import com.espressif.idf.ui.installcomponents.handler.InstallCommandHandler;
import com.espressif.idf.ui.installcomponents.vo.ComponentDetailsVO;
import com.espressif.idf.ui.installcomponents.vo.ComponentVO;

/**
 * Component Container UI element that contains information and controls for that component
 * 
 * @author Ali Azam Rana
 *
 */
public class ComponentContainer
{
	private ComponentVO componentVO;
	private ComponentDetailsVO componentDetailsVO;
	private Composite parent;
	private Group controlGroup;
	private Button installButton;
	private IProject project;
	private Font boldFont;
	private GridData layoutData;

	public ComponentContainer(ComponentVO componentVO, Composite parent, IProject project)
	{
		this.componentVO = componentVO;
		this.parent = parent;
		this.componentDetailsVO = componentVO != null ? componentVO.getComponentDetails() : null;
		this.project = project;
	}

	public ComponentVO getComponentVO()
	{
		return componentVO;
	}

	/**
	 * Shows or hides this component in the list. Updates GridData.exclude to remove whitespace when hidden.
	 * 
	 * @return true if the visibility state actually changed.
	 */
	public boolean setVisible(boolean visible)
	{
		if (controlGroup != null && !controlGroup.isDisposed())
		{
			boolean currentVisible = controlGroup.getVisible();

			if (currentVisible != visible)
			{
				controlGroup.setVisible(visible);

				if (layoutData != null)
				{
					layoutData.exclude = !visible;
				}
				return true;
			}
		}
		return false;
	}

	public Point createControl()
	{
		controlGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		controlGroup.setLayout(new GridLayout());

		layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		controlGroup.setLayoutData(layoutData);

		controlGroup.setText(componentVO.getName().toUpperCase());
		boldFont = new Font(controlGroup.getDisplay(), new FontData("Arial", 8, SWT.BOLD)); //$NON-NLS-1$
		controlGroup.setFont(boldFont);

		if (componentDetailsVO != null && componentDetailsVO.getDescription() != null)
		{
			Text detailsText = new Text(controlGroup, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
			detailsText.setBackground(controlGroup.getBackground());
			detailsText.setLayoutData(new GridData(GridData.FILL_BOTH));
			detailsText.setText(componentVO.getComponentDetails().getDescription());
		}

		if (componentDetailsVO != null && componentDetailsVO.getTargets() != null
				&& !componentDetailsVO.getTargets().isEmpty())
		{
			Label targetsLabel = new Label(controlGroup, SWT.NONE);
			StringBuilder sbTargets = new StringBuilder();
			sbTargets.append(componentVO.getComponentDetails().getTargets().get(0));

			for (int i = 1; i < componentVO.getComponentDetails().getTargets().size(); i++)
			{
				sbTargets.append(", "); //$NON-NLS-1$
				sbTargets.append(componentVO.getComponentDetails().getTargets().get(i));
			}

			targetsLabel.setText(sbTargets.toString());
		}

		if (componentDetailsVO != null
				&& !StringUtil.isEmpty(componentDetailsVO.getVersion()))
		{
			Label versionLabel = new Label(controlGroup, SWT.NONE);
			versionLabel.setText(componentDetailsVO.getVersion());
		}

		Composite btnComposite = new Composite(controlGroup, SWT.NONE);
		btnComposite.setLayout(new GridLayout(2, false));
		
		if (componentDetailsVO != null && !StringUtil.isEmpty(componentDetailsVO.getReadMe()))
		{
			Button openReadMe = new Button(btnComposite, SWT.PUSH);
			openReadMe.setText(Messages.InstallComponents_OpenReadmeButton);
			openReadMe.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					String url = componentDetailsVO.getReadMe();
					try
					{
						if (StringUtil.isEmpty(url))
							return;
						org.eclipse.swt.program.Program.launch(url);
					}
					catch (Exception e1)
					{
						Logger.log(e1);
					}
				}
			});
		}
		
		installButton = new Button(btnComposite, SWT.PUSH);
		GridData installBtnData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		installBtnData.widthHint = 90;
		installButton.setLayoutData(installBtnData);

		installButton.setText(Messages.InstallComponents_InstallButton);
		if (componentVO.isComponentAdded())
		{
			installButton.setText(Messages.InstallComponents_InstallButtonAlreadyAdded);
			installButton.setEnabled(false);
		}

		InstallCommandHandler installCommandHandler = new InstallCommandHandler(componentVO.getName(),
				componentVO.getNamespace(),
				componentDetailsVO != null && componentDetailsVO.getVersion() != null
						? componentDetailsVO.getVersion()
						: "", //$NON-NLS-1$
				project);

		installButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				try
				{
					installCommandHandler.executeInstallCommand();
					installButton.setEnabled(false);
					installButton.setText(Messages.InstallComponents_InstallButtonAlreadyAdded);
				}
				catch (Exception e1)
				{
					Logger.log(e1);
				}
			}
		});

		return controlGroup.getSize();
	}

	public void dispose()
	{
		if (boldFont != null && !boldFont.isDisposed())
		{
			boldFont.dispose();
		}

		if (controlGroup != null && !controlGroup.isDisposed())
		{
			controlGroup.dispose();
		}

		controlGroup = null;
		boldFont = null;
	}
}
