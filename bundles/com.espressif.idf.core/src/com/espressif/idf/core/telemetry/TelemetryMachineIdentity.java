/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.telemetry;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Creates a pseudonymous machine identifier using the same mechanism as VS Code: select the first valid network
 * interface MAC address and report only its SHA-256 hash. When no MAC address is available, a random identifier is
 * persisted in the user's home directory.
 *
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 */
public final class TelemetryMachineIdentity
{
	public static final String MAC_SOURCE = "mac-sha256"; //$NON-NLS-1$
	public static final String FALLBACK_SOURCE = "persistent-random"; //$NON-NLS-1$
	public static final String EPHEMERAL_SOURCE = "ephemeral"; //$NON-NLS-1$

	private static final Set<String> INVALID_MAC_ADDRESSES = Set.of("00:00:00:00:00:00", //$NON-NLS-1$
			"ff:ff:ff:ff:ff:ff", "ac:de:48:00:11:22"); //$NON-NLS-1$ //$NON-NLS-2$
	private static final HexFormat MAC_FORMAT = HexFormat.ofDelimiter(":"); //$NON-NLS-1$
	private static final Path FALLBACK_FILE = Path.of(System.getProperty("user.home", "."), //$NON-NLS-1$ //$NON-NLS-2$
			".espressif", "idf-eclipse", "telemetry-machine-id"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	private TelemetryMachineIdentity()
	{
	}

	/**
	 * @return the identity for the current machine
	 */
	public static Identity get()
	{
		return fromHardwareAddresses(getHardwareAddresses(), FALLBACK_FILE);
	}

	/**
	 * Resolves an identity from the supplied hardware addresses, without querying the host network.
	 *
	 * @param hardwareAddresses candidate hardware addresses in interface-index order
	 * @param fallbackFile      file used when no valid address is available
	 * @return the resolved identity
	 */
	public static Identity fromHardwareAddresses(Iterable<byte[]> hardwareAddresses, Path fallbackFile)
	{
		for (byte[] hardwareAddress : hardwareAddresses)
		{
			String macAddress = formatMacAddress(hardwareAddress);
			if (macAddress != null && !INVALID_MAC_ADDRESSES.contains(macAddress))
			{
				return new Identity(hash(macAddress), MAC_SOURCE);
			}
		}
		return getFallbackIdentity(fallbackFile);
	}

	private static List<byte[]> getHardwareAddresses()
	{
		List<byte[]> addresses = new ArrayList<>();
		try
		{
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			if (interfaces == null)
			{
				return addresses;
			}
			List<NetworkInterface> orderedInterfaces = new ArrayList<>();
			interfaces.asIterator().forEachRemaining(orderedInterfaces::add);
			orderedInterfaces.sort(Comparator.comparingInt(TelemetryMachineIdentity::interfaceIndex));
			for (NetworkInterface networkInterface : orderedInterfaces)
			{
				try
				{
					byte[] address = networkInterface.getHardwareAddress();
					if (address != null)
					{
						addresses.add(address);
					}
				}
				catch (SocketException | SecurityException e)
				{
					// Ignore interfaces that disappear or become inaccessible during enumeration.
				}
			}
		}
		catch (SocketException | SecurityException e)
		{
			// The persistent random fallback is used when interfaces cannot be enumerated.
		}
		return addresses;
	}

	private static int interfaceIndex(NetworkInterface networkInterface)
	{
		int index = networkInterface.getIndex();
		return index >= 0 ? index : Integer.MAX_VALUE;
	}

	private static String formatMacAddress(byte[] address)
	{
		return address == null || address.length == 0 ? null : MAC_FORMAT.formatHex(address);
	}

	private static String hash(String value)
	{
		try
		{
			MessageDigest digest = MessageDigest.getInstance("SHA-256"); //$NON-NLS-1$
			return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new IllegalStateException("SHA-256 is not available", e); //$NON-NLS-1$
		}
	}

	private static Identity getFallbackIdentity(Path fallbackFile)
	{
		try
		{
			Path parent = fallbackFile.toAbsolutePath().normalize().getParent();
			if (parent == null)
			{
				throw new IOException("Machine identifier path has no parent: " + fallbackFile); //$NON-NLS-1$
			}
			Files.createDirectories(parent);

			Path lockFile = fallbackFile.resolveSibling(fallbackFile.getFileName() + ".lock"); //$NON-NLS-1$
			try (FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
					FileLock ignored = channel.lock())
			{
				String storedId = readValidUuid(fallbackFile);
				if (storedId != null)
				{
					return new Identity(storedId, FALLBACK_SOURCE);
				}

				String generatedId = UUID.randomUUID().toString();
				writeAtomically(fallbackFile, generatedId);
				if (!generatedId.equals(readValidUuid(fallbackFile)))
				{
					throw new IOException("Machine identifier could not be verified after writing"); //$NON-NLS-1$
				}
				return new Identity(generatedId, FALLBACK_SOURCE);
			}
		}
		catch (IOException | SecurityException e)
		{
			return new Identity(UUID.randomUUID().toString(), EPHEMERAL_SOURCE);
		}
	}

	private static String readValidUuid(Path file)
	{
		try
		{
			String value = Files.readString(file, StandardCharsets.UTF_8).trim();
			UUID.fromString(value);
			return value;
		}
		catch (IllegalArgumentException | IOException e)
		{
			return null;
		}
	}

	private static void writeAtomically(Path file, String value) throws IOException
	{
		Path parent = file.toAbsolutePath().normalize().getParent();
		if (parent == null)
		{
			throw new IOException("Machine identifier path has no parent: " + file); //$NON-NLS-1$
		}
		Path temporaryFile = Files.createTempFile(parent, "machine-id-", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
		try
		{
			Files.writeString(temporaryFile, value + System.lineSeparator(), StandardCharsets.UTF_8,
					StandardOpenOption.TRUNCATE_EXISTING);
			try
			{
				Files.move(temporaryFile, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			}
			catch (AtomicMoveNotSupportedException e)
			{
				Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING);
			}
		}
		finally
		{
			try
			{
				Files.deleteIfExists(temporaryFile);
			}
			catch (IOException e)
			{
				// The identifier was already moved into place; stale temporary files can be removed later.
			}
		}
	}

	/**
	 * @param id     pseudonymous identifier
	 * @param source mechanism used to create the identifier
	 */
	public record Identity(String id, String source)
	{
		public boolean isPersistent()
		{
			return !EPHEMERAL_SOURCE.equals(source);
		}
	}
}
