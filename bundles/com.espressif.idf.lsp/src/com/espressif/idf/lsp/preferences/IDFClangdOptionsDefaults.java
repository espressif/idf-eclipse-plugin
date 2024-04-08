/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.lsp.preferences;

import java.util.Optional;

import org.eclipse.cdt.lsp.clangd.BuiltinClangdOptionsDefaults;
import org.eclipse.cdt.lsp.clangd.ClangdOptionsDefaults;
import org.osgi.service.component.annotations.Component;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.lsp.ILSPConstants;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
@SuppressWarnings("restriction")
@Component(service = ClangdOptionsDefaults.class, property = { "service.ranking:Integer=100" })
public class IDFClangdOptionsDefaults extends BuiltinClangdOptionsDefaults
{

	@Override
	public String clangdPath()
	{
		String clandPath = IDFUtil.findCommandFromBuildEnvPath(ILSPConstants.CLANGD_EXECUTABLE);
		Logger.log("clangd: " + clandPath); //$NON-NLS-1$
		return Optional.ofNullable(clandPath).orElse(ILSPConstants.CLANGD_EXECUTABLE);
	}
	
	@Override
	public String queryDriver()
	{
		//By passing --query-driver argument to clangd helps to resolve the cross-compiler toolchain headers.
		String toolchainPath = IDFUtil.getToolchainExePathForActiveTarget();
		Logger.log("toolchain path: " + toolchainPath); //$NON-NLS-1$
		return Optional.ofNullable(toolchainPath).orElse(super.queryDriver());
	}

}
