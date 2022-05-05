/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.eclipse.core.runtime.Platform;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.FileUtil;
import com.espressif.idf.ui.tools.vo.ToolsVO;

/**
 * Utility class for Tools Management operations
 * 
 * @author Ali Azam Rana
 *
 */
public class ToolsUtility
{
	private static final String SLASH = "\\"; //$NON-NLS-1$

	private static final String FORWARD_SLASH = "/"; //$NON-NLS-1$

	public static final String ESPRESSIF_HOME_DIR = System.getProperty("user.home").concat("/.espressif"); //$NON-NLS-1$ //$NON-NLS-2$

	public static final String ESPRESSIF_HOME_TOOLS_DIR = ESPRESSIF_HOME_DIR.concat("/tools"); //$NON-NLS-1$

	public static boolean isToolInstalled(String name, String versionName)
	{
		File homeDir = new File(ESPRESSIF_HOME_DIR);
		if (!homeDir.exists())
		{
			return false;
		}

		File toolDirectory = new File(
				ESPRESSIF_HOME_TOOLS_DIR.concat(FORWARD_SLASH).concat(name).concat(FORWARD_SLASH).concat(versionName));
		if (toolDirectory.exists())
		{
			IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
			String pathValue = idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.PATH);
			String[] splittedPaths = pathValue.split(File.pathSeparator);
			String directorySplittor = Platform.getOS().equals(Platform.OS_WIN32) ? SLASH : FORWARD_SLASH;
			for (String splittedPath : splittedPaths)
			{
				if (splittedPath.contains(name.concat(directorySplittor).concat(versionName)))
				{
					return true;
				}
			}
		}

		return false;
	}

	public static void removeToolDirectory(String toolName) throws IOException
	{
		File toolDirectory = new File(ESPRESSIF_HOME_TOOLS_DIR.concat(FORWARD_SLASH).concat(toolName));
		if (!toolDirectory.exists())
		{
			return;
		}

		FileUtil.deleteDirectory(toolDirectory);
	}

	public static String getFileExtension(String filename)
	{
		return Optional.ofNullable(filename).filter(f -> f.contains(".")) //$NON-NLS-1$
				.map(f -> f.substring(filename.lastIndexOf(".") + 1)).get(); //$NON-NLS-1$
	}

	public static void extractZip(String zipFilePath, String extractDirectory)
	{
		InputStream inputStream = null;
		try
		{
			Path filePath = Paths.get(zipFilePath);
			inputStream = Files.newInputStream(filePath);
			ArchiveStreamFactory archiveStreamFactory = new ArchiveStreamFactory();
			ArchiveInputStream archiveInputStream = archiveStreamFactory
					.createArchiveInputStream(ArchiveStreamFactory.ZIP, inputStream);
			ArchiveEntry archiveEntry = null;
			while ((archiveEntry = archiveInputStream.getNextEntry()) != null)
			{
				Path path = Paths.get(extractDirectory, archiveEntry.getName());
				File file = path.toFile();
				if (archiveEntry.isDirectory())
				{
					if (!file.isDirectory())
					{
						file.mkdirs();
					}
				}
				else
				{
					File parent = file.getParentFile();
					if (!parent.isDirectory())
					{
						parent.mkdirs();
					}
					try (OutputStream outputStream = Files.newOutputStream(path))
					{
						IOUtils.copy(archiveInputStream, outputStream);
					}
				}
			}
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}

	public static void extractTarGz(String tarFile, String outputDir)
	{
		Path pathInput = Paths.get(tarFile);
		Path pathOutput = Paths.get(outputDir);
		try
		{
			TarArchiveInputStream tararchiveinputstream = new TarArchiveInputStream(
					new GzipCompressorInputStream(new BufferedInputStream(Files.newInputStream(pathInput))));

			ArchiveEntry archiveentry = null;
			while ((archiveentry = tararchiveinputstream.getNextEntry()) != null)
			{
				Path pathEntryOutput = pathOutput.resolve(archiveentry.getName());
				if (archiveentry.isDirectory())
				{
					if (!Files.exists(pathEntryOutput))
						Files.createDirectory(pathEntryOutput);
				}
				else
					Files.copy(tararchiveinputstream, pathEntryOutput);
			}

			tararchiveinputstream.close();
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}

	public static String getReadableSizeMB(double size)
	{
		size /= 1024; // KB
		size /= 1024; // MB
		DecimalFormat df = new DecimalFormat("0.00"); //$NON-NLS-1$
		return String.valueOf(df.format(size)).concat(" MB"); //$NON-NLS-1$
	}

	public static Map<String, String> getAvailableToolVersions(ToolsVO toolsVo)
	{
		Map<String, String> availableVersions = new HashMap<String, String>();
		File toolDirectory = new File(
				ESPRESSIF_HOME_TOOLS_DIR.concat(FORWARD_SLASH).concat(toolsVo.getName()).concat(FORWARD_SLASH));
		if (toolDirectory.exists())
		{
			for (File file : toolDirectory.listFiles())
			{
				if (file.isDirectory())
				{
					availableVersions.put(file.getName(), file.getAbsolutePath());
				}
			}
		}

		return availableVersions;
	}

}
