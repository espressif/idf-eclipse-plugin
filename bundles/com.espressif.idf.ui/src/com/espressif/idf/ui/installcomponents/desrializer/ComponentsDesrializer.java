/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.installcomponents.desrializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.espressif.idf.ui.installcomponents.vo.ComponentDetailsDependenciesVO;
import com.espressif.idf.ui.installcomponents.vo.ComponentDetailsVO;
import com.espressif.idf.ui.installcomponents.vo.ComponentVO;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Components JSON response desrializer
 * 
 * @author Ali Azam Rana
 *
 */
public class ComponentsDesrializer implements JsonDeserializer<List<ComponentVO>>
{

	@Override
	public List<ComponentVO> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException
	{
		List<ComponentVO> componentVOs = new ArrayList<>();
		JsonArray jsonArray = json.getAsJsonArray();
		for (int i = 0; i < jsonArray.size(); i++)
		{
			JsonElement jsonElement = jsonArray.get(i);

			JsonObject mainJsonObject = jsonElement.getAsJsonObject();
			ComponentVO componentVO = new ComponentVO();
			String createdAt = mainJsonObject.get("created_at") == null || mainJsonObject.get("created_at").isJsonNull() //$NON-NLS-1$ //$NON-NLS-2$
					? null
					: mainJsonObject.get("created_at").getAsString(); //$NON-NLS-1$
			boolean featured = mainJsonObject.get("featured") == null || mainJsonObject.get("featured").isJsonNull() //$NON-NLS-1$ //$NON-NLS-2$
					? false
					: mainJsonObject.get("featured").getAsBoolean(); //$NON-NLS-1$
			String name = mainJsonObject.get("name") == null || mainJsonObject.get("name").isJsonNull() ? null //$NON-NLS-1$ //$NON-NLS-2$
					: mainJsonObject.get("name").getAsString(); //$NON-NLS-1$
			String namespace = mainJsonObject.get("namespace") == null || mainJsonObject.get("namespace").isJsonNull() //$NON-NLS-1$ //$NON-NLS-2$
					? null
					: mainJsonObject.get("namespace").getAsString(); //$NON-NLS-1$
			componentVO.setCreatedAt(createdAt);
			componentVO.setFeatured(featured);
			componentVO.setName(name);
			componentVO.setNamespace(namespace);

			if (mainJsonObject.get("latest_version").isJsonNull()) //$NON-NLS-1$
			{
				componentVOs.add(componentVO);
				continue;
			}
			JsonObject detailsJsonObject = mainJsonObject.getAsJsonObject("latest_version"); //$NON-NLS-1$

			String componentHash = detailsJsonObject.get("component_hash") == null //$NON-NLS-1$
					|| detailsJsonObject.get("component_hash").isJsonNull() ? null //$NON-NLS-1$
							: detailsJsonObject.get("component_hash").getAsString(); //$NON-NLS-1$
			String details = detailsJsonObject.get("description") == null //$NON-NLS-1$
					|| detailsJsonObject.get("description").isJsonNull() ? null //$NON-NLS-1$
							: detailsJsonObject.get("description").getAsString(); //$NON-NLS-1$
			createdAt = detailsJsonObject.get("created_at") == null || detailsJsonObject.get("created_at").isJsonNull() //$NON-NLS-1$ //$NON-NLS-2$
					? null
					: detailsJsonObject.get("created_at").getAsString(); //$NON-NLS-1$
			String url = detailsJsonObject.get("url") == null || detailsJsonObject.get("url").isJsonNull() ? null //$NON-NLS-1$ //$NON-NLS-2$
					: detailsJsonObject.get("url").getAsString(); //$NON-NLS-1$
			String version = detailsJsonObject.get("version") == null || detailsJsonObject.get("version").isJsonNull() //$NON-NLS-1$ //$NON-NLS-2$
					? null
					: detailsJsonObject.get("version").getAsString(); //$NON-NLS-1$
			ComponentDetailsVO componentDetailsVO = new ComponentDetailsVO();
			componentDetailsVO.setComponentHash(componentHash);
			componentDetailsVO.setCreatedAt(createdAt);
			componentDetailsVO.setDescription(details);
			componentDetailsVO.setUrl(url);
			componentDetailsVO.setVersion(version);
			if (detailsJsonObject.getAsJsonObject("docs") != null //$NON-NLS-1$
					&& detailsJsonObject.getAsJsonObject("docs").get("readme") != null) //$NON-NLS-1$ //$NON-NLS-2$
			{
				componentDetailsVO.setReadMe(detailsJsonObject.getAsJsonObject("docs").get("readme").getAsString()); //$NON-NLS-1$ //$NON-NLS-2$
			}

			JsonArray dependenciesArray = detailsJsonObject.getAsJsonArray("dependencies"); // $NON-NLS-2$
			List<ComponentDetailsDependenciesVO> componentDetailsDependenciesVOs = new ArrayList<ComponentDetailsDependenciesVO>();
			for (int x = 0; x < dependenciesArray.size(); x++)
			{
				JsonObject dependencyJsonObject = dependenciesArray.get(x).getAsJsonObject();
				ComponentDetailsDependenciesVO componentDetailsDependenciesVO = new ComponentDetailsDependenciesVO();
				boolean isPublic = dependencyJsonObject.get("is_public") == null //$NON-NLS-1$
						|| dependencyJsonObject.get("is_public").isJsonNull() ? false //$NON-NLS-1$
								: dependencyJsonObject.get("is_public").getAsBoolean(); //$NON-NLS-1$
				name = dependencyJsonObject.get("name") == null || dependencyJsonObject.get("name").isJsonNull() ? null //$NON-NLS-1$ //$NON-NLS-2$
						: dependencyJsonObject.get("name").getAsString(); //$NON-NLS-1$
				namespace = dependencyJsonObject.get("namespace") == null //$NON-NLS-1$
						|| dependencyJsonObject.get("namespace").isJsonNull() ? null //$NON-NLS-1$
								: dependencyJsonObject.get("namespace").getAsString(); //$NON-NLS-1$
				String source = dependencyJsonObject.get("source") == null //$NON-NLS-1$
						|| dependencyJsonObject.get("source").isJsonNull() ? null //$NON-NLS-1$
								: dependencyJsonObject.get("source").getAsString(); //$NON-NLS-1$
				String spec = dependencyJsonObject.get("spec") == null || dependencyJsonObject.get("spec").isJsonNull() //$NON-NLS-1$ //$NON-NLS-2$
						? null
						: dependencyJsonObject.get("spec").getAsString(); //$NON-NLS-1$
				componentDetailsDependenciesVO.setPublic(isPublic);
				componentDetailsDependenciesVO.setName(name);
				componentDetailsDependenciesVO.setNamespace(namespace);
				componentDetailsDependenciesVO.setSource(source);
				componentDetailsDependenciesVO.setSpec(spec);

				componentDetailsDependenciesVOs.add(componentDetailsDependenciesVO);
			}

			componentDetailsVO.setDependencies(componentDetailsDependenciesVOs);
			componentVO.setComponentDetails(componentDetailsVO);
			componentVOs.add(componentVO);
		}
		return componentVOs;
	}

}
