/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.resources.OpenDialogListenerSupport;
import com.espressif.idf.core.resources.PopupDialog;
import com.espressif.idf.core.tools.EimConstants;
import com.espressif.idf.core.tools.SetupToolsInIde;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.tools.manager.pages.ESPIDFMainTablePage;

/**
 * Listener for {@link SetupToolsInIde}
 * @author Ali Azam Rana
 *
 */
public class SetupToolsJobListener extends JobChangeAdapter
{
	private ESPIDFMainTablePage espidfMainTablePage;
	private SetupToolsInIde setupToolsInIde;
	
	public SetupToolsJobListener(ESPIDFMainTablePage espidfMainTablePage, SetupToolsInIde setupToolsInIde)
	{
		this.espidfMainTablePage = espidfMainTablePage;
		this.setupToolsInIde = setupToolsInIde;
	}
	
	public SetupToolsJobListener()
	{
	}

	@Override
	public void aboutToRun(IJobChangeEvent event)
	{
		OpenDialogListenerSupport.getSupport().firePropertyChange(PopupDialog.DISABLE_LAUNCHABAR_EVENTS.name(), null,
				null);
	}

	@Override
	public void done(IJobChangeEvent event)
	{
		Preferences scopedPreferenceStore = InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID);
		
		if (event.getResult().getSeverity() != IStatus.OK)
		{
			// Rollback all the changes
			setupToolsInIde.rollback();
		}
		
		if (event.getResult().isOK())
		{
			scopedPreferenceStore.putBoolean(EimConstants.INSTALL_TOOLS_FLAG, true);
		}
		else 
		{
			scopedPreferenceStore.putBoolean(EimConstants.INSTALL_TOOLS_FLAG, false);	
		}
		
		try
		{
			scopedPreferenceStore.flush();
		}
		catch (BackingStoreException e)
		{
			Logger.log(e);
		}
		
		Display.getDefault().asyncExec(() -> {
			if (espidfMainTablePage != null)
			{
				espidfMainTablePage.refreshEditorUI();
			}
		});
		OpenDialogListenerSupport.getSupport().firePropertyChange(PopupDialog.ENABLE_LAUNCHBAR_EVENTS.name(), null,
				null);
	}
}
