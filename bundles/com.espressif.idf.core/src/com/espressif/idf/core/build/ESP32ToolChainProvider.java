/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.build;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.build.gcc.core.GCCToolChain.GCCInfo;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.core.runtime.CoreException;

import com.aptana.core.ShellExecutable;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.IdfLog;
import com.espressif.idf.core.util.StringUtil;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class ESP32ToolChainProvider implements IToolChainProvider
{

	private static final String PATH = "PATH"; //$NON-NLS-1$
	public static final String ID = "com.espressif.idf.core.esp32.toolchainprovider"; //$NON-NLS-1$
	private static final Pattern gccPattern = Pattern.compile("xtensa-esp32-elf-gcc(\\.exe)?"); //$NON-NLS-1$

	@Override
	public String getId()
	{
		return ID;
	}

	@Override
	public void init(IToolChainManager manager) throws CoreException
	{

		List<String> paths = getAllPaths();
		for (String path : paths)
		{
			for (String dirStr : path.split(File.pathSeparator))
			{
				File dir = new File(dirStr);
				if (dir.isDirectory())
				{
					for (File file : dir.listFiles())
					{
						if (file.isDirectory())
						{
							continue;
						}
						Matcher matcher = gccPattern.matcher(file.getName());
						if (matcher.matches())
						{
							try
							{
								GCCInfo info = new GCCInfo(file.toString());
								if (info.target != null && info.version != null)
								{
									String[] tuple = info.target.split("-"); //$NON-NLS-1$
									if (tuple.length > 2) // xtensa-esp32-elf
									{
										ESP32ToolChain gcc = new ESP32ToolChain(this, file.toPath());
										try
										{
											if (manager.getToolChain(gcc.getTypeId(), gcc.getId()) == null)
											{
												// Only add if another provider hasn't already added it
												if (matcher.matches())
												{
													manager.addToolChain(gcc);
												}
											}
										}
										catch (CoreException e)
										{
											CCorePlugin.log(e.getStatus());
										}
									}
								}
							}
							catch (IOException e)
							{
								IdfLog.logError(IDFCorePlugin.getPlugin(), e);
							}
						}
					}
				}
			}
		}

	}

	protected List<String> getAllPaths()
	{
		List<String> paths = new ArrayList<String>();

		String path = new IDFEnvironmentVariables().getEnvValue(PATH);
		if (!StringUtil.isEmpty(path))
		{
			paths.add(path);
		}

		path = System.getenv(PATH);
		if (!StringUtil.isEmpty(path))
		{
			paths.add(path);
		}

		path = ShellExecutable.getEnvironment().get(PATH);
		if (StringUtil.isEmpty(path))
		{
			paths.add(path);
		}

		return paths;
	}

}