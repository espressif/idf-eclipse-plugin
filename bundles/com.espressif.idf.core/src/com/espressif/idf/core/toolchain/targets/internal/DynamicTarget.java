/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.toolchain.targets.internal;

import java.util.Locale;
import java.util.Objects;

import com.espressif.idf.core.toolchain.targets.Architecture;
import com.espressif.idf.core.toolchain.targets.TargetSpec;

/**
 * Represents a preview target that isn't (yet) in the {@link com.espressif.idf.core.target.Target} enum. Unknown
 * preview targets default to RISCV32 unless specified otherwise.
 *
 * Keep this class in an internal package; consumers should rely on the TargetSpec API only.
 * 
 * @author Ali Azam Rana
 */
public final class DynamicTarget implements TargetSpec
{
	private final String idfName;
	private final Architecture arch;

	public DynamicTarget(String idfName, Architecture arch)
	{
		this.idfName = Objects.requireNonNull(idfName, "idfName").toLowerCase(Locale.ROOT); //$NON-NLS-1$
		this.arch = Objects.requireNonNull(arch, "arch"); //$NON-NLS-1$
	}

	@Override
	public String idfName()
	{
		return idfName;
	}

	@Override
	public Architecture arch()
	{
		return arch;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof TargetSpec ts))
			return false;
		return idfName.equals(ts.idfName());
	}

	@Override
	public int hashCode()
	{
		return idfName.hashCode();
	}

	@Override
	public String toString()
	{
		return "DynamicTarget[" + idfName + "," + arch.id() + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
