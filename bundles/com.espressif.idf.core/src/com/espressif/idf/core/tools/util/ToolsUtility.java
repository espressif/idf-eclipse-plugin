/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.tukaani.xz.XZInputStream;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.SystemExecutableFinder;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.ToolsSystemWrapper;
import com.espressif.idf.core.tools.vo.ToolsVO;
import com.espressif.idf.core.util.FileUtil;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;

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

	public static boolean isToolInstalled(String toolName, String versionsName)
	{
		File homeDir = new File(ESPRESSIF_HOME_DIR);
		if (!homeDir.exists())
		{
			return false;
		}

		File toolDirectory = new File(ESPRESSIF_HOME_TOOLS_DIR.concat(FORWARD_SLASH).concat(toolName)
				.concat(FORWARD_SLASH).concat(versionsName));
		if (toolDirectory.exists())
		{
			IDFEnvironmentVariables idfEnvironmentVariables = new IDFEnvironmentVariables();
			String pathValue = idfEnvironmentVariables.getEnvValue(IDFEnvironmentVariables.PATH);
			String[] splittedPaths = pathValue.split(File.pathSeparator);
			String directorySplittor = Platform.getOS().equals(Platform.OS_WIN32) ? SLASH : FORWARD_SLASH;
			for (String splittedPath : splittedPaths)
			{
				if (splittedPath.contains(toolName.concat(directorySplittor).concat(versionsName)))
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
						Files.createDirectories(pathEntryOutput);
				}
				else
				{
					Files.createDirectories(pathEntryOutput.getParent());
					Files.copy(tararchiveinputstream, pathEntryOutput, StandardCopyOption.REPLACE_EXISTING);
					Runtime.getRuntime().exec("/bin/chmod 755 ".concat(pathEntryOutput.toString())); //$NON-NLS-1$
				}
					
			}

			tararchiveinputstream.close();
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}
	
	public static void extractTarXz(String tarFile, String outputDir)
	{
		Path pathOutput = Paths.get(outputDir);
		Map<Path, Path> symLinks = new HashMap<>();
		Map<Path, Path> hardLinks = new HashMap<>();
		try
		{
			FileInputStream fileInputStream = new FileInputStream(tarFile);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
			XZInputStream xzInputStream = new XZInputStream(bufferedInputStream);
			TarArchiveInputStream tararchiveinputstream = new TarArchiveInputStream(xzInputStream);
			TarArchiveEntry archiveentry = null;
			while ((archiveentry = tararchiveinputstream.getNextTarEntry()) != null)
			{
				Path pathEntryOutput = pathOutput.resolve(archiveentry.getName());
				if (archiveentry.isSymbolicLink())
				{
					symLinks.put(pathEntryOutput,
							pathOutput.resolve(archiveentry.getName()).getParent().resolve(archiveentry.getLinkName()));
					continue;
				}
				else if (archiveentry.isLink())
				{
					hardLinks.put(pathEntryOutput,
							pathOutput.resolve(archiveentry.getLinkName()));
					continue;
				}
				else if (archiveentry.isDirectory())
				{
					if (!Files.exists(pathEntryOutput))
						Files.createDirectories(pathEntryOutput);
				}
				else
				{
					System.out.println(pathEntryOutput.toString() + " " + archiveentry.getSize()); //$NON-NLS-1$
					Files.copy(tararchiveinputstream, pathEntryOutput, StandardCopyOption.REPLACE_EXISTING);
					Runtime.getRuntime().exec("/bin/chmod 755 ".concat(pathEntryOutput.toString())); //$NON-NLS-1$
				}
			}
			tararchiveinputstream.close();
			xzInputStream.close();
			fileInputStream.close();
			hardLinks.forEach(ToolsUtility::createHardLinks);
			symLinks.forEach(ToolsUtility::createSymLinks);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}
	
	private static void createHardLinks(Path link, Path target)
	{
		try
		{
			Files.deleteIfExists(link);
			Files.createLink(link, target);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}

	private static void createSymLinks(Path link, Path target)
	{
		try
		{
			Files.deleteIfExists(link);
			Files.createSymbolicLink(link, target.toRealPath());
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

	/**
	 * Gets the file checksum based on the provided message digest
	 * 
	 * @param digest
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String getFileChecksum(MessageDigest digest, File file) throws IOException
	{
		FileInputStream fis = new FileInputStream(file);
		byte[] byteArray = new byte[1024];
		int bytesCount = 0;
		while ((bytesCount = fis.read(byteArray)) != -1)
		{
			digest.update(byteArray, 0, bytesCount);
		}
		;
		fis.close();
		byte[] bytes = digest.digest();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++)
		{
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}
	
	/**
	 * Gets the absolute path for the tool from the given path
	 * @param toolName tool to find absolute path
	 * @param path the path to variable to look into, if null System.getenv() will be used
	 * @return absolute path to the tool
	 */
	public static IPath findAbsoluteToolPath(String toolName, String path)
	{
		if (StringUtil.isEmpty(path))
		{
			Map<String, String> env = IDFUtil.getSystemEnv();
			if (env.containsKey(IDFEnvironmentVariables.PATH)) 
				path = env.get(IDFEnvironmentVariables.PATH);
			else
				path = env.get("Path"); //$NON-NLS-1$
		}
		
		SystemExecutableFinder systemExecutableFinder = new SystemExecutableFinder(new ToolsSystemWrapper(path));
		return systemExecutableFinder.find(toolName);
	}
}
