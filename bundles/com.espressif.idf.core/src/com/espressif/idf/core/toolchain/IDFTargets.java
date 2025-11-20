/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.toolchain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Class to hold ESP-IDF target information including preview status
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class IDFTargets
{
	private static final Set<String> XTENSA_CHIPS = Set.of("esp32", "esp32s2", "esp32s3"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String XTENSA = "xtensa"; //$NON-NLS-1$
	private static final String RISCV32 = "riscv32"; //$NON-NLS-1$
	private static final String XTENSA_TOOLCHAIN_ID = XTENSA + "-%s-elf"; //$NON-NLS-1$
	private static final String RISCV32_TOOLCHAIN_ID = RISCV32 + "-esp-elf"; //$NON-NLS-1$
	private static final String XTENSA_UNIFIED_DIR = XTENSA + "-esp-elf"; //$NON-NLS-1$
	private static final String TOOLCHAIN_NAME = "toolchain-%s.cmake"; //$NON-NLS-1$
	private List<IDFTarget> supportedTargets;
	private List<IDFTarget> previewTargets;

	//Dummy change
	public IDFTargets()
	{
		this.supportedTargets = new ArrayList<>();
		this.previewTargets = new ArrayList<>();
	}

	public void addSupportedTarget(String target)
	{
		supportedTargets.add(new IDFTarget(target, false));
	}

	public void addPreviewTarget(String target)
	{
		previewTargets.add(new IDFTarget(target, true));
	}

	public List<IDFTarget> getAllTargets()
	{
		List<IDFTarget> allTargets = new ArrayList<>();
		allTargets.addAll(supportedTargets);
		allTargets.addAll(previewTargets);
		return allTargets;
	}

	public List<IDFTarget> getSupportedTargets()
	{
		return supportedTargets;
	}

	public List<IDFTarget> getPreviewTargets()
	{
		return previewTargets;
	}

	public boolean hasTarget(String targetName)
	{
		return getAllTargets().stream().anyMatch(target -> target.getName().equals(targetName));
	}

	/**
	 * Get a specific target by name
	 * 
	 * @param targetName Name of the target to find
	 * @return IDFTarget if found, null otherwise
	 */
	public IDFTarget getTarget(String targetName)
	{
		return getAllTargets().stream().filter(target -> target.getName().equals(targetName)).findFirst().orElse(null);
	}

	/**
	 * Get all target names as strings
	 * 
	 * @return List of target names
	 */
	public List<String> getAllTargetNames()
	{
		return getAllTargets().stream().map(IDFTarget::getName).collect(java.util.stream.Collectors.toList());
	}

	/**
	 * Get supported target names as strings
	 * 
	 * @return List of supported target names
	 */
	public List<String> getSupportedTargetNames()
	{
		return getSupportedTargets().stream().map(IDFTarget::getName).collect(java.util.stream.Collectors.toList());
	}

	/**
	 * Get preview target names as strings
	 * 
	 * @return List of preview target names
	 */
	public List<String> getPreviewTargetNames()
	{
		return getPreviewTargets().stream().map(IDFTarget::getName).collect(java.util.stream.Collectors.toList());
	}

	/**
	 * Inner class representing a single IDF target
	 */
	public static class IDFTarget
	{
		private final String name;
		private final boolean isPreview;

		public IDFTarget(String name, boolean isPreview)
		{
			this.name = name;
			this.isPreview = isPreview;
		}

		public String getName()
		{
			return name;
		}

		public boolean isPreview()
		{
			return isPreview;
		}

		/**
		 * Get the architecture for this target
		 * 
		 * @return "xtensa" for esp32/esp32s2/esp32s3, "riscv32" for others
		 */
		public String getArchitecture()
		{
			return XTENSA_CHIPS.contains(name) ? XTENSA : RISCV32;
		}

		/**
		 * Get the toolchain ID for this target
		 * 
		 * @return toolchain ID string
		 */
		public String getToolchainId()
		{
			return XTENSA_CHIPS.contains(name) ? String.format(XTENSA_TOOLCHAIN_ID, name) : RISCV32_TOOLCHAIN_ID;
		}

		/**
		 * Get the compiler pattern for this target
		 * 
		 * @return regex pattern for compiler
		 */
		public String getCompilerPattern()
		{
			String executableName = getExecutableName();

			// Support both old and new unified directory structures
			String targetSpecificDir = getTargetSpecificDirectoryName();
			String unifiedDir = getUnifiedDirectoryName();

			// Create pattern that matches either directory structure
			return "(?:" + targetSpecificDir + "|" + unifiedDir + ")[\\\\/]+bin[\\\\/]+" + executableName //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					+ "-gcc(?:\\.exe)?$"; //$NON-NLS-1$
		}

		/**
		 * Get the debugger pattern for this target
		 * 
		 * @return regex pattern for debugger
		 */
		public String getDebuggerPattern()
		{
			String executableName = getExecutableName();
			return executableName + "-gdb(?:\\.exe)?$"; //$NON-NLS-1$
		}

		/**
		 * Get the executable name prefix for this target (different from directory structure in ESP-IDF v5.5+)
		 * 
		 * @return executable name prefix
		 */
		private String getExecutableName()
		{
			return XTENSA_CHIPS.contains(name) ? String.format(XTENSA_TOOLCHAIN_ID, name)
					: RISCV32_TOOLCHAIN_ID;
		}

		private String getTargetSpecificDirectoryName()
		{
			return XTENSA_CHIPS.contains(name) ? String.format(XTENSA_TOOLCHAIN_ID, name)
					: RISCV32_TOOLCHAIN_ID;
		}

		private String getUnifiedDirectoryName()
		{
				return XTENSA_CHIPS.contains(name) ? XTENSA_UNIFIED_DIR : RISCV32_TOOLCHAIN_ID;
		}

		/**
		 * Get the CMake toolchain file name for this target
		 * 
		 * @return toolchain file name
		 */
		public String getToolchainFileName()
		{
			return String.format(TOOLCHAIN_NAME, name);
		}
	}
}
