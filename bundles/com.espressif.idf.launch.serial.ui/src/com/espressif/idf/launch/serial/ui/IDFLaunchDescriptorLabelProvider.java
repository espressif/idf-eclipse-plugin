/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.launch.serial.ui;

import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;
import org.eclipse.launchbar.ui.DefaultDescriptorLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.espressif.idf.launch.serial.core.IDFLaunchDescriptorType;
import com.espressif.idf.launch.serial.ui.internal.Activator;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFLaunchDescriptorLabelProvider extends DefaultDescriptorLabelProvider {

	@Override
	public Image getImage(Object element) {
		if (element instanceof ILaunchDescriptor) {
			ILaunchDescriptorType type = ((ILaunchDescriptor) element).getType();
			if (type instanceof IDFLaunchDescriptorType) {
				Image image = Activator.getImage("icons/c_app.gif"); //$NON-NLS-1$
				if (image != null) {
					return image;
				}
			}
		}
		return super.getImage(element);
	}
}
