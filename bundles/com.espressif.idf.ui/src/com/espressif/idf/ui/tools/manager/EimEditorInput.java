package com.espressif.idf.ui.tools.manager;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.espressif.idf.core.tools.vo.EimJson;

public class EimEditorInput implements IEditorInput
{
	private EimJson eimJson;
	private boolean firstStartup;
	private String FILE_NAME = "idf_eim.json"; //$NON-NLS-1$

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

	public boolean isFirstStartup()
	{
		return firstStartup;
	}

	public void setFirstStartup(boolean firstStartup)
	{
		this.firstStartup = firstStartup;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null || getClass() != obj.getClass())
		{
			return false;
		}
		EimEditorInput that = (EimEditorInput) obj;
		return FILE_NAME.equals(that.FILE_NAME);
	}

	@Override
	public int hashCode()
	{
		return FILE_NAME.hashCode();
	}

}
