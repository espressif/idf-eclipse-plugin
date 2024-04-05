/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.lsp.preferences;

import org.eclipse.cdt.lsp.editor.LanguageServerEnable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.osgi.service.component.annotations.Component;

import com.espressif.idf.core.IDFProjectNature;
import com.espressif.idf.core.logging.Logger;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 */
@Component(property = { "service.ranking:Integer=100" })
public class IDFClangdEnable implements LanguageServerEnable
{

	@Override
	public boolean isEnabledFor(IProject project)
	{
		if (project != null)
		{
			try
			{
				return project.hasNature(IDFProjectNature.ID); // IDF nature
			}
			catch (CoreException e)
			{
				Logger.log(e);
			}
		}
		return false;
	}

}
