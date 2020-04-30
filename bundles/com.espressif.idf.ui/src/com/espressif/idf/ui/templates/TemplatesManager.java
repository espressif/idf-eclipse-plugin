/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.templates;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.util.StringUtil;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class TemplatesManager implements ITemplateManager
{

	private TemplatesReader templateReader;

	public TemplatesManager()
	{
		templateReader = new TemplatesReader();
	}

	@Override
	public ITemplateNode getTemplates()
	{
		return templateReader.getTemplates();
	}

	@Override
	public String getDescription(ITemplateNode template) throws IOException
	{
		String readmePath = template.getFilePath().getAbsolutePath() + IPath.SEPARATOR
				+ IDFConstants.PROJECT_DESC_IDENTIFIER;
		if (new File(readmePath).isFile())
		{
			return read(readmePath);
		}
		return StringUtil.EMPTY; // $NON-NLS-1$
	}

	/**
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	protected String read(String filePath) throws IOException
	{
		String data = new String(Files.readAllBytes(Paths.get(filePath)));
		return data;
	}

	/**
	 * Search for the template node for a given templateID only at the root level
	 * 
	 * @param templateId unique identifier for template
	 * @return TemplateNode for given templateId if found, otherwise <b>null</b> will be returned.
	 */
	public ITemplateNode getTemplateNode(String templateId)
	{
		ITemplateNode root = templateReader.getTemplates();
		List<ITemplateNode> children = root.getChildren();
		for (ITemplateNode iTemplateNode : children)
		{
			if (iTemplateNode.getType() == IResource.PROJECT && iTemplateNode.getName().equals(templateId))
			{
				return iTemplateNode;
			}
		}
		return null;

	}
}
