package com.espressif.idf.core.util;

import org.eclipse.core.internal.registry.osgi.OSGIUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.espressif.idf.core.logging.Logger;

@SuppressWarnings("restriction")
public class GcovUtility
{
	private static IProject selectedProject;
	
	
	public static void setSelectedProject(IProject project)
	{
		selectedProject = project;
	}
	
	public static IProject getSelectedProject()
	{
		return selectedProject;
	}
	
	public static void clearSelectedProject()
	{
		selectedProject = null;
	}
	
	public static void setUpDialog(IFile gcFile, String elfFile)
	{
		try
		{
			Bundle bundle = OSGIUtils.getDefault().getBundle("org.eclipse.linuxtools.gcov.core"); //$NON-NLS-1$
			Class<?> openGcAction = bundle.loadClass("org.eclipse.linuxtools.internal.gcov.action.OpenGCAction"); //$NON-NLS-1$
			Class<?> openGcDialog = bundle.loadClass("org.eclipse.linuxtools.internal.gcov.dialog.OpenGCDialog"); //$NON-NLS-1$
			IDialogSettings ds = PlatformUI.getDialogSettingsProvider(FrameworkUtil.getBundle(openGcAction))
					.getDialogSettings();
	        IDialogSettings defaultMapping = ds.getSection(openGcDialog.getName());
	        if (defaultMapping == null) {
	            defaultMapping = ds.addNewSection(openGcDialog.getName());
	        }
	        
	        ds.put(gcFile.getRawLocation().toOSString(), elfFile);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}
}
