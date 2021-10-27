package com.espressif.idf.ui.templates;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.PatternFilter;

public class TemplateListPatternFilter extends PatternFilter
{

	@Override
	protected boolean isLeafMatch(Viewer viewer, Object element)
	{
		ITemplateNode node = (ITemplateNode) element;
		if (node == null)
		{
			return false;
		}
		return super.isLeafMatch(viewer, element) || isLeafMatch(viewer, node.getParent());
	}
}
