package com.espressif.idf.ui.tools.manager;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class EimEditorInput implements IEditorInput
{
	private boolean firstStartup;
	private final String fileName = "idf_eim.json"; //$NON-NLS-1$

	public EimEditorInput()
	{
	}

	@Override
	public <T> T getAdapter(Class<T> adapter)
	{
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
		return fileName.equals(that.fileName);
	}

	@Override
	public int hashCode()
	{
		return fileName.hashCode();
	}

}
