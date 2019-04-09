package com.espressif.idf.sdk.config.ui;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorReference;

public class SDKConfigEdittorMatchingStrategy implements IEditorMatchingStrategy
{

	@Override
	public boolean matches(IEditorReference editorRef, IEditorInput input)
	{
		if (editorRef.getId().equals("com.espressif.idf.sdk.config.ui.editor"))
		{
			return true;
		}
		return false;
	}

}
