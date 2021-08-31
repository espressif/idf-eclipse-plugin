/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.IDFProjectNature;
import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.launch.serial.core.IDFProjectLaunchDescriptor;
import com.espressif.idf.ui.EclipseUtil;

public class IDFDebugLaunchDescriptorType implements ILaunchDescriptorType
{

	public static final String ID = "com.espressif.idf.debug.gdbjtag.openocd.descriptorType"; //$NON-NLS-1$

	private Map<ILaunchConfiguration, IDFProjectLaunchDescriptor> descriptors = new HashMap<>();

	@Override
	public ILaunchDescriptor getDescriptor(Object launchObject)
	{
		if (!(launchObject instanceof ILaunchConfiguration))
		{
			return null;
		}
		ILaunchConfiguration config = (ILaunchConfiguration) launchObject;
		try
		{
			ILaunchConfigurationType type = config.getType();
			String identifier = type.getIdentifier();
			if (identifier.equals(IDFLaunchConstants.DEBUG_LAUNCH_CONFIG_TYPE))
			{
				IProject project = getProject();
				project = project != null ? project : config.getMappedResources()[0].getProject();
				if (IDFProjectNature.hasNature(project))
				{
					IDFProjectLaunchDescriptor descriptor = descriptors.get(config);
					if (descriptor == null)
					{
						descriptor = new IDFProjectLaunchDescriptor(this, project, (ILaunchConfiguration) launchObject);
						descriptors.put(config, descriptor);
					}
					return descriptor;
				}
			}
		}
		catch (CoreException ce)
		{
			Logger.log(ce);
		}

		return null;
	}

	private IProject getProject()
	{
		List<IProject> projectList = new ArrayList<>(1);
		Display.getDefault().syncExec(new Runnable()
		{

			@Override
			public void run()
			{
				IProject project = EclipseUtil.getSelectedProjectInExplorer();
				projectList.add(project);
			}
		});
		IProject project = projectList.get(0);
		return project;
	}

}
