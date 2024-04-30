/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.toolchain.ESPToolChainManager;
import com.espressif.idf.core.toolchain.ESPToolchain;
import com.espressif.idf.core.tools.vo.IDFToolSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * The class is responsible for managing the idf tool sets 
 * export and import configuration params from json in workspace
 * 
 * @author Ali Azam Rana
 *
 */
public class ToolSetConfigurationManager
{
	private List<IDFToolSet> idfToolSets;
	private Gson gson;
	private boolean reload;

	public ToolSetConfigurationManager()
	{
		gson = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization()
				.excludeFieldsWithoutExposeAnnotation().create();
	}

	public void delete(IDFToolSet idfToolSet)
	{
		reload = true;
		getIdfToolSets(false);
		List<IDFToolSet> idfToolSetsToExport = new ArrayList<IDFToolSet>();
		for (IDFToolSet idfTool : idfToolSets)
		{
			if (idfTool.getIdfLocation().equals(idfToolSet.getIdfLocation()))
			{
				continue;
			}

			idfToolSetsToExport.add(idfTool);
		}

		try (FileWriter fileWriter = new FileWriter(toolSetConfigFilePath()))
		{
			gson.toJson(idfToolSetsToExport, fileWriter);
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
		getIdfToolSets(false);
		reload = false;
	}

	public boolean isToolSetAlreadyPresent(String idfPath)
	{
		List<IDFToolSet> idfToolSets = getIdfToolSets(false);
		if (idfToolSets == null)
		{
			return false;
		}
		return idfToolSets.stream().filter(toolSet -> toolSet.getIdfLocation().equals(idfPath)).findAny().isPresent();
	}

	public List<IDFToolSet> getIdfToolSets(boolean loadToolchains)
	{
		if (reload || idfToolSets == null || idfToolSets.isEmpty())
		{
			idfToolSets = importToolSets();
		}

		if (loadToolchains && idfToolSets != null)
		{
			try
			{
				loadToolChainsInImportedToolSets();
			}
			catch (CoreException e)
			{
				Logger.log(e);
			}
		}
		return idfToolSets;
	}

	private List<IDFToolSet> importToolSets()
	{
		Type listType = new TypeToken<ArrayList<IDFToolSet>>()
		{
		}.getType();
		List<IDFToolSet> idfToolSets = new ArrayList<>();
		File toolSetFile = new File(toolSetConfigFilePath());
		if (!toolSetFile.exists())
		{
			try
			{
				toolSetFile.createNewFile();
			}
			catch (IOException e)
			{
				Logger.log(e);
			}
		}

		try (FileReader fileReader = new FileReader(toolSetConfigFilePath()))
		{
			idfToolSets = gson.fromJson(fileReader, listType);
		}
		catch (IOException e)
		{
			Logger.log(e);
		}

		return idfToolSets;
	}

	private void loadToolChainsInImportedToolSets() throws CoreException
	{
		ESPToolChainManager espToolChainManager = new ESPToolChainManager();
		for (IDFToolSet idfToolSet : idfToolSets)
		{
			String pathToLookForToolChains = idfToolSet.getEnvVars().get(IDFEnvironmentVariables.PATH);
			String idfPath = idfToolSet.getEnvVars().get(IDFEnvironmentVariables.IDF_PATH);
			Logger.log("Using: " + idfPath + " to find toolchains"); //$NON-NLS-1$//$NON-NLS-2$
			Logger.log("Env used: " + idfToolSet.getEnvVars()); //$NON-NLS-1$
			List<ESPToolchain> espToolChains = espToolChainManager
					.getStdToolChains(Arrays.asList(pathToLookForToolChains.split(File.pathSeparator)), idfPath);
			idfToolSet.setEspStdToolChains(espToolChains);
			List<ICMakeToolChainFile> cMakeToolChainFiles = espToolChainManager.getCmakeToolChains(idfPath);
			idfToolSet.setEspCmakeToolChainFiles(cMakeToolChainFiles);
		}
	}

	/**
	 * Looks for the existing idf tools from the given location if they are found it replaces the previous one
	 * 
	 * @param idfToolSet IDFToolSet to add {@link IDFToolSet}@
	 */
	public void export(IDFToolSet idfToolSet)
	{
		File toolSetConfigFile = new File(toolSetConfigFilePath());
		List<IDFToolSet> idfToolSets = null;
		Type listType = new TypeToken<ArrayList<IDFToolSet>>()
		{
		}.getType();

		if (toolSetConfigFile.exists())
		{
			try (FileReader fileReader = new FileReader(toolSetConfigFile))
			{
				idfToolSets = gson.fromJson(fileReader, listType);
			}
			catch (IOException e)
			{
				Logger.log(e);
				return;
			}
		}
		if (idfToolSets == null)
		{
			idfToolSets = new ArrayList<>();
		}

		// If the toolSet to be exported is active, set all others to inactive
		if (idfToolSet.isActive())
		{
			for (IDFToolSet toolSet : idfToolSets)
			{
				toolSet.setActive(false); // Set all to inactive
			}
		}

		boolean found = false;
		for (int i = 0; i < idfToolSets.size(); i++)
		{
			IDFToolSet existingToolSet = idfToolSets.get(i);
			if (existingToolSet.getId() == idfToolSet.getId())
			{
				idfToolSets.set(i, idfToolSet);
				found = true;
				break;
			}
		}

		// If the toolSet was not found, add it to the list
		if (!found)
		{
			idfToolSets.add(idfToolSet);
		}

		try (FileWriter fileWriter = new FileWriter(toolSetConfigFile))
		{
			gson.toJson(idfToolSets, fileWriter);
		}
		catch (IOException e)
		{
			Logger.log(e);
		}

	}

	public void updateToolSetConfiguration(IDFToolSet idfToolSet)
	{
		reload = true;
		getIdfToolSets(false);
		List<IDFToolSet> idfToolSetsToExport = new ArrayList<IDFToolSet>();
		for (IDFToolSet existingIdfToolSet : idfToolSets)
		{
			if (idfToolSet.getId() == existingIdfToolSet.getId())
			{
				idfToolSetsToExport.add(idfToolSet);
			}
			else
			{
				idfToolSetsToExport.add(existingIdfToolSet);
			}
		}

		try (FileWriter fileWriter = new FileWriter(toolSetConfigFilePath()))
		{
			gson.toJson(idfToolSets, fileWriter);
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
	}

	private String toolSetConfigFilePath()
	{
		IPath path = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(path.toOSString());
		stringBuilder.append(File.separatorChar);
		stringBuilder.append(IToolsInstallationWizardConstants.TOOL_SET_CONFIG_FILE);
		return stringBuilder.toString();
	}

	public void setReload(boolean reload)
	{
		this.reload = reload;
	}
}
