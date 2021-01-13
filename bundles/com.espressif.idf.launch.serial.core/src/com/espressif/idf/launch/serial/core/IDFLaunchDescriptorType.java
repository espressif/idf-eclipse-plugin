/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.launch.serial.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.IDFProjectNature;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.EclipseUtil;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFLaunchDescriptorType implements ILaunchDescriptorType {

	private Map<ILaunchConfiguration, IDFProjectLaunchDescriptor> descriptors = new HashMap<>();

	@Override
	public ILaunchDescriptor getDescriptor(Object launchObject) throws CoreException {
		if (launchObject instanceof IProject) {
			// Make sure it's a new style build
			IProject project = (IProject) launchObject;
			if (launchObject instanceof IProject && IDFProjectNature.hasNature((IProject) launchObject)) {
				return new IDFProjectLaunchDescriptor(this, project, null);
			}
		} else if (launchObject instanceof ILaunchConfiguration) {
			ILaunchConfiguration config = (ILaunchConfiguration) launchObject;
			String identifier = config.getType().getIdentifier();
			if (identifier.equals("com.espressif.idf.debug.gdbjtag.openocd.launchConfigurationType")) //$NON-NLS-1$
			{
				return null;
			}
			IProject project = getProject();
			try {
				if (isPublic(config) && IDFProjectNature.hasNature(project)) {
					IDFProjectLaunchDescriptor descriptor = descriptors.get(config);
					if (descriptor == null) {
						descriptor = new IDFProjectLaunchDescriptor(this, project, (ILaunchConfiguration) launchObject);
						descriptors.put(config, descriptor);
					}
					return descriptor;
				}
			} catch (CoreException ce) {
				Logger.log(ce);
			}
		}
		return null;
	}

	private IProject getProject() {
		List<IProject> projectList = new ArrayList<>(1);
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				IProject project = EclipseUtil.getSelectedProjectInExplorer();
				projectList.add(project);
			}
		});
		IProject project = projectList.get(0);
		return project;
	}

	/**
	 * Used to filter out private and external tools builders
	 *
	 * @param config
	 * @return
	 * @throws CoreException
	 */
	public static boolean isPublic(ILaunchConfiguration config) throws CoreException {
		ILaunchConfigurationType type = config.getType();
		if (type == null) {
			return false;
		}

		String category = type.getCategory();

		return type.isPublic() && !(config.getAttribute(ILaunchManager.ATTR_PRIVATE, false))
				&& !("org.eclipse.ui.externaltools.builder".equals(category)); //$NON-NLS-1$
	}
}
