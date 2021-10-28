/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.installcomponents.container;

import java.awt.Desktop;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
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
	private Composite parent;
	private Group controlGroup;
	private Text detailsText;
	private Label targetsLabel;
	private Label versionLabel;
	private Button installButton;
	private Button openReadMe;

	public ComponentContainer(ComponentVO componentVO, Composite parent)
	{
		this.componentVO = componentVO;
		this.parent = parent;
	}

	public Point createControl()
	{
		Color whiteColor = new Color(255, 255, 255);
		controlGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		controlGroup.setLayout(new GridLayout());
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		controlGroup.setLayoutData(layoutData);
		controlGroup.setBackground(new Color(240, 233, 233));
		controlGroup.setText(componentVO.getName().toUpperCase());
		Font boldFont = new Font(controlGroup.getDisplay(), new FontData("Arial", 8, SWT.BOLD)); //$NON-NLS-1$
		controlGroup.setFont(boldFont);

		if (componentVO.getComponentDetails() != null && componentVO.getComponentDetails().getDescription() != null)
		{
			detailsText = new Text(controlGroup, SWT.MULTI | SWT.WRAP);
			detailsText.setLayoutData(new GridData(GridData.FILL_BOTH));
			detailsText.setBackground(new Color(240, 233, 233));
			detailsText.setText(componentVO.getComponentDetails().getDescription());
		}

		if (componentVO.getComponentDetails() != null && componentVO.getComponentDetails().getTargets() != null)
		{
			targetsLabel = new Label(controlGroup, SWT.NONE);
			StringBuilder sbTargets = new StringBuilder();
			targetsLabel.setBackground(whiteColor);
			sbTargets.append(componentVO.getComponentDetails().getTargets().get(0));

			for (int i = 0; i < componentVO.getComponentDetails().getTargets().size(); i++)
			{
				sbTargets.append(", "); //$NON-NLS-1$
				sbTargets.append(componentVO.getComponentDetails().getTargets().get(i));
			}

			targetsLabel.setText(sbTargets.toString());
		}

		if (componentVO.getComponentDetails() != null
				&& !StringUtil.isEmpty(componentVO.getComponentDetails().getVersion()))
		{
			versionLabel = new Label(controlGroup, SWT.NONE);
			versionLabel.setBackground(new Color(240, 233, 233));
			versionLabel.setText(componentVO.getComponentDetails().getVersion());
		}

		Composite btnComposite = new Composite(controlGroup, SWT.NONE);
		btnComposite.setBackground(new Color(240, 233, 233));
		btnComposite.setLayout(new GridLayout(2, true));

		openReadMe = new Button(btnComposite, SWT.PUSH);
		openReadMe.setText(Messages.InstallComponents_OpenReadmeButton);
		openReadMe.setBackground(whiteColor);
		openReadMe.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				String url = componentVO.getComponentDetails().getReadMe();
				try
				{
					Desktop.getDesktop().browse(new URL(url).toURI());
				}
				catch (Exception e1)
				{
					Logger.log(e1);
				}
			}
		});

		installButton = new Button(btnComposite, SWT.PUSH);
		installButton.setText(Messages.InstallComponents_InstallButton);
		if (componentVO.isComponentAdded())
		{
			installButton.setText(Messages.InstallComponents_InstallButtonAlreadyAdded);	
			installButton.setEnabled(false);
		}
		installButton.setBackground(whiteColor);
		InstallCommandHandler installCommandHandler = new InstallCommandHandler(componentVO.getName(),
				componentVO.getNamespace(),
				componentVO.getComponentDetails() != null && componentVO.getComponentDetails().getVersion() != null
						? componentVO.getComponentDetails().getVersion()
						: "");
		installButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				try
				{
					installCommandHandler.executeInstallCommand();
				}
				catch (Exception e1)
				{
					Logger.log(e1);
				}
			}
		});

		return controlGroup.getSize();
	}
}
