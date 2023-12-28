package com.espressif.idf.lsp.preferences;

import org.eclipse.cdt.lsp.clangd.BuiltinClangdOptionsDefaults;
import org.eclipse.cdt.lsp.clangd.ClangdOptionsDefaults;
import org.osgi.service.component.annotations.Component;

@SuppressWarnings("restriction")
@Component(service = ClangdOptionsDefaults.class, property = { "service.ranking:Integer=100" })
public class IDFClangdOptionsDefaults extends BuiltinClangdOptionsDefaults {

	@Override
	public String clangdPath() {
		return super.clangdPath();//TODO: esp-clangd path
	}

	@Override
	public String queryDriver() {
		return super.queryDriver(); //TODO:esp xtensa-gcc path
	}

}
