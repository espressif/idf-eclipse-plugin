/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.installcomponents.container;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Composite;

import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.installcomponents.deserializer.ComponentsDeserializer;
import com.espressif.idf.ui.installcomponents.vo.ComponentVO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

/**
 * Composite for components install editor page
 * 
 * @author Ali Azam Rana
 *
 */
public class InstallComponentsCompositePage
{
	private IFile componentsJsonFile;
	private IProject project;
	private List<ComponentVO> componentVOs;
	private List<ComponentContainer> componentContainers;

	public InstallComponentsCompositePage(IFile componentsJsonFile, IProject project)
	{
		this.componentsJsonFile = componentsJsonFile;
		this.project = project;
		componentContainers = new ArrayList<ComponentContainer>();
	}

	public void createControls(Composite parent) throws IOException
	{
		loadComponents();
		
		for (ComponentVO componentVO : componentVOs)
		{
			setComponentAdded(componentVO);
			ComponentContainer componentContainer = new ComponentContainer(componentVO, parent, project);
			componentContainer.createControl();
			componentContainers.add(componentContainer);
		}
	}

	private void setComponentAdded(ComponentVO componentVO) throws IOException
	{
		String toMatch = componentVO.getNamespace().concat("/").concat(componentVO.getName()); //$NON-NLS-1$
		IFile file = componentsJsonFile.getProject().getFolder("main").getFile("idf_component.yml"); //$NON-NLS-1$ //$NON-NLS-2$
		if (file.exists())
		{
			List<String> ymlEntries = Files.readAllLines(file.getLocation().toFile().toPath());
			for (String ymlEntry : ymlEntries)
			{
				if ((!StringUtil.isEmpty(ymlEntry)) && ymlEntry.charAt(0) == '#') //$NON-NLS-1$
				{
					continue;
				}
				
				if (ymlEntry.contains(toMatch))
				{
					componentVO.setComponentAdded(true);
					return;
				}
			}
		}
	}

	private void loadComponents() throws IOException
	{
		String jsonString = Files.readString(componentsJsonFile.getLocation().toFile().toPath());
		Gson gson = new GsonBuilder().registerTypeAdapter(ArrayList.class, new ComponentsDeserializer())
				.setPrettyPrinting().disableHtmlEscaping().create();
		JsonArray jsonArray = gson.fromJson(jsonString, JsonArray.class);
		componentVOs = gson.fromJson(jsonArray.toString(), ArrayList.class);
	}

	public void dispose()
	{
		for (ComponentContainer componentContainer : componentContainers)
		{
			componentContainer.dispose();
		}
	}
}
