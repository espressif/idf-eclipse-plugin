/*******************************************************************************
 * Copyright (c) 2015 Liviu Ionescu.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Liviu Ionescu - initial version
 *******************************************************************************/

package ilg.gnumcueclipse.core.preferences;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import ilg.gnumcueclipse.core.Activator;
import ilg.gnumcueclipse.core.AltWindowsRegistry;
import ilg.gnumcueclipse.core.EclipseUtils;
import ilg.gnumcueclipse.core.StringUtils;

/**
 * Helper class used by debuggers.
 * 
 */
public class Discoverer {

	// ------------------------------------------------------------------------

	private static final String REG_PREFIX = "SOFTWARE";
	private static final String REG32_PREFIX = "SOFTWARE\\Wow6432Node";

	// ------------------------------------------------------------------------

	/**
	 * Find where the executable might have been installed. The returned path is
	 * known to be an existing folder.
	 * 
	 * @param executableName
	 * @param searchPath
	 *            a string with a sequence of folders.
	 * @param binFolder
	 *            a String, usually "bin", or null.
	 * @return a String with the absolute folder path, or null if not found.
	 */
	public static String searchInstallFolder(String executableName, String searchPath, String binFolder) {

		String resolvedPath = EclipseUtils.performStringSubstitution(searchPath);
		if (resolvedPath == null || resolvedPath.isEmpty()) {
			return null;
		}
		
		if (EclipseUtils.isWindows()) {
			resolvedPath = StringUtils.duplicateBackslashes(resolvedPath);
		}

		// Split into multiple paths.
		String[] paths = resolvedPath.split(EclipseUtils.getPathSeparator());
		if (paths.length == 0) {
			return null;
		}

		if (Activator.getInstance().isDebugging()) {
			System.out.println("Discoverer.searchInstallFolder() resolved path \"" + resolvedPath + "\"");
		}

		String value = null;

		// Try paths in order; return the first.
		for (int i = 0; i < paths.length; ++i) {
			value = getLastExecutable(paths[i], binFolder, executableName);
			if (value != null && !value.isEmpty()) {
				return value;
			}
		}

		return null;
	}

