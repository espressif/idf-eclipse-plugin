/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.Platform;

import com.espressif.idf.core.logging.Logger;

/**
 * Extracts EIM release zip archives.
 * <p>
 * Newer Linux/macOS packages ship a versioned binary ({@code eim_vX.Y.Z}) plus an {@code eim}
 * symlink. {@link ZipInputStream} materializes that symlink as a tiny regular file whose contents
 * are the link target. Running that file with arguments (e.g. {@code eim select …}) does not
 * forward argv to the real binary and can open the GUI. This extractor detects that case and
 * recreates a real symlink (or copies the target if symlinks are unsupported).
 * <p>
 * Older packages ship only a plain {@code eim} binary — left unchanged.
 */
public final class EimZipExtractor
{
	/**
	 * Symlink targets stored as ZIP entry payloads are short path strings (e.g. {@code eim_v0.17.4}).
	 * Real EIM binaries are multi‑MB; keep this well below any plausible binary size.
	 */
	private static final long MAX_SYMLINK_PAYLOAD_BYTES = 512;

	private EimZipExtractor()
	{
	}

	/**
	 * Extracts {@code zipPath} into {@code destDir} and returns the preferred EIM launch path
	 * ({@code eim} / {@code eim.exe} when present, otherwise a versioned {@code eim_v*} binary).
	 */
	public static Path extract(Path zipPath, Path destDir) throws IOException
	{
		Files.createDirectories(destDir);
		Path firstRegularFile = null;

		try (InputStream fileIn = Files.newInputStream(zipPath); ZipInputStream zis = new ZipInputStream(fileIn))
		{
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null)
			{
				Path newPath = destDir.resolve(entry.getName()).normalize();
				if (!newPath.startsWith(destDir.normalize()))
				{
					throw new IOException("ZIP entry is outside target dir: " + entry.getName()); //$NON-NLS-1$
				}

				if (entry.isDirectory())
				{
					Files.createDirectories(newPath);
					continue;
				}

				Files.createDirectories(newPath.getParent());
				Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
				if (shouldMarkExecutable(newPath))
				{
					newPath.toFile().setExecutable(true);
				}
				if (firstRegularFile == null)
				{
					firstRegularFile = newPath;
				}
			}
		}

		repairMaterializedEimSymlink(destDir);
		return resolvePreferredLaunchPath(destDir, firstRegularFile);
	}

	/**
	 * If {@code eim} is a tiny text file whose content names an existing sibling binary (the usual
	 * result of extracting a ZIP symlink with {@link ZipInputStream}), replace it with a real
	 * symlink or a copy of that target.
	 */
	public static void repairMaterializedEimSymlink(Path destDir) throws IOException
	{
		Path eim = destDir.resolve("eim"); //$NON-NLS-1$
		if (!Files.isRegularFile(eim) || Files.isSymbolicLink(eim))
		{
			return;
		}

		long size = Files.size(eim);
		if (size == 0 || size > MAX_SYMLINK_PAYLOAD_BYTES)
		{
			return;
		}

		byte[] bytes = Files.readAllBytes(eim);
		if (containsNullByte(bytes))
		{
			return;
		}

		String target = new String(bytes, StandardCharsets.UTF_8).trim();
		if (target.isEmpty() || target.contains("\n") || target.contains("\r") || target.contains("/") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				|| target.contains("\\") || target.contains("..")) //$NON-NLS-1$ //$NON-NLS-2$
		{
			return;
		}

		Path targetPath = destDir.resolve(target).normalize();
		if (!targetPath.startsWith(destDir.normalize()) || !Files.isRegularFile(targetPath)
				|| Files.isSameFile(eim, targetPath))
		{
			return;
		}

		if (!looksLikeEimBinaryName(targetPath.getFileName().toString()))
		{
			return;
		}

		Files.delete(eim);
		try
		{
			Files.createSymbolicLink(eim, Path.of(target));
			Logger.log("Restored EIM symlink " + eim + " -> " + target); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (UnsupportedOperationException | IOException e)
		{
			Logger.log("Could not create EIM symlink; copying target instead: " + e.getMessage()); //$NON-NLS-1$
			Files.copy(targetPath, eim, StandardCopyOption.REPLACE_EXISTING);
			eim.toFile().setExecutable(true);
		}
	}

	public static boolean looksLikeMaterializedSymlinkPayload(Path file, Path destDir) throws IOException
	{
		if (!Files.isRegularFile(file) || Files.isSymbolicLink(file))
		{
			return false;
		}
		long size = Files.size(file);
		if (size == 0 || size > MAX_SYMLINK_PAYLOAD_BYTES)
		{
			return false;
		}
		byte[] bytes = Files.readAllBytes(file);
		if (containsNullByte(bytes))
		{
			return false;
		}
		String target = new String(bytes, StandardCharsets.UTF_8).trim();
		if (target.isEmpty() || target.contains("\n") || target.contains("/") || target.contains("\\")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{
			return false;
		}
		Path targetPath = destDir.resolve(target).normalize();
		return targetPath.startsWith(destDir.normalize()) && Files.isRegularFile(targetPath)
				&& looksLikeEimBinaryName(targetPath.getFileName().toString());
	}

	private static boolean shouldMarkExecutable(Path extractedFile)
	{
		if (Platform.OS_WIN32.equals(Platform.getOS()))
		{
			return false;
		}
		return looksLikeEimBinaryName(extractedFile.getFileName().toString());
	}

	private static boolean looksLikeEimBinaryName(String name)
	{
		String lower = name.toLowerCase();
		return lower.equals("eim") || lower.startsWith("eim_v") || lower.startsWith("eim-"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private static boolean containsNullByte(byte[] bytes)
	{
		for (byte b : bytes)
		{
			if (b == 0)
			{
				return true;
			}
		}
		return false;
	}

	private static Path resolvePreferredLaunchPath(Path destDir, Path firstRegularFile) throws IOException
	{
		Path stableEim = destDir.resolve("eim"); //$NON-NLS-1$
		if (Files.exists(stableEim))
		{
			return stableEim;
		}

		Path stableEimExe = destDir.resolve("eim.exe"); //$NON-NLS-1$
		if (Files.exists(stableEimExe))
		{
			return stableEimExe;
		}

		Optional<Path> versioned = findVersionedEimBinary(destDir);
		if (versioned.isPresent())
		{
			return versioned.get();
		}

		return firstRegularFile != null ? firstRegularFile : destDir;
	}

	private static Optional<Path> findVersionedEimBinary(Path destDir) throws IOException
	{
		try (Stream<Path> entries = Files.list(destDir))
		{
			return entries.filter(Files::isRegularFile)
					.filter(p -> p.getFileName().toString().matches("(?i)eim_v.+")) //$NON-NLS-1$
					.sorted(Comparator.comparing(p -> p.getFileName().toString()))
					.findFirst();
		}
	}
}
