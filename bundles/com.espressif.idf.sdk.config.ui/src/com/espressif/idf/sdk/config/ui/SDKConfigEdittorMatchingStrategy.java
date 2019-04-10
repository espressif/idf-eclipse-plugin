/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.sdk.config.ui;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorReference;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class SDKConfigEdittorMatchingStrategy implements IEditorMatchingStrategy
{

	@Override
	public boolean matches(IEditorReference editorRef, IEditorInput input)
	{
		if (editorRef.getId().equals(SDKConfigurationEditor.EDITOR_ID))
		{
			return true;
		}
		return false;
	}

}
