/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.installcomponents.container;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;

import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.installcomponents.deserializer.ComponentsDeserializer;
import com.espressif.idf.ui.installcomponents.vo.ComponentVO;
import com.google.gson.GsonBuilder;

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

	private ScrolledComposite scrolledParent;
	private Composite containerComposite;

	public InstallComponentsCompositePage(IFile componentsJsonFile, IProject project)
	{
		this.componentsJsonFile = componentsJsonFile;
		this.project = project;
		componentContainers = new ArrayList<>();
	}

	public void createControls(Composite parent) throws IOException
	{
		this.containerComposite = parent;

		// Try to find the ScrolledComposite parent for resizing later
		if (parent.getParent() instanceof ScrolledComposite parentScrolledComposite)
		{
			this.scrolledParent = parentScrolledComposite;
		}

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
				if ((!StringUtil.isEmpty(ymlEntry)) && ymlEntry.charAt(0) == '#') // $NON-NLS-1$
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
		var eclipseFile = componentsJsonFile;
		var physicalFile = eclipseFile.getLocation().toFile();

		var charset = StandardCharsets.UTF_8;
		try
		{
			charset = Charset.forName(eclipseFile.getCharset());
		}
		catch (
				CoreException
				| IllegalArgumentException ignored)
		{
		}

		try (var reader = new FileReader(physicalFile, charset))
		{
			var gson = new GsonBuilder().registerTypeAdapter(ArrayList.class, new ComponentsDeserializer())
					.setPrettyPrinting().disableHtmlEscaping().create();

			componentVOs = gson.fromJson(reader, ArrayList.class);
		}
	}

	public void dispose()
	{
		for (ComponentContainer componentContainer : componentContainers)
		{
			componentContainer.dispose();
		}
	}

	public void filterComponents(String query)
	{
		String searchString = (query == null) ? "" : query.toLowerCase().trim(); //$NON-NLS-1$
		boolean layoutNeeded = false;

		for (ComponentContainer container : componentContainers)
		{
			ComponentVO vo = container.getComponentVO();
			if (vo == null)
				continue;

			String name = vo.getName();

			boolean matches = StringUtil.isEmpty(searchString)
					|| (name != null && name.toLowerCase().contains(searchString));

			if (container.setVisible(matches))
			{
				layoutNeeded = true;
			}
		}

		if (layoutNeeded && containerComposite != null && !containerComposite.isDisposed())
		{
			containerComposite.layout(true, true);

			if (scrolledParent != null)
			{
				int width = scrolledParent.getClientArea().width;
				scrolledParent.setMinSize(containerComposite.computeSize(width, SWT.DEFAULT));
			}
		}
	}
}
