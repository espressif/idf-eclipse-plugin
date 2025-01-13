package com.espressif.idf.ui.tools.manager;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.espressif.idf.core.tools.vo.EimJson;

public class EimEditorInput implements IEditorInput
{
	private EimJson eimJson;
	public EimEditorInput(EimJson eimJson)
	{
		this.eimJson = eimJson;
	}

	public EimJson getEimJson()
	{
		return eimJson;
	}
	
	@Override
	public <T> T getAdapter(Class<T> adapter)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exists()
	{
		return true;
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		return null;
	}

	@Override
	public String getName()
	{
		return null;
	}

	@Override
	public IPersistableElement getPersistable()
	{
		return null;
	}

	@Override
	public String getToolTipText()
	{
		return null;
	}

}
