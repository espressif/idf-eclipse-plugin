/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.installcomponents.deserializer;

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
public class ComponentsDeserializer implements JsonDeserializer<List<ComponentVO>>
{

	private static final String SPEC_JSON_KEY = "spec"; //$NON-NLS-1$
	private static final String SOURCE_JSON_KEY = "source"; //$NON-NLS-1$
	private static final String IS_PUBLIC_JSON_KEY = "is_public"; //$NON-NLS-1$
	private static final String DEPENDENCIES_JSON_KEY = "dependencies"; //$NON-NLS-1$
	private static final String README_JSON_KEY = "readme"; //$NON-NLS-1$
	private static final String DOCS_JSON_KEY = "docs"; //$NON-NLS-1$
	private static final String VERSION_JSON_KEY = "version"; //$NON-NLS-1$
	private static final String URL_JSON_KEY = "url"; //$NON-NLS-1$
	private static final String DESCRIPTION_JSON_KEY = "description"; //$NON-NLS-1$
	private static final String COMPONENT_HASH_JSON_KEY = "component_hash"; //$NON-NLS-1$
	private static final String LATEST_VERSION_JSON_KEY = "latest_version"; //$NON-NLS-1$
	private static final String NAMESPACE_JSON_KEY = "namespace"; //$NON-NLS-1$
	private static final String NAME_JSON_KEY = "name"; //$NON-NLS-1$
	private static final String FEATURED_JSON_KEY = "featured"; //$NON-NLS-1$
	private static final String CREATED_AT_JSON_KEY = "created_at"; //$NON-NLS-1$

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
			String createdAt = mainJsonObject.get(CREATED_AT_JSON_KEY) == null
					|| mainJsonObject.get(CREATED_AT_JSON_KEY).isJsonNull() ? null
							: mainJsonObject.get(CREATED_AT_JSON_KEY).getAsString();
			boolean featured = mainJsonObject.get(FEATURED_JSON_KEY) == null
					|| mainJsonObject.get(FEATURED_JSON_KEY).isJsonNull() ? false
							: mainJsonObject.get(FEATURED_JSON_KEY).getAsBoolean();
			String name = mainJsonObject.get(NAME_JSON_KEY) == null || mainJsonObject.get(NAME_JSON_KEY).isJsonNull()
					? null
					: mainJsonObject.get(NAME_JSON_KEY).getAsString();
			String namespace = mainJsonObject.get(NAMESPACE_JSON_KEY) == null
					|| mainJsonObject.get(NAMESPACE_JSON_KEY).isJsonNull() ? null
							: mainJsonObject.get(NAMESPACE_JSON_KEY).getAsString();
			componentVO.setCreatedAt(createdAt);
			componentVO.setFeatured(featured);
			componentVO.setName(name);
			componentVO.setNamespace(namespace);

			if (mainJsonObject.get(LATEST_VERSION_JSON_KEY).isJsonNull())
			{
				componentVOs.add(componentVO);
				continue;
			}
			JsonObject detailsJsonObject = mainJsonObject.getAsJsonObject(LATEST_VERSION_JSON_KEY);

			String componentHash = detailsJsonObject.get(COMPONENT_HASH_JSON_KEY) == null
					|| detailsJsonObject.get(COMPONENT_HASH_JSON_KEY).isJsonNull() ? null
							: detailsJsonObject.get(COMPONENT_HASH_JSON_KEY).getAsString();
			String details = detailsJsonObject.get(DESCRIPTION_JSON_KEY) == null
					|| detailsJsonObject.get(DESCRIPTION_JSON_KEY).isJsonNull() ? null
							: detailsJsonObject.get(DESCRIPTION_JSON_KEY).getAsString();
			createdAt = detailsJsonObject.get(CREATED_AT_JSON_KEY) == null
					|| detailsJsonObject.get(CREATED_AT_JSON_KEY).isJsonNull() ? null
							: detailsJsonObject.get(CREATED_AT_JSON_KEY).getAsString();
			String url = detailsJsonObject.get(URL_JSON_KEY) == null || detailsJsonObject.get(URL_JSON_KEY).isJsonNull()
					? null
					: detailsJsonObject.get(URL_JSON_KEY).getAsString();
			String version = detailsJsonObject.get(VERSION_JSON_KEY) == null
					|| detailsJsonObject.get(VERSION_JSON_KEY).isJsonNull() ? null
							: detailsJsonObject.get(VERSION_JSON_KEY).getAsString();
			ComponentDetailsVO componentDetailsVO = new ComponentDetailsVO();
			componentDetailsVO.setComponentHash(componentHash);
			componentDetailsVO.setCreatedAt(createdAt);
			componentDetailsVO.setDescription(details);
			componentDetailsVO.setUrl(url);
			componentDetailsVO.setVersion(version);
			if (detailsJsonObject.getAsJsonObject(DOCS_JSON_KEY) != null
					&& detailsJsonObject.getAsJsonObject(DOCS_JSON_KEY).get(README_JSON_KEY) != null)
			{
				componentDetailsVO
						.setReadMe(detailsJsonObject.getAsJsonObject(DOCS_JSON_KEY).get(README_JSON_KEY).getAsString());
			}

			JsonArray dependenciesArray = detailsJsonObject.getAsJsonArray(DEPENDENCIES_JSON_KEY);
			List<ComponentDetailsDependenciesVO> componentDetailsDependenciesVOs = new ArrayList<ComponentDetailsDependenciesVO>();
			for (int x = 0; x < dependenciesArray.size(); x++)
			{
				JsonObject dependencyJsonObject = dependenciesArray.get(x).getAsJsonObject();
				ComponentDetailsDependenciesVO componentDetailsDependenciesVO = new ComponentDetailsDependenciesVO();
				boolean isPublic = dependencyJsonObject.get(IS_PUBLIC_JSON_KEY) == null
						|| dependencyJsonObject.get(IS_PUBLIC_JSON_KEY).isJsonNull() ? false
								: dependencyJsonObject.get(IS_PUBLIC_JSON_KEY).getAsBoolean();
				name = dependencyJsonObject.get(NAME_JSON_KEY) == null
						|| dependencyJsonObject.get(NAME_JSON_KEY).isJsonNull() ? null
								: dependencyJsonObject.get(NAME_JSON_KEY).getAsString();
				namespace = dependencyJsonObject.get(NAMESPACE_JSON_KEY) == null
						|| dependencyJsonObject.get(NAMESPACE_JSON_KEY).isJsonNull() ? null
								: dependencyJsonObject.get(NAMESPACE_JSON_KEY).getAsString();
				String source = dependencyJsonObject.get(SOURCE_JSON_KEY) == null
						|| dependencyJsonObject.get(SOURCE_JSON_KEY).isJsonNull() ? null
								: dependencyJsonObject.get(SOURCE_JSON_KEY).getAsString();
				String spec = dependencyJsonObject.get(SPEC_JSON_KEY) == null
						|| dependencyJsonObject.get(SPEC_JSON_KEY).isJsonNull() ? null
								: dependencyJsonObject.get(SPEC_JSON_KEY).getAsString();
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
