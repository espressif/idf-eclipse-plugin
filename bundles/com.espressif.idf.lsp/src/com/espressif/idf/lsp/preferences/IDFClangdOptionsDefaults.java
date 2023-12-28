package com.espressif.idf.lsp.preferences;

import java.util.Optional;

import org.eclipse.cdt.lsp.clangd.BuiltinClangdOptionsDefaults;
import org.eclipse.cdt.lsp.clangd.ClangdOptionsDefaults;
import org.osgi.service.component.annotations.Component;

import com.espressif.idf.core.util.IDFUtil;

@SuppressWarnings("restriction")
@Component(service = ClangdOptionsDefaults.class, property = { "service.ranking:Integer=100" })
public class IDFClangdOptionsDefaults extends BuiltinClangdOptionsDefaults {

	@Override
	public String clangdPath() {
		return Optional.ofNullable(IDFUtil.findCommandFromBuildEnvPath("clangd")) //$NON-NLS-1$
				.orElse("clangd"); //$NON-NLS-1$
	}

	@Override
	public String queryDriver() {
		return super.queryDriver(); // TODO:esp xtensa-gcc path
	}

}
