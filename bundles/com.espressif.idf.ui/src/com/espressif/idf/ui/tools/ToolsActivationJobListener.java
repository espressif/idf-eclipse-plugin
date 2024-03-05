package com.espressif.idf.ui.tools;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.resources.OpenDialogListenerSupport;
import com.espressif.idf.core.resources.PopupDialog;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.tools.manager.pages.ESPIDFMainTablePage;
import com.espressif.idf.ui.update.InstallToolsHandler;

public class ToolsActivationJobListener extends JobChangeAdapter
{
	private ESPIDFMainTablePage espidfMainTablePage;
	
	public ToolsActivationJobListener(ESPIDFMainTablePage espidfMainTablePage)
	{
		this.espidfMainTablePage = espidfMainTablePage;
	}
	
	public ToolsActivationJobListener()
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
		Display.getDefault().asyncExec(() -> {
			if (espidfMainTablePage != null)
			{
				espidfMainTablePage.refreshTable();
			}
		});
		OpenDialogListenerSupport.getSupport().firePropertyChange(PopupDialog.ENABLE_LAUNCHBAR_EVENTS.name(), null,
				null);
		
		Preferences scopedPreferenceStore = InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID);
		if (event.getResult().isOK())
		{
			scopedPreferenceStore.putBoolean(InstallToolsHandler.INSTALL_TOOLS_FLAG, true);
		}
		else 
		{
			scopedPreferenceStore.putBoolean(InstallToolsHandler.INSTALL_TOOLS_FLAG, false);	
		}
		
		try
		{
			scopedPreferenceStore.flush();
		}
		catch (BackingStoreException e)
		{
			Logger.log(e);
		}
	}
}
