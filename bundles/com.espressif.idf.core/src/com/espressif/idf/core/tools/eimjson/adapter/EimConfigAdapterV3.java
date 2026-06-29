/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.eimjson.adapter;

import java.util.ArrayList;
import java.util.List;

import com.espressif.idf.core.tools.eimjson.EimJsonVersion;
import com.espressif.idf.core.tools.eimjson.InstallationStatus;
import com.espressif.idf.core.tools.eimjson.model.DefaultEimConfigModel;
import com.espressif.idf.core.tools.eimjson.model.DefaultEimInstallationModel;
import com.espressif.idf.core.tools.eimjson.model.EimConfigModel;
import com.espressif.idf.core.tools.eimjson.model.EimInstallationModel;
import com.espressif.idf.core.tools.eimjson.schema.v3.EimJsonV3;
import com.espressif.idf.core.tools.eimjson.schema.v3.IdfInstalledV3;

/** Adapter for schema 3.0 (installation lifecycle {@code status} field). */
public final class EimConfigAdapterV3 implements EimConfigAdapter
{
	@Override
	public EimConfigModel toModel(Object rawDocument)
	{
		EimJsonV3 raw = (EimJsonV3) rawDocument;
		List<EimInstallationModel> installations = new ArrayList<>();
		if (raw.getIdfInstalled() != null)
		{
			for (IdfInstalledV3 entry : raw.getIdfInstalled())
			{
				installations.add(mapEntry(entry));
			}
		}
		return new DefaultEimConfigModel(EimJsonVersion.V3, raw.getGitPath(), raw.getEimPath(),
				raw.getIdfSelectedId(), installations);
	}

	private static EimInstallationModel mapEntry(IdfInstalledV3 entry)
	{
		return new DefaultEimInstallationModel(entry.getId(), entry.getName(), entry.getPath(),
				entry.getIdfToolsPath(), entry.getActivationScript(), entry.getPython(),
				InstallationStatus.fromJson(entry.getStatus()));
	}
}
