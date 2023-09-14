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

			return wordMatches(highLevelElem.getTitle()) || wordMatches(highLevelElem.getName())
					|| recursiveMatch(highLevelElem);
		}

		return false;
	}

	private boolean recursiveMatch(KConfigMenuItem parent)
	{
		if (parent.getChildren().isEmpty())
		{
			return wordMatches(parent.getName()) || wordMatches(parent.getTitle());
		}

		for (KConfigMenuItem child : parent.getChildren())
		{
			if (wordMatches(child.getName()) || wordMatches(child.getTitle()) || recursiveMatch(child))
			{
				return true;
			}
		}
		return false;
	}
}
