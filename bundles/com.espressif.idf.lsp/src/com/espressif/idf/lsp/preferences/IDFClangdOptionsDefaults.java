/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.lsp.preferences;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.cdt.lsp.clangd.ClangdOptionsDefaults;
import org.osgi.service.component.annotations.Component;

import com.espressif.idf.core.ILSPConstants;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
@Component(service = ClangdOptionsDefaults.class, property = { "service.ranking:Integer=100" })
public class IDFClangdOptionsDefaults implements ClangdOptionsDefaults {

	@Override
	public String clangdPath() {
		String clandPath = IDFUtil.findCommandFromBuildEnvPath(ILSPConstants.CLANGD_EXECUTABLE);
		Logger.log("clangd: " + clandPath); //$NON-NLS-1$
		return Optional.ofNullable(clandPath).orElse(ILSPConstants.CLANGD_EXECUTABLE);
	}

	@Override
	public String queryDriver() {
		// By passing --query-driver argument to clangd helps to resolve the
		// cross-compiler toolchain headers.
		String toolchainPath = IDFUtil.getToolchainExePathForActiveTarget();
		Logger.log("toolchain path: " + toolchainPath); //$NON-NLS-1$
		return Optional.ofNullable(toolchainPath).orElse("");
	}

	@Override
	public boolean useTidy() {
		return true;
	}

	@Override
	public boolean useBackgroundIndex() {
		return true;
	}

	@Override
	public String completionStyle() {
		return "detailed"; //$NON-NLS-1$
	}

	@Override
	public boolean prettyPrint() {
		return true;
	}

	@Override
	public List<String> additionalOptions() {
		return Collections.emptyList();
	}

}
