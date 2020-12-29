/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.launch.serial.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.IDFProjectNature;
import com.espressif.idf.ui.EclipseUtil;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFLaunchDescriptorType implements ILaunchDescriptorType {

	@Override
	public ILaunchDescriptor getDescriptor(Object launchObject) throws CoreException {
		if (launchObject instanceof IProject) {
			// Make sure it's a new style build
			IProject project = (IProject) launchObject;
			if (launchObject instanceof IProject && IDFProjectNature.hasNature((IProject) launchObject)) {
				return new IDFProjectLaunchDescriptor(this, project, null);
			}
		} else if (launchObject instanceof ILaunchConfiguration) {
			//get project
			IProject project = getProject();
			if (project != null && IDFProjectNature.hasNature(project)
					&& launchObject instanceof ILaunchConfiguration) {
				return new IDFProjectLaunchDescriptor(this, project, (ILaunchConfiguration) launchObject);
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

}
