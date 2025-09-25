/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.toolchain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.espressif.idf.core.toolchain.enums.Target;

/**
 * Class to hold ESP-IDF target information including preview status
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>, Ali Azam Rana <ali.azamrana@espresif.com>
 *
 */
public class IDFTargets
{
	private List<IDFTarget> supportedTargets;
	private List<IDFTarget> previewTargets;

	public IDFTargets()
	{
		this.supportedTargets = new ArrayList<>();
		this.previewTargets = new ArrayList<>();
	}

	public void addSupportedTarget(String targetName)
	{
		Target t = Target.fromString(targetName);
		if (t != null)
			supportedTargets.add(new IDFTarget(t, false));
	}

	public void addPreviewTarget(String targetName)
	{
		Target t = Target.fromString(targetName);
		if (t != null)
			previewTargets.add(new IDFTarget(t, true));
	}

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
		Target t = Target.fromString(targetName);
		return t != null && getAllTargets().stream().anyMatch(x -> x.getTarget() == t);
	}

	public IDFTarget getTarget(String targetName)
	{
		Target t = Target.fromString(targetName);
		return t == null ? null : getAllTargets().stream().filter(x -> x.getTarget() == t).findFirst().orElse(null);
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

	/**
	 * Inner class representing a single IDF target
	 */
	public static class IDFTarget
	{
		private final Target target;
		private final boolean isPreview;

		public IDFTarget(Target target, boolean isPreview)
		{
			this.target = target;
			this.isPreview = isPreview;
		}

		public String getName()
		{
			return target.idfName();
		}

		public boolean isPreview()
		{
			return isPreview;
		}

		public Target getTarget()
		{
			return target;
		}

		public String getArchitecture()
		{
			return target.architectureId();
		}

		public String getToolchainId()
		{
			return target.toolchainId();
		}

		public String getCompilerPattern()
		{
			return target.compilerPattern();
		}

		public String getDebuggerPattern()
		{
			return target.debuggerPattern();
		}

		public String getToolchainFileName()
		{
			return target.toolchainFileName();
		}
	}
}
