package com.espressif.idf.core.tools;

import java.io.File;
import java.io.FileReader;
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

public class ToolSetConfigurationImporter
{
	private List<IDFToolSet> idfToolSets;
	private Gson gson;

	public ToolSetConfigurationImporter()
	{
		gson = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create();
	}
	
	public List<IDFToolSet> getIdfToolSets(boolean loadToolchains)
	{
		if (idfToolSets == null || idfToolSets.isEmpty())
		{
			idfToolSets = importToolSets();
		}

		if (loadToolchains)
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
			Logger.log("Using: " + idfPath + " to find toolchains" );  //$NON-NLS-1$//$NON-NLS-2$
			Logger.log("Env used: " + idfToolSet.getEnvVars()); //$NON-NLS-1$
			List<ESPToolchain> espToolChains = espToolChainManager
					.getStdToolChains(Arrays.asList(pathToLookForToolChains.split(File.pathSeparator)), idfPath);
			idfToolSet.setEspStdToolChains(espToolChains);
			List<ICMakeToolChainFile> cMakeToolChainFiles = espToolChainManager.getCmakeToolChains(idfPath);
			idfToolSet.setEspCmakeToolChainFiles(cMakeToolChainFiles);
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
}