	/**
	 * Get key value from registry and validate the executable. The returned
	 * path is known to be an existing folder.
	 * 
	 * @param executableName
	 * @param subFolder
	 *            a String, usually "bin", or null.
	 * @param registrySubKey
	 * @param registryName
	 * @return a String with the absolute folder path, or null if not found.
	 */
	public static String getRegistryInstallFolder(String executableName, String subFolder, String registrySubKey,
			String registryName) {

		String value = null;
		if (EclipseUtils.isWindows()) {

			WindowsRegistry registry = WindowsRegistry.getRegistry();

			if (registry != null) {
				value = getRegistryValue(registry, REG_PREFIX, registrySubKey, registryName);
				if (value == null) {
					// If on 64-bit, check the 32-bit registry too.
					value = getRegistryValue(registry, REG32_PREFIX, registrySubKey, registryName);
				}

				if (subFolder != null && value != null && !value.endsWith("\\" + subFolder)) {
					value += "\\" + subFolder;
				}

				if (value != null) {
					IPath path = new Path(value);
					// Make portable
					value = path.toString(); // includes /bin, if it exists
					if (Activator.getInstance().isDebugging()) {
						System.out.println(
								"Discoverer.getRegistryInstallFolder() " + registryName + " \"" + value + "\"");
					}

					File folder = path.append(executableName).toFile();
					if (folder.isFile()) {
						if (Activator.getInstance().isDebugging()) {
							System.out.println("Discoverer.getRegistryInstallFolder() = \"" + value + "\"");
						}
						return value;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Get the value of a registry key. It first tests the current user key,
	 * then the local machine key.
	 * 
	 * @param registry
	 * @param prefix
	 * @param registrySubKey
	 * @param registryName
	 * @return a String, or null if not found.
	 */
	private static String getRegistryValue(WindowsRegistry registry, String prefix, String registrySubKey,
			String registryName) {

		String value;
		// TODO: remove kludge after SEGGER fixes the bug
		if (!registrySubKey.startsWith("\\SEGGER")) {
			value = registry.getCurrentUserValue(prefix + registrySubKey, registryName);
		} else {
			// Kludge to compensate for SEGGER and CDT bug (the value is
			// terminated with lots of zeroes, more than CDT WindowsRegistry
			// class can handle).
			value = AltWindowsRegistry.query("HKEY_CURRENT_USER\\" + prefix + registrySubKey, registryName);
		}
		if (value == null) {
			value = registry.getLocalMachineValue(prefix + registrySubKey, registryName);
		}

		if (Activator.getInstance().isDebugging()) {
			System.out.println("Discoverer.getRegistryValue(\"" + prefix + "\", \"" + registrySubKey + "\", \""
					+ registryName + "\") = \"" + value + "\"");
		}

		return value;
	}

	/**
	 * Return the last (in lexicographical order) folder that contain
	 * "bin/executable". If not found, the folder itself is checked.
	 * 
	 * The returned path includes the ending /bin.
	 * 
	 * @param folder
	 * @param binFolder
	 *            a String, usually "bin", or null.
	 * @param executableName
	 * @return a String with the folder absolute path, or null if not found.
	 */
	public static String getLastExecutable(String folderName, final String binFolder, final String executableName) {

		IPath folderPath = new Path(folderName);

		if (Activator.getInstance().isDebugging()) {
			System.out.println("Discoverer.getLastExecutable(\"" + folderPath + "\", \"" + executableName + "\")");
		}

		List<String> list = new ArrayList<String>();
		File folder = folderPath.toFile();
		if (!folder.isDirectory()) {
			// System.out.println(folder + " not a folder");
			return null;
		}

		File[] files = folder.listFiles(new FilenameFilter() {

			/**
			 * Filter to select only
			 */
			@Override
			public boolean accept(File dir, String name) {
				IPath path = (new Path(dir.getAbsolutePath())).append(name);

				if (binFolder != null) {
					path = path.append(binFolder).append(executableName);
				} else {
					path = path.append(executableName);
				}
				if (path.toFile().isFile()) {
					if (Activator.getInstance().isDebugging()) {
						System.out.println("Discoverer.getLastExecutable() found \"" + path.toString() + "\"");
					}
					return true;
				}
				return false;
			}
		});

		if (files != null && files.length > 0) {

			for (int i = 0; i < files.length; ++i) {
				list.add(files[i].getName());
			}

			// The sort criteria is the lexicographical order on folder name.
			Collections.sort(list);

			// Get the last name in ordered list.
			String last = list.get(list.size() - 1);

			// System.out.println(last);
			IPath path = (new Path(folderName)).append(last);
			if (binFolder != null) {
				path = path.append(binFolder);
			}
			if (Activator.getInstance().isDebugging()) {
				System.out.println("Discoverer.getLastExecutable() = \"" + path.toString() + "\"");
			}
			return path.toString();
		} else {
			IPath path = (new Path(folderName));
			if (binFolder != null) {
				path = path.append(binFolder).append(executableName);
			} else {
				path = path.append(executableName);
			}
			folder = path.toFile();
			if (folder.isFile()) {
				// System.out.println(folder + " not a folder");
				path = (new Path(folderName));
				if (binFolder != null) {
					path = path.append(binFolder);
				}
				if (Activator.getInstance().isDebugging()) {
					System.out.println("Discoverer.getLastExecutable() = \"" + path.toString() + "\"");
				}
				return path.toString();
			}
		}

		if (Activator.getInstance().isDebugging()) {
			System.out.println("Discoverer.getLastExecutable() not found");
		}
		return null;
	}

	// ------------------------------------------------------------------------
}
