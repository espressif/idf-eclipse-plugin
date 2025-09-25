/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.toolchain;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.espressif.idf.core.toolchain.targets.Architecture;
import com.espressif.idf.core.toolchain.targets.Target;
import com.espressif.idf.core.toolchain.targets.TargetSpec;
import com.espressif.idf.core.toolchain.targets.internal.DynamicTarget;

/**
 * Class to hold ESP-IDF target information including preview status
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>, Ali Azam Rana <ali.azamrana@espresif.com>
 *
 */
public class IDFTargets
{

	private final List<IDFTarget> supportedTargets = new ArrayList<>();
	private final List<IDFTarget> previewTargets = new ArrayList<>();

	// ---------- Adders ----------

	/** Strict for supported: unknown => throw (fail-fast). */
	public void addSupportedTarget(String targetName)
	{
		Target known = Target.tryParse(targetName);
		if (known == null)
		{
			throw new IllegalArgumentException("Unknown supported target: " + targetName); //$NON-NLS-1$
		}
		supportedTargets.add(new IDFTarget(known, false));
	}

	/** Convenience overload for enum callers. */
	public void addSupportedTarget(Target target)
	{
		supportedTargets.add(new IDFTarget(target, false));
	}

	/**
	 * Lenient for preview: unknown => include as RISCV32 by default (PR goal).
	 */
	public void addPreviewTarget(String targetName)
	{
		TargetSpec spec = Target.tryParse(targetName);
		if (spec == null)
		{
			spec = new DynamicTarget(targetName, Architecture.RISCV32);
			// Optional: log a warning here if you have a logger
		}
		previewTargets.add(new IDFTarget(spec, true));
	}

	/** Convenience overload for enum callers. */
	public void addPreviewTarget(Target target)
	{
		previewTargets.add(new IDFTarget(target, true));
	}

	// ---------- Queries ----------

	public List<IDFTarget> getAllTargets()
	{
		List<IDFTarget> all = new ArrayList<>(supportedTargets);
		all.addAll(previewTargets);
		return all;
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
		String key = targetName == null ? "" : targetName.toLowerCase(Locale.ROOT); //$NON-NLS-1$
		return getAllTargets().stream().anyMatch(x -> x.getName().equals(key));
	}

	/**
	 * @return first match by name across supported+preview, or null.
	 */
	public IDFTarget getTarget(String targetName)
	{
		String key = targetName == null ? "" : targetName.toLowerCase(Locale.ROOT); //$NON-NLS-1$
		return getAllTargets().stream().filter(x -> x.getName().equals(key)).findFirst().orElse(null);
	}

	public List<String> getAllTargetNames()
	{
		return getAllTargets().stream().map(IDFTarget::getName).collect(Collectors.toList());
	}

	public List<String> getSupportedTargetNames()
	{
		return getSupportedTargets().stream().map(IDFTarget::getName).collect(Collectors.toList());
	}

	public List<String> getPreviewTargetNames()
	{
		return getPreviewTargets().stream().map(IDFTarget::getName).collect(Collectors.toList());
	}

	// ---------- Inner holder ----------

	/**
	 * Wrapper pairing a TargetSpec (enum or dynamic) with a preview flag.
	 */
	public static class IDFTarget
	{
		private final TargetSpec spec;
		private final boolean isPreview;

		public IDFTarget(TargetSpec spec, boolean isPreview)
		{
			this.spec = spec;
			this.isPreview = isPreview;
		}

		public TargetSpec getSpec()
		{
			return spec;
		}

		public boolean isPreview()
		{
			return isPreview;
		}

		// --- API mirroring your previous getters ---

		/** e.g., "esp32c6" */
		public String getName()
		{
			return spec.idfName();
		}

		/** "xtensa" or "riscv32" */
		public String getArchitecture()
		{
			return spec.architectureId();
		}

		/** e.g., "xtensa-esp32-elf" or "riscv32-esp-elf" */
		public String getToolchainId()
		{
			return spec.toolchainId();
		}

		/** Regex for compiler path ending in "-gcc(.exe)" matching legacy/unified layouts. */
		public String getCompilerPattern()
		{
			return spec.compilerPattern();
		}

		/** Regex for debugger executable (gdb). */
		public String getDebuggerPattern()
		{
			return spec.debuggerPattern();
		}

		/** e.g., "toolchain-esp32c6.cmake" */
		public String getToolchainFileName()
		{
			return spec.toolchainFileName();
		}
	}
}
