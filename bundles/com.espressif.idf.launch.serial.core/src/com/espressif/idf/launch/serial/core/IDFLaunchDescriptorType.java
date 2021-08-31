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
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.IDFProjectNature;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.EclipseUtil;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFLaunchDescriptorType implements ILaunchDescriptorType {

	private Map<ILaunchConfiguration, ILaunchDescriptor> descriptors = new HashMap<>();

	@Override
	public ILaunchDescriptor getDescriptor(Object launchObject) throws CoreException {
		if (launchObject instanceof IProject) {
			IProject project = (IProject) launchObject;
			if (launchObject instanceof IProject && IDFProjectNature.hasNature((IProject) launchObject)) {
				return new IDFProjectLaunchDescriptor(this, project, null);
			}
		} else if (launchObject instanceof ILaunchConfiguration) {
			ILaunchConfiguration config = (ILaunchConfiguration) launchObject;
			String identifier = config.getType().getIdentifier();
			if (identifier.equals(IDFLaunchConstants.DEBUG_LAUNCH_CONFIG_TYPE)) {
				return null;
			}
			IProject project = getProject();
			project = project != null ? project : config.getMappedResources()[0].getProject();
			try {
				if (IDFProjectNature.hasNature(project)) {
					ILaunchDescriptor descriptor = descriptors.get(config);
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

	protected IProject getProject() {
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

}
