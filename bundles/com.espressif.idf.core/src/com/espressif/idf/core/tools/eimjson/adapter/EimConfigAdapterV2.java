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
import com.espressif.idf.core.tools.vo.EimJson;
import com.espressif.idf.core.tools.vo.IdfInstalled;

/** Adapter for schema 2.0 (and files with no {@code version} field). */
public final class EimConfigAdapterV2 implements EimConfigAdapter
{
	@Override
	public EimConfigModel toModel(Object rawDocument)
	{
		EimJson raw = (EimJson) rawDocument;
		List<EimInstallationModel> installations = new ArrayList<>();
		if (raw.getIdfInstalled() != null)
		{
			for (IdfInstalled entry : raw.getIdfInstalled())
			{
				installations.add(mapEntry(entry));
			}
		}
		return new DefaultEimConfigModel(EimJsonVersion.V2, raw.getGitPath(), raw.getEimPath(),
				raw.getIdfSelectedId(), installations);
	}

	private static EimInstallationModel mapEntry(IdfInstalled entry)
	{
		return new DefaultEimInstallationModel(entry.getId(), entry.getName(), entry.getPath(),
				entry.getIdfToolsPath(), entry.getActivationScript(), entry.getPython(),
				InstallationStatus.FINISHED);
	}
}
