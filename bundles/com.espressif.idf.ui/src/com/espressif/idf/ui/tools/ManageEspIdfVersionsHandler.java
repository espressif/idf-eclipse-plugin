package com.espressif.idf.ui.tools;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.ui.handlers.EclipseHandler;
import com.espressif.idf.ui.tools.manager.ESPIDFManagerEditor;


public class ManageEspIdfVersionsHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
//		Map<String, String> existingVarMap = loadExistingVars();
//		boolean exisitngInstallPreferencesStatus = InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID)
//				.getBoolean(IToolsInstallationWizardConstants.INSTALL_TOOLS_FLAG, false);
//		IdfManagerWizard idfManagerWizard = new IdfManagerWizard(existingVarMap);
//		IdfManagerWizardDialog idfManagerWizardDialog = new IdfManagerWizardDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), 
//				idfManagerWizard);
//		idfManagerWizardDialog.open();
		launchEditor();
		return null;
	}

	private Map<String, String> loadExistingVars()
	{
		IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
		return new HashMap<>(idfEnvironmentVariables.getEnvMap());
	}
	
	private void launchEditor()
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				IWorkbenchWindow activeww = EclipseHandler.getActiveWorkbenchWindow();
				try
				{
					IDE.openEditor(activeww.getActivePage(), new CustomEditorInput(), ESPIDFManagerEditor.EDITOR_ID);
				}
				catch (PartInitException e)
				{
					Logger.log(e);
				}
			}
		});
	}
	
	private class CustomEditorInput implements IEditorInput {
	    @Override
	    public boolean exists() {
	        return false;
	    }

	    @Override
	    public ImageDescriptor getImageDescriptor() {
	        return null;
	    }

	    @Override
	    public String getName() {
	        return "New Editor";
	    }

	    @Override
	    public IPersistableElement getPersistable() {
	        return null;
	    }

	    @Override
	    public String getToolTipText() {
	        return "Custom Editor Without File";
	    }

	    @Override
	    public <T> T getAdapter(Class<T> adapter) {
	        return null;
	    }
	}

}
