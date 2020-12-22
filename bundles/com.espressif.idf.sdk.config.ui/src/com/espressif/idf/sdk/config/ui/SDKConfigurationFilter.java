package com.espressif.idf.sdk.config.ui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.PatternFilter;

import com.espressif.idf.sdk.config.core.KConfigMenuItem;

public class SDKConfigurationFilter extends PatternFilter
{
	@Override
	protected boolean isLeafMatch(Viewer viewer, Object element)
	{

		if (element instanceof KConfigMenuItem)
		{
			KConfigMenuItem highLevelElem = (KConfigMenuItem) element;

			return wordMatches(highLevelElem.getTitle()) || recursiveMatch(highLevelElem);
		}

		return false;
	}

	private boolean recursiveMatch(KConfigMenuItem parent)
	{
		if (parent.getChildren().size() == 0)
			return wordMatches(parent.getName());

		for (KConfigMenuItem child : parent.getChildren())
		{
			if (wordMatches(parent.getName()) || recursiveMatch(child))
			{
				return true;
			}
		}
		return false;
	}
}
