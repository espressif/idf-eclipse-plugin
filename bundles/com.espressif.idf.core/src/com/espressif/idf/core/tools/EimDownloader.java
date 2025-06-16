package com.espressif.idf.core.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

import com.espressif.idf.core.util.StringUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class EimDownloader
{
	private static final String URL_JSON = "https://dl.espressif.com/dl/eim/eim_unified_release.json"; //$NON-NLS-1$
	private static final Path DOWNLOAD_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "eim_gui"); //$NON-NLS-1$ //$NON-NLS-2$

	private String os;
	private String arch;
	DownloadListener listener;

	public EimDownloader(DownloadListener listener)
	{
		os = Platform.getOS();
		arch = Platform.getOSArch();
		this.listener = listener;
	}

	public void downloadEim(IProgressMonitor monitor)
	{
		try
		{
			monitor.beginTask("Downloading EIM GUI...", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
			JsonObject root = fetchJson();
			JsonArray assets = root.getAsJsonArray("assets"); //$NON-NLS-1$
			Optional<JsonObject> match = findMatchingAsset(assets);

			if (match.isEmpty())
			{
				listener.onError("No suitable EIM GUI asset found.", null); //$NON-NLS-1$
				monitor.done();
				return;
			}

			JsonObject asset = match.get();
			String name = asset.get("name").getAsString(); //$NON-NLS-1$
			String downloadUrl = asset.get("browser_download_url").getAsString(); //$NON-NLS-1$

			Files.createDirectories(DOWNLOAD_DIR);
			Path downloadPath = DOWNLOAD_DIR.resolve(name);

			downloadFile(downloadUrl, downloadPath, listener, monitor);

			if (name.endsWith(".zip")) //$NON-NLS-1$
			{
				Path extracted = unzip(downloadPath, DOWNLOAD_DIR.resolve("unzipped")); //$NON-NLS-1$
				listener.onCompleted(extracted.toAbsolutePath().toString());
			}
			else
			{
				listener.onCompleted(downloadPath.toAbsolutePath().toString());
			}
		}
		catch (Exception e)
		{
			listener.onError("Download failed", e); //$NON-NLS-1$
		} finally
		{
			monitor.done();
		}

	}

	private JsonObject fetchJson() throws IOException
	{
		URL url = new URL(URL_JSON);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("accept", "application/json"); //$NON-NLS-1$//$NON-NLS-2$
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);

		try (InputStreamReader reader = new InputStreamReader(connection.getInputStream()))
		{
			return JsonParser.parseReader(reader).getAsJsonObject();
		}
	}

	private Optional<JsonObject> findMatchingAsset(JsonArray assets)
	{
		String osToken = switch (os)
		{
		case Platform.OS_WIN32 -> "windows"; //$NON-NLS-1$
		case Platform.OS_MACOSX -> "macos"; //$NON-NLS-1$
		case Platform.OS_LINUX -> "linux"; //$NON-NLS-1$
		default -> StringUtil.EMPTY;
		};

		String archToken = switch (arch)
		{
		case Platform.ARCH_X86_64 -> "x64"; //$NON-NLS-1$
		case Platform.ARCH_AARCH64, "arm64" -> "aarch64"; //$NON-NLS-1$ //$NON-NLS-2$
		default -> StringUtil.EMPTY;
		};

		for (int i = 0; i < assets.size(); i++)
		{
			JsonObject asset = assets.get(i).getAsJsonObject();
			String name = asset.get("name").getAsString().toLowerCase(); //$NON-NLS-1$
			if (name.contains("eim-gui") && //$NON-NLS-1$
					name.contains(osToken) && name.contains(archToken)
					&& (name.endsWith(".exe") || name.endsWith(".dmg") || name.endsWith(".zip"))) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			{
				return Optional.of(asset);
			}
		}

		return Optional.empty();
	}

	private void downloadFile(String fileURL, Path targetPath, DownloadListener listener, IProgressMonitor monitor)
			throws IOException
	{
		URL url = new URL(fileURL);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);

		int contentLength = connection.getContentLength();
		monitor.beginTask("Downloading " + targetPath.getFileName(), contentLength); //$NON-NLS-1$

		try (InputStream in = connection.getInputStream(); OutputStream out = Files.newOutputStream(targetPath))
		{

			byte[] buffer = new byte[8192];
			int bytesRead;
			long totalRead = 0;
			int lastPercent = 0;

			while ((bytesRead = in.read(buffer)) != -1)
			{
				out.write(buffer, 0, bytesRead);
				totalRead += bytesRead;
				if (contentLength > 0)
				{
					int percent = (int) ((totalRead * 100) / contentLength);
					if (percent != lastPercent)
					{
						listener.onProgress(percent);
						lastPercent = percent;
						monitor.worked(bytesRead);
					}
				}
			}
		}
	}

	private Path unzip(Path zipPath, Path destDir) throws IOException
	{
		Files.createDirectories(destDir);
		Path firstExecutable = null;

		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath.toFile())))
		{
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null)
			{
				Path newPath = destDir.resolve(entry.getName());
				if (entry.isDirectory())
				{
					Files.createDirectories(newPath);
				}
				else
				{
					Files.createDirectories(newPath.getParent());
					Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
					if (firstExecutable == null && Files.isRegularFile(newPath))
					{
						newPath.toFile().setExecutable(true);
						firstExecutable = newPath;
					}
				}
			}
		}
		return firstExecutable != null ? firstExecutable : destDir;
	}

}
